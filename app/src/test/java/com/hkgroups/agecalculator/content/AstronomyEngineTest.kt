package com.hkgroups.agecalculator.content

import com.hkgroups.agecalculator.content.AstronomyEngine.TransitFlavor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class AstronomyEngineTest {

    @Test
    fun `sun sign of day matches calendar boundaries`() {
        assertEquals("Aries", AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 3, 21)))
        assertEquals("Pisces", AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 3, 20)))
        assertEquals("Capricorn", AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 1, 1)))
        assertEquals("Aquarius", AstronomyEngine.sunSignOfDay(LocalDate.of(2026, 1, 20)))
    }

    @Test
    fun `ruling planet of day follows traditional weekday mapping`() {
        // Pick a known date — 2026-05-04 is a Monday
        val monday = LocalDate.of(2026, 5, 4)
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        assertEquals("Moon", AstronomyEngine.rulingPlanetOfDay(monday))
        assertEquals("Mars", AstronomyEngine.rulingPlanetOfDay(monday.plusDays(1)))
        assertEquals("Mercury", AstronomyEngine.rulingPlanetOfDay(monday.plusDays(2)))
        assertEquals("Jupiter", AstronomyEngine.rulingPlanetOfDay(monday.plusDays(3)))
        assertEquals("Venus", AstronomyEngine.rulingPlanetOfDay(monday.plusDays(4)))
        assertEquals("Saturn", AstronomyEngine.rulingPlanetOfDay(monday.plusDays(5)))
        assertEquals("Sun", AstronomyEngine.rulingPlanetOfDay(monday.plusDays(6)))
    }

    @Test
    fun `mercury retrograde windows fire within expected ranges`() {
        // March 20 — within first window (Mar 14 - Apr 7)
        assertTrue(AstronomyEngine.isMercuryRetrograde(LocalDate.of(2026, 3, 20)))
        // April 8 — just outside first window
        assertEquals(false, AstronomyEngine.isMercuryRetrograde(LocalDate.of(2026, 4, 8)))
        // August 1 — within second window (Jul 18 - Aug 11)
        assertTrue(AstronomyEngine.isMercuryRetrograde(LocalDate.of(2026, 8, 1)))
        // June 1 — between windows
        assertEquals(false, AstronomyEngine.isMercuryRetrograde(LocalDate.of(2026, 6, 1)))
    }

    @Test
    fun `transit flavor is stable for known sign-pairs`() {
        // Aries natal, sun in Aries → Conjunction
        val ariesSeason = LocalDate.of(2026, 4, 1)
        assertEquals(TransitFlavor.Conjunction, AstronomyEngine.transitFlavor("Aries", ariesSeason))
        // Aries natal, sun in Libra → Opposite
        val libraSeason = LocalDate.of(2026, 10, 1)
        assertEquals(TransitFlavor.Opposite, AstronomyEngine.transitFlavor("Aries", libraSeason))
    }

    @Test
    fun `element of returns correct quadruplicity`() {
        assertEquals("Fire", AstronomyEngine.elementOf("Aries"))
        assertEquals("Earth", AstronomyEngine.elementOf("Taurus"))
        assertEquals("Air", AstronomyEngine.elementOf("Gemini"))
        assertEquals("Water", AstronomyEngine.elementOf("Cancer"))
        assertEquals("Fire", AstronomyEngine.elementOf("Leo"))
    }

    @Test
    fun `lucky number is deterministic and bounded`() {
        val a = AstronomyEngine.luckyNumber("Aries", LocalDate.of(2026, 5, 1))
        val b = AstronomyEngine.luckyNumber("Aries", LocalDate.of(2026, 5, 1))
        assertEquals(a, b)
        assertTrue(a in 1..99)
        // Different date → likely different number
        val c = AstronomyEngine.luckyNumber("Aries", LocalDate.of(2026, 5, 2))
        assertTrue(c in 1..99)
    }

    @Test
    fun `snapshot fields populate consistently`() {
        val snap = AstronomyEngine.snapshot(LocalDate.of(2026, 5, 1))
        assertNotNull(snap.sunSignOfDay)
        assertNotNull(snap.rulingPlanetOfDay)
        assertNotNull(snap.moonPhase)
        assertNotNull(snap.weather)
        assertTrue(snap.moonIllumination in 0..100)
    }
}
