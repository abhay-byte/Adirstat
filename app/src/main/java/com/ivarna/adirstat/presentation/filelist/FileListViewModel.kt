package com.ivarna.adirstat.presentation.filelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.VirtualNodeBuilder
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.domain.usecase.ScanStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FileListUiState(
    val isLoading: Boolean = true,
    val files: List<FileNode> = emptyList(),
    val currentDirectory: FileNode.Directory? = null,
    val navigationStack: List<FileNode.Directory> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.SIZE_DESC,
    val activeFilters: List<FilterOption> = emptyList(),
    val error: String? = null
)

enum class SortOption(val displayName: String) {
    SIZE_DESC("Size (Largest First)"),
    SIZE_ASC("Size (Smallest First)"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_DESC("Date Modified (Newest)"),
    DATE_ASC("Date Modified (Oldest)")
}

enum class FilterOption(val displayName: String) {
    ALL("All"),
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    APK("APKs"),
    APPS("Apps & APKs"),
    ARCHIVES("Archives"),
    LARGE_FILES("Large Files (>100MB)")
}

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase,
    private val virtualNodeBuilder: VirtualNodeBuilder
) : ViewModel() {

    companion object {
        private const val DEFAULT_ROOT_PATH = "/storage/emulated/0"
    }

    private val _uiState = MutableStateFlow(FileListUiState())
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()
    
    private var rootDirectory: FileNode.Directory? = null
    private var currentDirectory: FileNode.Directory? = null
    private var virtualRootNodes: List<FileNode.Directory> = emptyList()

    fun loadFiles(volumePath: String, rootPath: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val resolvedRootPath = rootPath?.takeIf { it.isNotBlank() } ?: when {
                    volumePath.startsWith("virtual://") -> DEFAULT_ROOT_PATH
                    volumePath.startsWith(DEFAULT_ROOT_PATH) -> DEFAULT_ROOT_PATH
                    else -> volumePath
                }

                val result = scanStorageUseCase.getCachedScan(resolvedRootPath)
                val rootNode = result.getOrNull()
                if (rootNode != null) {
                    rootDirectory = rootNode
                    virtualRootNodes = virtualNodeBuilder.buildAppVirtualNodes()

                    val targetDirectory = resolveDirectory(volumePath) ?: rootNode
                    val stack = buildNavigationStack(targetDirectory)
                    currentDirectory = targetDirectory

                    _uiState.update {
                        it.copy(
                            currentDirectory = targetDirectory,
                            navigationStack = stack,
                            error = null
                        )
                    }
                    applyFiltersAndSort()
                } else {
                    val error = result.exceptionOrNull()
                    if (error != null) {
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, files = emptyList()) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    fun setFilter(filter: FilterOption) {
        _uiState.update { 
            it.copy(activeFilters = (it.activeFilters + filter).distinct()) 
        }
        applyFiltersAndSort()
    }

    fun removeFilter(filter: FilterOption) {
        _uiState.update { 
            it.copy(activeFilters = it.activeFilters - filter) 
        }
        applyFiltersAndSort()
    }

    fun navigateInto(directory: FileNode.Directory) {
        currentDirectory = directory
        _uiState.update {
            it.copy(
                currentDirectory = directory,
                navigationStack = buildNavigationStack(directory)
            )
        }
        applyFiltersAndSort()
    }

    fun navigateBack(): Boolean {
        val stack = _uiState.value.navigationStack
        if (stack.isEmpty()) return false

        val newStack = stack.dropLast(1)
        val target = newStack.lastOrNull() ?: rootDirectory
        currentDirectory = target
        _uiState.update {
            it.copy(
                currentDirectory = target,
                navigationStack = newStack
            )
        }
        applyFiltersAndSort()
        return true
    }

    private fun applyFiltersAndSort() {
        val directory = currentDirectory
        val baseNodes = when {
            directory == null -> emptyList()
            directory == rootDirectory -> {
                (directory.children + virtualRootNodes).sortedByDescending { it.sizeBytes }
            }
            else -> directory.children
        }

        var filtered = baseNodes

        // Apply search query
        val query = _uiState.value.searchQuery
        if (query.isNotEmpty()) {
            filtered = filtered.filter { file ->
                file.name.contains(query, ignoreCase = true) ||
                file.path.contains(query, ignoreCase = true) ||
                (file.virtualLabel?.contains(query, ignoreCase = true) == true)
            }
        }

        // Apply filters
        val filters = _uiState.value.activeFilters
        if (filters.isNotEmpty()) {
            filtered = filtered.filter { file ->
                when {
                    filters.contains(FilterOption.LARGE_FILES) && file.sizeBytes > 100 * 1024 * 1024 -> true
                    filters.contains(FilterOption.APPS) && file.isAppNode -> true
                    file is FileNode.File -> {
                        val ext = file.extension.lowercase()
                        when {
                            filters.contains(FilterOption.IMAGES) && ext in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic") -> true
                            filters.contains(FilterOption.VIDEOS) && ext in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv") -> true
                            filters.contains(FilterOption.AUDIO) && ext in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> true
                            filters.contains(FilterOption.DOCUMENTS) && ext in listOf("pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx") -> true
                            filters.contains(FilterOption.APPS) && ext == "apk" -> true
                            filters.contains(FilterOption.ARCHIVES) && ext in listOf("zip", "rar", "7z", "tar", "gz") -> true
                            else -> false
                        }
                    }
                    else -> false
                }
            }
        }

        // Apply sorting
        val sorted = when (_uiState.value.sortOption) {
            SortOption.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
            SortOption.SIZE_ASC -> filtered.sortedBy { it.sizeBytes }
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_DESC -> filtered.sortedByDescending { 
                if (it is FileNode.File) it.lastModified else 0L 
            }
            SortOption.DATE_ASC -> filtered.sortedBy { 
                if (it is FileNode.File) it.lastModified else 0L 
            }
        }

        _uiState.update { it.copy(files = sorted, isLoading = false) }
    }

    private fun resolveDirectory(targetPath: String): FileNode.Directory? {
        val root = rootDirectory ?: return null

        if (targetPath == root.path || targetPath.isBlank()) {
            return root
        }

        return when {
            targetPath.startsWith("virtual://") -> {
                virtualRootNodes.firstNotNullOfOrNull { findDirectoryByPath(it, targetPath) }
            }
            else -> findDirectoryByPath(root, targetPath)
        }
    }

    private fun buildNavigationStack(target: FileNode.Directory): List<FileNode.Directory> {
        val root = rootDirectory ?: return emptyList()
        if (target.path == root.path) return emptyList()

        findDirectoryChain(root, target.path)?.let { chain ->
            return chain.drop(1)
        }

        return virtualRootNodes.firstNotNullOfOrNull { findDirectoryChain(it, target.path) } ?: emptyList()
    }

    private fun findDirectoryByPath(node: FileNode.Directory, path: String): FileNode.Directory? {
        if (node.path == path) return node

        node.children.forEach { child ->
            if (child is FileNode.Directory) {
                val match = findDirectoryByPath(child, path)
                if (match != null) return match
            }
        }
        return null
    }

    private fun findDirectoryChain(node: FileNode.Directory, path: String): List<FileNode.Directory>? {
        if (node.path == path) return listOf(node)

        node.children.forEach { child ->
            if (child is FileNode.Directory) {
                val match = findDirectoryChain(child, path)
                if (match != null) return listOf(node) + match
            }
        }

        return null
    }
}
