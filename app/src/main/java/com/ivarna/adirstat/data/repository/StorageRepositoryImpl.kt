package com.ivarna.adirstat.data.repository

import com.google.gson.Gson
import com.ivarna.adirstat.data.local.db.ScanCacheDao
import com.ivarna.adirstat.data.local.db.ScanCacheEntity
import com.ivarna.adirstat.data.source.AppStatsDataSource
import com.ivarna.adirstat.data.source.AppStorageInfoBytes
import com.ivarna.adirstat.data.source.FileSystemDataSource
import com.ivarna.adirstat.data.source.MediaStoreDataSource
import com.ivarna.adirstat.data.source.ScanProgress
import com.ivarna.adirstat.data.source.StorageCategories
import com.ivarna.adirstat.data.source.StorageStatsDataSource
import com.ivarna.adirstat.data.source.StorageVolume
import com.ivarna.adirstat.data.source.StorageVolumeDataSource
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.domain.repository.LastScanSummary
import com.ivarna.adirstat.domain.repository.StorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val fileSystemDataSource: FileSystemDataSource,
    private val storageVolumeDataSource: StorageVolumeDataSource,
    private val appStatsDataSource: AppStatsDataSource,
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val storageStatsDataSource: StorageStatsDataSource,
    private val scanCacheDao: ScanCacheDao,
    private val gson: Gson
) : StorageRepository {

    override suspend fun getStorageVolumes(): List<StorageVolume> {
        return storageVolumeDataSource.getAllVolumes()
    }

    override fun scanVolume(
        volumePath: String,
        excludedPaths: List<String>,
        minFileSize: Long
    ): Flow<ScanProgress> {
        val scope = CoroutineScope(Dispatchers.IO)
        return fileSystemDataSource.scanDirectory(
            rootPath = volumePath,
            excludedPaths = excludedPaths,
            minFileSize = minFileSize,
            scope = scope
        )
    }

    override suspend fun getCachedScan(volumePath: String): FileNode.Directory? = withContext(Dispatchers.IO) {
        try {
            val cache = scanCacheDao.getLatestCacheForPartition(volumePath)
            cache?.let {
                gson.fromJson(it.serializedTreeJson, FileNode.Directory::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveScanResult(scanResult: FileNode.Directory, volumePath: String): Unit = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(scanResult)
            val entity = ScanCacheEntity(
                scanHistoryId = 0,
                serializedTreeJson = json,
                createdAt = System.currentTimeMillis(),
                partitionPath = volumePath,
                totalSize = scanResult.size,
                fileCount = scanResult.fileCount
            )
            scanCacheDao.insertCache(entity)
        } catch (e: Exception) {
            // Log error
        }
    }

    override suspend fun getAppsWithStorageStats(): List<AppStorageInfoBytes> {
        return appStatsDataSource.getAllAppsWithStorageStats().map {
            AppStorageInfoBytes(
                packageName = it.packageName,
                appName = it.appName,
                apkBytes = it.apkSize,
                dataBytes = it.dataSize,
                cacheBytes = it.cacheSize,
                totalBytes = it.totalSize,
                isSystemApp = it.isSystemApp
            )
        }
    }

    override suspend fun getTopApps(count: Int): List<AppStorageInfoBytes> {
        return appStatsDataSource.getTopAppsByStorage(count).map {
            AppStorageInfoBytes(
                packageName = it.packageName,
                appName = it.appName,
                apkBytes = it.apkSize,
                dataBytes = it.dataSize,
                cacheBytes = it.cacheSize,
                totalBytes = it.totalSize,
                isSystemApp = it.isSystemApp
            )
        }
    }

    override suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getFileInfo(filePath: String): FileNode.File? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists() && file.isFile) {
                FileNode.File(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    extension = file.extension.lowercase()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getStorageBreakdown(volumePath: String): StorageCategories {
        val cachedScan = getCachedScan(volumePath)
        val appStats = getAppsWithStorageStats()
        val mediaTotals = try {
            mediaStoreDataSource.getMediaTotals()
        } catch (e: Exception) {
            MediaStoreDataSource.MediaTotals(0L, 0, 0L, 0, 0L, 0)
        }
        return storageStatsDataSource.computeStorageCategories(
            filesystemBytes = cachedScan?.size ?: 0L,
            appStats = appStats,
            mediaTotals = mediaTotals
        )
    }
    
    override suspend fun getLastScanResult(): FileNode.Directory? = withContext(Dispatchers.IO) {
        try {
            scanCacheDao.getLatestCache()?.let {
                gson.fromJson(it.serializedTreeJson, FileNode.Directory::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getLastScanTimestamp(): Long = withContext(Dispatchers.IO) {
        try {
            scanCacheDao.getLatestCache()?.createdAt ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun getLastScanSummary(): LastScanSummary? = withContext(Dispatchers.IO) {
        try {
            scanCacheDao.getLatestCache()?.let {
                LastScanSummary(
                    partitionPath = it.partitionPath,
                    totalSize = it.totalSize,
                    fileCount = it.fileCount,
                    createdAt = it.createdAt
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllScanSummaries(): List<LastScanSummary> = withContext(Dispatchers.IO) {
        try {
            scanCacheDao.getAllCacheList().map {
                LastScanSummary(
                    partitionPath = it.partitionPath,
                    totalSize = it.totalSize,
                    fileCount = it.fileCount,
                    createdAt = it.createdAt
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun clearAllScans(): Unit = withContext(Dispatchers.IO) {
        try { scanCacheDao.deleteAllCache() } catch (e: Exception) { /* ignore */ }
    }
}
