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

// Streak tracking
private val STREAK_CURRENT = intPreferencesKey("streak_current")
private val STREAK_LONGEST = intPreferencesKey("streak_longest")
private val STREAK_LAST_CHECK_IN = stringPreferencesKey("streak_last_check_in")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    // Flow to read the theme setting
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false
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

    // ---------- Streak tracking ----------

    /** Current consecutive-day streak (resets if a day is missed). */
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[STREAK_CURRENT] ?: 0 }

    /** Longest streak ever achieved. */
    val longestStreak: Flow<Int> = context.dataStore.data.map { it[STREAK_LONGEST] ?: 0 }

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