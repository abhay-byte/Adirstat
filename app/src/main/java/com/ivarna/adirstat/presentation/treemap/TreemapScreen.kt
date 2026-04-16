package com.ivarna.adirstat.presentation.treemap

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter

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
                isAppNode = navigationStack.lastOrNull()?.isAppNode == true,
                showListView = showListView,
                isSelectionMode = isListSelectionMode,
                onBack = { handleBackNavigation() },
                onSearch = { onNavigateToSearch(volumePath, navigationStack.lastOrNull()?.path ?: volumePath) },
                onToggleView = { showListView = !showListView },
                onRefresh = { viewModel.refresh() },
                onSelectAll = { selectedListPaths.clear(); selectedListPaths.addAll(listNodes.map { it.path }) },
                onClearSelection = { selectedListPaths.clear() }
            )
        },
        floatingActionButton = {
            TreemapFAB(onClick = { /* Could open analysis */ })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            if (!uiState.isLoading && uiState.error == null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                BreadcrumbNavigation(
                    stack = navigationStack,
                    onBreadcrumbClick = { index -> viewModel.navigateToBreadcrumb(index) }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = FileSizeFormatter.format(displayTotal),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "used of 256 GB", // Placeholder for actual capacity
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
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
            
            if (currentNodes.isNotEmpty()) {
                InfoBar(displayTotal = displayTotal)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showBottomSheet && uiState.selectedFile != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                modifier = Modifier.shadow(32.dp) // Glassmorphism handled via background blur in system if possible, or just opacity
            ) {
                FileDetailsContent(
                    file = uiState.selectedFile!!,
                    onDismiss = { showBottomSheet = false },
                    onOpen = { path -> FileActions.openFile(context, path) },
                    onShare = { path -> FileActions.shareFile(context, path) },
                    onDelete = { path -> FileActions.deleteFile(context, path); showBottomSheet = false }
                )
            }
        }

        if (showLeaveScanDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveScanDialog = false },
                title = { Text("Leave scan?") },
                text = { Text("The storage scan is still running. Leaving now may discard progress.") },
                confirmButton = { TextButton(onClick = { showLeaveScanDialog = false; onNavigateBack() }) { Text("Leave") } },
                dismissButton = { TextButton(onClick = { showLeaveScanDialog = false }) { Text("Stay") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreemapTopBar(
    title: String,
    isAppNode: Boolean,
    showListView: Boolean,
    isSelectionMode: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onToggleView: () -> Unit,
    onRefresh: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAppNode) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onSelectAll) { Icon(Icons.Default.SelectAll, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onClearSelection) { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            } else {
                IconButton(onClick = onSearch) { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onToggleView) { 
                    Icon(if (showListView) Icons.Default.GridView else Icons.Default.ViewList, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}
@Composable
private fun TreemapBottomBar() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().blur(16.dp).background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.GridView, "Dashboard", true)
            BottomNavItem(Icons.Default.Apps, "Apps", false)
            BottomNavItem(Icons.Default.History, "History", false)
            BottomNavItem(Icons.Default.Settings, "Settings", false)
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, isActive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Icon(icon, null, tint = if (isActive) Color.White else Color(0xFF607D8B), modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isActive) Color.White else Color(0xFF607D8B))
    }
}

@Composable
private fun TreemapFAB(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).size(56.dp).shadow(12.dp, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.White)
        }
    }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onBreadcrumbClick(0) }
        )
        stack.forEachIndexed { index, dir ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dir.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (index == stack.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (index == stack.lastIndex) FontWeight.Bold else FontWeight.Medium,
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
            onItemLongClick = { node -> onItemClick(node) }, // Just select for now
            onTransformGesture = { centroid, pan, zoom -> viewModel.onTransformGesture(centroid, pan, zoom) }
        )
    }
}

@Composable
private fun InfoBar(displayTotal: Long) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Text(
                    text = "Partition used: ${FileSizeFormatter.format(displayTotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
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
        CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
        Spacer(Modifier.height(24.dp))
        Text("Scanning storage...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(uiState.scanProgress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = { uiState.scanProgressPercent.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().clip(CircleShape))
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Error, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No data to display", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onClick(node) }, onLongClick = { onLongClick(node) })
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isSelectionMode) Checkbox(checked = isSelected, onCheckedChange = { onClick(node) })
                Icon(
                    imageVector = when {
                        node is FileNode.Directory && node.isAppNode -> Icons.Default.Android
                        node is FileNode.Directory -> Icons.Default.Folder
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(node.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(FileSizeFormatter.format(node.sizeBytes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (node is FileNode.Directory) Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Column(Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column {
            Text(file.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Size: ${FileSizeFormatter.format(file.sizeBytes)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
        
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text(file.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { onOpen(file.path) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50)) {
                Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("OPEN")
            }
            OutlinedButton(onClick = { onShare(file.path) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50)) {
                Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("SHARE")
            }
        }
        
        Button(
            onClick = { onDelete(file.path) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("DELETE")
        }
    }
}
