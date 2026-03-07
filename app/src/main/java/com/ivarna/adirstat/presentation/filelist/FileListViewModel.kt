package com.ivarna.adirstat.presentation.filelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    APPS("Apps & APKs"),
    ARCHIVES("Archives"),
    LARGE_FILES("Large Files (>100MB)")
}

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileListUiState())
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()
    
    private var allFiles: List<FileNode> = emptyList()

    fun loadFiles(volumePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = scanStorageUseCase.getCachedScan(volumePath)
                result.fold(
                    onSuccess = { rootNode ->
                        allFiles = flattenFileTree(rootNode)
                        applyFiltersAndSort()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                )
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
            it.copy(activeFilters = it.activeFilters + filter) 
        }
        applyFiltersAndSort()
    }

    fun removeFilter(filter: FilterOption) {
        _uiState.update { 
            it.copy(activeFilters = it.activeFilters - filter) 
        }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        var filtered = allFiles

        // Apply search query
        val query = _uiState.value.searchQuery
        if (query.isNotEmpty()) {
            filtered = filtered.filter { file ->
                file.name.contains(query, ignoreCase = true) ||
                file.path.contains(query, ignoreCase = true)
            }
        }

        // Apply filters
        val filters = _uiState.value.activeFilters
        if (filters.isNotEmpty()) {
            filtered = filtered.filter { file ->
                when {
                    filters.contains(FilterOption.LARGE_FILES) && file.size > 100 * 1024 * 1024 -> true
                    file is FileNode.File -> {
                        val ext = file.extension?.lowercase()
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
            SortOption.SIZE_DESC -> filtered.sortedByDescending { it.size }
            SortOption.SIZE_ASC -> filtered.sortedBy { it.size }
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

    private fun flattenFileTree(node: FileNode?): List<FileNode> {
        if (node == null) return emptyList()
        
        val result = mutableListOf<FileNode>()
        
        fun traverse(current: FileNode) {
            result.add(current)
            if (current is FileNode.Directory) {
                current.children.forEach { child ->
                    traverse(child)
                }
            }
        }
        
        traverse(node)
        return result
    }
}
