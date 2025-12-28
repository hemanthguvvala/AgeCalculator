package com.hkgroups.agecalculator.domain.usecase

import com.hkgroups.agecalculator.ui.viewmodel.MilestoneData
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Use case for time-related calculations (milestones, age tickers, etc.)
 * Extracted from MainViewModel for better testability and separation of concerns.
 */
class TimeCalculationUseCase @Inject constructor() {

    /**
     * Calculate the next milestone birthday (10,000 days, 15,000 days, etc.)
     * 
     * @param birthDate The user's birth date
     * @return MilestoneData containing the milestone count and date, or null if no upcoming milestone
     */
    fun calculateNextMilestone(birthDate: LocalDate): MilestoneData? {
        val currentDate = LocalDate.now()
        val daysLived = ChronoUnit.DAYS.between(birthDate, currentDate)

        // Find the next milestone (in increments of 5000 days starting from 10000)
        val nextMilestone = when {
            daysLived < 10000 -> 10000
            daysLived < 15000 -> 15000
            daysLived < 20000 -> 20000
            daysLived < 25000 -> 25000
            daysLived < 30000 -> 30000
            else -> {
                // For ages beyond 30000 days, calculate the next 5000-day increment
                ((daysLived / 5000) + 1) * 5000
            }
        }

        val daysUntilMilestone = nextMilestone - daysLived
        val milestoneDate = currentDate.plusDays(daysUntilMilestone)

        return MilestoneData(
            dayCount = nextMilestone.toInt(),
            date = milestoneDate
        )
    }

    /**
     * Calculate days until the next birthday
     * 
     * @param birthDate The user's birth date
     * @return Number of days until next birthday
     */
    fun calculateDaysUntilBirthday(birthDate: LocalDate): Int {
        val today = LocalDate.now()
        val nextBirthday = birthDate.withYear(today.year)
        
        val daysUntil = if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            // Birthday already passed this year, calculate for next year
            ChronoUnit.DAYS.between(today, nextBirthday.plusYears(1))
        } else {
            ChronoUnit.DAYS.between(today, nextBirthday)
        }
        
        return daysUntil.toInt()
    }

    /**
     * Calculate the exact age including seconds lived
     * 
     * @param birthDate The user's birth date
     * @return Total seconds lived
     */
    fun calculateSecondsLived(birthDate: LocalDate): Long {
        val birthDateTime = birthDate.atStartOfDay(java.time.ZoneId.systemDefault())
        val now = java.time.ZonedDateTime.now()
        return ChronoUnit.SECONDS.between(birthDateTime, now)
    }
}
