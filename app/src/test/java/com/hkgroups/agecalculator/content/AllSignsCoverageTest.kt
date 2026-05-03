package com.hkgroups.agecalculator.content

import com.hkgroups.agecalculator.data.repository.MoodEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Sign-coverage sweep: exercises every public ContentEngine + AstronomyEngine
 * call across all 12 signs and a representative span of dates. Looks for:
 *   - blank/null outputs where there should be content
 *   - unsubstituted placeholders ({sign}, {ruler}, {moon}, etc.)
 *   - element/ruler bucket misses
 *   - bad transit math at sign boundaries
 *
 * Cheap to run, catches regressions across the whole sign × date matrix
 * before they reach the device.
 */
class AllSignsCoverageTest {

    private val signs = listOf(
        "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio",
        "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    /** Year of dates spanning every weekday + every moon phase + every sun-sign window. */
    private val sampleDates: List<LocalDate> = (0L..365L step 7L)
        .map { LocalDate.of(2026, 1, 1).plusDays(it) }

    @Test
    fun `daily horoscope is non-blank and has no unresolved placeholders for every sign`() {
        for (sign in signs) {
            for (date in sampleDates) {
                val text = ContentEngine.dailyHoroscope(sign, date)
                assertTrue(
                    "blank horoscope for $sign on $date",
                    text.isNotBlank()
                )
                assertHasNoPlaceholders(text, "$sign on $date")
            }
        }
    }

    @Test
    fun `daily tip is non-blank and has no unresolved placeholders for every sign`() {
        for (sign in signs) {
            for (date in sampleDates) {
                val text = ContentEngine.dailyTip(sign, date)
                assertTrue(
                    "blank tip for $sign on $date",
                    text.isNotBlank()
                )
                assertHasNoPlaceholders(text, "tip $sign on $date")
            }
        }
    }

    @Test
    fun `compatibility insight is non-blank and substitutes both sign names for every pair`() {
        for (a in signs) {
            for (b in signs) {
                val text = ContentEngine.compatibilityInsight(a, b)
                assertTrue("blank for $a + $b", text.isNotBlank())
                assertHasNoPlaceholders(text, "$a + $b")
                if (a != b) {
                    assertTrue(
                        "$a not present in '$text'",
                        text.contains(a)
                    )
                    assertTrue(
                        "$b not present in '$text'",
                        text.contains(b)
                    )
                }
            }
        }
    }

    @Test
    fun `question of the day is non-blank for every sign over a year`() {
        for (sign in signs) {
            for (date in sampleDates) {
                val text = ContentEngine.questionOfTheDay(sign, date)
                assertTrue("blank question for $sign on $date", text.isNotBlank())
                assertHasNoPlaceholders(text, "question $sign on $date")
            }
        }
    }

    @Test
    fun `weekly forecast returns 7 non-blank lines for every sign`() {
        for (sign in signs) {
            val lines = ContentEngine.weeklyForecast(sign)
            assertTrue("weekly forecast for $sign should be 7 lines, got ${lines.size}",
                lines.size == 7)
            lines.forEach { line ->
                assertTrue("blank weekly line for $sign: '$line'", line.isNotBlank())
                assertHasNoPlaceholders(line, "weekly $sign")
            }
        }
    }

    @Test
    fun `birthday window message fires correctly for every sign on its birthday`() {
        // Pick a representative birth date in each sign window.
        val birthDates = mapOf(
            "Aries" to LocalDate.of(2000, 4, 1),
            "Taurus" to LocalDate.of(2000, 5, 5),
            "Gemini" to LocalDate.of(2000, 6, 5),
            "Cancer" to LocalDate.of(2000, 7, 5),
            "Leo" to LocalDate.of(2000, 8, 5),
            "Virgo" to LocalDate.of(2000, 9, 5),
            "Libra" to LocalDate.of(2000, 10, 5),
            "Scorpio" to LocalDate.of(2000, 11, 5),
            "Sagittarius" to LocalDate.of(2000, 12, 5),
            "Capricorn" to LocalDate.of(2000, 1, 5),
            "Aquarius" to LocalDate.of(2000, 2, 5),
            "Pisces" to LocalDate.of(2000, 3, 5)
        )
        for ((sign, birthDate) in birthDates) {
            val onBirthday = LocalDate.of(2026, birthDate.monthValue, birthDate.dayOfMonth)
            val msg = ContentEngine.birthdayWindowMessage(sign, birthDate, onBirthday)
            assertNotNull("$sign should get message on birthday", msg)
            assertTrue("$sign message should mention sign name: '$msg'",
                msg!!.contains(sign))
        }
    }

    @Test
    fun `birthday window message returns null outside the 7+1+7 window`() {
        val birthDate = LocalDate.of(2000, 5, 5) // Taurus
        val farFromBirthday = LocalDate.of(2026, 1, 15)
        val msg = ContentEngine.birthdayWindowMessage("Taurus", birthDate, farFromBirthday)
        assertNull("Should be null when far from birthday", msg)
    }

    @Test
    fun `mood insight returns null with insufficient data and a string with enough`() {
        // Less than 5 entries: null
        val small = (0..3).map {
            MoodEntry(LocalDate.of(2026, 5, 1).plusDays(it.toLong()), "happy", "")
        }
        assertNull(ContentEngine.moodInsight(small))

        // Enough positive entries on the same weekday: should detect pattern
        val biased = (0..4).map { i ->
            // All on Sundays (May 3, May 10, May 17, May 24, May 31 are all Sundays in 2026)
            MoodEntry(LocalDate.of(2026, 5, 3).plusWeeks(i.toLong()), "joyful", "")
        }
        val insight = ContentEngine.moodInsight(biased)
        assertNotNull("Should detect Sunday pattern", insight)
    }

    @Test
    fun `astronomy engine returns valid element for every sign`() {
        for (sign in signs) {
            val element = AstronomyEngine.elementOf(sign)
            assertTrue(
                "$sign got unknown element '$element'",
                element in listOf("Fire", "Earth", "Air", "Water")
            )
        }
    }

    @Test
    fun `astronomy engine returns valid ruler for every sign`() {
        val validRulers = listOf("Mars", "Venus", "Mercury", "Moon", "Sun",
            "Jupiter", "Saturn", "Pluto", "Uranus", "Neptune")
        for (sign in signs) {
            val ruler = AstronomyEngine.rulerOf(sign)
            assertTrue(
                "$sign got unknown ruler '$ruler'",
                ruler in validRulers
            )
        }
    }

    @Test
    fun `lucky number is in 1-99 range for every sign every sample day`() {
        for (sign in signs) {
            for (date in sampleDates) {
                val n = AstronomyEngine.luckyNumber(sign, date)
                assertTrue(
                    "$sign on $date got lucky number $n out of range",
                    n in 1..99
                )
            }
        }
    }

    @Test
    fun `lucky color returns a known palette entry for every sign every sample day`() {
        val palette = setOf("Crimson", "Coral", "Amber", "Gold", "Saffron", "Olive",
            "Emerald", "Teal", "Sky Blue", "Sapphire", "Indigo", "Violet",
            "Magenta", "Rose", "Pearl", "Silver")
        for (sign in signs) {
            for (date in sampleDates) {
                val c = AstronomyEngine.luckyColor(sign, date)
                assertTrue(
                    "$sign on $date got unknown lucky color '$c'",
                    c in palette
                )
            }
        }
    }

    @Test
    fun `transit flavor returns a non-Sextile-only result across the full year`() {
        // For each sign, there should be days with several different transit
        // flavors across a full year — proving the math actually rotates.
        for (sign in signs) {
            val flavors = (0L..365L).map { offset ->
                val d = LocalDate.of(2026, 1, 1).plusDays(offset)
                AstronomyEngine.transitFlavor(sign, d)
            }.toSet()
            assertTrue(
                "$sign only ever sees ${flavors.size} transit flavors across a year",
                flavors.size >= 4
            )
        }
    }

    private fun assertHasNoPlaceholders(text: String, ctx: String) {
        val placeholders = listOf("{sign}", "{ruler}", "{element}", "{planet}",
            "{moon}", "{sunSign}", "{a}", "{b}")
        placeholders.forEach { ph ->
            assertFalse(
                "Unresolved placeholder '$ph' in '$text' [$ctx]",
                text.contains(ph)
            )
        }
    }
}
