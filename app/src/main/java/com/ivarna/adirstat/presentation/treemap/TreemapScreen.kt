package com.ivarna.adirstat.presentation.treemap

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.presentation.common.components.AdirstatTopBar
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreemapScreen(
    volumePath: String,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (String, String?) -> Unit,
    viewModel: TreemapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val screenTitle by viewModel.screenTitle.collectAsState()
    val displayTotal by viewModel.displayTotalBytes.collectAsState()
    val currentNodes by viewModel.currentNodes.collectAsState()
    val listNodes by viewModel.listNodes.collectAsState()
    val navigationStack by viewModel.navigationStack.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showListView by remember { mutableStateOf(false) }
    var showLeaveScanDialog by remember { mutableStateOf(false) }
    
    val selectedListPaths = remember { mutableStateListOf<String>() }
    val isListSelectionMode = showListView && selectedListPaths.isNotEmpty()
    val shouldConfirmLeaveScan = uiState.isScanning || (uiState.isLoading && currentNodes.isEmpty() && uiState.error == null)

    fun handleBackNavigation() {
        when {
            shouldConfirmLeaveScan -> showLeaveScanDialog = true
            isListSelectionMode -> selectedListPaths.clear()
            viewModel.navigateBack() -> Unit
            else -> onNavigateBack()
        }
    }

    LaunchedEffect(volumePath) {
        viewModel.loadTreemap(volumePath)
    }

    BackHandler(enabled = shouldConfirmLeaveScan || isListSelectionMode || viewModel.canNavigateBack()) {
        handleBackNavigation()
    }

    Scaffold(
        topBar = {
            TreemapTopBar(
                title = screenTitle,
                showListView = showListView,
                isSelectionMode = isListSelectionMode,
                onSearch = { onNavigateToSearch(volumePath, navigationStack.lastOrNull()?.path ?: volumePath) },
                onToggleView = { showListView = !showListView },
                onRefresh = { viewModel.refresh() },
                onSelectAll = { selectedListPaths.clear(); selectedListPaths.addAll(listNodes.map { it.path }) },
                onClearSelection = { selectedListPaths.clear() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!uiState.isLoading && uiState.error == null) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BreadcrumbNavigation(
                        stack = navigationStack,
                        onBreadcrumbClick = { index -> viewModel.navigateToBreadcrumb(index) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = FileSizeFormatter.format(displayTotal),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "in this directory",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.isScanning -> ScanningState(uiState)
                    uiState.error != null -> ErrorState(uiState.error!!, onRetry = { viewModel.refresh() })
                    currentNodes.isEmpty() -> EmptyState()
                    else -> {
                        if (showListView) {
                            FileListView(
                                nodes = listNodes,
                                isSelectionMode = isListSelectionMode,
                                selectedPaths = selectedListPaths.toSet(),
                                onItemClick = { node -> 
                                    if (isListSelectionMode) {
                                        if (selectedListPaths.contains(node.path)) selectedListPaths.remove(node.path)
                                        else selectedListPaths.add(node.path)
                                    } else {
                                        when (node) {
                                            is FileNode.Directory -> viewModel.navigateInto(node)
                                            is FileNode.File -> { viewModel.selectFile(node); showBottomSheet = true }
                                        }
                                    }
                                },
                                onItemLongClick = { node -> 
                                    if (selectedListPaths.contains(node.path)) selectedListPaths.remove(node.path)
                                    else selectedListPaths.add(node.path)
                                }
                            )
                        } else {
                            TreemapVisualization(
                                nodes = currentNodes,
                                uiState = uiState,
                                viewModel = viewModel,
                                onItemClick = { node ->
                                    when (node) {
                                        is FileNode.Directory -> viewModel.navigateInto(node)
                                        is FileNode.File -> { viewModel.selectFile(node); showBottomSheet = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet && uiState.selectedFile != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                com.ivarna.adirstat.presentation.common.components.FileDetailsBottomSheet(
                    file = uiState.selectedFile!!,
                    onOpen = { path -> FileActions.openFile(context, path) },
                    onShare = { path -> FileActions.shareFile(context, path) },
                    onDelete = { path -> FileActions.deleteFile(context, path); showBottomSheet = false }
                )
            }
        }

        if (showLeaveScanDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveScanDialog = false },
                title = { Text("Stop scan?") },
                text = { Text("The storage scan is still in progress. Stop now?") },
                confirmButton = { TextButton(onClick = { showLeaveScanDialog = false; onNavigateBack() }) { Text("Stop") } },
                dismissButton = { TextButton(onClick = { showLeaveScanDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreemapTopBar(
    title: String,
    showListView: Boolean,
    isSelectionMode: Boolean,
    onSearch: () -> Unit,
    onToggleView: () -> Unit,
    onRefresh: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit
) {
    AdirstatTopBar(
        title = title,
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onSelectAll) { Icon(Icons.Default.SelectAll, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onClearSelection) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.primary) }
            } else {
                IconButton(onClick = onSearch) { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onToggleView) { 
                    Icon(if (showListView) Icons.Default.GridView else Icons.Default.List, null, tint = MaterialTheme.colorScheme.primary) 
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary) }
            }
        }
    )
}

@Composable
private fun BreadcrumbNavigation(stack: List<FileNode.Directory>, onBreadcrumbClick: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Storage",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clickable { onBreadcrumbClick(0) }
        )
        stack.forEachIndexed { index, dir ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = dir.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (index == stack.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (index == stack.lastIndex) FontWeight.ExtraBold else FontWeight.Bold,
                modifier = Modifier.clickable { onBreadcrumbClick(index + 1) }
            )
        }
    }
}

@Composable
private fun TreemapVisualization(
    nodes: List<FileNode>,
    uiState: TreemapUiState,
    viewModel: TreemapViewModel,
    onItemClick: (FileNode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size -> viewModel.updateCanvasSize(size.width.toFloat(), size.height.toFloat()) }
    ) {
        TreemapView(
            nodes = nodes,
            zoomScale = uiState.zoomScale,
            zoomOffset = uiState.zoomOffset,
            onItemClick = onItemClick,
            onItemLongClick = { node -> onItemClick(node) },
            onTransformGesture = { centroid, pan, zoom -> viewModel.onTransformGesture(centroid, pan, zoom) }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ScanningState(uiState: TreemapUiState) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 6.dp)
        Spacer(Modifier.height(24.dp))
        Text("Analyzing Space", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(uiState.scanProgress, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        LinearProgressIndicator(
            progress = { uiState.scanProgressPercent.coerceIn(0f, 1f) }, 
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Error, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("Retry Analysis") }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No files found here", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FileListView(
    nodes: List<FileNode>,
    isSelectionMode: Boolean,
    selectedPaths: Set<String>,
    onItemClick: (FileNode) -> Unit,
    onItemLongClick: (FileNode) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(nodes.sortedByDescending { it.sizeBytes }) { node ->
            FileListItem(node, isSelectionMode, selectedPaths.contains(node.path), onItemClick, onItemLongClick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    node: FileNode,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: (FileNode) -> Unit,
    onLongClick: (FileNode) -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onClick(node) }, onLongClick = { onLongClick(node) })
    ) {
        Column {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            node is FileNode.Directory && node.isAppNode -> Icons.Default.Android
                            node is FileNode.Directory -> Icons.Default.Folder
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = FileSizeFormatter.format(node.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                if (isSelectionMode) {
                    Checkbox(checked = isSelected, onCheckedChange = { onClick(node) })
                } else {
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(20.dp))
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 80.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)
            )
        }
    }
}
