package com.ivarna.adirstat.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class PermissionStatus(
        val hasAllFilesAccess: Boolean,        // MANAGE_EXTERNAL_STORAGE (API 30+) or READ_EXTERNAL_STORAGE (API < 30)
        val hasUsageStatsAccess: Boolean,      // PACKAGE_USAGE_STATS via AppOpsManager
        val hasMediaImagesAccess: Boolean,     // READ_MEDIA_IMAGES (API 33+)
        val hasMediaVideoAccess: Boolean,      // READ_MEDIA_VIDEO (API 33+)
        val hasMediaAudioAccess: Boolean       // READ_MEDIA_AUDIO (API 33+)
    ) {
        val hasAnyStorageAccess: Boolean get() = hasAllFilesAccess ||
            hasMediaImagesAccess || hasMediaVideoAccess || hasMediaAudioAccess
        val isFullyConfigured: Boolean get() = hasAllFilesAccess && hasUsageStatsAccess
    }

    /**
     * Check ALL permission states. Call this:
     * 1. On app start
     * 2. Every time onResume fires (user may have granted in Settings and returned)
     * 3. Before any scan
     */
    fun checkAllPermissions(): PermissionStatus {
        return PermissionStatus(
            hasAllFilesAccess = checkAllFilesAccess(),
            hasUsageStatsAccess = checkUsageStatsAccess(),
            hasMediaImagesAccess = checkMediaPermission(android.Manifest.permission.READ_MEDIA_IMAGES),
            hasMediaVideoAccess = checkMediaPermission(android.Manifest.permission.READ_MEDIA_VIDEO),
            hasMediaAudioAccess = checkMediaPermission(android.Manifest.permission.READ_MEDIA_AUDIO),
        )
    }

    /**
     * Check MANAGE_EXTERNAL_STORAGE (API 30+) or READ_EXTERNAL_STORAGE (API < 30).
     *
     * CRITICAL: Do NOT use ContextCompat.checkSelfPermission() for MANAGE_EXTERNAL_STORAGE.
     * It always returns DENIED. Use Environment.isExternalStorageManager() instead.
     */
    fun checkAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ — must use Environment.isExternalStorageManager()
            Environment.isExternalStorageManager()
        } else {
            // API 24-29 — standard runtime permission check is fine here
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check PACKAGE_USAGE_STATS.
     *
     * CRITICAL: Do NOT use ContextCompat.checkSelfPermission() for PACKAGE_USAGE_STATS.
     * It always returns DENIED. Must use AppOpsManager.checkOpNoThrow().
     */
    fun checkUsageStatsAccess(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ — use unsafeCheckOpNoThrow (checkOpNoThrow is deprecated on Q+)
            appOpsManager.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            // API 24-28
            appOpsManager.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return if (mode == android.app.AppOpsManager.MODE_DEFAULT) {
            // Some devices use MODE_DEFAULT — fallback to checkSelfPermission in this case only
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.PACKAGE_USAGE_STATS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == android.app.AppOpsManager.MODE_ALLOWED
        }
    }

    private fun checkMediaPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true // not needed below API 33
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the intent to request MANAGE_EXTERNAL_STORAGE.
     * Launch this with rememberLauncherForActivityResult — NOT requestPermissions().
     *
     * Try app-specific intent first, fall back to general all-files intent.
     */
    fun getAllFilesAccessIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            } catch (e: Exception) {
                // Fallback — some manufacturers don't support app-specific intent
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        } else {
            // API < 30 — use standard storage permission (requested via requestPermissions, not this)
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Get the intent to request PACKAGE_USAGE_STATS.
     * Launch this with rememberLauncherForActivityResult.
     */
    fun getUsageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            // On some devices, deep-link directly to this app's entry
            try {
                data = Uri.parse("package:${context.packageName}")
            } catch (e: Exception) {
                // ignore — will open general usage access list
            }
        }
    }
}
