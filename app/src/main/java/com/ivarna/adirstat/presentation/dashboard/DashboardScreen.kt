package com.ivarna.adirstat.presentation.dashboard

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ivarna.adirstat.R
import com.ivarna.adirstat.data.source.StorageCategories
import com.ivarna.adirstat.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTreemap: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Reload data on resume - critical for updating after permissions granted
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permissions after returning from settings
        viewModel.checkPermissions()
    }
    
    // Navigate to treemap after scan completes
    LaunchedEffect(uiState.scannedVolumePath) {
        uiState.scannedVolumePath?.let { path ->
            onNavigateToTreemap(path)
            viewModel.onNavigatedToTreemap()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground_image),
                        contentDescription = "Adirstat",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.permissionState.hasManageExternalStorage && !uiState.isScanning && uiState.storageVolumes.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { 
                        uiState.storageVolumes.firstOrNull()?.let { viewModel.startScan(it.path) }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    text = { Text("Scan Storage") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else if (uiState.isScanning) {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: Cancel scan */ },
                    icon = { Icon(Icons.Default.Close, contentDescription = null) },
                    text = { Text("Cancel Scan") },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                !uiState.permissionState.hasManageExternalStorage -> {
                    PermissionRequestContent(
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                permissionLauncher.launch(intent)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = android.net.Uri.parse("package:${context.packageName}")
                                permissionLauncher.launch(intent)
                            }
                        }
                    )
                }
                uiState.isScanning -> {
                    ScanProgressContent(
                        progress = (uiState.scanProgress * 100).toInt().toString() + "%",
                        onCancel = { }
                    )
                }
                else -> {
                    StorageVolumesContent(
                        volumes = uiState.storageVolumes,
                        onVolumeClick = onNavigateToTreemap
                    )
                }
            }
            
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Storage Access Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Adirstat needs access to your storage to analyze files and folders. Tap the button below to grant access in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Settings")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your data stays on your device. We never upload anything.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScanProgressContent(
    progress: String,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            strokeWidth = 8.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Scanning...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = progress,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@Composable
private fun StorageVolumesContent(
    volumes: List<DashboardViewModel.StorageVolumeInfo>,
    onVolumeClick: (String) -> Unit
) {
    val primaryVolume = volumes.firstOrNull { it.path == "/storage/emulated/0" } ?: volumes.firstOrNull()
    val secondaryVolumes = volumes.filter { it != primaryVolume }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        primaryVolume?.let { volume ->
            item {
                SectionHeader(
                    title = "Internal Storage",
                    subtitle = "Your main phone storage with scan status, space usage, and quick breakdown."
                )
            }

            item {
                InternalStorageSpotlightCard(
                    volume = volume,
                    onClick = { onVolumeClick(volume.path) }
                )
            }
        }

        if (secondaryVolumes.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Other Volumes",
                    subtitle = "SD cards and removable storage appear here when available."
                )
            }
        }

        items(secondaryVolumes) { volume ->
            StorageVolumeCard(
                volume = volume,
                onClick = { onVolumeClick(volume.path) }
            )
        }

        // Empty state when no external storage
        if (secondaryVolumes.isEmpty()) {
            item {
                EmptyStorageState()
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternalStorageSpotlightCard(
    volume: DashboardViewModel.StorageVolumeInfo,
    onClick: () -> Unit
) {
    val categories = volume.storageCategories ?: StorageCategories(
        appsBytes = 0L,
        appDataBytes = 0L,
        cacheBytes = 0L,
        filesBytes = 0L,
        mediaBytes = 0L,
        imageBytes = 0L,
        videoBytes = 0L,
        audioBytes = 0L,
        systemBytes = 0L,
        freeBytes = volume.freeBytes,
        totalBytes = volume.totalBytes,
        usedBytes = volume.usedBytes
    )

    Card(
        onClick = {
            if (!volume.neverScanned) {
                onClick()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SdCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = volume.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Main device storage · ${FileSizeFormatter.format(volume.totalBytes)} total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ScanStalenessIndicator(lastScanTime = volume.lastScanTime)
            }

            StorageHeadlineStats(volume = volume)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageSummaryPill(
                    modifier = Modifier.weight(1f),
                    label = "Apps",
                    value = FileSizeFormatter.format(categories.appsBytes + categories.appDataBytes + categories.cacheBytes)
                )
                StorageSummaryPill(
                    modifier = Modifier.weight(1f),
                    label = "Media",
                    value = FileSizeFormatter.format(categories.mediaBytes)
                )
                StorageSummaryPill(
                    modifier = Modifier.weight(1f),
                    label = "Files",
                    value = FileSizeFormatter.format(categories.filesBytes)
                )
            }

            MultiSegmentStorageBar(categories = categories)
            StorageBreakdownLegend(categories = categories)

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (volume.lastScanTime != null) {
                            "Last scanned: ${formatTimestamp(volume.lastScanTime)}"
                        } else {
                            "No scan yet. Use the Scan Storage button below to create the first map."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (volume.neverScanned) {
                            "Once scanned, this section opens the treemap and list views for deeper browsing."
                        } else {
                            "Tap anywhere on this section to open the storage map and browse inside folders or apps."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageSummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageVolumeCard(
    volume: DashboardViewModel.StorageVolumeInfo,
    onClick: () -> Unit,
    highlighted: Boolean = false
) {
    val categories = volume.storageCategories ?: StorageCategories(
        appsBytes = 0L,
        appDataBytes = 0L,
        cacheBytes = 0L,
        filesBytes = 0L,
        mediaBytes = 0L,
        imageBytes = 0L,
        videoBytes = 0L,
        audioBytes = 0L,
        systemBytes = 0L,
        freeBytes = volume.freeBytes,
        totalBytes = volume.totalBytes,
        usedBytes = volume.usedBytes
    )
    
    Card(
        onClick = {
            if (!volume.neverScanned) {
                onClick()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (highlighted) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = volume.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = volume.path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Scan status indicator
                ScanStalenessIndicator(lastScanTime = volume.lastScanTime)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            StorageHeadlineStats(volume = volume)

            Spacer(modifier = Modifier.height(14.dp))
            
            // Multi-segment storage bar - shows Apps, Media, Other breakdown
            MultiSegmentStorageBar(
                categories = categories
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Storage breakdown legend
            StorageBreakdownLegend(
                categories = categories
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Last scanned time
            if (volume.lastScanTime != null) {
                Text(
                    text = "Last scanned: ${formatTimestamp(volume.lastScanTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Use the Scan Storage button to create the first scan.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!volume.neverScanned) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap this card to open the storage map.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StorageHeadlineStats(
    volume: DashboardViewModel.StorageVolumeInfo
) {
    val usedPercent = if (volume.totalBytes > 0L) {
        ((volume.usedBytes.toDouble() / volume.totalBytes.toDouble()) * 100).toInt()
    } else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StorageStatChip(
            modifier = Modifier.weight(1f),
            label = "Used",
            value = "${FileSizeFormatter.format(volume.usedBytes)} · $usedPercent%"
        )
        StorageStatChip(
            modifier = Modifier.weight(1f),
            label = "Free",
            value = FileSizeFormatter.format(volume.freeBytes)
        )
    }
}

@Composable
private fun StorageStatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MultiSegmentStorageBar(
    categories: StorageCategories
) {
    val totalBytes = categories.totalBytes
    if (totalBytes <= 0) return
    val freeColor = Color(0xFF9E9E9E)
    val freeBytes = categories.freeBytes.coerceAtLeast(0L)
    val segments = listOf(
        Triple("Apps", categories.appsBytes, Color(0xFF2196F3)),
        Triple("Data", categories.appDataBytes, Color(0xFF4CAF50)),
        Triple("Cache", categories.cacheBytes, Color(0xFFFF9800)),
        Triple("Images", categories.imageBytes, Color(0xFFE91E63)),
        Triple("Video", categories.videoBytes, Color(0xFFF44336)),
        Triple("Audio", categories.audioBytes, Color(0xFF9C27B0)),
        Triple("Files", categories.filesBytes, Color(0xFF00BCD4)),
        Triple("System", categories.systemBytes, Color(0xFF607D8B))
    ).filter { (_, bytes, _) -> bytes > 0L }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            segments.forEach { (_, bytes, color) ->
                Box(
                    modifier = Modifier
                        .weight((bytes.toFloat() / totalBytes.toFloat()).coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(color)
                )
            }
            if (freeBytes > 0L) {
                Box(
                    modifier = Modifier
                        .weight((freeBytes.toFloat() / totalBytes.toFloat()).coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(freeColor.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun StorageBreakdownLegend(
    categories: StorageCategories
) {
    val legendItems = listOf(
        Triple("Apps", categories.appsBytes + categories.appDataBytes + categories.cacheBytes, Color(0xFF2196F3)),
        Triple("Media", categories.mediaBytes, Color(0xFFE91E63)),
        Triple("Files", categories.filesBytes, Color(0xFF00BCD4)),
        Triple("System", categories.systemBytes, Color(0xFF607D8B)),
        Triple("Free", categories.freeBytes, Color(0xFF9E9E9E))
    ).filter { (_, size, _) -> size > 0L }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        legendItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { (label, size, color) ->
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        LegendItem(color = color, label = label, size = size)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    size: Long
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = "$label ${FileSizeFormatter.format(size)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StorageBar(
    usedBytes: Long,
    freeBytes: Long,
    totalBytes: Long,
    usedPercentage: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Segmented bar with border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(outline)
        ) {
            // Used portion
            Box(
                modifier = Modifier
                    .fillMaxWidth(usedPercentage / 100f)
                    .fillMaxHeight()
                    .background(primaryColor)
            )
            
            // Border overlay (already handled by background)
        }
    }
}

@Composable
private fun ScanStalenessIndicator(lastScanTime: Long?) {
    val now = System.currentTimeMillis()
    val oneHourMs = 60 * 60 * 1000L
    val oneDayMs = 24 * oneHourMs
    
    val (color, text) = when {
        lastScanTime == null -> MaterialTheme.colorScheme.error to "Never"
        (now - lastScanTime) < oneHourMs -> Color(0xFF4CAF50) to "Recent"
        (now - lastScanTime) < oneDayMs -> Color(0xFFFFEB3B) to "Today"
        else -> MaterialTheme.colorScheme.error to "Stale"
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (lastScanTime == null || (now - lastScanTime) >= oneDayMs) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStorageState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.SdCard,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No external storage detected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Connect an SD card or USB OTG drive to scan it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
