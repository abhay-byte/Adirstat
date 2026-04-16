package com.ivarna.adirstat.presentation.search

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
    val context = LocalContext.current

    LaunchedEffect(rootPath, scopePath) {
        viewModel.refreshIndex(rootPath, scopePath)
    }
    
    Scaffold(
        topBar = {
            SearchTopBar(onBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search Input Section
            SearchInputSection(
                query = uiState.query,
                onQueryChange = { viewModel.search(it) },
                useRegex = uiState.useRegex,
                useWildcard = uiState.useWildcard,
                onToggleRegex = { viewModel.toggleRegex() },
                onToggleWildcard = { viewModel.toggleWildcard() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Filter Chips
            SearchFilterChips()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Results List
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isSearching -> LoadingIndicator()
                    uiState.results.isEmpty() && uiState.query.isNotEmpty() -> NoResultsState(uiState.isLoading)
                    uiState.query.isEmpty() -> EmptySearchState(uiState.isLoading, uiState.hasIndexedFiles)
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SEARCH RESULTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = "${uiState.results.size} items found",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            items(uiState.results) { file ->
                                SearchResultItem(
                                    file = file,
                                    onClick = {
                                        when (file) {
                                            is FileNode.Directory -> onNavigateToDirectory(file.path, uiState.activeRootPath)
                                            is FileNode.File -> { selectedNode = file; showBottomSheet = true }
                                        }
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
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                FileDetailsContent(
                    file = selectedNode!!,
                    onDismiss = { showBottomSheet = false },
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
private fun SearchTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Search Files",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
    )
}

@Composable
private fun SearchInputSection(
    query: String,
    onQueryChange: (String) -> Unit,
    useRegex: Boolean,
    useWildcard: Boolean,
    onToggleRegex: () -> Unit,
    onToggleWildcard: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search files and folders...", color = MaterialTheme.colorScheme.outline) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.outline) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip(label = "Regex", icon = Icons.Default.Code, isActive = useRegex, onClick = onToggleRegex)
            ToggleChip(label = "Wildcard", icon = Icons.Default.Star, isActive = useWildcard, onClick = onToggleWildcard)
        }
    }
}

@Composable
private fun ToggleChip(label: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchFilterChips() {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill("All", isActive = true)
        FilterPillWithColor("Images", SemanticColors.Images, isActive = false)
        FilterPillWithColor("Videos", SemanticColors.Videos, isActive = false)
        FilterPillWithColor("Audio", SemanticColors.Audio, isActive = false)
        FilterPillWithColor("Docs", SemanticColors.Documents, isActive = false)
        FilterPillWithColor("APKs", SemanticColors.Apk, isActive = false)
    }
}

@Composable
private fun FilterPill(label: String, isActive: Boolean) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = CircleShape,
        shadowElevation = if (isActive) 4.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilterPillWithColor(label: String, color: Color, isActive: Boolean) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Circle, // Simple dot
                contentDescription = null,
                modifier = Modifier.size(8.dp),
                tint = color
            )
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
private fun SearchResultItem(file: FileNode, onClick: () -> Unit) {
    val isLow = file.path.length % 2 == 0 // Just to alternate backgrounds as in design
    
    Surface(
        color = if (isLow) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
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
                        }
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
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (file is FileNode.Directory) "FOLDER" else FileSizeFormatter.format(file.sizeBytes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (file is FileNode.Directory) "DIRECTORY" else ((file as? FileNode.File)?.extension?.uppercase() ?: "FILE"),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
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
private fun SearchBottomBar() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(88.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().blur(16.dp).background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Dashboard, "Overview", false)
            BottomNavItem(Icons.Default.FolderOpen, "Files", false)
            BottomNavItem(Icons.Default.Search, "Search", true)
            BottomNavItem(Icons.Default.AutoFixHigh, "Clean", false)
            BottomNavItem(Icons.Default.VerifiedUser, "Secure", false)
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, isActive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Icon(
            icon,
            null,
            tint = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF607D8B),
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF607D8B),
            fontWeight = FontWeight.Bold
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
private fun NoResultsState(isLoading: Boolean) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Text(if (isLoading) "Loading scan data..." else "No matches found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptySearchState(isLoading: Boolean, hasIndexedFiles: Boolean) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(if (isLoading) Icons.Default.HourglassEmpty else Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Text(
            text = when {
                isLoading -> "Loading scan data..."
                !hasIndexedFiles -> "No scanned data available"
                else -> "Search this scan"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
