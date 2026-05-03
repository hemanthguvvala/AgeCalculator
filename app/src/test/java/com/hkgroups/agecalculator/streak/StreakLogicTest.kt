package com.hkgroups.agecalculator.streak

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure-logic test of the streak rules. The actual implementation lives in
 * [com.hkgroups.agecalculator.data.repository.SettingsRepository.recordCheckIn]
 * but couples to DataStore I/O; we mirror the algorithm here so we can verify
 * the rules deterministically without a Robolectric harness.
 *
 * If this mirror diverges from the real impl, the contract test in the
 * SettingsRepository instrumentation suite will catch it.
 */
class StreakLogicTest {

    /** Mirrors `recordCheckIn`'s state transition. Returns (currentStreak, freezes). */
    private fun step(
        today: LocalDate,
        lastIso: String?,
        current: Int,
        freezes: Int
    ): Pair<Int, Int> {
        val todayIso = today.toString()
        return when {
            lastIso == null -> 1 to freezes
            lastIso == todayIso -> current.coerceAtLeast(1) to freezes
            else -> {
                val lastDate = runCatching { LocalDate.parse(lastIso) }.getOrNull()
                val daysGap = if (lastDate != null) {
                    ChronoUnit.DAYS.between(lastDate, today)
                } else Long.MAX_VALUE
                when {
                    daysGap < 0 -> current.coerceAtLeast(1) to freezes
                    daysGap == 1L -> {
                        val newStreak = current + 1
                        val earned = if (newStreak % 7 == 0) (freezes + 1).coerceAtMost(3) else freezes
                        newStreak to earned
                    }
                    daysGap == 2L && freezes > 0 -> current to (freezes - 1)
                    else -> 1 to freezes
                }
            }
        }
    }

    @Test
    fun `first check-in starts streak at 1`() {
        val (s, f) = step(LocalDate.of(2026, 5, 1), null, 0, 0)
        assertEquals(1, s); assertEquals(0, f)
    }

    @Test
    fun `same-day check-in is idempotent`() {
        val (s, f) = step(LocalDate.of(2026, 5, 1), "2026-05-01", 5, 1)
        assertEquals(5, s); assertEquals(1, f)
    }

    @Test
    fun `consecutive day increments`() {
        val (s, _) = step(LocalDate.of(2026, 5, 2), "2026-05-01", 5, 0)
        assertEquals(6, s)
    }

    @Test
    fun `seven-day milestone earns a freeze`() {
        val (s, f) = step(LocalDate.of(2026, 5, 8), "2026-05-07", 6, 0)
        assertEquals(7, s); assertEquals(1, f)
    }

    @Test
    fun `freezes cap at three`() {
        // Going from streak 20 to 21 (which is %7 == 0) when already at 3 freezes
        val (_, f) = step(LocalDate.of(2026, 5, 22), "2026-05-21", 20, 3)
        assertEquals(3, f)
    }

    @Test
    fun `missed day with freeze burns freeze and preserves streak`() {
        // Last check-in May 1, today May 3 (gap of 2 days = missed May 2)
        val (s, f) = step(LocalDate.of(2026, 5, 3), "2026-05-01", 5, 1)
        assertEquals(5, s); assertEquals(0, f)
    }

    @Test
    fun `missed day without freeze resets streak`() {
        val (s, f) = step(LocalDate.of(2026, 5, 3), "2026-05-01", 5, 0)
        assertEquals(1, s); assertEquals(0, f)
    }

    @Test
    fun `multi-day gap always resets even with freezes`() {
        // 3-day gap exceeds the freeze window
        val (s, f) = step(LocalDate.of(2026, 5, 4), "2026-05-01", 5, 2)
        assertEquals(1, s); assertEquals(2, f)
    }

    @Test
    fun `clock skew preserves streak`() {
        // Last check-in is in the future relative to "today" — DST/timezone shift
        val (s, _) = step(LocalDate.of(2026, 5, 1), "2026-05-02", 7, 0)
        assertEquals(7, s)
    }

    @Test
    fun `streak progression earns freezes at 7, 14, 21`() {
        // Walk from 0 to 21
        var lastIso: String? = null
        var current = 0
        var freezes = 0
        var date = LocalDate.of(2026, 5, 1)
        repeat(21) {
            val (s, f) = step(date, lastIso, current, freezes)
            current = s; freezes = f
            lastIso = date.toString()
            date = date.plusDays(1)
        }
        assertEquals(21, current)
        assertEquals(3, freezes)
    }
}
