package com.ivarna.adirstat.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.domain.usecase.ScanStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<FileNode> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val useRegex: Boolean = false,
    val useWildcard: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allFiles: List<FileNode> = emptyList()

    init {
        loadAllFiles()
    }

    private fun loadAllFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Load files from cached scans - try internal storage first
                val result = scanStorageUseCase.getCachedScan("/storage/emulated/0")
                result.fold(
                    onSuccess = { node ->
                        if (node != null) {
                            allFiles = flattenFileNode(node)
                        }
                    },
                    onFailure = { /* ignore */ }
                )
                
                // If no files, try other common paths
                if (allFiles.isEmpty()) {
                    val primaryStorage = android.os.Environment.getExternalStorageDirectory()
                    val altResult = scanStorageUseCase.getCachedScan(primaryStorage.absolutePath)
                    altResult.fold(
                        onSuccess = { node ->
                            if (node != null) {
                                allFiles = flattenFileNode(node)
                            }
                        },
                        onFailure = { /* ignore */ }
                    )
                }
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    private fun flattenFileNode(node: FileNode): List<FileNode> {
        val result = mutableListOf<FileNode>()
        fun traverse(n: FileNode) {
            result.add(n)
            if (n is FileNode.Directory) {
                n.children.forEach { traverse(it) }
            }
        }
        traverse(node)
        return result
    }

    fun search(query: String) {
        _uiState.update { it.copy(query = query) }
        
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(300) // Debounce
            
            try {
                val results = performSearch(query)
                _uiState.update { it.copy(results = results, isSearching = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message) }
            }
        }
    }

    fun toggleRegex() {
        _uiState.update { it.copy(useRegex = !it.useRegex, useWildcard = false) }
        if (_uiState.value.query.isNotEmpty()) {
            search(_uiState.value.query)
        }
    }

    fun toggleWildcard() {
        _uiState.update { it.copy(useWildcard = !it.useWildcard, useRegex = false) }
        if (_uiState.value.query.isNotEmpty()) {
            search(_uiState.value.query)
        }
    }

    private fun performSearch(query: String): List<FileNode> {
        return if (_uiState.value.useRegex) {
            searchRegex(query)
        } else if (_uiState.value.useWildcard) {
            searchWildcard(query)
        } else {
            searchSimple(query)
        }
    }

    private fun searchSimple(query: String): List<FileNode> {
        return allFiles.filter { file ->
            file.name.contains(query, ignoreCase = true) ||
            file.path.contains(query, ignoreCase = true)
        }
    }

    private fun searchWildcard(pattern: String): List<FileNode> {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        return searchRegex(regex)
    }

    private fun searchRegex(pattern: String): List<FileNode> {
        return try {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            allFiles.filter { file ->
                regex.containsMatchIn(file.name) ||
                regex.containsMatchIn(file.path)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
