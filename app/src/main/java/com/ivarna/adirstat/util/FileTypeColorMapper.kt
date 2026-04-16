package com.ivarna.adirstat.util

import androidx.compose.ui.graphics.Color
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.presentation.theme.SemanticColors

/**
 * Maps file types to colors for treemap visualization.
 * Uses SemanticColors from the Precision Mosaic Design System.
 */
object FileTypeColorMapper {
    
    /**
     * Get color for a file node based on its extension
     */
    fun getColorForFile(extension: String): Color {
        return when (extension.lowercase()) {
            // Images
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp", "svg", "raw", "tiff", "tif" -> SemanticColors.Images
            // Videos
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg" -> SemanticColors.Videos
            // Audio
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "aiff" -> SemanticColors.Audio
            // Documents
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp", "csv" -> SemanticColors.Documents
            // APKs
            "apk", "xapk", "apks", "apkm" -> SemanticColors.Apk
            // Archives
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "dmg" -> SemanticColors.SystemOther // Or maybe a dedicated archive color
            // Code
            "kt", "java", "py", "js", "ts", "html", "css", "xml", "json", "yaml", "yml", "sh", "bash", "gradle", "kts", "cpp", "c", "h", "hpp", "cs", "go", "rs", "swift" -> SemanticColors.Code
            // Files without extension - treat as unknown
            "", "__noext__" -> SemanticColors.SystemOther
            // Fallback
            else -> SemanticColors.SystemOther
        }
    }
    
    /**
     * Get color for any file node (file or directory)
     */
    fun getColorForNode(node: FileNode): Color {
        if (node.isAppNode) return SemanticColors.SystemOther // Could use a specific App color if needed
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
        
        // Recursively collect all file extensions in the directory tree
        collectExtensionSizes(directory, extensionSizes)
        
        // Return the extension with the largest total size
        return extensionSizes.maxByOrNull { it.value }?.key ?: ""
    }
    
    /**
     * Recursively collect file extension sizes from a directory
     */
    private fun collectExtensionSizes(
        directory: FileNode.Directory,
        extensionSizes: MutableMap<String, Long>
    ) {
        directory.children.forEach { child ->
            when (child) {
                is FileNode.File -> {
                    val ext = child.extension.lowercase()
                    if (ext.isNotEmpty()) {
                        extensionSizes[ext] = (extensionSizes[ext] ?: 0L) + child.size
                    } else {
                        // Files without extension
                        extensionSizes["__noext__"] = (extensionSizes["__noext__"] ?: 0L) + child.size
                    }
                }
                is FileNode.Directory -> {
                    // Recursively process subdirectories
                    collectExtensionSizes(child, extensionSizes)
                }
            }
        }
    }
}
