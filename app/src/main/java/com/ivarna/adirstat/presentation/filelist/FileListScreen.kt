package com.ivarna.adirstat.presentation.filelist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.presentation.common.components.AdirstatTopBar
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileActions
import com.ivarna.adirstat.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    volumePath: String,
    rootPath: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: ((String, String?) -> Unit)? = null,
    viewModel: FileListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedNode by remember { mutableStateOf<FileNode?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    
    val selectedPaths = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedPaths.isNotEmpty()

    LaunchedEffect(volumePath, rootPath) {
        viewModel.loadFiles(volumePath, rootPath)
    }

    BackHandler(enabled = isSelectionMode || uiState.navigationStack.isNotEmpty()) {
        if (isSelectionMode) selectedPaths.clear()
        else if (!viewModel.navigateBack()) onNavigateBack()
    }

    Scaffold(
        topBar = {
            FileListTopBar(
                title = if (isSelectionMode) "${selectedPaths.size} selected" else (uiState.currentDirectory?.name ?: "Files"),
                isSelectionMode = isSelectionMode,
                onSearch = {
                    onNavigateToSearch?.invoke(
                        rootPath ?: volumePath,
                        uiState.currentDirectory?.path ?: volumePath
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterChipsSection(viewModel, uiState)
            
            SortBar(uiState.sortOption)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> LoadingIndicator()
                    uiState.files.isEmpty() -> EmptyFilesState()
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.files) { file ->
                                FileListItem(
                                    file = file,
                                    isSelected = selectedPaths.contains(file.path),
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (selectedPaths.contains(file.path)) selectedPaths.remove(file.path)
                                            else selectedPaths.add(file.path)
                                        } else {
                                            when (file) {
                                                is FileNode.Directory -> viewModel.loadFiles(volumePath, file.path)
                                                is FileNode.File -> { selectedNode = file; showBottomSheet = true }
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        if (!selectedPaths.contains(file.path)) selectedPaths.add(file.path)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet && selectedNode != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                com.ivarna.adirstat.presentation.common.components.FileDetailsBottomSheet(
                    file = selectedNode!!,
                    onOpen = { path -> FileActions.openFile(context, path) },
                    onShare = { path -> FileActions.shareFile(context, path) },
                    onDelete = { path -> FileActions.deleteFile(context, path); showBottomSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileListTopBar(
    title: String,
    isSelectionMode: Boolean,
    onSearch: () -> Unit
) {
    AdirstatTopBar(
        title = title,
        actions = {
            if (!isSelectionMode) {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}

@Composable
private fun FilterChipsSection(viewModel: FileListViewModel, uiState: FileListUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPillMinimal("All", isActive = uiState.activeFilters.isEmpty(), onClick = { viewModel.setFilter(FilterOption.ALL) })
        FilterPillMinimal("Images", isActive = uiState.activeFilters.contains(FilterOption.IMAGES), onClick = { viewModel.setFilter(FilterOption.IMAGES) })
        FilterPillMinimal("Videos", isActive = uiState.activeFilters.contains(FilterOption.VIDEOS), onClick = { viewModel.setFilter(FilterOption.VIDEOS) })
        FilterPillMinimal("Audio", isActive = uiState.activeFilters.contains(FilterOption.AUDIO), onClick = { viewModel.setFilter(FilterOption.AUDIO) })
        FilterPillMinimal("Docs", isActive = uiState.activeFilters.contains(FilterOption.DOCUMENTS), onClick = { viewModel.setFilter(FilterOption.DOCUMENTS) })
    }
}

@Composable
private fun FilterPillMinimal(label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp, 
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SortBar(sortOption: SortOption) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Sort, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Sorted by ${sortOption.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    file: FileNode,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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
                            file is FileNode.Directory -> Icons.Default.Folder
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = when {
                            file is FileNode.Directory -> MaterialTheme.colorScheme.primary
                            else -> getFileColor((file as? FileNode.File)?.extension)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = FileSizeFormatter.format(file.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
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

@Composable
private fun getFileColor(extension: String?) = when (extension?.lowercase()) {
    in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic") -> SemanticColors.Images
    in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv") -> SemanticColors.Videos
    in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> SemanticColors.Audio
    in listOf("pdf", "doc", "docx", "txt", "rtf", "odt") -> SemanticColors.Documents
    in listOf("apk") -> SemanticColors.Apk
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyFilesState() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Text("No files in this directory", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
