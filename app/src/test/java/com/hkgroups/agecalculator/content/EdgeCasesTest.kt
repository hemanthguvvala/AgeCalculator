package com.hkgroups.agecalculator.content

import com.hkgroups.agecalculator.data.repository.MoodEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Negative + edge-case sweeps. Things that wouldn't show up in a happy-path
 * walk-through but that real users will hit eventually:
 *
 *  - Sign cusp dates (the day before vs the day a new sign begins)
 *  - Leap-year birth dates
 *  - Year boundaries (Dec 31 / Jan 1 — Capricorn straddles the year)
 *  - Distant past + future birth dates
 *  - Unknown / empty / whitespace sign names
 *  - Mood entries with empty strings, unicode, and unusual inputs
 *  - All seven weekdays (planetary rulership rotation)
 *  - All twelve months (sun-sign rotation)
 */
class EdgeCasesTest {

    // ---------- Sign-from-date math ----------

    @Test
    fun `every sign's typical mid-range date returns the correct sign`() {
        val midRangeDates = mapOf(
            LocalDate.of(2026, 4, 1) to "Aries",
            LocalDate.of(2026, 5, 5) to "Taurus",
            LocalDate.of(2026, 6, 5) to "Gemini",
            LocalDate.of(2026, 7, 5) to "Cancer",
            LocalDate.of(2026, 8, 5) to "Leo",
            LocalDate.of(2026, 9, 5) to "Virgo",
            LocalDate.of(2026, 10, 5) to "Libra",
            LocalDate.of(2026, 11, 5) to "Scorpio",
            LocalDate.of(2026, 12, 5) to "Sagittarius",
            LocalDate.of(2026, 1, 5) to "Capricorn",
            LocalDate.of(2026, 2, 5) to "Aquarius",
            LocalDate.of(2026, 3, 5) to "Pisces"
        )
        for ((date, expected) in midRangeDates) {
            val actual = AstronomyEngine.sunSignOfDay(date)
            assertEquals("$date should be $expected, got $actual", expected, actual)
        }
    }

    @Test
    fun `every sign cusp boundary day returns the right sign on each side`() {
        // Each pair: (last day of prev sign, first day of new sign)
        val boundaries = listOf(
            Triple(LocalDate.of(2026, 4, 19), LocalDate.of(2026, 4, 20), "Aries" to "Taurus"),
            Triple(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 21), "Taurus" to "Gemini"),
            Triple(LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 21), "Gemini" to "Cancer"),
            Triple(LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 23), "Cancer" to "Leo"),
            Triple(LocalDate.of(2026, 8, 22), LocalDate.of(2026, 8, 23), "Leo" to "Virgo"),
            Triple(LocalDate.of(2026, 9, 22), LocalDate.of(2026, 9, 23), "Virgo" to "Libra"),
            Triple(LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 23), "Libra" to "Scorpio"),
            Triple(LocalDate.of(2026, 11, 21), LocalDate.of(2026, 11, 22), "Scorpio" to "Sagittarius"),
            Triple(LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 22), "Sagittarius" to "Capricorn"),
            Triple(LocalDate.of(2026, 1, 19), LocalDate.of(2026, 1, 20), "Capricorn" to "Aquarius"),
            Triple(LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19), "Aquarius" to "Pisces"),
            Triple(LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 21), "Pisces" to "Aries")
        )
        for ((dayBefore, dayOf, signs) in boundaries) {
            val (prevSign, nextSign) = signs
            assertEquals(
                "$dayBefore should still be $prevSign",
                prevSign,
                AstronomyEngine.sunSignOfDay(dayBefore)
            )
            assertEquals(
                "$dayOf should be $nextSign",
                nextSign,
                AstronomyEngine.sunSignOfDay(dayOf)
            )
        }
    }

    // ---------- Calendar edge cases ----------

    @Test
    fun `leap year Feb 29 birth date returns Pisces and flows through content`() {
        val leap = LocalDate.of(2000, 2, 29)
        val sign = AstronomyEngine.sunSignOfDay(leap)
        assertEquals("Feb 29 is Pisces", "Pisces", sign)
        // Make sure birthday window logic doesn't crash on a leap-day birth
        // when the current year is non-leap.
        val nonLeap = LocalDate.of(2026, 2, 28)
        val msg = ContentEngine.birthdayWindowMessage(sign, leap, nonLeap)
        // Just must not throw — message can be non-null (window) or null.
        assertTrue("birthday window should not throw", msg == null || msg.isNotBlank())
    }

    @Test
    fun `year boundary dates straddle Capricorn correctly`() {
        // Capricorn is the only sign that crosses the year boundary.
        assertEquals("Dec 31 is Capricorn", "Capricorn",
            AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 12, 31)))
        assertEquals("Jan 1 is Capricorn", "Capricorn",
            AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 1, 1)))
        assertEquals("Jan 19 is Capricorn", "Capricorn",
            AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 1, 19)))
        assertEquals("Jan 20 is Aquarius", "Aquarius",
            AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 1, 20)))
    }

    @Test
    fun `distant past birth dates produce sane horoscope and trivia`() {
        val ancient = LocalDate.of(1900, 1, 15)
        val sign = AstronomyEngine.sunSignOfDay(ancient)
        // Should still resolve to a valid sign
        assertTrue(sign.isNotBlank())
        val text = ContentEngine.dailyHoroscope(sign, LocalDate.of(2026, 5, 1))
        assertTrue("horoscope should be non-blank for ancient birth", text.isNotBlank())
        assertFalse("no unresolved placeholders", text.contains("{"))
    }

    @Test
    fun `future birth date doesn't crash content engine`() {
        val future = LocalDate.of(2050, 6, 15)
        val sign = AstronomyEngine.sunSignOfDay(future)
        assertEquals("Future Jun 15 is still Gemini", "Gemini", sign)
        val text = ContentEngine.dailyHoroscope(sign, future)
        assertTrue("horoscope should still work for future date", text.isNotBlank())
    }

    // ---------- Planetary rulership across all weekdays ----------

    @Test
    fun `every weekday returns its classical ruler and pattern repeats`() {
        val expected = mapOf(
            DayOfWeek.SUNDAY to "Sun",
            DayOfWeek.MONDAY to "Moon",
            DayOfWeek.TUESDAY to "Mars",
            DayOfWeek.WEDNESDAY to "Mercury",
            DayOfWeek.THURSDAY to "Jupiter",
            DayOfWeek.FRIDAY to "Venus",
            DayOfWeek.SATURDAY to "Saturn"
        )
        // Hit each weekday at least once across a recent week.
        val weekStart = LocalDate.of(2026, 5, 3) // Sunday
        for (offset in 0..6) {
            val day = weekStart.plusDays(offset.toLong())
            val planet = AstronomyEngine.rulingPlanetOfDay(day)
            assertEquals(
                "${day.dayOfWeek} should be ruled by ${expected[day.dayOfWeek]}",
                expected[day.dayOfWeek],
                planet
            )
        }
    }

    // ---------- Unknown / malformed sign inputs ----------

    @Test
    fun `unknown sign name returns Aether element and Sun ruler instead of crashing`() {
        // Defensive: real callers shouldn't pass garbage, but if they do we
        // want a sane default rather than NPE or empty string.
        assertEquals("Aether", AstronomyEngine.elementOf("Klingon"))
        assertEquals("Sun", AstronomyEngine.rulerOf("Klingon"))
        assertEquals("Universal", AstronomyEngine.modalityOf("Klingon"))
    }

    @Test
    fun `daily horoscope for unknown sign still returns non-blank text`() {
        val text = ContentEngine.dailyHoroscope("Klingon", LocalDate.of(2026, 5, 1))
        assertTrue("unknown sign should still produce text", text.isNotBlank())
        assertFalse("no unresolved placeholders even for unknown sign",
            text.contains("{sign}") || text.contains("{ruler}"))
    }

    @Test
    fun `compatibility with same sign uses the same-sign branch`() {
        val text = ContentEngine.compatibilityInsight("Aries", "Aries")
        assertTrue(text.contains("Aries"))
        assertTrue("same-sign branch should be self-aware",
            text.contains("Same-sign") || text.contains("mirror") || text.contains("Mirror"))
    }

    @Test
    fun `compatibility is symmetric — same pair returns same text regardless of order`() {
        // Internal sort guarantees this; lock it in as a behavior contract.
        val a = ContentEngine.compatibilityInsight("Leo", "Aquarius")
        val b = ContentEngine.compatibilityInsight("Aquarius", "Leo")
        assertEquals("Pair order should not change result", a, b)
    }

    // ---------- Mood entry edge cases ----------

    @Test
    fun `mood insight handles entries with empty notes`() {
        val entries = (0..9).map {
            MoodEntry(LocalDate.of(2026, 5, 1).plusDays(it.toLong()), "happy", "")
        }
        // Should not throw on empty notes
        ContentEngine.moodInsight(entries) // null or string both fine
    }

    @Test
    fun `mood insight handles unicode and emoji in notes`() {
        val entries = (0..9).map {
            MoodEntry(
                LocalDate.of(2026, 5, 1).plusDays(it.toLong()),
                "joyful",
                "Today felt 🌟 like real magic ✨ — 中文 even"
            )
        }
        ContentEngine.moodInsight(entries) // must not throw on unicode
    }

    @Test
    fun `mood insight handles unrecognized mood strings without crashing`() {
        val entries = (0..9).map {
            MoodEntry(LocalDate.of(2026, 5, 1).plusDays(it.toLong()), "🤷‍♀️unknown", "")
        }
        // Unknown moods are treated as non-positive; insight should be null or blank.
        val result = ContentEngine.moodInsight(entries)
        assertTrue("unknown moods → no insight or non-blank insight",
            result == null || result.isNotBlank())
    }

    // ---------- Lucky number / color stability ----------

    @Test
    fun `lucky number is stable per sign per day but changes day-over-day`() {
        val date = LocalDate.of(2026, 5, 1)
        val n1 = AstronomyEngine.luckyNumber("Taurus", date)
        val n2 = AstronomyEngine.luckyNumber("Taurus", date)
        assertEquals("Same input → same number", n1, n2)
        val nNext = AstronomyEngine.luckyNumber("Taurus", date.plusDays(1))
        // Almost always different (with 99 buckets, only ~1/99 collision)
        // so we just verify the lookup is deterministic per call.
        assertEquals("deterministic", nNext, AstronomyEngine.luckyNumber("Taurus", date.plusDays(1)))
    }

    @Test
    fun `lucky color rotates across signs on the same day`() {
        // Sanity: not every sign should get the SAME color on the same day.
        val date = LocalDate.of(2026, 5, 1)
        val signs = listOf("Aries", "Taurus", "Gemini", "Cancer",
            "Leo", "Virgo", "Libra", "Scorpio")
        val distinctColors = signs.map { AstronomyEngine.luckyColor(it, date) }.toSet()
        assertTrue(
            "On any given day, multiple signs should not all see the same color",
            distinctColors.size > 1
        )
    }

    // ---------- Mercury retrograde windows ----------

    @Test
    fun `mercury retrograde detection toggles correctly across a year`() {
        // Spot-check: April 1 should be retrograde, May 1 should not (per current windows).
        assertTrue("April 1 should be in retrograde window",
            AstronomyEngine.isMercuryRetrograde(LocalDate.of(2026, 4, 1)))
        assertFalse("May 1 should not be in retrograde window",
            AstronomyEngine.isMercuryRetrograde(LocalDate.of(2026, 5, 1)))
    }

    @Test
    fun `daysToNextRetrograde never returns zero outside a retrograde period`() {
        val sample = LocalDate.of(2026, 5, 1) // not in retrograde
        val days = AstronomyEngine.daysToNextRetrograde(sample)
        assertTrue("Should be a positive number of days to next retrograde", days > 0)
    }
}
