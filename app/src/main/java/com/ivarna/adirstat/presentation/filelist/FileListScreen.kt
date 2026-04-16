package com.ivarna.adirstat.presentation.filelist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val selectedPaths = remember { mutableStateListOf<String>() }
    var showBottomSheet by remember { mutableStateOf(false) }
    val isSelectionMode = selectedPaths.isNotEmpty()
    
    LaunchedEffect(volumePath, rootPath) {
        viewModel.loadFiles(volumePath, rootPath)
    }

    BackHandler(enabled = isSelectionMode || uiState.navigationStack.isNotEmpty()) {
        if (isSelectionMode) {
            selectedPaths.clear()
            return@BackHandler
        }
        if (!viewModel.navigateBack()) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            FileListTopBar(
                title = if (isSelectionMode) "${selectedPaths.size} selected" else (uiState.currentDirectory?.name ?: "Files"),
                isAppNode = uiState.currentDirectory?.isAppNode == true,
                isSelectionMode = isSelectionMode,
                onBack = {
                    if (isSelectionMode) selectedPaths.clear()
                    else if (!viewModel.navigateBack()) onNavigateBack()
                },
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
            // Filter Chips
            FilterChipsSection(viewModel, uiState)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sort Bar
            SortBar(uiState.sortOption)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // List Area
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> LoadingIndicator()
                    uiState.files.isEmpty() -> EmptyFilesState()
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.files.sortedByDescending { it.sizeBytes }) { file ->
                                FileListItem(
                                    file = file,
                                    isSelected = selectedPaths.contains(file.path),
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (selectedPaths.contains(file.path)) selectedPaths.remove(file.path)
                                            else selectedPaths.add(file.path)
                                        } else {
                                            when (file) {
                                                is FileNode.Directory -> viewModel.navigateInto(file)
                                                is FileNode.File -> { selectedNode = file; showBottomSheet = true }
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        if (selectedPaths.contains(file.path)) selectedPaths.remove(file.path)
                                        else selectedPaths.add(file.path)
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
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
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
    isAppNode: Boolean,
    isSelectionMode: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAppNode) {
                    Icon(Icons.Default.Android, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        actions = {
            if (!isSelectionMode) {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun FilterChipsSection(viewModel: FileListViewModel, uiState: FileListUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill("All", isActive = uiState.activeFilters.isEmpty(), onClick = { viewModel.setFilter(FilterOption.ALL) })
        FilterPillWithColor("Images", SemanticColors.Images, isActive = uiState.activeFilters.contains(FilterOption.IMAGES), onClick = { viewModel.setFilter(FilterOption.IMAGES) })
        FilterPillWithColor("Videos", SemanticColors.Videos, isActive = uiState.activeFilters.contains(FilterOption.VIDEOS), onClick = { viewModel.setFilter(FilterOption.VIDEOS) })
        FilterPillWithColor("Audio", SemanticColors.Audio, isActive = uiState.activeFilters.contains(FilterOption.AUDIO), onClick = { viewModel.setFilter(FilterOption.AUDIO) })
        FilterPillWithColor("Docs", SemanticColors.Documents, isActive = uiState.activeFilters.contains(FilterOption.DOCUMENTS), onClick = { viewModel.setFilter(FilterOption.DOCUMENTS) })
        FilterPillWithColor("APKs", SemanticColors.Apk, isActive = uiState.activeFilters.contains(FilterOption.APK), onClick = { viewModel.setFilter(FilterOption.APK) })
    }
}

@Composable
private fun FilterPill(label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = CircleShape,
        modifier = Modifier.clickable(onClick = onClick),
        shadowElevation = if (isActive) 4.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilterPillWithColor(label: String, color: Color, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = CircleShape,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SortBar(sortOption: SortOption) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "SORTED BY: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = sortOption.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            file is FileNode.Directory -> Icons.Default.Folder
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = when {
                            file is FileNode.Directory -> Color(0xFF607D8B)
                            else -> getFileColor((file as? FileNode.File)?.extension)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = FileSizeFormatter.format(file.sizeBytes),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceGrotesk),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getFileColor(extension: String?) = when (extension?.lowercase()) {
    in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic") -> SemanticColors.Images
    in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv") -> SemanticColors.Videos
    in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> SemanticColors.Audio
    in listOf("pdf", "doc", "docx", "txt", "rtf", "odt") -> SemanticColors.Documents
    in listOf("apk") -> SemanticColors.Apk
    else -> SemanticColors.SystemOther
}

@Composable
private fun FileListBottomBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.Default.GridView, "Dashboard", false)
                BottomNavItem(Icons.Default.Apps, "Apps", false)
                BottomNavItem(Icons.Default.History, "History", false)
                BottomNavItem(Icons.Default.Settings, "Settings", false)
            }
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, isActive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isActive) Modifier.background(MaterialTheme.colorScheme.primary)
                else Modifier.clickable { }
            )
            .padding(vertical = 10.dp, horizontal = 18.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color.White else Color(0xFF607D8B),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (isActive) Color.White else Color(0xFF607D8B),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyFilesState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No files found", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
