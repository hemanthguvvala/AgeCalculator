package com.hkgroups.agecalculator.util

import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * Lunar phase math. Pure, no Android dependencies — easy to test.
 *
 * Synodic month length and a known new-moon reference epoch let us compute
 * the moon's phase as a fraction in [0,1) where 0 = new, 0.5 = full.
 */
object LunarPhase {

    private const val SYNODIC_MONTH = 29.530588853

    // Reference new moon: 2000-01-06 18:14 UTC, expressed in fractional days since epoch.
    private val REFERENCE_NEW_MOON_DAYS: Double =
        LocalDate.of(2000, 1, 6).atStartOfDay(ZoneOffset.UTC).toEpochSecond() / 86400.0 + (18 * 60 + 14) / 1440.0

    /** Fractional position in the lunar cycle, 0 = new, 0.5 = full. */
    fun phaseFraction(date: LocalDate = LocalDate.now()): Double {
        val days = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond() / 86400.0
        val cycles = (days - REFERENCE_NEW_MOON_DAYS) / SYNODIC_MONTH
        val frac = cycles - kotlin.math.floor(cycles)
        return if (frac < 0) frac + 1.0 else frac
    }

    /** Illumination of the disk, 0..1 (0 = new, 1 = full). */
    fun illumination(fraction: Double): Double = (1 - cos(2 * PI * fraction)) / 2

    /** Whole-percent illumination, 0..100. */
    fun illuminationPercent(fraction: Double): Int = (illumination(fraction) * 100).roundToInt()

    enum class Phase(val title: String) {
        NewMoon("New Moon"),
        WaxingCrescent("Waxing Crescent"),
        FirstQuarter("First Quarter"),
        WaxingGibbous("Waxing Gibbous"),
        FullMoon("Full Moon"),
        WaningGibbous("Waning Gibbous"),
        LastQuarter("Last Quarter"),
        WaningCrescent("Waning Crescent")
    }

    fun phase(fraction: Double): Phase = when {
        fraction < 0.03 || fraction >= 0.97 -> Phase.NewMoon
        fraction < 0.22 -> Phase.WaxingCrescent
        fraction < 0.28 -> Phase.FirstQuarter
        fraction < 0.47 -> Phase.WaxingGibbous
        fraction < 0.53 -> Phase.FullMoon
        fraction < 0.72 -> Phase.WaningGibbous
        fraction < 0.78 -> Phase.LastQuarter
        else -> Phase.WaningCrescent
    }

    /** Days until the next major phase (new / first quarter / full / last quarter). */
    fun daysToNextMajorPhase(fraction: Double): Pair<Phase, Int> {
        val majors = listOf(0.0, 0.25, 0.5, 0.75, 1.0)
        val nextBoundary = majors.first { it > fraction }
        val days = ((nextBoundary - fraction) * SYNODIC_MONTH).roundToInt().coerceAtLeast(1)
        val nextPhase = when (nextBoundary) {
            0.25 -> Phase.FirstQuarter
            0.5 -> Phase.FullMoon
            0.75 -> Phase.LastQuarter
            else -> Phase.NewMoon
        }
        return nextPhase to days
    }

    /** Sign-influence hint: pithy line tying the current phase to the user's energy. */
    fun energyHint(phase: Phase): String = when (phase) {
        Phase.NewMoon -> "Set intentions. A clean slate to plant seeds."
        Phase.WaxingCrescent -> "Take the first step. Momentum builds quietly."
        Phase.FirstQuarter -> "Push through resistance. Decisions sharpen now."
        Phase.WaxingGibbous -> "Refine and adjust. The harvest is near."
        Phase.FullMoon -> "Release what no longer serves. Energy peaks."
        Phase.WaningGibbous -> "Reflect on what you've gathered. Share wisdom."
        Phase.LastQuarter -> "Let go and forgive. Make space for what's next."
        Phase.WaningCrescent -> "Rest, restore, and prepare. Inward focus."
    }

    /** Compact glyph used in compact widget layouts. */
    fun shortGlyph(phase: Phase): String = when (phase) {
        Phase.NewMoon -> "🌑"
        Phase.WaxingCrescent -> "🌒"
        Phase.FirstQuarter -> "🌓"
        Phase.WaxingGibbous -> "🌔"
        Phase.FullMoon -> "🌕"
        Phase.WaningGibbous -> "🌖"
        Phase.LastQuarter -> "🌗"
        Phase.WaningCrescent -> "🌘"
    }
}

/** Convenience top-level helper used in a few view layers. */
fun moonIlluminationOnDate(date: LocalDate = LocalDate.now()): Int =
    LunarPhase.illuminationPercent(LunarPhase.phaseFraction(date))
