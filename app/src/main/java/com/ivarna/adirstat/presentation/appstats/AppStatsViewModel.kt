package com.ivarna.adirstat.presentation.appstats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.AppStorageInfoBytes
import com.ivarna.adirstat.data.source.StorageStatsDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppStatsUiState(
    val isLoading: Boolean = true,
    val apps: List<AppStorageInfoBytes> = emptyList(),
    val totalAppSize: Long = 0,
    val totalDataSize: Long = 0,
    val totalCacheSize: Long = 0,
    val error: String? = null
)

@HiltViewModel
class AppStatsViewModel @Inject constructor(
    private val storageStatsDataSource: StorageStatsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStatsUiState())
    val uiState: StateFlow<AppStatsUiState> = _uiState.asStateFlow()

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    storageStatsDataSource.getPerAppStorageStats()
                }
                
                val apps = result.apps
                val totalApp = apps.sumOf { it.apkBytes }
                val totalData = apps.sumOf { it.dataBytes }
                val totalCache = apps.sumOf { it.cacheBytes }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        apps = apps,
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
