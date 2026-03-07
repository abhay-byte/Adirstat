package com.ivarna.adirstat.data.source

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for querying app storage statistics.
 */
@Singleton
class AppStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    /**
     * Get all installed apps with their storage stats
     */
    suspend fun getAllAppsWithStorageStats(): List<AppStorageInfo> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<AppStorageInfo>()
        
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (appInfo in installedApps) {
            try {
                val sourceDir = appInfo.sourceDir
                val apkSize = if (sourceDir != null) {
                    java.io.File(sourceDir).length()
                } else 0L
                
                // Only include apps with some storage usage
                if (apkSize > 0) {
                    apps.add(
                        AppStorageInfo(
                            packageName = appInfo.packageName,
                            appName = packageManager.getApplicationLabel(appInfo).toString(),
                            apkSize = apkSize,
                            dataSize = 0L,
                            cacheSize = 0L,
                            totalSize = apkSize,
                            isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                            installTime = 0L,
                            lastUpdateTime = 0L
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip this app
            }
        }
        
        apps.sortedByDescending { it.totalSize }
    }

    /**
     * Get top N apps by storage usage
     */
    suspend fun getTopAppsByStorage(count: Int): List<AppStorageInfo> {
        return getAllAppsWithStorageStats().take(count)
    }
}

/**
 * App storage information
 */
data class AppStorageInfo(
    val packageName: String,
    val appName: String,
    val apkSize: Long,
    val dataSize: Long,
    val cacheSize: Long,
    val totalSize: Long,
    val isSystemApp: Boolean,
    val installTime: Long,
    val lastUpdateTime: Long
)
