package com.hkgroups.agecalculator.data.model

import java.time.LocalDate

data class HistoricalEvent(
    val date: LocalDate,
    val title: String,
    val description: String
) {
}