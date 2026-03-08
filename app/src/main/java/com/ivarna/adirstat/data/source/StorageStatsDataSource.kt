package com.ivarna.adirstat.data.source

import android.app.Application
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import android.util.Log
import com.ivarna.adirstat.util.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {
    private val storageStatsManager: StorageStatsManager by lazy {
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
    }
    private val storageManager: StorageManager by lazy {
        context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    }
    private val packageManager = context.packageManager

    companion object {
        private const val TAG = "StorageStatsDataSource"
    }

    /**
     * Get partition totals (total, used, free bytes).
     * getTotalBytes() and getFreeBytes() REQUIRE NO SPECIAL PERMISSION.
     * Call this on Dashboard load even before scan.
     */
    fun getPartitionTotals(): PartitionTotals {
        return try {
            val total = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            val free = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
            PartitionTotals(
                totalBytes = total,
                freeBytes = free,
                usedBytes = total - free
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to get partition totals", e)
            // Fallback to StatFs
            val stat = StatFs(Environment.getDataDirectory().path)
            PartitionTotals(
                totalBytes = stat.totalBytes,
                freeBytes = stat.availableBytes,
                usedBytes = stat.totalBytes - stat.availableBytes
            )
        }
    }

    /**
     * Get per-app storage stats for ALL installed packages.
     *
     * REQUIRES: PACKAGE_USAGE_STATS permission checked via AppOpsManager.
     * Check permissionManager.checkAllPermissions().hasUsageStatsAccess before calling this.
     *
     * Returns empty list with isPermissionMissing=true if permission not granted.
     */
    fun getPerAppStorageStats(): AppStorageResult {
        val permissions = permissionManager.checkAllPermissions()
        if (!permissions.hasUsageStatsAccess) {
            Log.w(TAG, "PACKAGE_USAGE_STATS not granted — returning empty app stats")
            return AppStorageResult(apps = emptyList(), isPermissionMissing = true)
        }

        val userHandle = Process.myUserHandle()
        val apps = mutableListOf<AppStorageInfoBytes>()

        // Get all installed packages including system apps
        val installedPackages = try {
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get installed packages", e)
            return AppStorageResult(apps = emptyList(), isPermissionMissing = false)
        }
        
        Log.d(TAG, "Querying stats for ${installedPackages.size} packages")

        for (pkg in installedPackages) {
            try {
                val stats = storageStatsManager.queryStatsForPackage(
                    StorageManager.UUID_DEFAULT,  // internal storage UUID
                    pkg.packageName,
                    userHandle
                )

                val appName = try {
                    pkg.applicationInfo?.loadLabel(packageManager)?.toString() ?: pkg.packageName
                } catch (e: Exception) {
                    pkg.packageName
                }

                apps.add(AppStorageInfoBytes(
                    packageName = pkg.packageName,
                    appName = appName,
                    apkBytes = stats.appBytes,
                    dataBytes = stats.dataBytes,
                    cacheBytes = stats.cacheBytes,
                    totalBytes = stats.appBytes + stats.dataBytes + stats.cacheBytes,
                    isSystemApp = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
                ))

                Log.d(TAG, "${pkg.packageName}: apk=${stats.appBytes} data=${stats.dataBytes} cache=${stats.cacheBytes}")

            } catch (e: PackageManager.NameNotFoundException) {
                // Package removed between listing and querying — skip silently
            } catch (e: IOException) {
                Log.w(TAG, "IOException for ${pkg.packageName}: ${e.message}")
                // App may be on different volume or unavailable — include with APK size only
                val apkFile = pkg.applicationInfo?.sourceDir?.let { File(it) }
                if (apkFile?.exists() == true) {
                    apps.add(AppStorageInfoBytes(
                        packageName = pkg.packageName,
                        appName = try { pkg.applicationInfo?.loadLabel(packageManager)?.toString() ?: pkg.packageName }
                                  catch (e2: Exception) { pkg.packageName },
                        apkBytes = apkFile.length(),
                        dataBytes = 0L,
                        cacheBytes = 0L,
                        totalBytes = apkFile.length(),
                        isSystemApp = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0,
                        isPartialData = true
                    ))
                }
            }
        }

        Log.d(TAG, "Total apps: ${apps.size}, Total app storage: ${apps.sumOf { it.totalBytes }}")
        return AppStorageResult(apps = apps.sortedByDescending { it.totalBytes }, isPermissionMissing = false)
    }

    /**
     * Aggregate the per-app stats into category totals for the Dashboard bar.
     */
    fun computeStorageCategories(
        filesystemBytes: Long,        // from file scan
        appStats: List<AppStorageInfoBytes>,
        mediaTotals: MediaStoreDataSource.MediaTotals
    ): StorageCategories {
        val totals = getPartitionTotals()

        val totalAppBytes = appStats.sumOf { stats -> stats.apkBytes }
        val totalDataBytes = appStats.sumOf { stats -> stats.dataBytes }
        val totalCacheBytes = appStats.sumOf { stats -> stats.cacheBytes }
        val systemBytes = maxOf(0L, totals.usedBytes - filesystemBytes - totalAppBytes - totalDataBytes - totalCacheBytes)

        return StorageCategories(
            appsBytes = totalAppBytes,
            appDataBytes = totalDataBytes,
            cacheBytes = totalCacheBytes,
            filesBytes = filesystemBytes,
            mediaBytes = mediaTotals.totalBytes,
            imageBytes = mediaTotals.imageBytes,
            videoBytes = mediaTotals.videoBytes,
            audioBytes = mediaTotals.audioBytes,
            systemBytes = systemBytes,
            freeBytes = totals.freeBytes,
            totalBytes = totals.totalBytes,
            usedBytes = totals.usedBytes
        )
    }
}

data class PartitionTotals(val totalBytes: Long, val freeBytes: Long, val usedBytes: Long)

// App storage info for StorageStats - with Bytes naming convention
data class AppStorageInfoBytes(
    val packageName: String,
    val appName: String,
    val apkBytes: Long,
    val dataBytes: Long,
    val cacheBytes: Long,
    val totalBytes: Long,
    val isSystemApp: Boolean = false,
    val isPartialData: Boolean = false
)

// Type alias for easier usage
typealias AppStorageInfo = AppStorageInfoBytes

data class AppStorageResult(
    val apps: List<AppStorageInfoBytes>,
    val isPermissionMissing: Boolean
)

data class StorageCategories(
    val appsBytes: Long,
    val appDataBytes: Long,
    val cacheBytes: Long,
    val filesBytes: Long,
    val mediaBytes: Long,
    val imageBytes: Long,
    val videoBytes: Long,
    val audioBytes: Long,
    val systemBytes: Long,
    val freeBytes: Long,
    val totalBytes: Long,
    val usedBytes: Long
)

typealias StorageBreakdown = StorageCategories
