package com.hkgroups.agecalculator.domain.usecase

import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

class CalculateAgeUseCase @Inject constructor() {

    operator fun invoke(birthDate: LocalDate): String {
        val now = LocalDate.now()
        val period = Period.between(birthDate, now)
        return "${period.years} years, ${period.months} months, ${period.days} days"
    }
}