package com.ivarna.adirstat.presentation.treemap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.domain.usecase.ScanStorageUseCase
import com.ivarna.adirstat.util.Rect
import com.ivarna.adirstat.util.TreemapLayoutEngine
import com.ivarna.adirstat.util.TreemapRect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TreemapUiState(
    val isLoading: Boolean = true,
    val isScanning: Boolean = false,
    val scanProgress: String = "",
    val currentNode: FileNode? = null,
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val treemapRects: List<TreemapRect> = emptyList(),
    val fileCount: Int = 0,
    val folderCount: Int = 0,
    val selectedFile: FileNode? = null,
    val error: String? = null
)

data class Breadcrumb(
    val name: String,
    val node: FileNode
)

@HiltViewModel
class TreemapViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase,
    private val treemapLayoutEngine: TreemapLayoutEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(TreemapUiState())
    val uiState: StateFlow<TreemapUiState> = _uiState.asStateFlow()

    private var rootNode: FileNode? = null
    private var currentVolumePath: String = ""

    fun loadTreemap(volumePath: String) {
        currentVolumePath = volumePath
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // First try to load from cache
                val result = scanStorageUseCase.getCachedScan(volumePath)
                result.fold(
                    onSuccess = { node ->
                        if (node != null) {
                            rootNode = node
                            displayNode(node)
                        } else {
                            // No cached data - start a new scan
                            startScan(volumePath)
                        }
                    },
                    onFailure = { e ->
                        // Error loading cache - start a new scan
                        startScan(volumePath)
                    }
                )
            } catch (e: Exception) {
                // Exception - start a new scan
                startScan(volumePath)
            }
        }
    }
    
    private fun startScan(volumePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false, isScanning = true, scanProgress = "Starting scan...") }
            try {
                scanStorageUseCase.scanVolume(volumePath).collect { result ->
                    result.fold(
                        onSuccess = { state ->
                            when (state) {
                                is com.ivarna.adirstat.domain.usecase.ScanState.Scanning -> {
                                    _uiState.update { 
                                        it.copy(scanProgress = state.currentPath) 
                                    }
                                }
                                is com.ivarna.adirstat.domain.usecase.ScanState.Complete -> {
                                    // Save to cache
                                    scanStorageUseCase.saveScanResult(state.rootNode, volumePath)
                                    
                                    rootNode = state.rootNode
                                    _uiState.update { 
                                        it.copy(
                                            isScanning = false, 
                                            scanProgress = ""
                                        )
                                    }
                                    displayNode(state.rootNode)
                                }
                                else -> {}
                            }
                        },
                        onFailure = { e ->
                            _uiState.update { 
                                it.copy(
                                    isScanning = false, 
                                    error = e.message
                                ) 
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isScanning = false, 
                        error = e.message
                    ) 
                }
            }
        }
    }

    private fun displayNode(node: FileNode) {
        val dirNode = node as? FileNode.Directory
        val (files, folders) = if (dirNode != null) {
            dirNode.fileCount.toInt() to dirNode.directoryCount.toInt()
        } else 0 to 0
        
        // Get children for treemap
        val children = when (node) {
            is FileNode.Directory -> node.children.sortedByDescending { it.size }
            is FileNode.File -> listOf(node)
            else -> emptyList()
        }
        
        // Filter out very small items for better visualization
        val minSize = if (node.size > 0) node.size / 1000 else 0L // Only show items > 0.1%
        val visibleChildren = children.filter { it.size >= minSize }.take(100)
        
        // Calculate treemap layout
        val bounds = Rect(0f, 0f, 1000f, 1000f)
        val rects = treemapLayoutEngine.calculateLayout(
            items = visibleChildren,
            bounds = bounds
        )
        
        _uiState.update {
            it.copy(
                isLoading = false,
                currentNode = node,
                breadcrumbs = listOf(Breadcrumb("Root", node)),
                treemapRects = rects,
                fileCount = files,
                folderCount = folders,
                error = null
            )
        }
    }

    fun onBlockClick(rect: TreemapRect) {
        val node = rect.node
        if (node is FileNode.Directory && node.children.isNotEmpty()) {
            navigateTo(node)
        }
    }

    private fun navigateTo(node: FileNode) {
        val currentBreadcrumbs = _uiState.value.breadcrumbs.toMutableList()
        currentBreadcrumbs.add(Breadcrumb(node.name, node))
        
        val dirNode = node as? FileNode.Directory
        val (files, folders) = if (dirNode != null) {
            dirNode.fileCount.toInt() to dirNode.directoryCount.toInt()
        } else 0 to 0
        
        val children = when (node) {
            is FileNode.Directory -> node.children.sortedByDescending { it.size }
            is FileNode.File -> listOf(node)
            else -> emptyList()
        }
        
        val minSize = if (node.size > 0) node.size / 1000 else 0L
        val visibleChildren = children.filter { it.size >= minSize }.take(100)
        
        val bounds = Rect(0f, 0f, 1000f, 1000f)
        val rects = treemapLayoutEngine.calculateLayout(
            items = visibleChildren,
            bounds = bounds
        )
        
        _uiState.update {
            it.copy(
                currentNode = node,
                breadcrumbs = currentBreadcrumbs,
                treemapRects = rects,
                fileCount = files,
                folderCount = folders
            )
        }
    }

    fun navigateUp() {
        val breadcrumbs = _uiState.value.breadcrumbs
        if (breadcrumbs.size > 1) {
            val newBreadcrumbs = breadcrumbs.dropLast(1)
            val parent = newBreadcrumbs.last().node
            navigateTo(parent)
        }
    }

    fun navigateToBreadcrumb(index: Int) {
        val breadcrumbs = _uiState.value.breadcrumbs
        if (index < breadcrumbs.size) {
            val target = breadcrumbs[index].node
            val newBreadcrumbs = breadcrumbs.take(index + 1)
            
            val dirNode = target as? FileNode.Directory
            val (files, folders) = if (dirNode != null) {
                dirNode.fileCount.toInt() to dirNode.directoryCount.toInt()
            } else 0 to 0
            
            val children = when (target) {
                is FileNode.Directory -> target.children.sortedByDescending { it.size }
                is FileNode.File -> listOf(target)
                else -> emptyList()
            }
            
            val minSize = if (target.size > 0) target.size / 1000 else 0L
            val visibleChildren = children.filter { it.size >= minSize }.take(100)
            
            val bounds = Rect(0f, 0f, 1000f, 1000f)
            val rects = treemapLayoutEngine.calculateLayout(
                items = visibleChildren,
                bounds = bounds
            )
            
            _uiState.update {
                it.copy(
                    currentNode = target,
                    breadcrumbs = newBreadcrumbs,
                    treemapRects = rects,
                    fileCount = files,
                    folderCount = folders
                )
            }
        }
    }

    fun selectFile(node: FileNode) {
        _uiState.update { it.copy(selectedFile = node) }
    }

    fun refresh() {
        if (currentVolumePath.isNotEmpty()) {
            loadTreemap(currentVolumePath)
        }
    }
}
