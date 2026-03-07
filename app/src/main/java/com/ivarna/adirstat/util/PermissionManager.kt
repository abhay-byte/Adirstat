package com.ivarna.adirstat.util

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PermissionManager handles all Android permission requests across API levels.
 * 
 * Key permissions:
 * - READ_EXTERNAL_STORAGE (API < 33)
 * - MANAGE_EXTERNAL_STORAGE (API 30+) - Required for full file access
 * - READ_MEDIA_IMAGES/VIDEO/AUDIO (API 33+)
 * - PACKAGE_USAGE_STATS - For per-app storage breakdown
 * - QUERY_ALL_PACKAGES - For listing all installed apps
 */
class PermissionManager(private val context: Context) {

    companion object {
        const val REQUEST_CODE_READ_STORAGE = 100
        const val REQUEST_CODE_MANAGE_STORAGE = 101
        const val REQUEST_CODE_MEDIA_PERMISSIONS = 102
        const val REQUEST_CODE_PACKAGE_USAGE = 103
    }

    // Permission states
    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    /**
     * Check all required permissions and return current state
     */
    fun checkAllPermissions(): PermissionState {
        val state = PermissionState(
            hasReadStorage = hasReadStoragePermission(),
            hasManageExternalStorage = hasManageExternalStoragePermission(),
            hasMediaPermissions = hasMediaPermissions(),
            hasPackageUsageStats = hasPackageUsageStatsPermission(),
            hasQueryAllPackages = hasQueryAllPackagesPermission()
        )
        _permissionState.value = state
        return state
    }

    /**
     * API 29 and below: Request READ_EXTERNAL_STORAGE
     */
    fun requestReadStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_STORAGE
            )
        }
    }

    /**
     * API 30-32: Request MANAGE_EXTERNAL_STORAGE via Settings
     */
    fun requestManageExternalStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && 
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
        }
    }

    /**
     * Alternative: Request MANAGE_EXTERNAL_STORAGE via Settings (for API 30+)
     */
    fun requestManageExternalStoragePermissionViaSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
        }
    }

    /**
     * API 33+: Request granular media permissions
     */
    fun requestMediaPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = mutableListOf<String>()
            
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            
            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions.toTypedArray(),
                    REQUEST_CODE_MEDIA_PERMISSIONS
                )
            }
        }
    }

    /**
     * API 34+: Request partial media access (READ_MEDIA_VISUAL_USER_SELECTED)
     */
    fun requestPartialMediaPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permissions = mutableListOf<String>()
            
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            
            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions.toTypedArray(),
                    REQUEST_CODE_MEDIA_PERMISSIONS
                )
            }
        }
    }

    /**
     * Open Usage Access settings for PACKAGE_USAGE_STATS
     */
    fun openUsageAccessSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        activity.startActivityForResult(intent, REQUEST_CODE_PACKAGE_USAGE)
    }

    /**
     * Check if READ_EXTERNAL_STORAGE is granted (API < 33)
     */
    fun hasReadStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On API 33+, we don't need READ_EXTERNAL_STORAGE
            true
        }
    }

    /**
     * Check if MANAGE_EXTERNAL_STORAGE is granted (API 30+)
     */
    fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false
        }
    }

    /**
     * Check if granular media permissions are granted (API 33+)
     */
    fun hasMediaPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val images = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val video = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            val audio = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            images && video && audio
        } else {
            hasReadStoragePermission()
        }
    }

    /**
     * Check if PACKAGE_USAGE_STATS permission is granted
     * This is NOT a runtime permission - it requires Usage Access in Settings
     */
    fun hasPackageUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Check if QUERY_ALL_PACKAGES is declared in manifest
     */
    fun hasQueryAllPackagesPermission(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the overall permission status for the app
     * Returns the most restrictive level of access
     */
    fun getOverallPermissionLevel(): PermissionLevel {
        return when {
            hasManageExternalStoragePermission() -> PermissionLevel.FULL_ACCESS
            hasMediaPermissions() -> PermissionLevel.MEDIA_ACCESS
            hasReadStoragePermission() -> PermissionLevel.LEGACY_STORAGE
            else -> PermissionLevel.NO_ACCESS
        }
    }

    /**
     * Check if we have enough permissions to do a scan
     */
    fun canPerformScan(): Boolean {
        return hasManageExternalStoragePermission() || hasMediaPermissions() || hasReadStoragePermission()
    }

    /**
     * Check if we can show app storage stats
     */
    fun canShowAppStats(): Boolean {
        return hasPackageUsageStatsPermission()
    }

    /**
     * Handle permission result from Activity
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ): PermissionResult {
        return when (requestCode) {
            REQUEST_CODE_READ_STORAGE -> {
                val granted = grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                PermissionResult.READ_STORAGE_RESULT(granted)
            }
            REQUEST_CODE_MEDIA_PERMISSIONS -> {
                val allGranted = grantResults.isNotEmpty() && 
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                PermissionResult.MEDIA_PERMISSIONS_RESULT(allGranted)
            }
            else -> PermissionResult.UNKNOWN
        }
    }

    /**
     * Handle activity result for MANAGE_EXTERNAL_STORAGE
     */
    fun onManageStorageResult(): PermissionResult {
        return if (hasManageExternalStoragePermission()) {
            PermissionResult.MANAGE_STORAGE_GRANTED
        } else {
            PermissionResult.MANAGE_STORAGE_DENIED
        }
    }

    /**
     * Handle activity result for Usage Access
     */
    fun onUsageAccessResult(): PermissionResult {
        return if (hasPackageUsageStatsPermission()) {
            PermissionResult.USAGE_ACCESS_GRANTED
        } else {
            PermissionResult.USAGE_ACCESS_DENIED
        }
    }

    /**
     * Get permission rationale message based on current state
     */
    fun getPermissionRationale(): String {
        return when {
            !hasManageExternalStoragePermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                "To scan all files on your device, Adirstat needs 'Files and media' access. " +
                "This allows the app to analyze all folders and show you exactly what's using storage."
            }
            !hasMediaPermissions() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                "To scan media files (photos, videos, music), Adirstat needs permission to access them."
            }
            !hasReadStoragePermission() -> {
                "To scan files on your device, Adirstat needs storage permission."
            }
            else -> ""
        }
    }
}

/**
 * Overall permission state
 */
data class PermissionState(
    val hasReadStorage: Boolean = false,
    val hasManageExternalStorage: Boolean = false,
    val hasMediaPermissions: Boolean = false,
    val hasPackageUsageStats: Boolean = false,
    val hasQueryAllPackages: Boolean = false
)

/**
 * Permission level based on what access is granted
 */
enum class PermissionLevel {
    FULL_ACCESS,      // MANAGE_EXTERNAL_STORAGE granted
    MEDIA_ACCESS,     // Granular media permissions (API 33+)
    LEGACY_STORAGE,   // READ_EXTERNAL_STORAGE only (API < 33)
    NO_ACCESS         // No permissions granted
}

/**
 * Result of permission request
 */
sealed class PermissionResult {
    data class READ_STORAGE_RESULT(val granted: Boolean) : PermissionResult()
    data class MEDIA_PERMISSIONS_RESULT(val granted: Boolean) : PermissionResult()
    data object MANAGE_STORAGE_GRANTED : PermissionResult()
    data object MANAGE_STORAGE_DENIED : PermissionResult()
    data object USAGE_ACCESS_GRANTED : PermissionResult()
    data object USAGE_ACCESS_DENIED : PermissionResult()
    data object UNKNOWN : PermissionResult()
}
