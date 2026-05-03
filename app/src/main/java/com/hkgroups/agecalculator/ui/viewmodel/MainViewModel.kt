package com.hkgroups.agecalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hkgroups.agecalculator.content.AstronomyEngine
import com.hkgroups.agecalculator.content.ContentEngine
import com.hkgroups.agecalculator.util.MidnightTicker
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.data.repository.ZodiacRepository
import com.hkgroups.agecalculator.domain.usecase.CalculateAgeUseCase
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    val ageData: AgeData? = null,
    val zodiacSign: ZodiacSign? = null,
    val daysUntilBirthday: Int? = null,
    val dailyTip: String? = null,
    val milestoneData: MilestoneData? = null,
    val selectedCompatibilitySign: ZodiacSign? = null,
    val horoscope: String? = null,
    val historicalEvents: PersistentList<HistoricalEvent> = persistentListOf(),
    val birthdayEvents: PersistentList<HistoricalEvent> = persistentListOf(),
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
    val streakDays: StateFlow<Int> = settingsRepository.currentStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Longest streak ever achieved. */
    val longestStreakDays: StateFlow<Int> = settingsRepository.longestStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Available streak freezes — earned at every 7-day milestone. */
    val streakFreezes: StateFlow<Int> = settingsRepository.streakFreezes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Mood-pattern insight (null when not enough data). Recomputes when mood log changes. */
    val moodInsight: StateFlow<String?> = settingsRepository.moodEntries
        .map { ContentEngine.moodInsight(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Midnight-aware "today" — re-emits whenever the local day rolls over,
    // so all date-derived content stays fresh when the app is left open.
    private val todayFlow = MidnightTicker.flow()

    /** Today's question of the day for the user's sign. Empty until birth date is set. */
    val questionOfTheDay: StateFlow<String> = combine(
        settingsRepository.savedBirthDate,
        todayFlow
    ) { millis, today ->
        millis?.let {
            val birth = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            val sign = AstronomyEngine.sunSignOfDay(birth)
            ContentEngine.questionOfTheDay(sign, today)
        } ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /** Has the user answered today's QOD? */
    val hasAnsweredQuestionToday: StateFlow<Boolean> = combine(
        settingsRepository.questionAnsweredDate,
        todayFlow
    ) { answered, today ->
        answered == today
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Birthday-window message (null outside the ±7 day window). */
    val birthdayWindowMessage: StateFlow<String?> = combine(
        settingsRepository.savedBirthDate,
        todayFlow
    ) { millis, today ->
        millis?.let {
            val birth = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            val sign = AstronomyEngine.sunSignOfDay(birth)
            ContentEngine.birthdayWindowMessage(sign, birth, today)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Today's cosmic snapshot — drives "what's in the air" content blocks.
     *  Re-emits at local midnight so the dashboard stays fresh across day rollover. */
    val cosmicSnapshot: StateFlow<AstronomyEngine.CosmicSnapshot> = todayFlow
        .map { AstronomyEngine.snapshot(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AstronomyEngine.snapshot(LocalDate.now())
        )

    /**
     * Mark today as visited. Idempotent within a calendar day. Streak increments,
     * freezes consume on a missed day before reset.
     */
    fun checkInToday() {
        viewModelScope.launch { settingsRepository.recordCheckIn() }
    }

    /** Persist the user's answer to today's question of the day. */
    fun answerQuestionOfTheDay(answer: String) {
        if (answer.isBlank()) return
        viewModelScope.launch {
            settingsRepository.saveQuestionAnswer(LocalDate.now(), answer)
        }
    }

    /** 7-day forecast for the user's sign (premium-gated in UI).
     *  Re-emits at midnight so a long-lived session sees Tuesday's week, not Monday's. */
    val weeklyForecast: StateFlow<List<String>> = combine(
        settingsRepository.savedBirthDate,
        todayFlow
    ) { millis, today ->
        millis?.let {
            val birth = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            val sign = AstronomyEngine.sunSignOfDay(birth)
            ContentEngine.weeklyForecast(sign, today)
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Compatibility narrative between two signs. */
    fun compatibilityInsight(signA: String, signB: String): String =
        ContentEngine.compatibilityInsight(signA, signB)

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