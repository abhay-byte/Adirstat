package com.ivarna.adirstat.presentation.settings

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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFileSizeDialog by remember { mutableStateOf(false) }
    var showExcludedPathsDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showScanHistoryDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.cacheCleared) {
        if (uiState.cacheCleared) {
            scope.launch { snackbarHostState.showSnackbar("Scan cache cleared") }
            viewModel.dismissCacheCleared()
        }
    }

    LaunchedEffect(uiState.exportSuccess, uiState.exportError) {
        when {
            uiState.exportSuccess -> {
                scope.launch { snackbarHostState.showSnackbar("Exported to Downloads folder") }
                viewModel.dismissExportResult()
            }
            uiState.exportError != null -> {
                scope.launch { snackbarHostState.showSnackbar("Export failed: ${uiState.exportError}") }
                viewModel.dismissExportResult()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = uiState.theme.displayName,
                    onClick = { showThemeDialog = true }
                )
            }
            
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Scanning",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FolderOff,
                    title = "Excluded Paths",
                    subtitle = "${uiState.excludedPaths.size} paths excluded",
                    onClick = { showExcludedPathsDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FilterList,
                    title = "Minimum File Size",
                    subtitle = uiState.minimumFileSize.displayName,
                    onClick = { showFileSizeDialog = true }
                )
            }
            
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear Scan Cache",
                    subtitle = "Free up storage space",
                    onClick = { showClearCacheDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Scan History",
                    subtitle = if (uiState.scanHistory.isEmpty()) "No scans yet" else "${uiState.scanHistory.size} scan(s) cached",
                    onClick = { showScanHistoryDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export Data",
                    subtitle = if (uiState.isExporting) "Exporting…" else "Export scan results to CSV",
                    onClick = { if (!uiState.isExporting) viewModel.exportToCsv() }
                )
            }
            
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.2",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { }
                )
            }
        }
    }
    
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeOption.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.theme == theme,
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showFileSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFileSizeDialog = false },
            title = { Text("Minimum File Size") },
            text = {
                Column {
                    MinimumFileSize.entries.forEach { size ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setMinimumFileSize(size)
                                    showFileSizeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.minimumFileSize == size,
                                onClick = {
                                    viewModel.setMinimumFileSize(size)
                                    showFileSizeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(size.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFileSizeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showExcludedPathsDialog) {
        var newPath by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showExcludedPathsDialog = false },
            title = { Text("Excluded Paths") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.excludedPaths.isEmpty()) {
                        Text(
                            "No paths excluded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.excludedPaths.forEach { path ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = path,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                IconButton(onClick = { viewModel.removeExclusion(path) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPath,
                            onValueChange = { newPath = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("/sdcard/folder") },
                            singleLine = true,
                            label = { Text("Add path") }
                        )
                        IconButton(
                            onClick = {
                                if (newPath.isNotBlank()) {
                                    viewModel.addExclusion(newPath.trim())
                                    newPath = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExcludedPathsDialog = false }) { Text("Done") }
            }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("This will delete all cached scan results. You'll need to re-scan to view storage analysis.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCache()
                        showClearCacheDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showScanHistoryDialog) {
        val dateFmt = remember { SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { showScanHistoryDialog = false },
            title = { Text("Scan History") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.scanHistory.isEmpty()) {
                        Text(
                            "No scans recorded yet. Run a scan from the dashboard to see history here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.scanHistory.forEach { entry ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = entry.partitionPath,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${formatSize(entry.totalSize)}  •  ${entry.fileCount} files",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFmt.format(Date(entry.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScanHistoryDialog = false }) { Text("Close") }
            }
        )
    }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.1f GB".format(gb)
        mb >= 1.0 -> "%.1f MB".format(mb)
        kb >= 1.0 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
