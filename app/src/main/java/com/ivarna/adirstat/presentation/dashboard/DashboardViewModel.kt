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
        val isScanning: Boolean = false,
        val scanProgress: Float = 0f,
        val error: String? = null,
        // UI compatibility fields
        val permissionState: PermissionState = PermissionState(),
        val storageVolumes: List<StorageVolumeInfo> = emptyList(),
        val scannedVolumePath: String? = null
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

    init {
        loadDashboardData()
    }

    /**
     * Main entry point — call this on init AND every time onResume fires.
     * This is the function that loads both file scan results AND StorageStatsManager data.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val permissions = permissionManager.checkAllPermissions()
            _uiState.update { it.copy(permissionStatus = permissions) }

            // Step 1: Load partition totals — NO permission needed
            val totals = withContext(Dispatchers.IO) {
                storageStatsDataSource.getPartitionTotals()
            }
            _uiState.update { it.copy(partitionTotals = totals) }

            // Step 2: Load per-app stats — needs PACKAGE_USAGE_STATS
            val appResult = withContext(Dispatchers.IO) {
                storageStatsDataSource.getPerAppStorageStats()
            }

            val mediaTotals = withContext(Dispatchers.IO) {
                try {
                    mediaStoreDataSource.getMediaTotals()
                } catch (e: Exception) {
                    MediaStoreDataSource.MediaTotals(0L, 0, 0L, 0, 0L, 0)
                }
            }

            // Step 3: Load last scan result from Room cache
            val cachedScan = withContext(Dispatchers.IO) {
                storageRepository.getLastScanResult()
            }
            val filesystemBytes = cachedScan?.size ?: 0L
            val lastScanTime = storageRepository.getLastScanTimestamp()

            // Step 4: Compute combined categories
            val categories = withContext(Dispatchers.IO) {
                storageStatsDataSource.computeStorageCategories(
                    filesystemBytes = filesystemBytes,
                    appStats = appResult.apps,
                    mediaTotals = mediaTotals
                )
            }

            _uiState.update { it.copy(
                isLoading = false,
                storageCategories = categories,
                appStats = appResult.apps,
                lastScanTime = lastScanTime,
                neverScanned = cachedScan == null,
                // UI compatibility
                permissionState = PermissionState(
                    hasManageExternalStorage = permissions.hasAllFilesAccess,
                    hasUsageAccess = permissions.hasUsageStatsAccess
                ),
                // Create a single storage volume from the partition totals
                storageVolumes = listOf(
                    StorageVolumeInfo(
                        path = "/storage/emulated/0",
                        name = "Internal Storage",
                        totalBytes = totals.totalBytes,
                        usedBytes = totals.usedBytes,
                        freeBytes = totals.freeBytes,
                        lastScanTime = lastScanTime.takeIf { it > 0 },
                        appsBytes = categories.appsBytes + categories.appDataBytes,
                        mediaBytes = categories.mediaBytes,
                        otherBytes = categories.filesBytes + categories.systemBytes,
                        storageCategories = categories,
                        neverScanned = cachedScan == null
                    )
                )
            )}
        }
    }

    fun startScan(path: String = "/storage/emulated/0") {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanProgress = 0f) }
            try {
                storageRepository.scanVolume(
                    volumePath = path,
                    excludedPaths = emptyList(),
                    minFileSize = 0
                ).collect { progress ->
                    val percent = when (progress) {
                        is com.ivarna.adirstat.data.source.ScanProgress.Scanning -> progress.progressPercent / 100f
                        is com.ivarna.adirstat.data.source.ScanProgress.Complete -> {
                            // Save scan result to cache - only if rootNode is a Directory
                            val directoryNode = progress.rootNode as? com.ivarna.adirstat.domain.model.FileNode.Directory
                            if (directoryNode != null) {
                                storageRepository.saveScanResult(directoryNode, path)
                            }
                            1f
                        }
                        else -> 0f
                    }
                    _uiState.update { it.copy(scanProgress = percent) }
                }
                // After scan completes, reload dashboard data to show updated numbers
                loadDashboardData()
                // Set the scanned path to trigger navigation
                _uiState.update { it.copy(scannedVolumePath = path) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Scan failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isScanning = false, scanProgress = 0f) }
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
    
    fun onNavigatedToTreemap() {
        _uiState.update { it.copy(scannedVolumePath = null) }
    }
}
