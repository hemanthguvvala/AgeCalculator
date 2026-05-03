package com.hkgroups.agecalculator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey // Import longPreferencesKey
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
private val SAVED_BIRTH_DATE = longPreferencesKey("saved_birth_date") // New key
private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
// Saved partners: encoded as "Name|Sign;Name|Sign;..." in a single string pref
// to avoid pulling in Room for what's effectively a small ordered set.
private val SAVED_PARTNERS = stringPreferencesKey("saved_partners")

// Streak tracking
private val STREAK_CURRENT = intPreferencesKey("streak_current")
private val STREAK_LONGEST = intPreferencesKey("streak_longest")
private val STREAK_LAST_CHECK_IN = stringPreferencesKey("streak_last_check_in")

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

    // --- NEW: Flow to read the saved birth date ---
    val savedBirthDate: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[SAVED_BIRTH_DATE]
        }

    // --- NEW: Function to save the birth date ---
    suspend fun saveBirthDate(dateInMillis: Long) {
        context.dataStore.edit { settings ->
            settings[SAVED_BIRTH_DATE] = dateInMillis
        }
    }

    // --- NEW: Function to clear the birth date (reset data) ---
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
     * Record a check-in for today.
     * - First ever check-in -> streak = 1
     * - Same day -> no-op (streak unchanged)
     * - Consecutive day -> streak + 1
     * - Missed day -> streak resets to 1
     * Always updates longest streak when current exceeds it.
     */
    suspend fun recordCheckIn(today: LocalDate = LocalDate.now()) {
        context.dataStore.edit { prefs ->
            val lastIso = prefs[STREAK_LAST_CHECK_IN]
            val current = prefs[STREAK_CURRENT] ?: 0
            val longest = prefs[STREAK_LONGEST] ?: 0
            val todayIso = today.toString()

            val newCurrent = when {
                lastIso == null -> 1
                lastIso == todayIso -> current.coerceAtLeast(1)
                lastIso == today.minusDays(1).toString() -> current + 1
                else -> 1
            }

            prefs[STREAK_CURRENT] = newCurrent
            prefs[STREAK_LAST_CHECK_IN] = todayIso
            if (newCurrent > longest) prefs[STREAK_LONGEST] = newCurrent
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