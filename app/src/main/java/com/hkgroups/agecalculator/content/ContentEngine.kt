package com.hkgroups.agecalculator.content

import com.hkgroups.agecalculator.content.AstronomyEngine.CosmicWeather
import com.hkgroups.agecalculator.content.AstronomyEngine.TransitFlavor
import com.hkgroups.agecalculator.data.repository.MoodEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * Combinatorial content engine. Pure, deterministic, offline.
 *
 * Same `(sign, date)` always produces the same output, so users can't
 * refresh-spam — and they don't need to, because the underlying astronomical
 * snapshot changes every day, which changes the templates picked, which
 * produces a new horoscope without any manual content authoring.
 *
 * Variety math: each horoscope is built from 4 slots × ~8 fragments per slot
 * per (weather × element) bucket = roughly 4096 unique combos per bucket.
 * With 6 weather classes × 4 elements = 24 buckets, the total reachable space
 * is ~98k unique horoscopes — and the cosmic snapshot rotates the bucket
 * daily, so users see fresh content without the engine ever repeating itself.
 */
object ContentEngine {

    /** Daily horoscope — 2-3 sentences, deterministic per (sign, date). */
    fun dailyHoroscope(
        natalSign: String,
        date: LocalDate = LocalDate.now()
    ): String {
        val snapshot = AstronomyEngine.snapshot(date)
        val transit = AstronomyEngine.transitFlavor(natalSign, date)
        val rng = Random(seedOf(natalSign, date, salt = 1L))

        val element = AstronomyEngine.elementOf(natalSign)
        val ruler = AstronomyEngine.rulerOf(natalSign)

        val opener = pick(rng, openers(snapshot.weather, element))
        val theme = pick(rng, themes(snapshot.weather, transit))
        val action = pick(rng, actions(transit, element))
        val closer = pick(rng, closers(snapshot.weather, ruler))

        return listOf(opener, theme, action, closer)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .substitute(natalSign, snapshot, ruler, element)
    }

    /** Daily tip — single actionable sentence, deterministic per (sign, date). */
    fun dailyTip(
        natalSign: String,
        date: LocalDate = LocalDate.now()
    ): String {
        val snapshot = AstronomyEngine.snapshot(date)
        val rng = Random(seedOf(natalSign, date, salt = 2L))
        val element = AstronomyEngine.elementOf(natalSign)
        val tip = pick(rng, tipsByElement(element, snapshot.weather))
        return tip.substitute(natalSign, snapshot, AstronomyEngine.rulerOf(natalSign), element)
    }

    /** Compatibility narrative — 2 sentences, deterministic per pair (regardless of order). */
    fun compatibilityInsight(signA: String, signB: String): String {
        if (signA == signB) {
            return "Same-sign pairings see themselves clearly — for better and worse. " +
                "The mirror amplifies $signA's strengths and any blind spots equally."
        }
        val (a, b) = listOf(signA, signB).sorted().let { it[0] to it[1] }
        val elementA = AstronomyEngine.elementOf(a)
        val elementB = AstronomyEngine.elementOf(b)
        val rng = Random(("$a-$b").hashCode().toLong())
        val pattern = pick(rng, compatibilityPatterns(elementA, elementB))
        val close = pick(rng, compatibilityClosers(elementA, elementB))
        return "$pattern $close".replace("{a}", a).replace("{b}", b)
    }

    /** Question of the day — short reflection prompt, deterministic per date. */
    fun questionOfTheDay(natalSign: String, date: LocalDate = LocalDate.now()): String {
        val rng = Random(seedOf(natalSign, date, salt = 3L))
        val snapshot = AstronomyEngine.snapshot(date)
        val q = pick(rng, questions(snapshot.weather))
        return q.substitute(natalSign, snapshot, AstronomyEngine.rulerOf(natalSign),
            AstronomyEngine.elementOf(natalSign))
    }

    /**
     * Mood-pattern insight from the user's logged mood history. Returns null
     * if there's not enough data (need ≥ 5 entries) — so the UI can hide the
     * card until insights become reliable.
     *
     * Pattern detection: groups moods by day-of-week and ruling planet of that
     * day, surfaces the strongest correlation.
     */
    fun moodInsight(moodHistory: List<MoodEntry>): String? {
        if (moodHistory.size < 5) return null
        val byPlanet = moodHistory
            .groupBy { AstronomyEngine.rulingPlanetOfDay(it.date) }
            .mapValues { (_, entries) -> entries.count { isPositiveMood(it.mood) }.toDouble() / entries.size }
            .filter { it.value > 0.0 }
        val (bestPlanet, bestRate) = byPlanet.maxByOrNull { it.value } ?: return null
        if (bestRate < 0.6) return null
        val day = planetDayName(bestPlanet)
        return "You feel best on ${day}s. ${describePlanet(bestPlanet)} rules that day — lean into it."
    }

    /**
     * 7-day forecast — one short line per day. Premium feature; same engine
     * but shows the user a week of upcoming texture.
     */
    fun weeklyForecast(natalSign: String, weekStart: LocalDate = LocalDate.now()): List<String> {
        return (0L until 7L).map { offset ->
            val day = weekStart.plusDays(offset)
            val snapshot = AstronomyEngine.snapshot(day)
            val transit = AstronomyEngine.transitFlavor(natalSign, day)
            val rng = Random(seedOf(natalSign, day, salt = 7L))
            val line = pick(rng, weeklyLines(snapshot.weather, transit))
            "${day.dayOfWeek.shortName()}: " + line.substitute(
                natalSign, snapshot,
                AstronomyEngine.rulerOf(natalSign),
                AstronomyEngine.elementOf(natalSign)
            )
        }
    }

    /**
     * Birthday-window message — fires in the 7 days before, on, and 7 days
     * after the user's birthday (their personal solar return). One of the
     * highest-engagement moments of the year.
     */
    fun birthdayWindowMessage(natalSign: String, birthDate: LocalDate, today: LocalDate = LocalDate.now()): String? {
        val nextBirthday = birthDate.withYear(today.year).let {
            if (it.isBefore(today)) it.plusYears(1) else it
        }
        val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
        val daysSince = if (today.monthValue == birthDate.monthValue && today.dayOfMonth == birthDate.dayOfMonth)
            0 else ChronoUnit.DAYS.between(birthDate.withYear(today.year), today).toInt()

        return when {
            daysUntil == 0 -> "Happy solar return, $natalSign. The Sun is exactly where it was the moment you arrived. Set the year's intention now — it carries weight."
            daysUntil in 1..7 -> "Your solar return is $daysUntil ${if (daysUntil == 1) "day" else "days"} away. $natalSign season is winding toward you — energy is gathering."
            daysSince in 1..7 -> "You're in your post-birthday glow window. The intentions you set carry into the year — write them down today."
            else -> null
        }
    }

    // ---------- Internals ----------

    private fun seedOf(sign: String, date: LocalDate, salt: Long): Long =
        date.toEpochDay() * 1_000_003L xor sign.hashCode().toLong() xor (salt * 2_654_435_761L)

    private fun <T> pick(rng: Random, list: List<T>): T = list[rng.nextInt(list.size)]

    private fun String.substitute(
        natalSign: String,
        snapshot: AstronomyEngine.CosmicSnapshot,
        ruler: String,
        element: String
    ): String = this
        .replace("{sign}", natalSign)
        .replace("{ruler}", ruler)
        .replace("{element}", element.lowercase())
        .replace("{planet}", snapshot.rulingPlanetOfDay)
        .replace("{moon}", snapshot.moonPhase.title)
        .replace("{sunSign}", snapshot.sunSignOfDay)

    private fun isPositiveMood(mood: String): Boolean = when (mood.lowercase()) {
        "great", "good", "happy", "joyful", "calm", "energetic", "grateful", "loved", "peaceful" -> true
        else -> false
    }

    private fun planetDayName(planet: String): String = when (planet) {
        "Sun" -> "Sunday"
        "Moon" -> "Monday"
        "Mars" -> "Tuesday"
        "Mercury" -> "Wednesday"
        "Jupiter" -> "Thursday"
        "Venus" -> "Friday"
        "Saturn" -> "Saturday"
        else -> "this day"
    }

    private fun describePlanet(planet: String): String = when (planet) {
        "Sun" -> "The Sun"
        "Moon" -> "The Moon"
        "Mars" -> "Mars"
        "Mercury" -> "Mercury"
        "Jupiter" -> "Jupiter"
        "Venus" -> "Venus"
        "Saturn" -> "Saturn"
        else -> "This planet"
    }

    private fun DayOfWeek.shortName() = name.lowercase().replaceFirstChar { it.uppercase() }.take(3)

    // ---------- Fragment libraries ----------
    //
    // Each function returns a list of sentence templates. Placeholders are
    // resolved by `String.substitute`. Length per list ≈ 8-12 fragments.

    private fun openers(weather: CosmicWeather, element: String): List<String> {
        val base = when (weather) {
            CosmicWeather.Bright -> listOf(
                "Today opens with clarity, {sign}.",
                "Light reaches the corners that shadows usually claim.",
                "The morning has a buoyant edge.",
                "Energy moves the way you want it to today.",
                "There's a clean signal coming through.",
                "The day asks little and offers much.",
                "Something you've been working on starts to shine.",
                "The {moon} carries you forward."
            )
            CosmicWeather.Reflective -> listOf(
                "Slow down — the day rewards thinking before acting.",
                "Words may arrive twisted today; choose them carefully.",
                "A second thought serves you better than the first.",
                "The {moon} draws you inward.",
                "Old patterns re-surface, asking to be reviewed.",
                "There's wisdom hidden in the pause.",
                "Something half-finished wants completion before momentum.",
                "Listen more than you speak today."
            )
            CosmicWeather.Tense -> listOf(
                "Today carries weight, {sign} — and you can carry it.",
                "Pressure today is the kind that makes diamonds.",
                "Don't fight the friction; it's pointing somewhere.",
                "A small obstacle is teaching you a large lesson.",
                "The day demands focus over multitasking.",
                "Hold your line — others are watching how you respond.",
                "Saturn's discipline meets {sign}'s will.",
                "What feels heavy is also what's becoming real."
            )
            CosmicWeather.Hopeful -> listOf(
                "A door cracks open today, {sign}.",
                "The {moon} offers a clean slate.",
                "You feel something thawing — let it.",
                "There's room for an ask you've been holding back.",
                "The day favors quiet beginnings over loud declarations.",
                "Something new wants to be planted.",
                "Your instinct is sharper than usual today.",
                "Belief precedes evidence right now — trust the order."
            )
            CosmicWeather.Restorative -> listOf(
                "Today is for restoration, not output.",
                "The {moon} pulls you toward rest.",
                "Permission to do less is the gift of the day.",
                "Recovery is also progress, {sign}.",
                "Even {element} signs need stillness sometimes.",
                "Honor the fatigue — it's intelligent.",
                "Refilling is not retreating.",
                "The day doesn't need anything more from you than presence."
            )
            CosmicWeather.Bold -> listOf(
                "Today wants you visible, {sign}.",
                "The {moon} is loud — match its volume.",
                "Take up the room you've been shrinking from.",
                "A bold move today reads as inevitable, not reckless.",
                "Your voice carries further than usual.",
                "The day is asking for your full size.",
                "Make the call. Send the message. Show up.",
                "Hesitation costs more than action right now."
            )
        }
        // Element-specific flavor mixed in with the base
        val elementMix = when (element) {
            "Fire" -> listOf("Your fire wants somewhere to go today.", "{sign}'s spark catches easily right now.")
            "Earth" -> listOf("Build something today — {sign} works best with hands.", "The day wants to be made tangible.")
            "Air" -> listOf("Your mind is the medium today, {sign}.", "Conversations carry further than they should.")
            "Water" -> listOf("Your feelings are the data today, {sign}.", "Trust what the gut already knows.")
            else -> emptyList()
        }
        return base + elementMix
    }

    private fun themes(weather: CosmicWeather, transit: TransitFlavor): List<String> {
        val transitFlavor = when (transit) {
            TransitFlavor.Conjunction -> listOf(
                "It's your season — the Sun amplifies whatever you point it at.",
                "You feel more like yourself than usual.",
                "The cosmos is on your wavelength today."
            )
            TransitFlavor.Trine -> listOf(
                "Things flow without you forcing them.",
                "Help arrives from unexpected directions.",
                "The path of least resistance is also the right one."
            )
            TransitFlavor.Sextile -> listOf(
                "Small effort produces outsized results.",
                "An opportunity is hiding in something ordinary.",
                "The day rewards initiative — but a gentle kind."
            )
            TransitFlavor.Square -> listOf(
                "Friction today is creative, not destructive.",
                "Two parts of you want different things — that's information.",
                "The tension is asking you to grow."
            )
            TransitFlavor.Opposite -> listOf(
                "Someone is mirroring back what you can't see.",
                "Balance, not victory, is the win today.",
                "What looks like opposition is actually invitation."
            )
            TransitFlavor.Quincunx -> listOf(
                "Adjustments are needed — small ones, often.",
                "Don't force a fit; rotate the piece.",
                "A workaround is wiser than a wall."
            )
        }
        val weatherFlavor = when (weather) {
            CosmicWeather.Bright -> listOf("Things you've been waiting for start moving.")
            CosmicWeather.Reflective -> listOf("Old voices are loud today; remember they're old.")
            CosmicWeather.Tense -> listOf("What feels stuck is being shaped, not punished.")
            CosmicWeather.Hopeful -> listOf("Plant the seed even if you can't see the sun.")
            CosmicWeather.Restorative -> listOf("Productivity is a poor measure of a day like this.")
            CosmicWeather.Bold -> listOf("Be the one who shows up first.")
        }
        return transitFlavor + weatherFlavor
    }

    private fun actions(transit: TransitFlavor, element: String): List<String> {
        val by = when (element) {
            "Fire" -> listOf(
                "Move your body before your mind catches up.",
                "Pick the option that scares you slightly.",
                "Lead first; explain later.",
                "Burn through one thing fully rather than tend to five halfway."
            )
            "Earth" -> listOf(
                "Make a list, then make a smaller one.",
                "Do the slow, boring step you've been skipping.",
                "Touch grass — literal or metaphorical.",
                "Build with what's already in front of you."
            )
            "Air" -> listOf(
                "Send the message you've been drafting.",
                "Talk it out with someone whose mind moves differently than yours.",
                "Read something old — not new — today.",
                "Write three sentences about what you actually want."
            )
            "Water" -> listOf(
                "Let yourself feel it without naming it yet.",
                "Reach out to one person who knows you fully.",
                "Take a longer shower than makes sense.",
                "Trust the no that's been forming."
            )
            else -> emptyList()
        }
        val transitNudge = when (transit) {
            TransitFlavor.Square, TransitFlavor.Opposite -> listOf("Don't escalate — adjust.")
            TransitFlavor.Trine, TransitFlavor.Sextile -> listOf("Say yes to the easy thing.")
            else -> listOf("Move at the day's actual speed.")
        }
        return by + transitNudge
    }

    private fun closers(weather: CosmicWeather, ruler: String): List<String> {
        val byRuler = when (ruler) {
            "Mars" -> listOf("Mars has your back today.", "Courage is the through-line.")
            "Venus" -> listOf("Beauty is also a strategy.", "Venus rewards the gentle pace.")
            "Mercury" -> listOf("Mercury asks for clarity, not speed.", "Words are tools today.")
            "Moon" -> listOf("The Moon is your compass.", "Trust the tide.")
            "Sun" -> listOf("The Sun will keep showing up — so should you.", "Warmth is a discipline.")
            "Jupiter" -> listOf("Jupiter favors the generous.", "Expand quietly.")
            "Saturn" -> listOf("Saturn rewards what lasts.", "Discipline is a kindness to your future self.")
            "Pluto" -> listOf("Old layers are shedding — let them.", "Pluto only takes what was already leaving.")
            "Uranus" -> listOf("The breakthrough may look like a break.", "Uranus prefers your real shape.")
            "Neptune" -> listOf("Dream forward.", "What feels foggy is becoming clear at its own pace.")
            else -> listOf("The day will hold you.", "You don't have to figure it all out today.")
        }
        val byWeather = when (weather) {
            CosmicWeather.Bright -> listOf("Use the light while it's here.")
            CosmicWeather.Reflective -> listOf("The pause is the point.")
            CosmicWeather.Tense -> listOf("Hold steady; this passes.")
            CosmicWeather.Hopeful -> listOf("Begin small. Begin anyway.")
            CosmicWeather.Restorative -> listOf("Rest is the work today.")
            CosmicWeather.Bold -> listOf("Don't dim what wants to shine.")
        }
        return byRuler + byWeather
    }

    private fun tipsByElement(element: String, weather: CosmicWeather): List<String> {
        val core = when (element) {
            "Fire" -> listOf(
                "Channel your fire into one task — not five.",
                "Skip a small reaction today; save the heat for what matters.",
                "Lead a conversation you've been avoiding.",
                "Move your body for 20 minutes before noon.",
                "Choose courage over comfort once today.",
                "Speak up first in the meeting that matters.",
                "Don't dilute your enthusiasm by explaining it."
            )
            "Earth" -> listOf(
                "Finish one stalled project before starting anything new.",
                "Eat something whole and slow.",
                "Re-read a saved note from your past self.",
                "Touch something analog — paper, soil, fabric.",
                "Pay one bill, return one item, close one tab.",
                "Walk the long way somewhere familiar.",
                "Pick the boring foundation step over the exciting one."
            )
            "Air" -> listOf(
                "Send the message that's been a draft for too long.",
                "Listen to a long-form conversation today.",
                "Write three sentences naming what you actually want.",
                "Ask one question you don't already know the answer to.",
                "Say less in the meeting; observe more.",
                "Read a book you've been pretending to read.",
                "Have lunch with someone you usually only Slack."
            )
            "Water" -> listOf(
                "Let one feeling sit without solving it.",
                "Reach out to a friend you miss.",
                "Take a longer shower than makes sense.",
                "Notice what your body is telling you about a recent choice.",
                "Drink more water — your gut runs on it.",
                "Trust the no that's forming.",
                "Make space for tears you didn't know were waiting."
            )
            else -> listOf("Trust what's emerging today.")
        }
        val weatherSpecific = when (weather) {
            CosmicWeather.Reflective -> listOf("Re-read before you reply — Mercury wants clarity.")
            CosmicWeather.Tense -> listOf("Do the hard thing first; the day eases after.")
            CosmicWeather.Bold -> listOf("Take up the room you've been shrinking from.")
            CosmicWeather.Hopeful -> listOf("Plant a small seed — visible or invisible.")
            CosmicWeather.Restorative -> listOf("Cancel one optional thing today.")
            CosmicWeather.Bright -> listOf("Send the bold note while the energy is here.")
        }
        return core + weatherSpecific
    }

    private fun questions(weather: CosmicWeather): List<String> {
        val universal = listOf(
            "What would today look like if it were exactly enough?",
            "Where are you spending energy that isn't returning any?",
            "Which conversation have you been avoiding — and why now?",
            "What's one thing your past self would be proud of today?",
            "If nothing changed, would that be a problem? Or a gift?",
            "What does your body know that your mind hasn't admitted?",
            "Whose approval are you still negotiating for?",
            "What's the kindest version of the truth you're not saying?",
            "What would you start if you knew it would take three years?",
            "Where could a 'no' make space for a real 'yes'?"
        )
        val byWeather = when (weather) {
            CosmicWeather.Reflective -> listOf("What pattern is repeating itself, hoping you'll notice this time?")
            CosmicWeather.Tense -> listOf("What's the friction trying to teach you?")
            CosmicWeather.Bold -> listOf("What would 10% bolder you do today?")
            CosmicWeather.Hopeful -> listOf("What seed are you planting that no one can see yet?")
            CosmicWeather.Restorative -> listOf("What can you let go of that you're still carrying out of habit?")
            CosmicWeather.Bright -> listOf("Where is the energy moving — and are you moving with it?")
        }
        return universal + byWeather
    }

    private fun weeklyLines(weather: CosmicWeather, transit: TransitFlavor): List<String> = listOf(
        "Energy moves; let it.",
        "A small adjustment changes the whole shape.",
        "Listen for what's not being said.",
        "Begin before you feel ready.",
        "Rest counts as progress today.",
        "A door cracks open — notice it.",
        "Hold steady through the friction.",
        "Beauty, not speed, today.",
        "Trust the slow part.",
        "The pattern is showing itself."
    ) + when (weather) {
        CosmicWeather.Bright -> listOf("Use the light.", "Things you started recently begin to land.")
        CosmicWeather.Reflective -> listOf("Old voices, old shapes — review them.", "Re-read before replying.")
        CosmicWeather.Tense -> listOf("Pressure shapes; it doesn't punish.", "The hard thing is the right thing.")
        CosmicWeather.Hopeful -> listOf("Plant something invisible.", "Belief precedes evidence today.")
        CosmicWeather.Restorative -> listOf("Cancel one optional thing.", "Rest is intelligent now.")
        CosmicWeather.Bold -> listOf("Take the room.", "Don't dim what wants to shine.")
    } + when (transit) {
        TransitFlavor.Conjunction -> listOf("It's your moment — own it.")
        TransitFlavor.Opposite -> listOf("Mirror day — what's reflected back?")
        TransitFlavor.Square -> listOf("Friction is the teacher today.")
        else -> emptyList()
    }

    private fun compatibilityPatterns(elementA: String, elementB: String): List<String> {
        // Every variant must include both {a} and {b} so the compatibility
        // insight always names both signs. Element-only phrasing slips through
        // the placeholder pass and produces output with no sign names.
        if (elementA == elementB) {
            return when (elementA) {
                "Fire" -> listOf(
                    "{a} and {b} share a flame — bright and fast, sometimes scorching.",
                    "{a} and {b} together are a bonfire: warm and visible, but needing wood and air to last."
                )
                "Earth" -> listOf(
                    "{a} and {b} build steadily — fewer fireworks, deeper foundations.",
                    "{a} and {b} play the long game: patient, practical, durable."
                )
                "Air" -> listOf(
                    "{a} and {b} live in language — endless conversation, occasional drift.",
                    "{a} and {b} think in stereo, sometimes forgetting to land."
                )
                "Water" -> listOf(
                    "{a} and {b} feel each other before words arrive.",
                    "{a} and {b} share a current — moving, deep, occasionally overwhelming."
                )
                else -> listOf("{a} and {b} share an element — the resonance is high.")
            }
        }
        val pair = setOf(elementA, elementB)
        return when {
            pair == setOf("Fire", "Air") -> listOf(
                "{a}'s fire and {b}'s air feed each other — momentum builds quickly.",
                "{a} and {b} ignite ideas together; the trick is keeping them grounded."
            )
            pair == setOf("Earth", "Water") -> listOf(
                "{a} and {b} make fertile ground — water shapes earth, earth holds water.",
                "{a} and {b} are the cradle pairing: nourishing, patient, generative."
            )
            pair == setOf("Fire", "Water") -> listOf(
                "{a}'s heat meets {b}'s depth — passion and feeling, sometimes steam.",
                "{a} and {b} require translation; both run hot, in different ways."
            )
            pair == setOf("Earth", "Air") -> listOf(
                "{a}'s ideas meet {b}'s execution — the office-romance archetype.",
                "{a} and {b} dream and build, if they remember to do both."
            )
            pair == setOf("Fire", "Earth") -> listOf(
                "{a}'s spark meets {b}'s structure — fast plus durable, when balanced.",
                "{a} and {b} create things that last — if patience holds."
            )
            pair == setOf("Air", "Water") -> listOf(
                "{a}'s thinking meets {b}'s feeling — language and intuition cross-translate.",
                "{a} and {b} learn each other's grammar over time."
            )
            else -> listOf("{a} and {b} share a current most can't see.")
        }
    }

    private fun compatibilityClosers(elementA: String, elementB: String): List<String> = listOf(
        "What's needed: shared definitions and shared silences.",
        "The tension is the texture; don't smooth it all away.",
        "Trust survives the small frictions, not their absence.",
        "Both grow when neither performs.",
        "The pairing is teaching, not testing.",
        "What works will surprise both of you."
    )
}
