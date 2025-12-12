package com.hkgroups.agecalculator.domain.usecase

import com.hkgroups.agecalculator.data.model.HistoricalEvent
import java.time.LocalDate
import javax.inject.Inject

class FindBirthdayEventsUseCase @Inject constructor() {

    operator fun invoke(birthDate: LocalDate, allEvents: List<HistoricalEvent>): List<HistoricalEvent> {
        return allEvents.filter { event ->
            event.date.month == birthDate.month && event.date.dayOfMonth == birthDate.dayOfMonth
        }
    }
}