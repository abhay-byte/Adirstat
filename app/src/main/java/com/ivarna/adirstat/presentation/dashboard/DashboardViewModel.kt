package com.ivarna.adirstat.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.StorageVolume
import com.ivarna.adirstat.domain.usecase.ScanStorageUseCase
import com.ivarna.adirstat.util.PermissionManager
import com.ivarna.adirstat.util.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val permissionState: PermissionState = PermissionState(),
    val storageVolumes: List<StorageVolumeUi> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: String = "",
    val error: String? = null
)

data class StorageVolumeUi(
    val id: String,
    val name: String,
    val path: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val lastScanTime: Long? = null,
    val fileCount: Int = 0,
    val folderCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val state = permissionManager.checkAllPermissions()
        _uiState.update { it.copy(permissionState = state) }
        
        if (state.hasManageExternalStorage) {
            loadStorageVolumes()
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun requestAllFilesAccess() {
        // This should be called from Activity context - simplified for now
        // In production, this would use a launcher from the composable
        checkPermissions()
    }

    private fun loadStorageVolumes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = scanStorageUseCase.getStorageVolumes()
                result.fold(
                    onSuccess = { volumes ->
                        val volumeUis = volumes.map { volume ->
                            StorageVolumeUi(
                                id = volume.path,
                                name = volume.displayName,
                                path = volume.path,
                                totalBytes = volume.totalBytes,
                                usedBytes = volume.usedBytes,
                                freeBytes = volume.freeBytes
                            )
                        }
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                storageVolumes = volumeUis
                            ) 
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = e.message
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message
                    ) 
                }
            }
        }
    }

    fun startScan(volumePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanProgress = "Starting scan...") }
            try {
                scanStorageUseCase.scanVolume(volumePath).collect { result ->
                    result.fold(
                        onSuccess = { state ->
                            when (state) {
                                is com.ivarna.adirstat.domain.usecase.ScanState.Scanning -> {
                                    _uiState.update { 
                                        it.copy(scanProgress = state.currentPath) 
                                    }
                                }
                                is com.ivarna.adirstat.domain.usecase.ScanState.Complete -> {
                                    _uiState.update { 
                                        it.copy(
                                            isScanning = false, 
                                            scanProgress = "",
                                            storageVolumes = _uiState.value.storageVolumes.map { vol ->
                                                if (vol.path == volumePath) {
                                                    vol.copy(
                                                        lastScanTime = System.currentTimeMillis(),
                                                        fileCount = state.totalFiles.toInt(),
                                                        folderCount = 0
                                                    )
                                                } else vol
                                            }
                                        ) 
                                    }
                                }
                                else -> {}
                            }
                        },
                        onFailure = { e ->
                            _uiState.update { 
                                it.copy(
                                    isScanning = false, 
                                    error = e.message
                                ) 
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isScanning = false, 
                        error = e.message
                    ) 
                }
            }
        }
    }
}
