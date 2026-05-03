package com.hkgroups.agecalculator.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

/**
 * Emits the current `LocalDate` immediately, then re-emits whenever the local
 * calendar day rolls over. Used by the ViewModel to keep "today's snapshot"
 * accurate when the app stays open across midnight.
 *
 * Sleeps until the next local midnight rather than busy-polling, so the cost
 * is one timer per active collector.
 */
object MidnightTicker {
    fun flow(zone: ZoneId = ZoneId.systemDefault()): Flow<LocalDate> = flow {
        while (true) {
            val today = LocalDate.now(zone)
            emit(today)
            val now = ZonedDateTime.now(zone)
            val tomorrow = today.plusDays(1).atStartOfDay(zone)
            val sleep = Duration.between(now, tomorrow).toMillis().coerceAtLeast(60_000L)
            delay(sleep)
        }
    }
}
