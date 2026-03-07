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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
                    // Toggle list/treemap view
                    IconButton(onClick = { showListView = !showListView }) {
                        Icon(
                            imageVector = if (showListView) Icons.Default.ViewModule else Icons.Default.List,
                            contentDescription = if (showListView) "Treemap View" else "List View",
                            tint = if (showListView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToFileList) {
                        Icon(Icons.Default.List, contentDescription = "File List")
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
            // Info bar with distinct background
            if (uiState.currentNode != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
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
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${uiState.fileCount} files, ${uiState.folderCount} folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                    // Treemap or List view
                    if (showListView) {
                        // List view
                        FileListView(
                            directory = uiState.currentNode as FileNode.Directory,
                            onItemClick = { node -> viewModel.selectFile(node); showBottomSheet = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Treemap view
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            val dirNode = uiState.currentNode as FileNode.Directory
                            TreemapView(
                                fileNode = dirNode,
                                rects = uiState.treemapRects,
                                onItemClick = { node ->
                                    if (node is FileNode.Directory) {
                                        viewModel.onBlockClick(
                                            com.ivarna.adirstat.util.TreemapRect(
                                                node = node,
                                                x = 0f,
                                                y = 0f,
                                                width = 0f,
                                                height = 0f
                                            )
                                        )
                                    }
                                },
                                onItemLongClick = { node ->
                                    viewModel.selectFile(node)
                                    showBottomSheet = true
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
                        LegendItem(color = 0xFF795548, label = "Apps")
                        LegendItem(color = 0xFF607D8B, label = "Other")
                    }
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
                onDelete = { /* Delete file */ showBottomSheet = false },
                onShare = { /* Share file */ },
                onOpen = { /* Open file */ }
            )
        }
    }
}

@Composable
private fun BreadcrumbRow(
    breadcrumbs: List<Breadcrumb>,
    onBreadcrumbClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, crumb ->
            if (index > 0) {
                Text(
                    text = " > ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            val isLast = index == breadcrumbs.lastIndex
            
            Text(
                text = crumb.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLast) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                textDecoration = if (isLast) TextDecoration.None else TextDecoration.Underline,
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = androidx.compose.ui.graphics.Color(color),
            shape = MaterialTheme.shapes.extraSmall
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
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
    
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = sortedItems,
            key = { item -> item.path }
        ) { item ->
            val itemNode = item
            ListItem(
                headlineContent = { Text(itemNode.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(FileSizeFormatter.format(itemNode.size)) },
                leadingContent = {
                    Icon(
                        imageVector = if (itemNode is FileNode.Directory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    val percentage = if (directory.size > 0) {
                        (itemNode.size.toFloat() / directory.size * 100).toInt()
                    } else 0
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable { onItemClick(itemNode) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FileDetailsContent(
    file: FileNode,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (file) {
                    is FileNode.File -> Icons.Default.InsertDriveFile
                    is FileNode.Directory -> Icons.Default.Folder
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = FileSizeFormatter.format(file.size),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (file is FileNode.File) {
            Text(
                text = file.path,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onOpen) {
                Icon(Icons.Default.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open")
            }
            TextButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share")
            }
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
