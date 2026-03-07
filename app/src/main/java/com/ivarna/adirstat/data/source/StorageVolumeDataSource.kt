package com.ivarna.adirstat.data.source

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for enumerating all storage volumes on the device.
 */
@Singleton
class StorageVolumeDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Get all storage volumes on the device
     */
    suspend fun getAllVolumes(): List<StorageVolume> = withContext(Dispatchers.IO) {
        val volumes = mutableListOf<StorageVolume>()

        // Add primary external storage (internal)
        volumes.add(getPrimaryStorageVolume())

        volumes.filter { it.isMounted }
    }

    /**
     * Get the primary storage volume (internal storage)
     */
    private fun getPrimaryStorageVolume(): StorageVolume {
        val path = Environment.getExternalStorageDirectory()
        
        return try {
            val statFs = StatFs(path.absolutePath)
            val totalBytes = statFs.totalBytes
            val freeBytes = statFs.availableBytes
            val usedBytes = totalBytes - freeBytes

            StorageVolume(
                id = "primary",
                path = path.absolutePath,
                displayName = "Internal Storage",
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                freeBytes = freeBytes,
                isRemovable = false,
                isPrimary = true,
                state = Environment.getExternalStorageState()
            )
        } catch (e: Exception) {
            StorageVolume(
                id = "primary",
                path = path.absolutePath,
                displayName = "Internal Storage",
                totalBytes = 0,
                usedBytes = 0,
                freeBytes = 0,
                isRemovable = false,
                isPrimary = true,
                state = Environment.MEDIA_UNKNOWN
            )
        }
    }

    /**
     * Get storage stats for a specific path
     */
    fun getStorageStats(path: String): StorageStats? {
        return try {
            val statFs = StatFs(path)
            StorageStats(
                totalBytes = statFs.totalBytes,
                availableBytes = statFs.availableBytes,
                usedBytes = statFs.totalBytes - statFs.availableBytes
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Domain model for storage volume
 */
data class StorageVolume(
    val id: String,
    val path: String,
    val displayName: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val isRemovable: Boolean,
    val isPrimary: Boolean,
    val state: String
) {
    val isMounted: Boolean
        get() = state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY

    val usagePercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100f else 0f
}

/**
 * Storage statistics
 */
data class StorageStats(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long
)
