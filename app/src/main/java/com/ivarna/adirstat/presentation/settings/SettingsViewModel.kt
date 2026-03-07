package com.ivarna.adirstat.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.local.datastore.UserPreferences
import com.ivarna.adirstat.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeOption(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    DYNAMIC("Dynamic (Material You)")
}

enum class MinimumFileSize(val displayName: String) {
    ALL("Show All Files"),
    KB_1("1 KB+"),
    KB_10("10 KB+"),
    MB_1("1 MB+"),
    MB_10("10 MB+"),
    MB_100("100 MB+")
}

data class SettingsUiState(
    val theme: ThemeOption = ThemeOption.SYSTEM,
    val minimumFileSize: MinimumFileSize = MinimumFileSize.ALL,
    val excludedPaths: List<String> = emptyList(),
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        theme = when (prefs.theme) {
                            "light" -> ThemeOption.LIGHT
                            "dark" -> ThemeOption.DARK
                            "dynamic" -> ThemeOption.DYNAMIC
                            else -> ThemeOption.SYSTEM
                        },
                        minimumFileSize = when (prefs.minFileSizeMb) {
                            0 -> MinimumFileSize.ALL
                            1 -> MinimumFileSize.MB_1
                            10 -> MinimumFileSize.MB_10
                            100 -> MinimumFileSize.MB_100
                            else -> MinimumFileSize.ALL
                        },
                        excludedPaths = prefs.excludedPaths
                    )
                }
            }
        }
    }

    fun setTheme(theme: ThemeOption) {
        viewModelScope.launch {
            preferencesRepository.setTheme(
                when (theme) {
                    ThemeOption.LIGHT -> "light"
                    ThemeOption.DARK -> "dark"
                    ThemeOption.DYNAMIC -> "dynamic"
                    ThemeOption.SYSTEM -> "system"
                }
            )
        }
    }

    fun setMinimumFileSize(size: MinimumFileSize) {
        viewModelScope.launch {
            val mb = when (size) {
                MinimumFileSize.ALL -> 0
                MinimumFileSize.KB_1 -> 0
                MinimumFileSize.KB_10 -> 0
                MinimumFileSize.MB_1 -> 1
                MinimumFileSize.MB_10 -> 10
                MinimumFileSize.MB_100 -> 100
            }
            preferencesRepository.setMinFileSizeMb(mb)
        }
    }

    fun addExclusion(path: String) {
        viewModelScope.launch {
            preferencesRepository.addExcludedPath(path)
        }
    }

    fun removeExclusion(path: String) {
        viewModelScope.launch {
            preferencesRepository.removeExcludedPath(path)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // Clear scan cache logic here
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            // Export logic here
            _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
        }
    }
}
