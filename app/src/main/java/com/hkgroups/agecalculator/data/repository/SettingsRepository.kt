package com.hkgroups.agecalculator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// Define keys for our preferences
private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
private val SAVED_BIRTH_DATE = longPreferencesKey("saved_birth_date")
private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
// Saved partners: encoded as "Name|Sign;Name|Sign;..." in a single string pref
// to avoid pulling in Room for what's effectively a small ordered set.
private val SAVED_PARTNERS = stringPreferencesKey("saved_partners")

// Streak tracking
private val STREAK_CURRENT = intPreferencesKey("streak_current")
private val STREAK_LONGEST = intPreferencesKey("streak_longest")
private val STREAK_LAST_CHECK_IN = stringPreferencesKey("streak_last_check_in")
// Streak freezes — Duolingo-style "missed-day forgiveness". User earns one
// freeze per 7-day streak; consumed automatically when a day is missed.
private val STREAK_FREEZES = intPreferencesKey("streak_freezes")
// Question-of-day journaling: user's answer for today's reflection prompt.
private val QOD_LAST_ANSWERED = stringPreferencesKey("qod_last_answered")
private val QOD_LAST_ANSWER = stringPreferencesKey("qod_last_answer")

// Micro-interaction preferences
private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
private val CHIMES_ENABLED = booleanPreferencesKey("chimes_enabled")
private val COSMIC_EVENT_NOTIFICATIONS = booleanPreferencesKey("cosmic_event_notifications")

// Rising + Moon sign overrides (manual entry — no birth-time math needed)
private val RISING_SIGN = stringPreferencesKey("rising_sign")
private val MOON_SIGN = stringPreferencesKey("moon_sign")

// Daily mood journal — encoded "isoDate|mood|note;..." capped at the most
// recent 30 entries to keep the prefs file small.
private val MOOD_ENTRIES = stringPreferencesKey("mood_entries")

// Ad-free flag — flipped on by future premium tier. Default off (free tier sees ads).
private val ADS_DISABLED = booleanPreferencesKey("ads_disabled")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    // Flow to read the theme setting. Default is dark — the entire UI (cosmic palette,
    // starry background, white text) is built for it. Falling back to light produced
    // unreadable black-on-black surfaces on first launch before the user picked.
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: true
        }

    // Function to save the theme setting
    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_DARK_MODE] = isDarkMode
        }
    }

    /** User's saved birth date, in millis since epoch. Null until onboarding completes. */
    val savedBirthDate: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[SAVED_BIRTH_DATE]
        }

    suspend fun saveBirthDate(dateInMillis: Long) {
        context.dataStore.edit { settings ->
            settings[SAVED_BIRTH_DATE] = dateInMillis
        }
    }

    /** Clears the saved birth date — used by Settings → Reset. */
    suspend fun clearBirthDate() {
        context.dataStore.edit { settings ->
            settings.remove(SAVED_BIRTH_DATE)
        }
    }

    // ---------- Daily horoscope notification opt-in ----------

    /** Whether the user wants the daily 8am horoscope notification. Defaults on. */
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    // ---------- Onboarding ----------

    /** Whether the user has dismissed the first-run tooltip overlay. */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }

    suspend fun markOnboardingComplete() {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }

    // ---------- Saved partners ----------

    /** Saved partner list — pairs of (name, sign). */
    val savedPartners: Flow<List<SavedPartner>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[SAVED_PARTNERS] ?: return@map emptyList()
            raw.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank())
                    SavedPartner(name = parts[0], sign = parts[1])
                else null
            }
        }

    suspend fun addPartner(name: String, sign: String) {
        context.dataStore.edit { prefs ->
            val existing = prefs[SAVED_PARTNERS] ?: ""
            val sanitized = name.replace("|", "").replace(";", "").trim()
            if (sanitized.isBlank()) return@edit
            val newEntry = "$sanitized|$sign"
            prefs[SAVED_PARTNERS] = if (existing.isBlank()) newEntry
                                    else "$existing;$newEntry"
        }
    }

    suspend fun removePartner(name: String, sign: String) {
        context.dataStore.edit { prefs ->
            val existing = prefs[SAVED_PARTNERS] ?: return@edit
            val target = "$name|$sign"
            val filtered = existing.split(";").filter { it != target }.joinToString(";")
            prefs[SAVED_PARTNERS] = filtered
        }
    }

    // ---------- Streak tracking ----------

    /** Current consecutive-day streak (resets if a day is missed). */
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[STREAK_CURRENT] ?: 0 }

    /** Longest streak ever achieved. */
    val longestStreak: Flow<Int> = context.dataStore.data.map { it[STREAK_LONGEST] ?: 0 }

    /** Available streak freezes — used automatically on a missed day before
     *  the streak resets. Earned at every 7-day streak milestone. */
    val streakFreezes: Flow<Int> = context.dataStore.data.map { it[STREAK_FREEZES] ?: 0 }

    // ---------- Micro-interactions (haptics / chimes) ----------

    /** Whether haptic feedback fires on taps and milestones. Defaults on. */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[HAPTICS_ENABLED] ?: true }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    /** Whether subtle chimes play on milestones. Off by default — sound is intrusive. */
    val chimesEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[CHIMES_ENABLED] ?: false }

    suspend fun setChimesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[CHIMES_ENABLED] = enabled }
    }

    /** Whether cosmic event reminders (eclipses, retrogrades, equinoxes) fire. Defaults on. */
    val cosmicEventNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[COSMIC_EVENT_NOTIFICATIONS] ?: true }

    suspend fun setCosmicEventNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[COSMIC_EVENT_NOTIFICATIONS] = enabled }
    }

    // ---------- Ads ----------

    /** Whether the user is ad-free (premium tier). Default false. */
    val adsDisabled: Flow<Boolean> = context.dataStore.data
        .map { it[ADS_DISABLED] ?: false }

    suspend fun setAdsDisabled(disabled: Boolean) {
        context.dataStore.edit { it[ADS_DISABLED] = disabled }
    }

    // ---------- Rising + Moon sign ----------

    val risingSign: Flow<String?> = context.dataStore.data
        .map { it[RISING_SIGN]?.takeIf { v -> v.isNotBlank() } }

    val moonSign: Flow<String?> = context.dataStore.data
        .map { it[MOON_SIGN]?.takeIf { v -> v.isNotBlank() } }

    suspend fun setRisingSign(sign: String?) {
        context.dataStore.edit { prefs ->
            if (sign.isNullOrBlank()) prefs.remove(RISING_SIGN)
            else prefs[RISING_SIGN] = sign
        }
    }

    suspend fun setMoonSign(sign: String?) {
        context.dataStore.edit { prefs ->
            if (sign.isNullOrBlank()) prefs.remove(MOON_SIGN)
            else prefs[MOON_SIGN] = sign
        }
    }

    // ---------- Daily mood journal ----------

    val moodEntries: Flow<List<MoodEntry>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[MOOD_ENTRIES] ?: return@map emptyList()
            raw.split(";").mapNotNull { MoodEntry.decode(it) }
        }

    /** Adds or updates today's mood (one entry per day). Trims to 30 most recent. */
    suspend fun saveMoodEntry(date: LocalDate, mood: String, note: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[MOOD_ENTRIES] ?: ""
            val existing = raw.split(";").mapNotNull { MoodEntry.decode(it) }
            val withoutToday = existing.filter { it.date != date }
            val updated = (withoutToday + MoodEntry(date, mood, note))
                .sortedByDescending { it.date }
                .take(30)
            prefs[MOOD_ENTRIES] = updated.joinToString(";") { it.encode() }
        }
    }

    /**
     * Record a check-in for today. Idempotent within a calendar day.
     *
     * Streak rules:
     * - First-ever check-in: streak = 1
     * - Same day: no-op
     * - Consecutive day: streak + 1; +1 freeze every 7 (max 3 stored)
     * - Missed exactly one day with freezes available: consume a freeze, keep streak
     * - Otherwise: streak resets to 1
     * - Clock-skew protection: if `lastIso` is in the future relative to `today`
     *   (DST, timezone shift, manual clock change), preserve the streak.
     */
    suspend fun recordCheckIn(today: LocalDate = LocalDate.now()) {
        context.dataStore.edit { prefs ->
            val lastIso = prefs[STREAK_LAST_CHECK_IN]
            val current = prefs[STREAK_CURRENT] ?: 0
            val longest = prefs[STREAK_LONGEST] ?: 0
            val freezes = prefs[STREAK_FREEZES] ?: 0
            val todayIso = today.toString()

            val (newCurrent, newFreezes) = when {
                lastIso == null -> 1 to freezes
                lastIso == todayIso -> current.coerceAtLeast(1) to freezes
                else -> {
                    val lastDate = runCatching { LocalDate.parse(lastIso) }.getOrNull()
                    val daysGap = if (lastDate != null) {
                        java.time.temporal.ChronoUnit.DAYS.between(lastDate, today)
                    } else Long.MAX_VALUE

                    when {
                        // Clock skew (lastIso is in the future) — preserve streak silently
                        daysGap < 0 -> current.coerceAtLeast(1) to freezes
                        daysGap == 1L -> {
                            val newStreak = current + 1
                            // Earn a freeze every 7-day milestone (max 3 stored)
                            val earned = if (newStreak % 7 == 0) (freezes + 1).coerceAtMost(3) else freezes
                            newStreak to earned
                        }
                        // Missed exactly one day, freeze available — burn it, keep streak
                        daysGap == 2L && freezes > 0 -> current to (freezes - 1)
                        else -> 1 to freezes
                    }
                }
            }

            prefs[STREAK_CURRENT] = newCurrent
            prefs[STREAK_FREEZES] = newFreezes
            prefs[STREAK_LAST_CHECK_IN] = todayIso
            if (newCurrent > longest) prefs[STREAK_LONGEST] = newCurrent
        }
    }

    // ---------- Question of the day ----------

    /** ISO date of the day the user last answered the QOD prompt. */
    val questionAnsweredDate: Flow<LocalDate?> = context.dataStore.data.map { prefs ->
        prefs[QOD_LAST_ANSWERED]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }

    /** Last QOD answer text (for showing in-line in journal views). */
    val lastQuestionAnswer: Flow<String?> = context.dataStore.data.map {
        it[QOD_LAST_ANSWER]?.takeIf { v -> v.isNotBlank() }
    }

    suspend fun saveQuestionAnswer(date: LocalDate, answer: String) {
        context.dataStore.edit { prefs ->
            prefs[QOD_LAST_ANSWERED] = date.toString()
            prefs[QOD_LAST_ANSWER] = answer.take(500)
        }
    }
}

/** Saved partner record — surfaces frequently-checked friends/family in the
 * compatibility list without re-typing each time. */
data class SavedPartner(val name: String, val sign: String)

/** Single day's mood log. Note is optional and pipe/semicolon-stripped on encode. */
data class MoodEntry(val date: LocalDate, val mood: String, val note: String) {
    fun encode(): String = "${date}|$mood|${note.replace("|", " ").replace(";", ",")}"

    companion object {
        fun decode(raw: String): MoodEntry? {
            val parts = raw.split("|")
            if (parts.size < 2) return null
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return null
            val mood = parts[1].takeIf { it.isNotBlank() } ?: return null
            val note = if (parts.size >= 3) parts[2] else ""
            return MoodEntry(date, mood, note)
        }
    }
}