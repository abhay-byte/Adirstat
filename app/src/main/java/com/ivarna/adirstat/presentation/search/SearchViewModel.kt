package com.ivarna.adirstat.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.VirtualNodeBuilder
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
    val hasIndexedFiles: Boolean = false,
    val activeRootPath: String? = null,
    val scopePath: String? = null,
    val useRegex: Boolean = false,
    val useWildcard: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase,
    private val virtualNodeBuilder: VirtualNodeBuilder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allFiles: List<FileNode> = emptyList()

    init {
        refreshIndex()
    }

    fun refreshIndex(rootPath: String? = null, scopePath: String? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    activeRootPath = rootPath ?: it.activeRootPath,
                    scopePath = scopePath ?: it.scopePath,
                    error = null
                )
            }
            try {
                val normalizedRootPath = rootPath?.takeIf { it.isNotBlank() }
                val result = if (normalizedRootPath != null) {
                    scanStorageUseCase.getCachedScan(normalizedRootPath)
                } else {
                    scanStorageUseCase.getLastScanResult()
                }

                val rootNode = result.getOrNull()
                val realNodes = rootNode?.let(::flattenFileNode).orEmpty()
                val effectiveRootPath = normalizedRootPath ?: rootNode?.path
                val appNodes = if (effectiveRootPath == "/storage/emulated/0") {
                    virtualNodeBuilder.buildAppVirtualNodes().flatMap { flattenFileNode(it) }
                } else {
                    emptyList()
                }

                allFiles = (realNodes + appNodes)
                    .distinctBy { it.path }
                    .sortedByDescending { it.sizeBytes }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasIndexedFiles = allFiles.isNotEmpty(),
                        activeRootPath = effectiveRootPath,
                        scopePath = scopePath ?: it.scopePath,
                        results = if (it.query.isBlank()) emptyList() else performSearch(it.query)
                    )
                }
            } catch (e: Exception) {
                allFiles = emptyList()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasIndexedFiles = false,
                        results = emptyList(),
                        error = e.message
                    )
                }
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
            matchesScope(file) && (
                file.name.contains(query, ignoreCase = true) ||
                file.path.contains(query, ignoreCase = true) ||
                (file.virtualLabel?.contains(query, ignoreCase = true) == true)
            )
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
                matchesScope(file) && (
                    regex.containsMatchIn(file.name) ||
                    regex.containsMatchIn(file.path) ||
                    (file.virtualLabel?.let(regex::containsMatchIn) == true)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun matchesScope(file: FileNode): Boolean {
        val scopePath = _uiState.value.scopePath?.takeIf { it.isNotBlank() } ?: return true
        if (file.path == scopePath) return true

        val normalizedScope = if (scopePath.endsWith('/')) scopePath else "$scopePath/"
        return file.path.startsWith(normalizedScope)
    }
}
