package com.ivarna.adirstat.data.source

import android.os.Environment
import com.ivarna.adirstat.domain.model.FileNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for scanning the file system.
 * Provides recursive file traversal with progress reporting via Flow.
 */
@Singleton
class FileSystemDataSource @Inject constructor() {

    companion object {
        // Batch size for UI updates - don't flood the UI with every file
        private const val BATCH_SIZE = 100
        // Minimum file size to include in scan (default: 0 bytes)
        private const val MIN_FILE_SIZE = 0L
    }

    /**
     * Scan a directory recursively and emit progress updates
     */
    fun scanDirectory(
        rootPath: String,
        excludedPaths: List<String> = emptyList(),
        minFileSize: Long = MIN_FILE_SIZE,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Flow<ScanProgress> = callbackFlow {
        var totalFiles = 0L
        var scannedFiles = 0L
        var totalSize = 0L
        var currentPath = rootPath

        val rootFile = File(rootPath)
        if (!rootFile.exists() || !rootFile.canRead()) {
            trySend(ScanProgress.Error("Cannot access path: $rootPath"))
            close()
            return@callbackFlow
        }

        // First, count total files for progress calculation
        trySend(ScanProgress.Counting(rootPath))
        
        launch(Dispatchers.IO) {
            try {
                totalFiles = countFiles(rootFile, excludedPaths)
                trySend(ScanProgress.CountingComplete(totalFiles))
            } catch (e: Exception) {
                // Continue with scan even if counting fails
                trySend(ScanProgress.CountingComplete(0))
            }
        }

        // Build the file tree
        val rootNode = withContext(Dispatchers.IO) {
            scanRecursive(
                file = rootFile,
                excludedPaths = excludedPaths,
                minFileSize = minFileSize,
                onProgress = { path, fileCount, size ->
                    scannedFiles = fileCount
                    currentPath = path
                    totalSize = size
                    
                    if (scannedFiles % BATCH_SIZE == 0L || scannedFiles == totalFiles) {
                        val progress = if (totalFiles > 0) {
                            (scannedFiles.toFloat() / totalFiles * 100).toInt()
                        } else 0
                        
                        trySend(
                            ScanProgress.Scanning(
                                currentPath = path,
                                filesScanned = scannedFiles,
                                totalFiles = totalFiles,
                                totalSize = totalSize,
                                progressPercent = progress
                            )
                        )
                    }
                },
                scope = scope
            )
        }

        trySend(
            ScanProgress.Complete(
                rootNode = rootNode,
                totalFiles = scannedFiles,
                totalSize = totalSize,
                durationMs = 0 // Could add timing
            )
        )

        close()
        awaitClose { }
    }

    /**
     * Count total files in directory (for progress calculation)
     */
    private fun countFiles(file: File, excludedPaths: List<String>): Long {
        if (!file.canRead() || isExcluded(file.absolutePath, excludedPaths)) {
            return 0
        }

        if (file.isFile) {
            return 1
        }

        val children = file.listFiles() ?: return 0
        return children.sumOf { countFiles(it, excludedPaths) }
    }

    /**
     * Recursively scan directory and build FileNode tree
     */
    private suspend fun scanRecursive(
        file: File,
        excludedPaths: List<String>,
        minFileSize: Long,
        onProgress: (String, Long, Long) -> Unit,
        scope: CoroutineScope
    ): FileNode {
        var fileCount = 0L
        var totalSize = 0L

        if (!file.canRead() || isExcluded(file.absolutePath, excludedPaths)) {
            return FileNode.Directory(
                name = file.name,
                path = file.absolutePath,
                children = emptyList(),
                size = 0,
                lastModified = file.lastModified()
            )
        }

        if (file.isFile) {
            val size = file.length()
            if (size >= minFileSize) {
                onProgress(file.absolutePath, 1, size)
            }
            return FileNode.File(
                name = file.name,
                path = file.absolutePath,
                size = size,
                lastModified = file.lastModified(),
                extension = file.extension.lowercase()
            )
        }

        // Directory - scan children
        val children = mutableListOf<FileNode>()
        val childFiles = file.listFiles() ?: emptyArray()

        for (childFile in childFiles) {
            if (!scope.isActive) break
            
            // Check if we're in a symlink loop
            if (childFile.canonicalPath == file.canonicalPath) continue
            
            val childNode = scanRecursive(
                file = childFile,
                excludedPaths = excludedPaths,
                minFileSize = minFileSize,
                onProgress = onProgress,
                scope = scope
            )
            
            children.add(childNode)
            when (childNode) {
                is FileNode.File -> {
                    fileCount++
                    totalSize += childNode.size
                }
                is FileNode.Directory -> {
                    fileCount += childNode.fileCount
                    totalSize += childNode.size
                }
            }
        }

        // Sort children by size (largest first)
        val sortedChildren = children.sortedByDescending { it.size }

        return FileNode.Directory(
            name = file.name,
            path = file.absolutePath,
            children = sortedChildren,
            size = totalSize,
            lastModified = file.lastModified()
        )
    }

    /**
     * Check if path should be excluded
     */
    private fun isExcluded(path: String, excludedPaths: List<String>): Boolean {
        return excludedPaths.any { excluded ->
            path.startsWith(excluded) || path.contains(excluded)
        }
    }

    /**
     * Quick scan - get only top-level files and folders without full recursion
     */
    suspend fun quickScan(rootPath: String): FileNode.Directory = withContext(Dispatchers.IO) {
        val rootFile = File(rootPath)
        
        if (!rootFile.exists() || !rootFile.canRead()) {
            return@withContext FileNode.Directory(
                name = rootFile.name,
                path = rootFile.absolutePath,
                children = emptyList(),
                size = 0,
                lastModified = rootFile.lastModified()
            )
        }

        val children = rootFile.listFiles()?.map { file ->
            if (file.isFile) {
                FileNode.File(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    extension = file.extension.lowercase()
                )
            } else {
                // For directories, just get size without full recursion
                val size = getDirectorySize(file)
                FileNode.Directory(
                    name = file.name,
                    path = file.absolutePath,
                    children = emptyList(),
                    size = size,
                    lastModified = file.lastModified()
                )
            }
        }?.sortedByDescending { it.size } ?: emptyList()

        FileNode.Directory(
            name = rootFile.name,
            path = rootFile.absolutePath,
            children = children,
            size = children.sumOf { it.size },
            lastModified = rootFile.lastModified()
        )
    }

    /**
     * Get directory size without full recursion (approximation)
     */
    private fun getDirectorySize(file: File): Long {
        if (!file.canRead()) return 0
        if (file.isFile()) return file.length()
        
        return file.listFiles()?.sumOf { getDirectorySize(it) } ?: 0
    }
}

/**
 * Scan progress states
 */
sealed class ScanProgress {
    data class Counting(val path: String) : ScanProgress()
    data class CountingComplete(val totalFiles: Long) : ScanProgress()
    data class Scanning(
        val currentPath: String,
        val filesScanned: Long,
        val totalFiles: Long,
        val totalSize: Long,
        val progressPercent: Int
    ) : ScanProgress()
    data class Complete(
        val rootNode: FileNode,
        val totalFiles: Long,
        val totalSize: Long,
        val durationMs: Long
    ) : ScanProgress()
    data class Error(val message: String) : ScanProgress()
    data object Cancelled : ScanProgress()
}
