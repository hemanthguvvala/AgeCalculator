package com.hkgroups.agecalculator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey // Import longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define keys for our preferences
private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
private val SAVED_BIRTH_DATE = longPreferencesKey("saved_birth_date") // New key

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
}