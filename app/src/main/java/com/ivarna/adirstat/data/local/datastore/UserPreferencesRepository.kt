package com.ivarna.adirstat.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "adirstat_preferences")

data class UserPreferences(
    val theme: String = "system",
    val minFileSizeMb: Int = 0,
    val excludedPaths: List<String> = emptyList(),
    val showHiddenFiles: Boolean = false,
    val scanOnStartup: Boolean = false
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val MIN_FILE_SIZE_MB = intPreferencesKey("min_file_size_mb")
        val EXCLUDED_PATHS = stringSetPreferencesKey("excluded_paths")
        val SHOW_HIDDEN_FILES = booleanPreferencesKey("show_hidden_files")
        val SCAN_ON_STARTUP = booleanPreferencesKey("scan_on_startup")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            theme = preferences[PreferencesKeys.THEME] ?: "system",
            minFileSizeMb = preferences[PreferencesKeys.MIN_FILE_SIZE_MB] ?: 0,
            excludedPaths = preferences[PreferencesKeys.EXCLUDED_PATHS]?.toList() ?: emptyList(),
            showHiddenFiles = preferences[PreferencesKeys.SHOW_HIDDEN_FILES] ?: false,
            scanOnStartup = preferences[PreferencesKeys.SCAN_ON_STARTUP] ?: false
        )
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun setMinFileSizeMb(sizeMb: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_FILE_SIZE_MB] = sizeMb
        }
    }

    suspend fun setShowHiddenFiles(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_HIDDEN_FILES] = show
        }
    }

    suspend fun setScanOnStartup(scan: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCAN_ON_STARTUP] = scan
        }
    }

    suspend fun addExcludedPath(path: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_PATHS] ?: emptySet()
            preferences[PreferencesKeys.EXCLUDED_PATHS] = current + path
        }
    }

    suspend fun removeExcludedPath(path: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_PATHS] ?: emptySet()
            preferences[PreferencesKeys.EXCLUDED_PATHS] = current - path
        }
    }

    suspend fun clearExcludedPaths() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXCLUDED_PATHS] = emptySet()
        }
    }
}
