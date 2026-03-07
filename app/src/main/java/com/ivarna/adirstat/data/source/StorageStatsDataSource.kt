package com.ivarna.adirstat.data.source

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for accurate storage statistics using StorageStatsManager.
 * This provides actual storage consumption data at the partition level.
 */
@Singleton
class StorageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val storageStatsManager: StorageStatsManager? =
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager

    private val storageManager: StorageManager =
        context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

    private val packageManager: PackageManager = context.packageManager

    /**
     * Get comprehensive storage breakdown for a volume.
     * This uses multiple sources to build an accurate picture.
     */
    suspend fun getStorageBreakdown(volumePath: String): StorageBreakdown = withContext(Dispatchers.IO) {
        val breakdown = StorageBreakdown()

        // Get basic stats using StatFs
        try {
            val statFs = StatFs(volumePath)
            breakdown.totalBytes = statFs.totalBytes
            breakdown.freeBytes = statFs.availableBytes
            breakdown.usedBytes = breakdown.totalBytes - breakdown.freeBytes
        } catch (e: Exception) {
            // Fall back to defaults
        }

        // If we have StorageStatsManager, get more accurate data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val uuid = getStorageUuid(volumePath)
                if (uuid != null) {
                    // Get internal storage stats
                    val internalStats = getInternalStorageStats(uuid)
                    breakdown.appsBytes = internalStats.appsBytes
                    breakdown.mediaBytes = internalStats.mediaBytes
                    breakdown.otherBytes = internalStats.otherBytes

                    // Get app breakdown using StorageStatsManager
                    breakdown.appBreakdown = getAppStorageBreakdown()
                }
            } catch (e: Exception) {
                // StorageStatsManager not available or permission denied
            }
        }

        breakdown
    }

    /**
     * Get storage UUID for a path
     */
    private fun getStorageUuid(path: String): UUID? {
        return try {
            val volume = storageManager.getStorageVolume(File(path))
            volume?.uuid?.let { UUID.fromString(it) }
        } catch (e: Exception) {
            // Default to internal storage UUID
            if (path.startsWith(Environment.getExternalStorageDirectory().absolutePath)) {
                try {
                    StorageManager.UUID_DEFAULT
                } catch (e: Exception) {
                    null
                }
            } else null
        }
    }

    /**
     * Get internal storage statistics using StorageStatsManager
     */
    private fun getInternalStorageStats(uuid: UUID): InternalStorageStats {
        val stats = InternalStorageStats()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Get total and free space
                stats.totalBytes = storageStatsManager?.getTotalBytes(uuid) ?: 0L
                stats.freeBytes = storageStatsManager?.getFreeBytes(uuid) ?: 0L
                stats.usedBytes = stats.totalBytes - stats.freeBytes

                // Get stats for current app
                val appStats = storageStatsManager?.queryStatsForPackage(
                    uuid,
                    context.packageName,
                    Process.myUserHandle()
                )

                if (appStats != null) {
                    // This is just one app, we need all apps
                }
            } catch (e: Exception) {
                // Permission denied or other error
            }
        }

        return stats
    }

    /**
     * Get per-app storage breakdown using StorageStatsManager
     */
    private fun getAppStorageBreakdown(): List<AppStorageBreakdownItem> {
        val apps = mutableListOf<AppStorageBreakdownItem>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                for (appInfo in installedApps) {
                    try {
                        val uuid = StorageManager.UUID_DEFAULT
                        val stats = storageStatsManager?.queryStatsForPackage(
                            uuid,
                            appInfo.packageName,
                            Process.myUserHandle()
                        )

                        if (stats != null) {
                            val total = stats.appBytes + stats.dataBytes + stats.cacheBytes
                            apps.add(
                                AppStorageBreakdownItem(
                                    packageName = appInfo.packageName,
                                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                                    apkSize = stats.appBytes,
                                    dataSize = stats.dataBytes,
                                    cacheSize = stats.cacheBytes,
                                    totalSize = total,
                                    isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Skip this app
                    }
                }
            } catch (e: Exception) {
                // Permission denied
            }
        }

        return apps.sortedByDescending { it.totalSize }
    }

    /**
     * Get total storage for a specific volume path
     */
    fun getVolumeTotalBytes(path: String): Long {
        return try {
            StatFs(path).totalBytes
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get free storage for a specific volume path
     */
    fun getVolumeFreeBytes(path: String): Long {
        return try {
            StatFs(path).availableBytes
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get used storage for a specific volume path
     */
    fun getVolumeUsedBytes(path: String): Long {
        return getVolumeTotalBytes(path) - getVolumeFreeBytes(path)
    }

    /**
     * Calculate estimated apps storage from package manager (fallback when StorageStatsManager unavailable)
     */
    suspend fun getEstimatedAppsStorage(): Long = withContext(Dispatchers.IO) {
        var total = 0L
        try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in installedApps) {
                try {
                    val sourceDir = appInfo.sourceDir
                    if (sourceDir != null) {
                        total += File(sourceDir).length()
                    }
                } catch (e: Exception) {
                    // Skip
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        total
    }
}

/**
 * Comprehensive storage breakdown for a volume
 */
data class StorageBreakdown(
    var totalBytes: Long = 0L,
    var freeBytes: Long = 0L,
    var usedBytes: Long = 0L,
    var appsBytes: Long = 0L,
    var mediaBytes: Long = 0L,
    var otherBytes: Long = 0L,
    var appBreakdown: List<AppStorageBreakdownItem> = emptyList()
)

/**
 * Internal storage statistics
 */
data class InternalStorageStats(
    var totalBytes: Long = 0L,
    var freeBytes: Long = 0L,
    var usedBytes: Long = 0L,
    var appsBytes: Long = 0L,
    var mediaBytes: Long = 0L,
    var otherBytes: Long = 0L
)

/**
 * Per-app storage breakdown item
 */
data class AppStorageBreakdownItem(
    val packageName: String,
    val appName: String,
    val apkSize: Long,
    val dataSize: Long,
    val cacheSize: Long,
    val totalSize: Long,
    val isSystemApp: Boolean
)
