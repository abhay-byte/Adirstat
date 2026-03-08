package com.ivarna.adirstat.domain.repository

import com.ivarna.adirstat.data.source.AppStorageInfoBytes
import com.ivarna.adirstat.data.source.ScanProgress
import com.ivarna.adirstat.data.source.StorageCategories
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
    suspend fun getAppsWithStorageStats(): List<AppStorageInfoBytes>
    
    /**
     * Get top N apps by storage usage
     */
    suspend fun getTopApps(count: Int): List<AppStorageInfoBytes>
    
    /**
     * Delete a file
     */
    suspend fun deleteFile(filePath: String): Boolean
    
    /**
     * Get file info
     */
    suspend fun getFileInfo(filePath: String): FileNode.File?
    
    /**
     * Get comprehensive storage breakdown for a volume
     * Includes apps, media, and other breakdown from StorageStatsManager
     */
    suspend fun getStorageBreakdown(volumePath: String): StorageCategories
    
    /**
     * Get the most recent scan result (for dashboard display)
     * Returns null if no scan has been performed
     */
    suspend fun getLastScanResult(): FileNode.Directory?
    
    /**
     * Get the timestamp of the last scan
     * Returns 0 if no scan has been performed
     */
    suspend fun getLastScanTimestamp(): Long
}
