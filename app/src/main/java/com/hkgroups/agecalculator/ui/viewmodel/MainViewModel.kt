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
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
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
    private val findBirthdayEventsUseCase: FindBirthdayEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _zodiacSigns = MutableStateFlow<List<ZodiacSign>>(emptyList())
    val zodiacSigns: List<ZodiacSign>
        get() = _zodiacSigns.value

    init {
        // Collect zodiac signs Flow with Resource wrapper
        viewModelScope.launch {
            repository.getZodiacSigns().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Update loading state
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                        // Use cached data if available
                        resource.data?.let { _zodiacSigns.value = it }
                    }
                    is Resource.Success -> {
                        // Data loaded successfully
                        _zodiacSigns.value = resource.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> {
                        // Error occurred, but we might have cached data
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
                    // No date saved, reset to welcome screen
                    resetUiState()
                } else {
                    // Date exists, load the data
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
                Date(dateInMillis).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            // Calculate age as a Period (for localization)
            val agePeriod = Period.between(selectedDate, LocalDate.now())
            val ageData = AgeData(agePeriod)

            // --- DELEGATE ZODIAC SIGN LOGIC TO THE USE CASE ---
            val zodiac = findZodiacSignUseCase(selectedDate, zodiacSigns)

            val dailyTip = zodiac?.let { repository.getDailyTip(it.name) }
            val milestoneData = calculateNextMilestone(selectedDate)
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

            var nextBirthday = selectedDate.withYear(LocalDate.now().year)
            if (nextBirthday.isBefore(LocalDate.now()) || nextBirthday.isEqual(LocalDate.now())) {
                nextBirthday = nextBirthday.plusYears(1)
            }
            val daysUntilBirthday = Period.between(LocalDate.now(), nextBirthday).days

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


    private fun calculateNextMilestone(birthDate: LocalDate): MilestoneData? {
        val now = LocalDate.now()
        val daysAlive = java.time.temporal.ChronoUnit.DAYS.between(birthDate, now)

        val milestones = listOf(10000, 15000, 20000, 25000, 30000)
        val nextMilestone = milestones.firstOrNull { it > daysAlive }

        return nextMilestone?.let {
            val milestoneDate = birthDate.plusDays(it.toLong())
            MilestoneData(dayCount = it, date = milestoneDate)
        }
    }

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
                delay(1000)
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
}