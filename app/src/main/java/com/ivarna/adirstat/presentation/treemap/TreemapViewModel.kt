package com.ivarna.adirstat.presentation.treemap

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.source.PartitionTotals
import com.ivarna.adirstat.data.source.StorageStatsDataSource
import com.ivarna.adirstat.data.source.VirtualNodeBuilder
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.domain.usecase.ScanStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class TreemapUiState(
    val isLoading: Boolean = true,
    val isScanning: Boolean = false,
    val scanProgress: String = "",
    val selectedFile: FileNode? = null,
    val error: String? = null,
    val zoomScale: Float = 1f,
    val zoomOffset: Offset = Offset.Zero,
    val canvasWidth: Float = 1000f,
    val canvasHeight: Float = 1000f
)

@HiltViewModel
class TreemapViewModel @Inject constructor(
    private val scanStorageUseCase: ScanStorageUseCase,
    private val storageStatsDataSource: StorageStatsDataSource,
    private val virtualNodeBuilder: VirtualNodeBuilder
) : ViewModel() {

    companion object {
        private const val MAX_ROOT_NODES = 20
    }

    private val _uiState = MutableStateFlow(TreemapUiState())
    val uiState: StateFlow<TreemapUiState> = _uiState.asStateFlow()

    private val _partitionTotals = MutableStateFlow<PartitionTotals?>(null)
    private val _realNodes = MutableStateFlow<List<FileNode>>(emptyList())
    private val _virtualAppNodes = MutableStateFlow<List<FileNode.Directory>>(emptyList())
    private val _currentSourceNodes = MutableStateFlow<List<FileNode>>(emptyList())
    val listNodes: StateFlow<List<FileNode>> = _currentSourceNodes.asStateFlow()
    private val _currentNodes = MutableStateFlow<List<FileNode>>(emptyList())
    val currentNodes: StateFlow<List<FileNode>> = _currentNodes.asStateFlow()

    private val _navigationStack = MutableStateFlow<List<FileNode.Directory>>(emptyList())
    val navigationStack: StateFlow<List<FileNode.Directory>> = _navigationStack.asStateFlow()

    val screenTitle: StateFlow<String> = _navigationStack
        .map { stack ->
            when {
                stack.isEmpty() -> "Storage"
                stack.last().isVirtual -> stack.last().name
                else -> stack.last().name
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Storage")

    val displayTotalBytes: StateFlow<Long> = combine(
        _navigationStack,
        _partitionTotals,
        _currentSourceNodes
    ) { stack, totals, sourceNodes ->
        if (stack.isEmpty()) {
            totals?.usedBytes ?: sourceNodes.sumOf { it.sizeBytes }
        } else {
            stack.last().sizeBytes
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val displayItemCount: StateFlow<Int> = _currentSourceNodes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private var realRootNode: FileNode.Directory? = null
    private var currentVolumePath: String = ""

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _partitionTotals.value = storageStatsDataSource.getPartitionTotals()
        }
    }

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
                            buildAndDisplayRoot(node)
                        } else {
                            // No cached data - start a new scan
                            startScan(volumePath)
                        }
                    },
                    onFailure = {
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

                                    _uiState.update { 
                                        it.copy(
                                            isScanning = false, 
                                            scanProgress = ""
                                        )
                                    }
                                    buildAndDisplayRoot(state.rootNode)
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

    private suspend fun buildAndDisplayRoot(root: FileNode.Directory) {
        realRootNode = root
        val virtualNodes = virtualNodeBuilder.buildAppVirtualNodes()
        _realNodes.value = root.children.sortedByDescending { it.sizeBytes }
        _virtualAppNodes.value = virtualNodes.sortedByDescending { it.sizeBytes }
        _navigationStack.value = emptyList()
        showRoot()

        Log.d("TreemapVM", "Root: ${root.children.size} real + ${virtualNodes.size} virtual nodes")
        _uiState.update { it.copy(isLoading = false, error = null, zoomScale = 1f, zoomOffset = Offset.Zero) }
    }

    private fun showRoot() {
        val allNodes = (_realNodes.value + _virtualAppNodes.value).sortedByDescending { it.sizeBytes }
        _currentSourceNodes.value = allNodes
        _currentNodes.value = buildDisplayNodes(allNodes)
        _uiState.update { it.copy(isLoading = false, error = null, zoomScale = 1f, zoomOffset = Offset.Zero) }
    }

    private fun showDirectory(directory: FileNode.Directory, stack: List<FileNode.Directory>) {
        val sourceNodes = directory.children.sortedByDescending { it.sizeBytes }
        _navigationStack.value = stack
        _currentSourceNodes.value = sourceNodes
        _currentNodes.value = if (directory.path.startsWith("virtual://others")) {
            sourceNodes
        } else {
            buildDisplayNodes(sourceNodes)
        }
        _uiState.update { it.copy(isLoading = false, error = null, zoomScale = 1f, zoomOffset = Offset.Zero) }
    }

    private fun buildDisplayNodes(allNodes: List<FileNode>): List<FileNode> {
        val sorted = allNodes.sortedByDescending { it.sizeBytes }
        if (sorted.size <= MAX_ROOT_NODES) return sorted

        val top = sorted.take(MAX_ROOT_NODES)
        val rest = sorted.drop(MAX_ROOT_NODES)
        val restBytes = rest.sumOf { it.sizeBytes }
        val othersNode = FileNode.Directory(
            name = "Others (${rest.size})",
            path = "virtual://others/${rest.hashCode()}",
            children = rest,
            size = restBytes,
            lastModified = 0L,
            isVirtual = true,
            virtualLabel = "Others (${rest.size} items)"
        )
        return top + othersNode
    }

    fun navigateInto(directory: FileNode.Directory) {
        val newStack = _navigationStack.value + directory
        showDirectory(directory, newStack)
    }

    fun onNodeTapped(node: FileNode) {
        when (node) {
            is FileNode.Directory -> {
                if (node.children.isNotEmpty()) navigateInto(node) else selectFile(node)
            }
            is FileNode.File -> selectFile(node)
        }
    }

    fun navigateBack(): Boolean {
        val stack = _navigationStack.value
        if (stack.isEmpty()) return false

        val newStack = stack.dropLast(1)
        if (newStack.isEmpty()) {
            _navigationStack.value = emptyList()
            showRoot()
        } else {
            showDirectory(newStack.last(), newStack)
        }
        return true
    }

    fun navigateToBreadcrumb(index: Int) {
        if (index == 0) {
            _navigationStack.value = emptyList()
            showRoot()
            return
        }

        val stack = _navigationStack.value
        if (index <= stack.size) {
            val newStack = stack.take(index)
            if (newStack.isEmpty()) {
                showRoot()
            } else {
                showDirectory(newStack.last(), newStack)
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

    fun canNavigateBack(): Boolean = _navigationStack.value.isNotEmpty()
    
    // Zoom and pan functions
    fun onTransformGesture(centroid: Offset, pan: Offset, zoom: Float) {
        _uiState.update { state ->
            val newScale = (state.zoomScale * zoom).coerceIn(0.5f, 10f)
            val scaleDelta = newScale / state.zoomScale
            val newOffset = (state.zoomOffset + centroid) * scaleDelta - centroid + pan
            state.copy(
                zoomScale = newScale,
                zoomOffset = newOffset
            )
        }
    }
    
    fun resetZoom() {
        _uiState.update { it.copy(zoomScale = 1f, zoomOffset = Offset.Zero) }
    }
    
    fun updateCanvasSize(width: Float, height: Float) {
        _uiState.update { state ->
            if (abs(state.canvasWidth - width) > 50 || abs(state.canvasHeight - height) > 50) {
                state.copy(
                    canvasWidth = width,
                    canvasHeight = height
                )
            } else {
                state.copy(canvasWidth = width, canvasHeight = height)
            }
        }
    }
}
