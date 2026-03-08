package com.ivarna.adirstat.data.source

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val resolver = context.contentResolver

    data class MediaTotals(
        val imageBytes: Long,
        val imageCount: Int,
        val videoBytes: Long,
        val videoCount: Int,
        val audioBytes: Long,
        val audioCount: Int
    ) {
        val totalBytes: Long get() = imageBytes + videoBytes + audioBytes
    }

    suspend fun getMediaTotals(): MediaTotals = withContext(Dispatchers.IO) {
        val result = MediaTotals(
            imageBytes = queryTotalSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.SIZE),
            imageCount = queryCount(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            videoBytes = queryTotalSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.SIZE),
            videoCount = queryCount(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
            audioBytes = queryTotalSize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.SIZE),
            audioCount = queryCount(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        )
        Log.d(
            "MediaStore",
            "Images=${result.imageBytes}B (${result.imageCount}) Video=${result.videoBytes}B (${result.videoCount}) Audio=${result.audioBytes}B (${result.audioCount}) TOTAL=${result.totalBytes}B"
        )
        result
    }

    private fun queryTotalSize(uri: Uri, sizeCol: String): Long {
        try {
            resolver.query(uri, arrayOf("SUM($sizeCol) AS t"), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex("t")
                    if (idx >= 0) {
                        val v = c.getLong(idx)
                        if (v > 0) return v
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MediaStore", "SUM failed for $uri: ${e.message}")
        }

        var total = 0L
        try {
            resolver.query(uri, arrayOf(sizeCol), null, null, null)?.use { c ->
                val idx = c.getColumnIndex(sizeCol)
                if (idx >= 0) {
                    while (c.moveToNext()) total += c.getLong(idx)
                }
            }
        } catch (e: Exception) {
            Log.e("MediaStore", "Iteration also failed for $uri: ${e.message}")
        }
        return total
    }

    private fun queryCount(uri: Uri): Int = try {
        resolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use { it.count } ?: 0
    } catch (e: Exception) {
        0
    }
}
