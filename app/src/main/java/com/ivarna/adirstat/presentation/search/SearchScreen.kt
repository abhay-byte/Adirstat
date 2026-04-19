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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                
                SearchInputSection(
                    query = uiState.query,
                    onQueryChange = { viewModel.search(it) },
                    useRegex = uiState.useRegex,
                    useWildcard = uiState.useWildcard,
                    onToggleRegex = { viewModel.toggleRegex() },
                    onToggleWildcard = { viewModel.toggleWildcard() }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                SearchFilterChips()
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isSearching -> LoadingIndicator()
                    uiState.results.isEmpty() && uiState.query.isNotEmpty() -> NoResultsState(uiState.isLoading)
                    uiState.query.isEmpty() -> EmptySearchState(uiState.isLoading, uiState.hasIndexedFiles)
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SEARCH RESULTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${uiState.results.size} items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun SearchTopBar(onBack: () -> Unit) {
    AdirstatTopBar(
        title = "Search Files"
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search files and folders...", color = MaterialTheme.colorScheme.outline) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
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
        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp, 
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label, 
                style = MaterialTheme.typography.labelMedium, 
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SearchFilterChips() {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipMinimal("All", isActive = true)
        FilterChipMinimal("Images", isActive = false)
        FilterChipMinimal("Videos", isActive = false)
        FilterChipMinimal("Audio", isActive = false)
        FilterChipMinimal("Docs", isActive = false)
    }
}

@Composable
private fun FilterChipMinimal(label: String, isActive: Boolean) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp, 
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
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
private fun SearchResultItem(file: FileNode, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(20.dp))
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
