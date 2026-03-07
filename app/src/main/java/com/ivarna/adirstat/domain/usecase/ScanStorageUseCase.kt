package com.ivarna.adirstat.domain.usecase

import com.ivarna.adirstat.data.source.ScanProgress
import com.ivarna.adirstat.data.source.StorageBreakdown
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
        minFileSize: Long = 0,
        addVirtualNodes: Boolean = true
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
                            // Add virtual Android/data and Android/obb nodes if requested
                            val enhancedRoot = if (addVirtualNodes) {
                                addVirtualStorageNodes(rootNode, volumePath)
                            } else {
                                rootNode
                            }
                            Result.success(
                                ScanState.Complete(
                                    rootNode = enhancedRoot,
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
     * Add virtual Android/data and Android/obb nodes to the file tree.
     * These directories are restricted on Android 11+ and cannot be scanned directly.
     * We use StorageStatsManager to estimate their sizes.
     */
    private suspend fun addVirtualStorageNodes(
        rootNode: FileNode.Directory,
        volumePath: String
    ): FileNode.Directory {
        // Get storage breakdown to estimate Android/data and obb sizes
        val breakdown = try {
            repository.getStorageBreakdown(volumePath)
        } catch (e: Exception) {
            null
        }
        
        // Estimate Android/data size (usually includes app data and cache)
        // We'll use a portion of the "apps" bytes as an estimate
        val estimatedAppsBytes = breakdown?.appsBytes ?: 0L
        
        // Create virtual Android/data directory
        val androidDataNode = FileNode.Directory(
            name = "Android",
            path = "$volumePath/Android",
            children = listOf(
                // Android/data subdirectory
                FileNode.Directory(
                    name = "data",
                    path = "$volumePath/Android/data",
                    children = emptyList(),
                    size = estimatedAppsBytes / 2, // Estimate half for data
                    lastModified = System.currentTimeMillis(),
                    isRestricted = true
                ),
                // Android/obb subdirectory
                FileNode.Directory(
                    name = "obb",
                    path = "$volumePath/Android/obb",
                    children = emptyList(),
                    size = estimatedAppsBytes / 2, // Estimate half for obb
                    lastModified = System.currentTimeMillis(),
                    isRestricted = true
                )
            ),
            size = estimatedAppsBytes,
            lastModified = System.currentTimeMillis(),
            isRestricted = true
        )
        
        // Add virtual node to the root's children
        val updatedChildren = rootNode.children + androidDataNode
        
        // Return updated root with increased total size
        return rootNode.copy(
            children = updatedChildren,
            size = rootNode.size + estimatedAppsBytes
        )
    }

    /**
     * Get cached scan result if available
     */
    suspend fun getCachedScan(volumePath: String, addVirtualNodes: Boolean = true): Result<FileNode.Directory?> {
        return try {
            val cached = repository.getCachedScan(volumePath)
            val enhanced = if (addVirtualNodes && cached != null) {
                addVirtualStorageNodes(cached, volumePath)
            } else {
                cached
            }
            Result.success(enhanced)
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
