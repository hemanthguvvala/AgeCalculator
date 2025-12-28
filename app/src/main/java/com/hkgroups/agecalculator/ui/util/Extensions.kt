package com.hkgroups.agecalculator.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.time.LocalDate
import java.time.Period

/**
 * Extension functions and utilities for the Age Calculator app
 */

// Calculate age from timestamp
fun Long.toAge(): Int {
    val birthDate = LocalDate.ofEpochDay(this / (24 * 60 * 60 * 1000))
    val now = LocalDate.now()
    return Period.between(birthDate, now).years
}

// Format age with units
fun Int.formatAge(unit: String = "years"): String {
    return "$this $unit"
}

// Convert dp to pixels
@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

// Safe string resource with fallback
fun String?.orDefault(default: String): String {
    return if (this.isNullOrBlank()) default else this
}

// Zodiac emoji mapper
fun String.toZodiacEmoji(): String {
    return when (this.lowercase()) {
        "rat" -> "🐭"
        "ox" -> "🐂"
        "tiger" -> "🐯"
        "rabbit" -> "🐰"
        "dragon" -> "🐲"
        "snake" -> "🐍"
        "horse" -> "🐴"
        "goat" -> "🐐"
        "monkey" -> "🐵"
        "rooster" -> "🐔"
        "dog" -> "🐕"
        "pig" -> "🐖"
        "aries" -> "♈"
        "taurus" -> "♉"
        "gemini" -> "♊"
        "cancer" -> "♋"
        "leo" -> "♌"
        "virgo" -> "♍"
        "libra" -> "♎"
        "scorpio" -> "♏"
        "sagittarius" -> "♐"
        "capricorn" -> "♑"
        "aquarius" -> "♒"
        "pisces" -> "♓"
        else -> "✨"
    }
}

// Format large numbers with commas
fun Int.formatWithCommas(): String {
    return String.format("%,d", this)
}

// Calculate days between dates
fun Long.daysSince(other: Long): Long {
    return (this - other) / (24 * 60 * 60 * 1000)
}
