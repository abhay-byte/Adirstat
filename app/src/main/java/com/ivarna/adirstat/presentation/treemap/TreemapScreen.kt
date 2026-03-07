package com.ivarna.adirstat.presentation.treemap

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    
    LaunchedEffect(volumePath) {
        viewModel.loadTreemap(volumePath)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (uiState.breadcrumbs.isNotEmpty()) {
                        Row {
                            uiState.breadcrumbs.forEachIndexed { index, crumb ->
                                if (index > 0) {
                                    Text(" > ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = crumb.name,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        Text("Treemap")
                    }
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
            // Info bar
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
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${uiState.fileCount} files, ${uiState.folderCount} folders",
                            style = MaterialTheme.typography.labelMedium,
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
                    // Treemap
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        val dirNode = uiState.currentNode as FileNode.Directory
                        TreemapView(
                            fileNode = dirNode,
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
            
            // Color legend
            if (uiState.currentNode is FileNode.Directory) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                    fontWeight = FontWeight.Bold
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
