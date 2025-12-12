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


data class UiState(
    val selectedDate: LocalDate? = null,
    val age: String? = null,
    val zodiacSign: ZodiacSign? = null,
    val birthdayCountdown: String? = null,
    val dailyTip: String? = null,
    val milestone: String? = null,
    val selectedCompatibilitySign: ZodiacSign? = null,
    val horoscope: String? = null,
    val historicalEvents: PersistentList<HistoricalEvent> = persistentListOf(),
    val birthdayEvents: PersistentList<HistoricalEvent> = persistentListOf()
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

    val zodiacSigns = repository.getZodiacSigns()

    init {
        // --- NEW: Check for a saved date on startup ---
        viewModelScope.launch {
            val savedDate = settingsRepository.savedBirthDate.first()
            savedDate?.let {
                // If a date is found, populate the screen
                onDateSelected(it)
            }
        }
    }

    fun onDateSelected(dateInMillis: Long) {
        viewModelScope.launch {
            settingsRepository.saveBirthDate(dateInMillis)
        }

        val selectedDate =
            Date(dateInMillis).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        val ageString = calculateAgeUseCase(selectedDate)

        // --- DELEGATE ZODIAC SIGN LOGIC TO THE USE CASE ---
        val zodiac = findZodiacSignUseCase(selectedDate, zodiacSigns)

        val dailyTip = zodiac?.let { repository.getDailyTip(it.name) }
        val milestone = calculateNextMilestone(selectedDate)
        val horoscope = zodiac?.let { repository.getDailyHoroscope(it.name) }

        val allEvents = repository.getHistoricalEvents()
        val userEvents = allEvents.filter { it.date.isAfter(selectedDate) }.toPersistentList()
        val birthdayEvents = findBirthdayEventsUseCase(selectedDate, allEvents).toPersistentList()

        var nextBirthday = selectedDate.withYear(LocalDate.now().year)
        if (nextBirthday.isBefore(LocalDate.now()) || nextBirthday.isEqual(LocalDate.now())) {
            nextBirthday = nextBirthday.plusYears(1)
        }
        val daysUntilBirthday = Period.between(LocalDate.now(), nextBirthday).days
        val countdownString = "Your next birthday is in $daysUntilBirthday days!"

        _uiState.value = UiState(
            selectedDate = selectedDate,
            age = ageString,
            zodiacSign = zodiac,
            birthdayCountdown = countdownString,
            dailyTip = dailyTip,
            milestone = milestone,
            horoscope = horoscope,
            historicalEvents = userEvents,
            birthdayEvents = birthdayEvents
        )
    }


    private fun calculateNextMilestone(birthDate: LocalDate): String {
        val now = LocalDate.now()
        val daysAlive = java.time.temporal.ChronoUnit.DAYS.between(birthDate, now)

        val milestones = listOf(10000, 15000, 20000, 25000, 30000)
        val nextMilestone = milestones.firstOrNull { it > daysAlive }

        return if (nextMilestone != null) {
            val milestoneDate = birthDate.plusDays(nextMilestone.toLong())
            val formattedDate = milestoneDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            "Your ${"%,d".format(nextMilestone)}th day will be on $formattedDate."
        } else {
            "You've passed all the major day milestones!"
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