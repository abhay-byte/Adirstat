package com.ivarna.adirstat.presentation.treemap

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.presentation.common.components.AppDetailsShortcutCard
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter
import com.ivarna.adirstat.util.FileTypeColorMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreemapScreen(
    volumePath: String,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (String, String?) -> Unit,
    onNavigateToFileList: (String) -> Unit,
    viewModel: TreemapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val screenTitle by viewModel.screenTitle.collectAsState()
    val displayTotal by viewModel.displayTotalBytes.collectAsState()
    val currentNodes by viewModel.currentNodes.collectAsState()
    val listNodes by viewModel.listNodes.collectAsState()
    val navigationStack by viewModel.navigationStack.collectAsState()
    val displayItemCount by viewModel.displayItemCount.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showListView by remember { mutableStateOf(false) }
    var showLeaveScanDialog by remember { mutableStateOf(false) }
    val selectedListPaths = remember { mutableStateListOf<String>() }
    val isListSelectionMode = showListView && selectedListPaths.isNotEmpty()

    LaunchedEffect(volumePath) {
        viewModel.loadTreemap(volumePath)
    }

    LaunchedEffect(listNodes, showListView) {
        val visiblePaths = listNodes.map { it.path }.toSet()
        selectedListPaths.retainAll(visiblePaths)
        if (!showListView) selectedListPaths.clear()
    }

    BackHandler(enabled = uiState.isScanning || isListSelectionMode || viewModel.canNavigateBack()) {
        if (uiState.isScanning) {
            showLeaveScanDialog = true
            return@BackHandler
        }
        if (isListSelectionMode) {
            selectedListPaths.clear()
            return@BackHandler
        }
        viewModel.navigateBack()
    }

    fun toggleListSelection(node: FileNode) {
        if (selectedListPaths.contains(node.path)) {
            selectedListPaths.remove(node.path)
        } else {
            selectedListPaths.add(node.path)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (navigationStack.lastOrNull()?.isVirtual == true) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = screenTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isScanning) {
                            showLeaveScanDialog = true
                            return@IconButton
                        }
                        if (!viewModel.navigateBack()) {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isListSelectionMode) {
                        IconButton(
                            onClick = {
                                selectedListPaths.clear()
                                selectedListPaths.addAll(listNodes.map { it.path })
                            }
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select all items")
                        }
                        IconButton(onClick = { selectedListPaths.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    } else {
                        IconButton(onClick = {
                            onNavigateToSearch(
                                volumePath,
                                navigationStack.lastOrNull()?.path ?: volumePath
                            )
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                        IconButton(onClick = { viewModel.resetZoom() }) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "Expand"
                            )
                        }

                        IconButton(onClick = {
                            selectedListPaths.clear()
                            showListView = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Treemap",
                                tint = if (!showListView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { showListView = true }) {
                            Icon(
                                Icons.Default.FormatListBulleted,
                                contentDescription = "List",
                                tint = if (showListView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                onNavigateToFileList(navigationStack.lastOrNull()?.path ?: volumePath)
                            }
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Open full list")
                        }

                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!uiState.isLoading && uiState.error == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total: ${FileSizeFormatter.format(displayTotal)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$displayItemCount items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                BreadcrumbRow(
                    stack = navigationStack,
                    onBreadcrumbClick = { index -> viewModel.navigateToBreadcrumb(index) }
                )
            }
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading treemap...")
                        }
                    }
                }
                uiState.isScanning -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 6.dp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Scanning storage...",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.scanProgress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                currentNodes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data to display",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // Treemap or List view - use weight to fill remaining space - Bug 1a fix
                    if (showListView) {
                        FileListView(
                            nodes = listNodes,
                            isSelectionMode = isListSelectionMode,
                            selectedPaths = selectedListPaths.toSet(),
                            onItemClick = { node -> 
                                if (isListSelectionMode) {
                                    toggleListSelection(node)
                                } else {
                                    when (node) {
                                        is FileNode.Directory -> viewModel.navigateInto(node)
                                        is FileNode.File -> {
                                            viewModel.selectFile(node)
                                            showBottomSheet = true
                                        }
                                    }
                                }
                            },
                            onItemLongClick = { node -> toggleListSelection(node) },
                            onAppDetailsClick = { node ->
                                FileActions.getPackageNameFromVirtualPath(node.path)?.let { packageName ->
                                    FileActions.openAppInfo(context, packageName)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(8.dp)
                                .onSizeChanged { size ->
                                    viewModel.updateCanvasSize(size.width.toFloat(), size.height.toFloat())
                                }
                        ) {
                            TreemapView(
                                nodes = currentNodes,
                                zoomScale = uiState.zoomScale,
                                zoomOffset = uiState.zoomOffset,
                                onItemClick = { node ->
                                    when (node) {
                                        is FileNode.Directory -> viewModel.navigateInto(node)
                                        is FileNode.File -> {
                                            viewModel.selectFile(node)
                                            showBottomSheet = true
                                        }
                                    }
                                },
                                onItemLongClick = { node ->
                                    viewModel.selectFile(node)
                                    showBottomSheet = true
                                },
                                onTransformGesture = { centroid, pan, zoom ->
                                    viewModel.onTransformGesture(centroid, pan, zoom)
                                }
                            )
                        }
                    }
                }
            }
            
            if (currentNodes.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendItem(color = 0xFF4CAF50, label = "Images")
                        LegendItem(color = 0xFFF44336, label = "Video")
                        LegendItem(color = 0xFF9C27B0, label = "Audio")
                        LegendItem(color = 0xFFFF9800, label = "Docs")
                        LegendItem(color = 0xFF795548, label = "Archives")
                        LegendItem(color = 0xFF00BCD4, label = "Code")
                        LegendItem(color = FileTypeColorMapper.APP_DATA_COLOR.value.toLong(), label = "App Data")
                        LegendItem(color = 0xFF607D8B, label = "Other")
                        // Extra padding at end for scroll
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
        
        // Bottom sheet for file details
        if (showBottomSheet && uiState.selectedFile != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                FileDetailsContent(
                    file = uiState.selectedFile!!,
                    onDismiss = { showBottomSheet = false },
                    onOpen = { path -> FileActions.openFile(context, path) },
                    onShare = { path -> FileActions.shareFile(context, path) },
                    onDelete = { path -> 
                        FileActions.deleteFile(context, path)
                        showBottomSheet = false
                    }
                )
            }
        }

        if (showLeaveScanDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveScanDialog = false },
                title = { Text("Leave scan?") },
                text = {
                    Text("The storage scan is still running. Leaving now may discard progress and return you to the previous screen.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLeaveScanDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveScanDialog = false }) {
                        Text("Stay")
                    }
                }
            )
        }
    }
}

@Composable
private fun BreadcrumbRow(
    stack: List<FileNode.Directory>,
    onBreadcrumbClick: (Int) -> Unit
) {
    if (stack.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                "Storage",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onBreadcrumbClick(0) }
            )
        }

        itemsIndexed(stack) { index, dir ->
            Text(
                " › ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (index < stack.lastIndex) Modifier.clickable { onBreadcrumbClick(index + 1) } else Modifier
            ) {
                if (dir.isVirtual) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = if (index == stack.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = dir.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (index == stack.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    fontWeight = if (index == stack.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Long, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = Color(color),
            shape = MaterialTheme.shapes.small
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListView(
    nodes: List<FileNode>,
    isSelectionMode: Boolean,
    selectedPaths: Set<String>,
    onItemClick: (FileNode) -> Unit,
    onItemLongClick: (FileNode) -> Unit,
    onAppDetailsClick: (FileNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedItems = remember(nodes) { 
        nodes
            .sortedByDescending { it.sizeBytes }
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = sortedItems,
            key = { item -> item.path }
        ) { item ->
            val isSelected = selectedPaths.contains(item.path)
            ListItem(
                headlineContent = { Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(FileSizeFormatter.format(item.size)) },
                leadingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onItemClick(item) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = when {
                                item is FileNode.Directory && item.isVirtual -> Icons.Default.Android
                                item is FileNode.Directory -> Icons.Default.Folder
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = when {
                                item is FileNode.Directory && item.isVirtual -> FileTypeColorMapper.APP_DATA_COLOR
                                item is FileNode.Directory -> Color(0xFF5C7A99)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                },
                overlineContent = {
                    if (item.isVirtual) {
                        Text(
                            text = item.virtualLabel ?: "Protected app storage",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isSelectionMode && item.isVirtual) {
                            FilledTonalIconButton(
                                onClick = { onAppDetailsClick(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Open app details"
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (!isSelectionMode && item is FileNode.Directory) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onItemClick(item) },
                        onLongClick = { onItemLongClick(item) }
                    )
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FileDetailsContent(
    file: FileNode,
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit,
    onShare: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = file.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Size: ${FileSizeFormatter.format(file.sizeBytes)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Path: ${file.path}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (file.isVirtual) {
            val packageName = FileActions.getPackageNameFromVirtualPath(file.path)
            AppDetailsShortcutCard(
                summary = "Protected app-storage summary. This node is virtual, read-only, and cannot be opened, shared, or deleted.",
                onOpenAppDetails = packageName?.let {
                    {
                        FileActions.openAppInfo(context, it)
                        onDismiss()
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { onOpen(file.path) }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open")
                }
                TextButton(onClick = { onShare(file.path) }) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
                TextButton(
                    onClick = { onDelete(file.path) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
