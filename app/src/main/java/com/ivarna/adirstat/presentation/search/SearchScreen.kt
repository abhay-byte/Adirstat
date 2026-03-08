package com.ivarna.adirstat.presentation.search

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    rootPath: String? = null,
    scopePath: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToDirectory: (String, String?) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNode by remember { mutableStateOf<FileNode?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshIndex(rootPath, scopePath)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(rootPath, scopePath) {
        viewModel.refreshIndex(rootPath, scopePath)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.search(it) },
                        placeholder = { Text("Search files and apps...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.surface
                        ),
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.query.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.useRegex,
                    onClick = { viewModel.toggleRegex() },
                    label = { Text("Regex") }
                )
                FilterChip(
                    selected = uiState.useWildcard,
                    onClick = { viewModel.toggleWildcard() },
                    label = { Text("Wildcard") }
                )
            }
            
            when {
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.results.isEmpty() && uiState.query.isNotEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.isLoading) "Loading scan data..." else "No matches found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!uiState.isLoading && uiState.query.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try a different search term",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                uiState.query.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (uiState.isLoading) Icons.Default.HourglassEmpty else Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when {
                                    uiState.isLoading -> "Loading scan data..."
                                    !uiState.hasIndexedFiles -> "No scanned data available"
                                    else -> "Search this scan"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!uiState.isLoading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (uiState.hasIndexedFiles) {
                                        "Search is limited to the current scanned location."
                                    } else {
                                        "Run a storage scan first, then open search from a scan screen."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${uiState.results.size} results",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(
                            items = uiState.results,
                            key = { it.path }
                        ) { file ->
                            SearchResultItem(
                                file = file,
                                onClick = {
                                    when (file) {
                                        is FileNode.Directory -> onNavigateToDirectory(file.path, uiState.activeRootPath)
                                        is FileNode.File -> {
                                            selectedNode = file
                                            showBottomSheet = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet && selectedNode != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                SearchResultBottomSheet(
                    node = selectedNode!!,
                    onDismiss = { showBottomSheet = false },
                    onBrowse = { path ->
                        showBottomSheet = false
                        onNavigateToDirectory(path, uiState.activeRootPath)
                    },
                    onOpen = { path ->
                        FileActions.openFile(context, path)
                        showBottomSheet = false
                    },
                    onShare = { path ->
                        FileActions.shareFile(context, path)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    file: FileNode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    is FileNode.Directory -> Icons.Default.Folder
                },
                contentDescription = null,
                tint = when (file) {
                    is FileNode.File -> MaterialTheme.colorScheme.primary
                    is FileNode.Directory -> MaterialTheme.colorScheme.tertiary
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
                if (file is FileNode.File) {
                    Text(
                        text = file.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                } else if (file.isVirtual) {
                    Text(
                        text = file.virtualLabel ?: file.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                } else {
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
                    text = FileSizeFormatter.format(file.size),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (file is FileNode.File && file.lastModified > 0) {
                    Text(
                        text = formatDate(file.lastModified),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultBottomSheet(
    node: FileNode,
    onDismiss: () -> Unit,
    onBrowse: (String) -> Unit,
    onOpen: (String) -> Unit,
    onShare: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = node.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = FileSizeFormatter.format(node.sizeBytes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = node.virtualLabel ?: node.path,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (node) {
            is FileNode.Directory -> {
                Button(
                    onClick = { onBrowse(node.path) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse here")
                }
            }
            is FileNode.File -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onBrowse(node.path.substringBeforeLast('/', "")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Show folder")
                    }
                    Button(
                        onClick = { onOpen(node.path) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open")
                    }
                }
                OutlinedButton(
                    onClick = { onShare(node.path) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Close")
        }
    }
}

private fun getFileIcon(extension: String?) = when (extension?.lowercase()) {
    in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> Icons.Default.Image
    in listOf("mp4", "mkv", "avi", "mov") -> Icons.Default.VideoFile
    in listOf("mp3", "wav", "ogg", "flac") -> Icons.Default.AudioFile
    in listOf("pdf", "doc", "docx", "txt") -> Icons.Default.Description
    in listOf("zip", "rar", "7z") -> Icons.Default.Archive
    in listOf("apk") -> Icons.Default.Android
    else -> Icons.Default.InsertDriveFile
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
