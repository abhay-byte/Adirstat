package com.ivarna.adirstat.domain.repository

import com.ivarna.adirstat.data.source.AppStorageInfo
import com.ivarna.adirstat.data.source.ScanProgress
import com.ivarna.adirstat.data.source.StorageVolume
import com.ivarna.adirstat.domain.model.FileNode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for storage scanning operations
 */
interface StorageRepository {
    
    /**
     * Get all storage volumes
     */
    suspend fun getStorageVolumes(): List<StorageVolume>
    
    /**
     * Scan a storage volume
     */
    fun scanVolume(
        volumePath: String,
        excludedPaths: List<String> = emptyList(),
        minFileSize: Long = 0
    ): Flow<ScanProgress>
    
    /**
     * Get cached scan result for a volume
     */
    suspend fun getCachedScan(volumePath: String): FileNode.Directory?
    
    /**
     * Save scan result to cache
     */
    suspend fun saveScanResult(scanResult: FileNode.Directory, volumePath: String)
    
    /**
     * Get all apps with storage stats
     */
    suspend fun getAppsWithStorageStats(): List<AppStorageInfo>
    
    /**
     * Get top N apps by storage usage
     */
    suspend fun getTopApps(count: Int): List<AppStorageInfo>
    
    /**
     * Delete a file
     */
    suspend fun deleteFile(filePath: String): Boolean
    
    /**
     * Get file info
     */
    suspend fun getFileInfo(filePath: String): FileNode.File?
}
