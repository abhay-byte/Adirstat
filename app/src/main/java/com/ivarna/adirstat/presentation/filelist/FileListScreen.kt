package com.ivarna.adirstat.presentation.filelist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.presentation.common.components.AppDetailsShortcutCard
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    volumePath: String,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (() -> Unit)? = null,
    viewModel: FileListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedNode by remember { mutableStateOf<FileNode?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(volumePath) {
        viewModel.loadFiles(volumePath)
    }
    
    BackHandler(enabled = uiState.navigationStack.isNotEmpty()) {
        if (!viewModel.navigateBack()) {
            onNavigateBack()
        }
    }

    fun onItemClick(file: FileNode) {
        when (file) {
            is FileNode.Directory -> viewModel.navigateInto(file)
            is FileNode.File -> {
                selectedNode = file
                showBottomSheet = true
            }
        }
    }

    fun onItemLongPress(file: FileNode) {
        selectedNode = file
        showBottomSheet = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentTitle = uiState.currentDirectory?.name?.ifBlank { "Files" } ?: "Files"
                    Text(
                        if (uiState.currentDirectory?.isVirtual == true) "🔒 $currentTitle" else currentTitle
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!viewModel.navigateBack()) {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onNavigateToSearch != null) {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    var showSortMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (uiState.sortOption == option) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                    
                    var showFilterMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        FilterOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    viewModel.setFilter(option)
                                    showFilterMenu = false
                                }
                            )
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
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search files...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.activeFilters.forEach { filter ->
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.removeFilter(filter) },
                        label = { Text(filter.displayName) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.files.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No files found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.files,
                            key = { it.path }
                        ) { file ->
                            FileListItem(
                                file = file,
                                onClick = { onItemClick(file) },
                                onLongPress = { onItemLongPress(file) },
                                onAppDetailsClick = FileActions.getPackageNameFromVirtualPath(file.path)?.let { packageName ->
                                    {
                                        FileActions.openAppInfo(context, packageName)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom sheet for file details
        if (showBottomSheet && selectedNode != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                FileDetailBottomSheet(
                    file = selectedNode!!,
                    onDismiss = { showBottomSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    file: FileNode,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onAppDetailsClick: (() -> Unit)? = null
) {
    val isDirectory = file is FileNode.Directory
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (file) {
                    is FileNode.File -> getFileIcon(file.extension)
                    is FileNode.Directory -> if (file.isVirtual) Icons.Default.Android else Icons.Default.Folder
                },
                contentDescription = null,
                tint = when (file) {
                    is FileNode.File -> getFileColor(file.extension)
                    is FileNode.Directory -> if (file.isVirtual) androidx.compose.ui.graphics.Color(0xFF3F51B5) else androidx.compose.ui.graphics.Color(0xFF5C7A99)
                },
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                when {
                    file.isVirtual -> {
                        Text(
                            text = file.virtualLabel ?: "Protected app storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    file is FileNode.File -> {
                        Text(
                            text = file.path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                if (file is FileNode.Directory && !file.isVirtual) {
                    Text(
                        text = file.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FileSizeFormatter.format(file.sizeBytes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (file is FileNode.File && file.lastModified > 0) {
                    Text(
                        text = formatDate(file.lastModified),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (file.isVirtual && onAppDetailsClick != null) {
                    FilledTonalIconButton(
                        onClick = onAppDetailsClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open app details"
                        )
                    }
                }
            }
            
            // Chevron for folders to indicate navigability
            if (isDirectory) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate into folder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getFileIcon(extension: String?) = when (extension?.lowercase()) {
    in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic") -> Icons.Default.Image
    in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv") -> Icons.Default.VideoFile
    in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> Icons.Default.AudioFile
    in listOf("pdf", "doc", "docx", "txt", "rtf", "odt") -> Icons.Default.Description
    in listOf("zip", "rar", "7z", "tar", "gz") -> Icons.Default.Archive
    in listOf("apk") -> Icons.Default.Android
    else -> Icons.Default.InsertDriveFile
}

private fun getFileColor(extension: String?) = when (extension?.lowercase()) {
    in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic") -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
    in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv") -> androidx.compose.ui.graphics.Color(0xFFF44336)
    in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    in listOf("pdf", "doc", "docx", "txt", "rtf", "odt") -> androidx.compose.ui.graphics.Color(0xFFFF9800)
    in listOf("zip", "rar", "7z", "tar", "gz") -> androidx.compose.ui.graphics.Color(0xFF795548)
    else -> androidx.compose.ui.graphics.Color(0xFF607D8B)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
private fun FileDetailBottomSheet(
    file: FileNode,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // File icon and name
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (file) {
                    is FileNode.File -> getFileIcon(file.extension)
                    is FileNode.Directory -> if (file.isVirtual) Icons.Default.Android else Icons.Default.Folder
                },
                contentDescription = null,
                tint = when (file) {
                    is FileNode.File -> getFileColor(file.extension)
                    is FileNode.Directory -> if (file.isVirtual) androidx.compose.ui.graphics.Color(0xFF3F51B5) else androidx.compose.ui.graphics.Color(0xFF5C7A99)
                },
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = FileSizeFormatter.format(file.sizeBytes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // File details
        Text(
            text = if (file.isVirtual) (file.virtualLabel ?: file.path) else file.path,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (file is FileNode.File && file.lastModified > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Modified: ${formatDate(file.lastModified)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        if (file.isVirtual) {
            val packageName = FileActions.getPackageNameFromVirtualPath(file.path)
            AppDetailsShortcutCard(
                summary = "Protected app-storage summary. You can drill into this item from the list, but it remains read-only.",
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
                // Open button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            FileActions.openFile(context, file.path)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Share button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            FileActions.shareFile(context, file.path)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Delete button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            FileActions.deleteFile(context, file.path)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
