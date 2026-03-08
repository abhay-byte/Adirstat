package com.ivarna.adirstat.presentation.treemap

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter
import com.ivarna.adirstat.util.FileTypeColorMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreemapScreen(
    volumePath: String,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFileList: (String) -> Unit,
    viewModel: TreemapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val screenTitle by viewModel.screenTitle.collectAsState()
    val displayTotal by viewModel.displayTotalBytes.collectAsState()
    val currentNodes by viewModel.currentNodes.collectAsState()
    val listNodes by viewModel.listNodes.collectAsState()
    val navigationStack by viewModel.navigationStack.collectAsState()
    val displayItemCount by viewModel.displayItemCount.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showListView by remember { mutableStateOf(false) }
    
    LaunchedEffect(volumePath) {
        viewModel.loadTreemap(volumePath)
    }

    BackHandler(enabled = viewModel.canNavigateBack()) {
        viewModel.navigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = {
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
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    IconButton(onClick = { viewModel.resetZoom() }) {
                        Icon(
                            imageVector = Icons.Default.ZoomOutMap,
                            contentDescription = "Expand"
                        )
                    }

                    IconButton(onClick = { showListView = false }) {
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
            val context = LocalContext.current
            
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
            Text(
                text = if (dir.isVirtual) "🔒 ${dir.name}" else dir.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (index == stack.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                fontWeight = if (index == stack.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                modifier = if (index < stack.lastIndex) Modifier.clickable { onBreadcrumbClick(index + 1) } else Modifier
            )
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

@Composable
private fun FileListView(
    nodes: List<FileNode>,
    onItemClick: (FileNode) -> Unit,
    onItemLongClick: (FileNode) -> Unit,
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
            ListItem(
                headlineContent = { Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(FileSizeFormatter.format(item.size)) },
                leadingContent = {
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
                    if (item is FileNode.Directory) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier
                    .clickable { onItemClick(item) }
                    .pointerInput(item.path) {
                        detectTapGestures(
                            onLongPress = { onItemLongClick(item) }
                        )
                    }
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
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Protected app-storage summary. This node is virtual, read-only, and cannot be opened, shared, or deleted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
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
