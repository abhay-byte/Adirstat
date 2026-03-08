package com.ivarna.adirstat.domain.usecase

import com.ivarna.adirstat.data.source.ScanProgress
import com.ivarna.adirstat.data.source.StorageCategories
import com.ivarna.adirstat.data.source.StorageVolume
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for scanning storage volumes
 */
class ScanStorageUseCase @Inject constructor(
    private val repository: StorageRepository
) {
    /**
     * Get all storage volumes
     */
    suspend fun getStorageVolumes(): Result<List<StorageVolume>> {
        return try {
            val volumes = repository.getStorageVolumes()
            Result.success(volumes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Scan a storage volume and return progress updates
     */
    fun scanVolume(
        volumePath: String,
        excludedPaths: List<String> = emptyList(),
        minFileSize: Long = 0
    ): Flow<Result<ScanState>> {
        return repository.scanVolume(volumePath, excludedPaths, minFileSize)
            .map { progress ->
                when (progress) {
                    is ScanProgress.Counting -> {
                        Result.success(ScanState.Counting(progress.path))
                    }
                    is ScanProgress.CountingComplete -> {
                        Result.success(ScanState.CountingComplete(progress.totalFiles))
                    }
                    is ScanProgress.Scanning -> {
                        Result.success(
                            ScanState.Scanning(
                                currentPath = progress.currentPath,
                                filesScanned = progress.filesScanned,
                                totalFiles = progress.totalFiles,
                                totalSize = progress.totalSize,
                                progressPercent = progress.progressPercent
                            )
                        )
                    }
                    is ScanProgress.Complete -> {
                        val rootNode = progress.rootNode
                        if (rootNode is FileNode.Directory) {
                            Result.success(
                                ScanState.Complete(
                                    rootNode = rootNode,
                                    totalFiles = progress.totalFiles,
                                    totalSize = progress.totalSize,
                                    durationMs = progress.durationMs
                                )
                            )
                        } else {
                            Result.failure(Exception("Invalid scan result"))
                        }
                    }
                    is ScanProgress.Error -> {
                        Result.failure(Exception(progress.message))
                    }
                    is ScanProgress.Cancelled -> {
                        Result.success(ScanState.Cancelled)
                    }
                }
            }
            .catch { e ->
                emit(Result.failure(e))
            }
    }

    /**
     * Get cached scan result if available
     */
    suspend fun getCachedScan(volumePath: String): Result<FileNode.Directory?> {
        return try {
            Result.success(repository.getCachedScan(volumePath))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save scan result to cache
     */
    suspend fun saveScanResult(scanResult: FileNode.Directory, volumePath: String): Result<Unit> {
        return try {
            repository.saveScanResult(scanResult, volumePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLastScanResult(): Result<FileNode.Directory?> {
        return try {
            Result.success(repository.getLastScanResult())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Sealed class representing scan states
 */
sealed class ScanState {
    data class Counting(val path: String) : ScanState()
    data class CountingComplete(val totalFiles: Long) : ScanState()
    data class Scanning(
        val currentPath: String,
        val filesScanned: Long,
        val totalFiles: Long,
        val totalSize: Long,
        val progressPercent: Int
    ) : ScanState()
    data class Complete(
        val rootNode: FileNode.Directory,
        val totalFiles: Long,
        val totalSize: Long,
        val durationMs: Long
    ) : ScanState()
    data object Cancelled : ScanState()
}
