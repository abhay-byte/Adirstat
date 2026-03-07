package com.ivarna.adirstat.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.ivarna.adirstat.domain.model.FileCategory
import com.ivarna.adirstat.domain.model.FileNode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for scanning files via MediaStore API.
 * Used as fallback when MANAGE_EXTERNAL_STORAGE is not granted.
 * Provides access to media files (images, videos, audio) and Downloads folder.
 */
@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Scan all media files using MediaStore
     */
    suspend fun scanMediaFiles(): Map<MediaType, List<MediaFile>> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<MediaType, MutableList<MediaFile>>()
        
        result[MediaType.IMAGES] = mutableListOf()
        result[MediaType.VIDEOS] = mutableListOf()
        result[MediaType.AUDIO] = mutableListOf()
        result[MediaType.DOWNLOADS] = mutableListOf()
        
        // Scan Images
        queryMediaStore(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaType = MediaType.IMAGES
        )?.let { result[MediaType.IMAGES]?.addAll(it) }
        
        // Scan Videos
        queryMediaStore(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            mediaType = MediaType.VIDEOS
        )?.let { result[MediaType.VIDEOS]?.addAll(it) }
        
        // Scan Audio
        queryMediaStore(
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            mediaType = MediaType.AUDIO
        )?.let { result[MediaType.AUDIO]?.addAll(it) }
        
        // Scan Downloads (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryMediaStore(
                uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                mediaType = MediaType.DOWNLOADS
            )?.let { result[MediaType.DOWNLOADS]?.addAll(it) }
        }
        
        result
    }

    /**
     * Query MediaStore for files
     */
    private fun queryMediaStore(
        uri: Uri,
        mediaType: MediaType
    ): List<MediaFile>? {
        val files = mutableListOf<MediaFile>()

        val projection = when (mediaType) {
            MediaType.IMAGES -> arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.MIME_TYPE
            )
            MediaType.VIDEOS -> arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.MIME_TYPE
            )
            MediaType.AUDIO -> arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.MIME_TYPE
            )
            MediaType.DOWNLOADS -> arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.DATA,
                MediaStore.Downloads.SIZE,
                MediaStore.Downloads.DATE_MODIFIED,
                MediaStore.Downloads.MIME_TYPE
            )
        }

        val sortOrder = "${MediaStore.MediaColumns.SIZE} DESC"

        try {
            contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown"
                    val path = cursor.getString(dataColumn) ?: ""
                    val size = cursor.getLong(sizeColumn)
                    val dateModified = cursor.getLong(dateColumn) * 1000 // Convert to millis
                    val mimeType = cursor.getString(mimeColumn) ?: ""

                    // Get file extension from mime type or name
                    val extension = getExtensionFromMimeType(mimeType)
                        ?: name.substringAfterLast('.', "")

                    files.add(
                        MediaFile(
                            id = id,
                            name = name,
                            path = path,
                            size = size,
                            lastModified = dateModified,
                            mimeType = mimeType,
                            mediaType = mediaType,
                            uri = ContentUris.withAppendedId(uri, id)
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission denied - return partial results
            return files
        } catch (e: Exception) {
            return files
        }

        return files
    }

    /**
     * Convert media files to FileNode structure
     */
    fun toFileNodes(mediaFiles: Map<MediaType, List<MediaFile>>): FileNode.Directory {
        val allFiles = mediaFiles.values.flatten()
        
        // Group by category
        val byCategory = allFiles.groupBy { file ->
            when (file.mediaType) {
                MediaType.IMAGES -> FileCategory.IMAGES
                MediaType.VIDEOS -> FileCategory.VIDEOS
                MediaType.AUDIO -> FileCategory.AUDIO
                MediaType.DOWNLOADS -> FileCategory.ARCHIVES
            }
        }

        val children = byCategory.map { (category, files) ->
            val categoryChildren = files.map { file ->
                FileNode.File(
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    lastModified = file.lastModified,
                    extension = file.name.substringAfterLast('.', "").lowercase()
                )
            }.sortedByDescending { it.size }

            FileNode.Directory(
                name = category.displayName,
                path = "media://${category.name}",
                children = categoryChildren,
                size = files.sumOf { it.size },
                lastModified = files.maxOfOrNull { it.lastModified } ?: 0
            )
        }.sortedByDescending { it.size }

        return FileNode.Directory(
            name = "Media",
            path = "media://",
            children = children,
            size = allFiles.sumOf { it.size },
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Get total size of all media files
     */
    fun getTotalMediaSize(): Long {
        var total = 0L
        try {
            total += getMediaTypeSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            total += getMediaTypeSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            total += getMediaTypeSize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                total += getMediaTypeSize(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return total
    }

    private fun getMediaTypeSize(uri: Uri): Long {
        var size = 0L
        try {
            contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                while (cursor.moveToNext()) {
                    size += cursor.getLong(sizeColumn)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return size
    }

    private fun getExtensionFromMimeType(mimeType: String): String? {
        return when (mimeType.lowercase()) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            "audio/mpeg" -> "mp3"
            "audio/flac" -> "flac"
            "application/pdf" -> "pdf"
            "application/zip" -> "zip"
            else -> null
        }
    }
}

/**
 * Media types for categorization
 */
enum class MediaType {
    IMAGES,
    VIDEOS,
    AUDIO,
    DOWNLOADS
}

/**
 * Media file data class
 */
data class MediaFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val mimeType: String,
    val mediaType: MediaType,
    val uri: Uri
)
