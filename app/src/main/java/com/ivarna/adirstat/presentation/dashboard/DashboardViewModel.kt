package com.ivarna.adirstat.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.AppStorageInfoBytes
import com.ivarna.adirstat.data.source.MediaStoreDataSource
import com.ivarna.adirstat.data.source.PartitionTotals
import com.ivarna.adirstat.data.source.StorageCategories
import com.ivarna.adirstat.data.source.StorageStatsDataSource
import com.ivarna.adirstat.domain.repository.StorageRepository
import com.ivarna.adirstat.util.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val storageStatsDataSource: StorageStatsDataSource,
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val permissionManager: PermissionManager
) : ViewModel() {

    data class DashboardUiState(
        val isLoading: Boolean = true,
        val partitionTotals: PartitionTotals? = null,
        val storageCategories: StorageCategories? = null,
        val appStats: List<AppStorageInfoBytes> = emptyList(),
        val lastScanTime: Long? = null,
        val neverScanned: Boolean = false,
        val permissionStatus: PermissionManager.PermissionStatus? = null,
        val isRefreshingBreakdown: Boolean = false,
        val error: String? = null,
        // UI compatibility fields
        val permissionState: PermissionState = PermissionState(),
        val storageVolumes: List<StorageVolumeInfo> = emptyList()
    )
    
    data class PermissionState(
        val hasManageExternalStorage: Boolean = false,
        val hasUsageAccess: Boolean = false
    )
    
    data class StorageVolumeInfo(
        val path: String,
        val name: String,
        val totalBytes: Long = 0L,
        val usedBytes: Long = 0L,
        val freeBytes: Long = 0L,
        val lastScanTime: Long? = null,
        val appsBytes: Long = 0L,
        val mediaBytes: Long = 0L,
        val otherBytes: Long = 0L,
        val storageCategories: StorageCategories? = null,
        val neverScanned: Boolean = false
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var dashboardLoadJob: Job? = null

    init {
        loadDashboardData()
    }

    /**
     * Main entry point — call this on init AND every time onResume fires.
     * This is the function that loads both file scan results AND StorageStatsManager data.
     */
    fun loadDashboardData() {
        dashboardLoadJob?.cancel()
        dashboardLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val permissions = permissionManager.checkAllPermissions()

            // Load all data in parallel
            val totalsDeferred = async(Dispatchers.IO) { storageStatsDataSource.getPartitionTotals() }
            val scanSummaryDeferred = async(Dispatchers.IO) { storageRepository.getLastScanSummary() }
            val appResultDeferred = async(Dispatchers.IO) { storageStatsDataSource.getPerAppStorageStats() }
            val mediaTotalsDeferred = async(Dispatchers.IO) {
                try { mediaStoreDataSource.getMediaTotals() }
                catch (e: Exception) { MediaStoreDataSource.MediaTotals(0L, 0, 0L, 0, 0L, 0) }
            }

            val totals = totalsDeferred.await()
            val scanSummary = scanSummaryDeferred.await()
            val appResult = appResultDeferred.await()
            val mediaTotals = mediaTotalsDeferred.await()

            val categories = withContext(Dispatchers.IO) {
                storageStatsDataSource.computeStorageCategories(
                    filesystemBytes = scanSummary?.totalSize ?: 0L,
                    appStats = appResult.apps,
                    mediaTotals = mediaTotals
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    partitionTotals = totals,
                    storageCategories = categories,
                    appStats = appResult.apps,
                    lastScanTime = scanSummary?.createdAt,
                    neverScanned = scanSummary == null,
                    isRefreshingBreakdown = false,
                    permissionStatus = permissions,
                    permissionState = PermissionState(
                        hasManageExternalStorage = permissions.hasAllFilesAccess,
                        hasUsageAccess = permissions.hasUsageStatsAccess
                    ),
                    storageVolumes = listOf(
                        StorageVolumeInfo(
                            path = "/storage/emulated/0",
                            name = "Internal Storage",
                            totalBytes = totals.totalBytes,
                            usedBytes = totals.usedBytes,
                            freeBytes = totals.freeBytes,
                            lastScanTime = scanSummary?.createdAt,
                            appsBytes = categories.appsBytes + categories.appDataBytes,
                            mediaBytes = categories.mediaBytes,
                            otherBytes = categories.filesBytes + categories.systemBytes,
                            storageCategories = categories,
                            neverScanned = scanSummary == null
                        )
                    )
                )
            }
        }
    }

    fun checkPermissions() {
        viewModelScope.launch {
            val permissions = permissionManager.checkAllPermissions()
            _uiState.update { it.copy(
                permissionState = PermissionState(
                    hasManageExternalStorage = permissions.hasAllFilesAccess,
                    hasUsageAccess = permissions.hasUsageStatsAccess
                )
            )}
            // Reload data after permission check
            loadDashboardData()
        }
    }
}
