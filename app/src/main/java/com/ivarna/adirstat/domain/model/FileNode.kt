package com.ivarna.adirstat.domain.model

import java.io.Serializable

/**
 * Represents a file or directory in the file system tree.
 * This is the core data structure for the treemap visualization.
 */
sealed class FileNode : Serializable {
    abstract val name: String
    abstract val path: String
    abstract val size: Long
    abstract val lastModified: Long

    /**
     * A file in the file system
     */
    data class File(
        override val name: String,
        override val path: String,
        override val size: Long,
        override val lastModified: Long,
        val extension: String
    ) : FileNode() {
        
        /**
         * Get the file category based on extension
         */
        fun getCategory(): FileCategory {
            return FileCategory.fromExtension(extension)
        }
    }

    /**
     * A directory in the file system
     */
    data class Directory(
        override val name: String,
        override val path: String,
        val children: List<FileNode>,
        override val size: Long,
        override val lastModified: Long,
        val isRestricted: Boolean = false  // For Android/data, Android/obb etc.
    ) : FileNode() {
        
        /**
         * Number of files (including files in subdirectories)
         */
        val fileCount: Long
            get() = countFiles(this)
        
        /**
         * Number of directories (including subdirectories)
         */
        val directoryCount: Long
            get() = countDirectories(this)
        
        /**
         * Get top N largest children
         */
        fun getLargestChildren(count: Int): List<FileNode> {
            return children.sortedByDescending { it.size }.take(count)
        }
        
        /**
         * Get file type breakdown
         */
        fun getFileTypeBreakdown(): Map<FileCategory, Long> {
            return getFileCategories(this)
        }
        
        private fun countFiles(node: FileNode): Long {
            return when (node) {
                is File -> 1
                is Directory -> node.children.sumOf { countFiles(it) }
            }
        }
        
        private fun countDirectories(node: FileNode): Long {
            return when (node) {
                is File -> 0
                is Directory -> 1 + node.children.sumOf { countDirectories(it) }
            }
        }
        
        private fun getFileCategories(node: FileNode): Map<FileCategory, Long> {
            return when (node) {
                is File -> mapOf(node.getCategory() to node.size)
                is Directory -> {
                    node.children
                        .flatMap { getFileCategories(it).entries }
                        .groupBy { it.key }
                        .mapValues { (_, values) -> values.sumOf { it.value } }
                }
            }
        }
    }
    
    companion object {
        /**
         * Create a FileNode from a file
         */
        fun fromFile(file: java.io.File): FileNode {
            return if (file.isDirectory) {
                Directory(
                    name = file.name,
                    path = file.absolutePath,
                    children = emptyList(), // Children loaded lazily
                    size = 0,
                    lastModified = file.lastModified()
                )
            } else {
                File(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    extension = file.extension.lowercase()
                )
            }
        }
    }
}

/**
 * File categories for color coding in treemap
 */
enum class FileCategory(val displayName: String, val extensions: List<String>) {
    IMAGES("Images", listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "svg", "raw", "tiff", "tif")),
    VIDEOS("Videos", listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg")),
    AUDIO("Audio", listOf("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "aiff")),
    DOCUMENTS("Documents", listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp", "csv")),
    ARCHIVES("Archives", listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "dmg")),
    APK("APK", listOf("apk", "xapk", "apks", "apkm")),
    CODE("Code", listOf("kt", "java", "py", "js", "ts", "html", "css", "xml", "json", "yaml", "yml", "sh", "bash", "gradle", "kts", "cpp", "c", "h", "hpp", "cs", "go", "rs", "swift")),
    OTHER("Other", emptyList());
    
    companion object {
        /**
         * Get file category from extension
         */
        fun fromExtension(extension: String): FileCategory {
            val ext = extension.lowercase()
            return entries.find { it.extensions.contains(ext) } ?: OTHER
        }
    }
}
