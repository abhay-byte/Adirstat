package com.ivarna.adirstat.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility class for file operations: open, share, delete
 */
object FileActions {

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open app details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getPackageNameFromVirtualPath(path: String): String? {
        if (!path.startsWith("virtual://") || path.startsWith("virtual://others")) return null
        val suffix = path.removePrefix("virtual://")
        return suffix.substringBefore('/').takeIf { it.isNotBlank() }
    }

    /**
     * Open a file with an external app
     */
    fun openFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = getMimeType(filePath) ?: "*/*"
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share a file via Android share sheet
     */
    fun shareFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = getMimeType(filePath) ?: "*/*"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Delete a file
     * Returns true if successful, false otherwise
     */
    fun deleteFile(context: Context, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                return false
            }
            
            val deleted = file.delete()
            if (deleted) {
                Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Cannot delete file", Toast.LENGTH_SHORT).show()
            }
            deleted
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot delete file: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Get MIME type from file extension
     */
    private fun getMimeType(filePath: String): String? {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}
