package com.ivarna.adirstat.presentation.treemap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreemapScreen(
    volumePath: String,
    onNavigateBack: () -> Unit,
    onNavigateToFileList: () -> Unit,
    viewModel: TreemapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showListView by remember { mutableStateOf(false) }
    
    LaunchedEffect(volumePath) {
        viewModel.loadTreemap(volumePath)
    }
    
    // Calculate if zoomed
    val isZoomed = uiState.zoomScale != 1f || uiState.zoomOffset != Offset.Zero
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Clickable breadcrumbs
                    BreadcrumbRow(
                        breadcrumbs = uiState.breadcrumbs,
                        onBreadcrumbClick = { index -> viewModel.navigateToBreadcrumb(index) }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.breadcrumbs.size > 1) {
                            viewModel.navigateUp()
                        } else {
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
                    // Zoom controls - show only when zoomed
                    if (isZoomed) {
                        IconButton(onClick = { viewModel.resetZoom() }) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "Reset zoom",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Toggle list/treemap view - Grid icon for treemap
                    IconButton(onClick = { showListView = !showListView }) {
                        Icon(
                            imageVector = if (showListView) Icons.Default.GridView else Icons.Default.FormatListBulleted,
                            contentDescription = if (showListView) "Treemap View" else "List View",
                            tint = if (showListView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // File list button
                    IconButton(onClick = onNavigateToFileList) {
                        Icon(Icons.Default.List, contentDescription = "File List")
                    }
                    
                    // Refresh
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
            // Info bar with distinct background - Bug 6 fix
            if (uiState.currentNode != null) {
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
                            text = "Total: ${FileSizeFormatter.format(uiState.currentNode!!.size)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.fileCount} files, ${uiState.folderCount} folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                uiState.currentNode !is FileNode.Directory -> {
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
                        // List view
                        FileListView(
                            directory = uiState.currentNode as FileNode.Directory,
                            onItemClick = { node -> 
                                viewModel.selectFile(node)
                                showBottomSheet = true 
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    } else {
                        // Treemap view - fill remaining space
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(8.dp)
                                .onSizeChanged { size ->
                                    viewModel.updateCanvasSize(size.width.toFloat(), size.height.toFloat())
                                }
                        ) {
                            val dirNode = uiState.currentNode as FileNode.Directory
                            TreemapView(
                                fileNode = dirNode,
                                rects = uiState.treemapRects,
                                zoomScale = uiState.zoomScale,
                                zoomOffset = uiState.zoomOffset,
                                onItemClick = { node ->
                                    when (node) {
                                        is FileNode.Directory -> viewModel.onBlockClick(
                                            com.ivarna.adirstat.util.TreemapRect(
                                                node = node,
                                                x = 0f,
                                                y = 0f,
                                                width = 0f,
                                                height = 0f
                                            )
                                        )
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
            
            // Color legend
            if (uiState.currentNode is FileNode.Directory) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendItem(color = 0xFF4CAF50, label = "Images")
                        LegendItem(color = 0xFFF44336, label = "Video")
                        LegendItem(color = 0xFF9C27B0, label = "Audio")
                        LegendItem(color = 0xFFFF9800, label = "Docs")
                        LegendItem(color = 0xFF795548, label = "Archives")
                        LegendItem(color = 0xFF00BCD4, label = "Code")
                        LegendItem(color = 0xFF607D8B, label = "Other")
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
                    onDismiss = { showBottomSheet = false }
                )
            }
        }
    }
}

@Composable
private fun BreadcrumbRow(
    breadcrumbs: List<Breadcrumb>,
    onBreadcrumbClick: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        breadcrumbs.forEachIndexed { index, crumb ->
            val isLast = index == breadcrumbs.lastIndex
            
            if (index > 0) {
                Text(
                    " › ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = crumb.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (!isLast) {
                    Modifier.clickable { onBreadcrumbClick(index) }
                } else {
                    Modifier
                }
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
    directory: FileNode.Directory,
    onItemClick: (FileNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedItems = remember(directory.children) { 
        directory.children.sortedByDescending { it.size } 
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
                        imageVector = if (item is FileNode.Directory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = if (item is FileNode.Directory) Color(0xFF5C7A99) else MaterialTheme.colorScheme.primary
                    )
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
                modifier = Modifier.clickable { onItemClick(item) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FileDetailsContent(
    file: FileNode,
    onDismiss: () -> Unit
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
            text = "Size: ${FileSizeFormatter.format(file.size)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Path: ${file.path}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { /* TODO: Open */ }) {
                Icon(Icons.Default.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open")
            }
            TextButton(onClick = { /* TODO: Share */ }) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share")
            }
            TextButton(
                onClick = { /* TODO: Delete */ },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
