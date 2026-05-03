package com.hkgroups.agecalculator.content

import com.hkgroups.agecalculator.data.repository.MoodEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ContentEngineTest {

    @Test
    fun `daily horoscope is deterministic per sign and date`() {
        val date = LocalDate.of(2026, 5, 1)
        val a = ContentEngine.dailyHoroscope("Aries", date)
        val b = ContentEngine.dailyHoroscope("Aries", date)
        assertEquals(a, b)
    }

    @Test
    fun `daily horoscope changes between days`() {
        val a = ContentEngine.dailyHoroscope("Aries", LocalDate.of(2026, 5, 1))
        val b = ContentEngine.dailyHoroscope("Aries", LocalDate.of(2026, 5, 2))
        // Not strictly required, but the random pick should virtually always differ
        // across ~98k unique combos. Treat collision as a flag-worthy event.
        assertFalse("Two consecutive days should rarely produce identical horoscopes", a == b)
    }

    @Test
    fun `daily horoscope differs by sign`() {
        val date = LocalDate.of(2026, 5, 1)
        val aries = ContentEngine.dailyHoroscope("Aries", date)
        val pisces = ContentEngine.dailyHoroscope("Pisces", date)
        assertFalse(aries == pisces)
    }

    @Test
    fun `daily horoscope substitutes sign placeholder when present`() {
        val date = LocalDate.of(2026, 5, 1)
        val text = ContentEngine.dailyHoroscope("Aries", date)
        // {sign} placeholder must be resolved (not literally present)
        assertFalse(text.contains("{sign}"))
        assertFalse(text.contains("{ruler}"))
        assertFalse(text.contains("{element}"))
        assertFalse(text.contains("{planet}"))
    }

    @Test
    fun `compatibility insight is order-independent`() {
        val a = ContentEngine.compatibilityInsight("Aries", "Leo")
        val b = ContentEngine.compatibilityInsight("Leo", "Aries")
        assertEquals(a, b)
    }

    @Test
    fun `compatibility insight handles same-sign pairs`() {
        val text = ContentEngine.compatibilityInsight("Aries", "Aries")
        assertTrue(text.contains("Aries"))
    }

    @Test
    fun `weekly forecast returns 7 entries`() {
        val forecast = ContentEngine.weeklyForecast("Aries", LocalDate.of(2026, 5, 4))
        assertEquals(7, forecast.size)
        assertTrue(forecast.all { it.isNotBlank() })
    }

    @Test
    fun `mood insight returns null below threshold`() {
        val entries = (1..3).map {
            MoodEntry(LocalDate.of(2026, 5, it), "happy", "")
        }
        assertNull(ContentEngine.moodInsight(entries))
    }

    @Test
    fun `mood insight surfaces best day-of-week pattern`() {
        // 8 entries — all positive moods on Thursdays
        val entries = (0L until 8L).map { offset ->
            // 2026-04-30 is a Thursday — alternate between Thu (happy) and Mon (sad)
            val date = LocalDate.of(2026, 4, 30).plusDays(offset)
            val mood = if (date.dayOfWeek.value == 4) "happy" else "sad"
            MoodEntry(date, mood, "")
        }
        val insight = ContentEngine.moodInsight(entries)
        assertNotNull(insight)
        assertTrue(insight!!.contains("Thursday"))
    }

    @Test
    fun `birthday window message fires within range`() {
        val birth = LocalDate.of(1990, 5, 5)
        // On the day
        val onDay = ContentEngine.birthdayWindowMessage("Taurus", birth, LocalDate.of(2026, 5, 5))
        assertNotNull(onDay)
        // 3 days before
        val before = ContentEngine.birthdayWindowMessage("Taurus", birth, LocalDate.of(2026, 5, 2))
        assertNotNull(before)
        // 30 days before — null
        val far = ContentEngine.birthdayWindowMessage("Taurus", birth, LocalDate.of(2026, 4, 1))
        assertNull(far)
    }

    @Test
    fun `question of the day differs by date`() {
        val q1 = ContentEngine.questionOfTheDay("Aries", LocalDate.of(2026, 5, 1))
        val q2 = ContentEngine.questionOfTheDay("Aries", LocalDate.of(2026, 5, 2))
        assertFalse(q1 == q2)
    }
}
