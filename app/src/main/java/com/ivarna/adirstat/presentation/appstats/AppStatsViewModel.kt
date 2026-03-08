package com.ivarna.adirstat.presentation.appstats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.AppStatsDataSource
import com.ivarna.adirstat.data.source.InstalledAppStorageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppStatsUiState(
    val isLoading: Boolean = true,
    val apps: List<InstalledAppStorageInfo> = emptyList(),
    val totalAppSize: Long = 0,
    val totalDataSize: Long = 0,
    val totalCacheSize: Long = 0,
    val error: String? = null
)

@HiltViewModel
class AppStatsViewModel @Inject constructor(
    private val appStatsDataSource: AppStatsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStatsUiState())
    val uiState: StateFlow<AppStatsUiState> = _uiState.asStateFlow()

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val apps = appStatsDataSource.getAllAppsWithStorageStats()
                val totalApp = apps.sumOf { it.apkSize }
                val totalData = apps.sumOf { it.dataSize }
                val totalCache = apps.sumOf { it.cacheSize }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        apps = apps.sortedByDescending { app -> app.totalSize },
                        totalAppSize = totalApp,
                        totalDataSize = totalData,
                        totalCacheSize = totalCache
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
