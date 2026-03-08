package com.ivarna.adirstat.data.source

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.os.storage.StorageManager
import android.util.Log
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualNodeBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {
    private val storageStatsManager =
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
    private val packageManager = context.packageManager

    suspend fun buildAppVirtualNodes(): List<FileNode.Directory> = withContext(Dispatchers.IO) {
        if (!permissionManager.checkUsageStatsAccess()) {
            Log.w("VirtualNodeBuilder", "PACKAGE_USAGE_STATS not granted — skipping virtual app nodes")
            return@withContext emptyList()
        }

        val userHandle = Process.myUserHandle()
        val result = mutableListOf<FileNode.Directory>()
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        Log.d("VirtualNodeBuilder", "Building virtual nodes for ${packages.size} packages")

        for (pkg in packages) {
            try {
                val stats = storageStatsManager.queryStatsForPackage(
                    StorageManager.UUID_DEFAULT,
                    pkg.packageName,
                    userHandle
                )
                val totalBytes = stats.appBytes + stats.dataBytes + stats.cacheBytes
                if (totalBytes < 1_000_000L) continue

                val appName = try {
                    pkg.applicationInfo?.loadLabel(packageManager)?.toString() ?: pkg.packageName
                } catch (e: Exception) {
                    pkg.packageName
                }

                val children = mutableListOf<FileNode>()
                if (stats.appBytes > 0) {
                    children.add(
                        FileNode.Directory(
                            name = "APK",
                            path = "virtual://${pkg.packageName}/apk",
                            children = emptyList(),
                            size = stats.appBytes,
                            lastModified = 0L,
                            isVirtual = true,
                            virtualLabel = "APK · ${formatBytes(stats.appBytes)}"
                        )
                    )
                }
                if (stats.dataBytes > 0) {
                    children.add(
                        FileNode.Directory(
                            name = "Data",
                            path = "virtual://${pkg.packageName}/data",
                            children = emptyList(),
                            size = stats.dataBytes,
                            lastModified = 0L,
                            isVirtual = true,
                            virtualLabel = "Data · ${formatBytes(stats.dataBytes)}"
                        )
                    )
                }
                if (stats.cacheBytes > 0) {
                    children.add(
                        FileNode.Directory(
                            name = "Cache",
                            path = "virtual://${pkg.packageName}/cache",
                            children = emptyList(),
                            size = stats.cacheBytes,
                            lastModified = 0L,
                            isVirtual = true,
                            virtualLabel = "Cache · ${formatBytes(stats.cacheBytes)}"
                        )
                    )
                }

                result.add(
                    FileNode.Directory(
                        name = appName,
                        path = "virtual://${pkg.packageName}",
                        children = children.sortedByDescending { it.sizeBytes },
                        size = totalBytes,
                        lastModified = 0L,
                        isVirtual = true,
                        virtualLabel = "$appName (App Data)"
                    )
                )
            } catch (_: PackageManager.NameNotFoundException) {
            } catch (e: IOException) {
                Log.w("VirtualNodeBuilder", "IOException for ${pkg.packageName}: ${e.message}")
            }
        }

        Log.d(
            "VirtualNodeBuilder",
            "Built ${result.size} virtual app nodes, total size=${result.sumOf { it.sizeBytes }}"
        )
        result.sortedByDescending { it.sizeBytes }
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024L -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}
