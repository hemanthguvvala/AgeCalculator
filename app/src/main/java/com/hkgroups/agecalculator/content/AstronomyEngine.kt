package com.hkgroups.agecalculator.content

import com.hkgroups.agecalculator.util.LunarPhase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Pure deterministic astronomy: Sun-sign of the day, ruling-planet of the day,
 * Mercury-retrograde windows, moon phase, "cosmic weather" classification.
 *
 * No network, no DB. Same input → same output. The astronomical math is
 * approximate (suitable for a consumer astrology app, not a planetarium) but
 * good enough to produce real, daily-changing context that drives content.
 */
object AstronomyEngine {

    /**
     * The zodiac sign the Sun is currently transiting (i.e. "today's sign").
     * Independent of the user's natal sign — drives "what energy is in the air".
     */
    fun sunSignOfDay(date: LocalDate = LocalDate.now()): String {
        val m = date.monthValue
        val d = date.dayOfMonth
        return when {
            (m == 3 && d >= 21) || (m == 4 && d <= 19) -> "Aries"
            (m == 4 && d >= 20) || (m == 5 && d <= 20) -> "Taurus"
            (m == 5 && d >= 21) || (m == 6 && d <= 20) -> "Gemini"
            (m == 6 && d >= 21) || (m == 7 && d <= 22) -> "Cancer"
            (m == 7 && d >= 23) || (m == 8 && d <= 22) -> "Leo"
            (m == 8 && d >= 23) || (m == 9 && d <= 22) -> "Virgo"
            (m == 9 && d >= 23) || (m == 10 && d <= 22) -> "Libra"
            (m == 10 && d >= 23) || (m == 11 && d <= 21) -> "Scorpio"
            (m == 11 && d >= 22) || (m == 12 && d <= 21) -> "Sagittarius"
            (m == 12 && d >= 22) || (m == 1 && d <= 19) -> "Capricorn"
            (m == 1 && d >= 20) || (m == 2 && d <= 18) -> "Aquarius"
            else -> "Pisces"
        }
    }

    /**
     * Classical planetary day rulership — a documented astrological tradition
     * that maps each weekday to a celestial body. Used to flavor daily content.
     */
    fun rulingPlanetOfDay(date: LocalDate = LocalDate.now()): String = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "Sun"
        DayOfWeek.MONDAY -> "Moon"
        DayOfWeek.TUESDAY -> "Mars"
        DayOfWeek.WEDNESDAY -> "Mercury"
        DayOfWeek.THURSDAY -> "Jupiter"
        DayOfWeek.FRIDAY -> "Venus"
        DayOfWeek.SATURDAY -> "Saturn"
    }

    /**
     * Mercury retrograde windows (approximated). Mercury goes retrograde
     * 3-4 times per year for ~3 weeks. Real ephemeris data would be more
     * precise; these windows are within a few days of the true dates.
     */
    private val MercuryRetrogradeWindows: List<Pair<MonthDay, MonthDay>> = listOf(
        // Late winter / early spring
        MonthDay.of(3, 14) to MonthDay.of(4, 7),
        // Mid-summer
        MonthDay.of(7, 18) to MonthDay.of(8, 11),
        // Late autumn / early winter
        MonthDay.of(11, 9) to MonthDay.of(11, 29)
    )

    fun isMercuryRetrograde(date: LocalDate = LocalDate.now()): Boolean {
        val md = MonthDay.of(date.monthValue, date.dayOfMonth)
        return MercuryRetrogradeWindows.any { (start, end) ->
            !md.isBefore(start) && !md.isAfter(end)
        }
    }

    /** Days until next Mercury Retrograde period. */
    fun daysToNextRetrograde(date: LocalDate = LocalDate.now()): Int {
        val candidates = MercuryRetrogradeWindows.flatMap { (start, _) ->
            listOf(start.atYear(date.year), start.atYear(date.year + 1))
        }.filter { it.isAfter(date) }.sorted()
        return candidates.firstOrNull()?.let {
            ChronoUnit.DAYS.between(date, it).toInt()
        } ?: 0
    }

    /** Element of a zodiac sign. */
    fun elementOf(sign: String): String = when (sign) {
        "Aries", "Leo", "Sagittarius" -> "Fire"
        "Taurus", "Virgo", "Capricorn" -> "Earth"
        "Gemini", "Libra", "Aquarius" -> "Air"
        "Cancer", "Scorpio", "Pisces" -> "Water"
        else -> "Aether"
    }

    /** Modality (cardinal/fixed/mutable) of a zodiac sign. */
    fun modalityOf(sign: String): String = when (sign) {
        "Aries", "Cancer", "Libra", "Capricorn" -> "Cardinal"
        "Taurus", "Leo", "Scorpio", "Aquarius" -> "Fixed"
        "Gemini", "Virgo", "Sagittarius", "Pisces" -> "Mutable"
        else -> "Universal"
    }

    /** Traditional ruling planet of a sign. */
    fun rulerOf(sign: String): String = when (sign) {
        "Aries" -> "Mars"
        "Taurus" -> "Venus"
        "Gemini" -> "Mercury"
        "Cancer" -> "Moon"
        "Leo" -> "Sun"
        "Virgo" -> "Mercury"
        "Libra" -> "Venus"
        "Scorpio" -> "Pluto"
        "Sagittarius" -> "Jupiter"
        "Capricorn" -> "Saturn"
        "Aquarius" -> "Uranus"
        "Pisces" -> "Neptune"
        else -> "Sun"
    }

    /** Whether the user's natal sign is "lit up" by today's sun-sign or the
     *  classical aspect signs (trine, sextile, opposite). */
    enum class TransitFlavor { Conjunction, Trine, Sextile, Square, Opposite, Quincunx }

    fun transitFlavor(natalSign: String, date: LocalDate = LocalDate.now()): TransitFlavor {
        val today = sunSignOfDay(date)
        if (today == natalSign) return TransitFlavor.Conjunction
        val signs = listOf(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        )
        val natalIdx = signs.indexOf(natalSign)
        val todayIdx = signs.indexOf(today)
        if (natalIdx < 0 || todayIdx < 0) return TransitFlavor.Sextile
        val diff = ((todayIdx - natalIdx + 12) % 12)
        return when (diff) {
            2, 10 -> TransitFlavor.Sextile
            3, 9 -> TransitFlavor.Square
            4, 8 -> TransitFlavor.Trine
            5, 7 -> TransitFlavor.Quincunx
            6 -> TransitFlavor.Opposite
            else -> TransitFlavor.Sextile
        }
    }

    /**
     * Cosmic-weather classification — a compact, deterministic enum derived
     * from moon phase + retrograde + ruling-planet of the day. Drives which
     * tone bucket the content engine pulls from.
     */
    enum class CosmicWeather { Bright, Reflective, Tense, Hopeful, Restorative, Bold }

    fun cosmicWeather(date: LocalDate = LocalDate.now()): CosmicWeather {
        val frac = LunarPhase.phaseFraction(date)
        val phase = LunarPhase.phase(frac)
        val retro = isMercuryRetrograde(date)
        val planet = rulingPlanetOfDay(date)
        return when {
            retro -> CosmicWeather.Reflective
            phase == LunarPhase.Phase.FullMoon -> CosmicWeather.Bold
            phase == LunarPhase.Phase.NewMoon -> CosmicWeather.Hopeful
            phase == LunarPhase.Phase.WaningCrescent -> CosmicWeather.Restorative
            phase == LunarPhase.Phase.WaxingGibbous -> CosmicWeather.Bright
            phase == LunarPhase.Phase.WaningGibbous -> CosmicWeather.Reflective
            planet == "Mars" -> CosmicWeather.Bold
            planet == "Saturn" -> CosmicWeather.Tense
            planet == "Venus" -> CosmicWeather.Hopeful
            planet == "Jupiter" -> CosmicWeather.Bright
            else -> CosmicWeather.Bright
        }
    }

    /** Stable "lucky number 1..99" derived from the user's sign + date.
     *  Same user same day = same number; changes daily. */
    fun luckyNumber(natalSign: String, date: LocalDate = LocalDate.now()): Int {
        val seed = (date.toEpochDay() * 31 + natalSign.hashCode()).toInt()
        return ((seed and 0x7fffffff) % 99) + 1
    }

    /** Stable lucky color name. */
    fun luckyColor(natalSign: String, date: LocalDate = LocalDate.now()): String {
        val palette = listOf(
            "Crimson", "Coral", "Amber", "Gold", "Saffron", "Olive",
            "Emerald", "Teal", "Sky Blue", "Sapphire", "Indigo", "Violet",
            "Magenta", "Rose", "Pearl", "Silver"
        )
        val seed = (date.toEpochDay() * 17 + natalSign.hashCode()).toInt()
        return palette[((seed and 0x7fffffff) % palette.size)]
    }

    /** Compact summary of the day's astronomical state. Single source of truth
     *  for the content engine — pass this around instead of recomputing. */
    data class CosmicSnapshot(
        val date: LocalDate,
        val sunSignOfDay: String,
        val rulingPlanetOfDay: String,
        val moonPhase: LunarPhase.Phase,
        val moonIllumination: Int,
        val isMercuryRetrograde: Boolean,
        val daysToNextRetrograde: Int,
        val weather: CosmicWeather
    )

    fun snapshot(date: LocalDate = LocalDate.now()): CosmicSnapshot {
        val frac = LunarPhase.phaseFraction(date)
        return CosmicSnapshot(
            date = date,
            sunSignOfDay = sunSignOfDay(date),
            rulingPlanetOfDay = rulingPlanetOfDay(date),
            moonPhase = LunarPhase.phase(frac),
            moonIllumination = LunarPhase.illuminationPercent(frac),
            isMercuryRetrograde = isMercuryRetrograde(date),
            daysToNextRetrograde = daysToNextRetrograde(date),
            weather = cosmicWeather(date)
        )
    }
}
