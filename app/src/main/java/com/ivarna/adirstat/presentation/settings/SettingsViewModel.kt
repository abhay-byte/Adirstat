package com.ivarna.adirstat.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.local.datastore.UserPreferencesRepository
import com.ivarna.adirstat.domain.repository.LastScanSummary
import com.ivarna.adirstat.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val cacheCleared: Boolean = false,
    val scanHistory: List<LastScanSummary> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val storageRepository: StorageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadScanHistory()
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

    fun loadScanHistory() {
        viewModelScope.launch {
            val history = storageRepository.getAllScanSummaries()
            _uiState.update { it.copy(scanHistory = history) }
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
            storageRepository.clearAllScans()
            _uiState.update { it.copy(cacheCleared = true, scanHistory = emptyList()) }
        }
    }

    fun dismissCacheCleared() {
        _uiState.update { it.copy(cacheCleared = false) }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                val summaries = storageRepository.getAllScanSummaries()
                val dateFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val fileFmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                val fileName = "adirstat_export_${fileFmt.format(Date())}.csv"
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val outFile = File(downloadsDir, fileName)

                withContext(Dispatchers.IO) {
                    outFile.bufferedWriter().use { writer ->
                        writer.write("Partition,Total Size (bytes),File Count,Scanned At\n")
                        if (summaries.isEmpty()) {
                            writer.write("No scans found,,, \n")
                        } else {
                            summaries.forEach { s ->
                                val date = dateFmt.format(Date(s.createdAt))
                                writer.write("${s.partitionPath},${s.totalSize},${s.fileCount},$date\n")
                            }
                        }
                    }
                }
                _uiState.update { it.copy(isExporting = false, exportSuccess = true, exportError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportSuccess = false, exportError = e.message ?: "Export failed") }
            }
        }
    }

    fun dismissExportResult() {
        _uiState.update { it.copy(exportSuccess = false, exportError = null) }
    }
}
