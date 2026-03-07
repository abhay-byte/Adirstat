package com.ivarna.adirstat.util

import androidx.compose.ui.graphics.Color
import com.ivarna.adirstat.domain.model.FileNode

/**
 * Maps file types to colors for treemap visualization.
 * These are hardcoded colors, NOT Material theme colors.
 */
object FileTypeColorMapper {
    
    // Color palette for treemap
    val IMAGE_COLOR = Color(0xFF4CAF50)       // Green
    val VIDEO_COLOR = Color(0xFFF44336)        // Red
    val AUDIO_COLOR = Color(0xFF9C27B0)       // Purple
    val DOCUMENT_COLOR = Color(0xFFFF9800)    // Orange
    val ARCHIVE_COLOR = Color(0xFF795548)     // Brown
    val CODE_COLOR = Color(0xFF00BCD4)        // Cyan
    val OTHER_COLOR = Color(0xFF607D8B)       // Blue-grey (fallback)
    
    /**
     * Get color for a file node based on its extension
     */
    fun getColorForFile(extension: String): Color {
        return when (extension.lowercase()) {
            // Images
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp", "svg", "raw", "tiff", "tif" -> IMAGE_COLOR
            // Videos
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg" -> VIDEO_COLOR
            // Audio
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "aiff" -> AUDIO_COLOR
            // Documents
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp", "csv" -> DOCUMENT_COLOR
            // Archives / APKs
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "dmg", "apk", "xapk", "apks", "apkm" -> ARCHIVE_COLOR
            // Code
            "kt", "java", "py", "js", "ts", "html", "css", "xml", "json", "yaml", "yml", "sh", "bash", "gradle", "kts", "cpp", "c", "h", "hpp", "cs", "go", "rs", "swift" -> CODE_COLOR
            // Fallback
            else -> OTHER_COLOR
        }
    }
    
    /**
     * Get color for any file node (file or directory)
     */
    fun getColorForNode(node: FileNode): Color {
        return when (node) {
            is FileNode.File -> getColorForFile(node.extension)
            is FileNode.Directory -> {
                // For directories, find dominant extension among direct children
                val dominantExt = getDominantExtension(node)
                getColorForFile(dominantExt)
            }
        }
    }
    
    /**
     * Get the dominant file extension in a directory based on total size
     */
    fun getDominantExtension(directory: FileNode.Directory): String {
        val extensionSizes = mutableMapOf<String, Long>()
        
        // Only check direct children (not recursive for performance)
        directory.children.forEach { child ->
            when (child) {
                is FileNode.File -> {
                    val ext = child.extension.lowercase()
                    extensionSizes[ext] = (extensionSizes[ext] ?: 0L) + child.size
                }
                is FileNode.Directory -> {
                    // For child directories, add their size but mark as directory
                    extensionSizes["__dir__"] = (extensionSizes["__dir__"] ?: 0L) + child.size
                }
            }
        }
        
        // Return the extension with the largest total size
        return extensionSizes.maxByOrNull { it.value }?.key ?: ""
    }
}
