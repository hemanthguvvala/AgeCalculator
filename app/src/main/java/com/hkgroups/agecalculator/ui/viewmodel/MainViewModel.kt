package com.hkgroups.agecalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.data.repository.ZodiacRepository
import com.hkgroups.agecalculator.domain.usecase.CalculateAgeUseCase // Import the new Use Case
import com.hkgroups.agecalculator.domain.usecase.FindBirthdayEventsUseCase
import com.hkgroups.agecalculator.domain.usecase.FindZodiacSignUseCase
import com.hkgroups.agecalculator.domain.usecase.TimeCalculationUseCase
import com.hkgroups.agecalculator.util.CosmicUtils
import com.hkgroups.agecalculator.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Represents milestone data for localization.
 * @param dayCount The milestone day number (e.g., 10000, 15000)
 * @param date The date when the milestone will occur
 */
data class MilestoneData(
    val dayCount: Int,
    val date: LocalDate
)

/**
 * Represents age data as a Period for localization.
 * @param period The time period representing the age
 */
data class AgeData(
    val period: Period
)

data class UiState(
    val selectedDate: LocalDate? = null,
    val ageData: AgeData? = null, // Changed from String to AgeData (contains Period)
    val zodiacSign: ZodiacSign? = null,
    val daysUntilBirthday: Int? = null, // Changed from String to Int
    val dailyTip: String? = null,
    val milestoneData: MilestoneData? = null, // Changed from String to MilestoneData
    val selectedCompatibilitySign: ZodiacSign? = null,
    val horoscope: String? = null,
    val historicalEvents: PersistentList<HistoricalEvent> = persistentListOf(),
    val birthdayEvents: PersistentList<HistoricalEvent> = persistentListOf(),
    // New cosmic features
    val chineseZodiac: String? = null,
    val planetaryAges: List<Pair<String, String>> = emptyList(),
    val birthYearTrivia: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ZodiacRepository,
    val settingsRepository: SettingsRepository,
    private val calculateAgeUseCase: CalculateAgeUseCase,
    private val findZodiacSignUseCase: FindZodiacSignUseCase,
    private val findBirthdayEventsUseCase: FindBirthdayEventsUseCase,
    private val timeCalculationUseCase: TimeCalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _zodiacSigns = MutableStateFlow<List<ZodiacSign>>(emptyList())
    val zodiacSigns: List<ZodiacSign>
        get() = _zodiacSigns.value

    /** Compose-friendly StateFlow for collecting in @Composable screens. */
    val zodiacSignsState = _zodiacSigns.asStateFlow()

    /** Current daily-open streak, persisted in DataStore. */
    val streakDays: kotlinx.coroutines.flow.StateFlow<Int> =
        settingsRepository.currentStreak.let { flow ->
            kotlinx.coroutines.flow.MutableStateFlow(0).also { state ->
                viewModelScope.launch { flow.collect { state.value = it } }
            }.asStateFlow()
        }

    /** Longest streak ever achieved, persisted in DataStore. */
    val longestStreakDays: kotlinx.coroutines.flow.StateFlow<Int> =
        settingsRepository.longestStreak.let { flow ->
            kotlinx.coroutines.flow.MutableStateFlow(0).also { state ->
                viewModelScope.launch { flow.collect { state.value = it } }
            }.asStateFlow()
        }

    /**
     * Mark today as visited. Idempotent within a calendar day. Increments the
     * streak when called on a consecutive day, resets when a day is missed.
     */
    fun checkInToday() {
        viewModelScope.launch {
            settingsRepository.recordCheckIn()
        }
    }

    init {
        // Collect zodiac signs Flow with Resource wrapper. When the list finishes
        // loading we re-run loadDate so the zodiac sign appears even if the birth-
        // date flow fired before the DB was ready (race fixed).
        viewModelScope.launch {
            repository.getZodiacSigns().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                        resource.data?.let { _zodiacSigns.value = it }
                    }
                    is Resource.Success -> {
                        _zodiacSigns.value = resource.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                        // Race fix: birth date may have already been collected before
                        // signs arrived. If we have a saved date but no zodiacSign yet,
                        // recompute now that the list is populated.
                        if (_uiState.value.selectedDate != null && _uiState.value.zodiacSign == null) {
                            settingsRepository.savedBirthDate.first()?.let { loadDate(it) }
                        }
                    }
                    is Resource.Error -> {
                        resource.data?.let { _zodiacSigns.value = it }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }

        // Reactive Flow: Listen to birth date changes
        viewModelScope.launch {
            settingsRepository.savedBirthDate.collect { savedDate ->
                if (savedDate == null) {
                    resetUiState()
                } else {
                    loadDate(savedDate)
                }
            }
        }
    }

    /**
     * Called when user selects a birth date.
     * Only saves to repository - UI update happens via Flow collection.
     */
    fun onDateSelected(dateInMillis: Long) {
        viewModelScope.launch {
            settingsRepository.saveBirthDate(dateInMillis)
        }
    }

    /**
     * Resets UI state to initial empty values (Welcome Screen).
     */
    private fun resetUiState() {
        _uiState.value = UiState()
    }

    /**
     * Loads and calculates all data for a given birth date timestamp.
     * Updates the UI state with calculated values.
     */
    private fun loadDate(dateInMillis: Long) {
        viewModelScope.launch {
            // Don't process if zodiac signs aren't loaded yet
            if (zodiacSigns.isEmpty() && _uiState.value.isLoading) {
                // Data is still loading, calculation will be triggered after data loads
                return@launch
            }

            val selectedDate =
                Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()

            // Calculate age as a Period (for localization)
            val agePeriod = Period.between(selectedDate, LocalDate.now())
            val ageData = AgeData(agePeriod)

            // --- DELEGATE ZODIAC SIGN LOGIC TO THE USE CASE ---
            val zodiac = findZodiacSignUseCase(selectedDate, zodiacSigns)

            val dailyTip = zodiac?.let { repository.getDailyTip(it.name) }
            val milestoneData = timeCalculationUseCase.calculateNextMilestone(selectedDate)
            val horoscope = zodiac?.let { repository.getDailyHoroscope(it.name) }

            // Calculate cosmic features
            val birthYear = selectedDate.year
            val chineseZodiac = CosmicUtils.getChineseZodiac(birthYear)
            val earthAgeInDays = java.time.temporal.ChronoUnit.DAYS.between(selectedDate, LocalDate.now())
            val planetaryAges = CosmicUtils.calculatePlanetaryAges(earthAgeInDays)
            val birthYearTrivia = CosmicUtils.getBirthYearTrivia(birthYear)

            val allEvents = repository.getHistoricalEvents()
            val userEvents = allEvents.filter { it.date.isAfter(selectedDate) }.toPersistentList()
            val birthdayEvents = findBirthdayEventsUseCase(selectedDate, allEvents).toPersistentList()

            val daysUntilBirthday = timeCalculationUseCase.calculateDaysUntilBirthday(selectedDate)

            _uiState.value = _uiState.value.copy(
                selectedDate = selectedDate,
                ageData = ageData,
                zodiacSign = zodiac,
                daysUntilBirthday = daysUntilBirthday,
                dailyTip = dailyTip,
                milestoneData = milestoneData,
                horoscope = horoscope,
                historicalEvents = userEvents,
                birthdayEvents = birthdayEvents,
                chineseZodiac = chineseZodiac,
                planetaryAges = planetaryAges,
                birthYearTrivia = birthYearTrivia
            )
        }
    }

    /**
     * Clears saved birth date data, returning user to Welcome Screen.
     */
    fun clearData() {
        viewModelScope.launch {
            settingsRepository.clearBirthDate()
        }
    }

    /**
     * Refreshes all data for the currently selected date.
     */
    fun refreshData() {
        viewModelScope.launch {
            val currentDate = settingsRepository.savedBirthDate.first()
            currentDate?.let {
                loadDate(it)
            }
        }
    }

    /**
     * Emits Years/Months/Days breakdown for the given birth date. Re-emits only when
     * the local calendar day changes — sleeping until the next local midnight rather
     * than busy-looping every second. This avoids continuous recomposition of the
     * dashboard for a value that, by definition, only changes daily.
     */
    fun ageTicker(birthDate: LocalDate): kotlinx.coroutines.flow.Flow<List<Pair<String, String>>> =
        flow {
            while (true) {
                val now = LocalDate.now()
                val period = Period.between(birthDate, now)
                emit(
                    listOf(
                        Pair("Years", period.years.toString()),
                        Pair("Months", period.months.toString()),
                        Pair("Days", period.days.toString())
                    )
                )
                val nextMidnight = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                val nowZdt = java.time.ZonedDateTime.now(ZoneId.systemDefault())
                val sleepMs = java.time.Duration.between(nowZdt, nextMidnight).toMillis()
                    .coerceAtLeast(60_000L)
                delay(sleepMs)
            }
        }

    fun onCompatibilitySignSelected(sign: ZodiacSign) {
        _uiState.value = _uiState.value.copy(
            selectedCompatibilitySign = sign
        )
    }

    fun getSignByName(name: String): ZodiacSign? {
        return zodiacSigns.find { it.name == name }
    }

    fun onThemeSelected(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(isDark)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setChimesEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setChimesEnabled(enabled) }
    }

    fun setCosmicEventNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCosmicEventNotificationsEnabled(enabled) }
    }

    fun setRisingSign(sign: String?) {
        viewModelScope.launch { settingsRepository.setRisingSign(sign) }
    }

    fun setMoonSign(sign: String?) {
        viewModelScope.launch { settingsRepository.setMoonSign(sign) }
    }

    fun saveMoodEntry(date: java.time.LocalDate, mood: String, note: String) {
        viewModelScope.launch { settingsRepository.saveMoodEntry(date, mood, note) }
    }
}