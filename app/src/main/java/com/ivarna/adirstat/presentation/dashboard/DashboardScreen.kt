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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                title = { Text("Adirstat") },
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
                        progress = uiState.scanProgress,
                        onCancel = { }
                    )
                }
                else -> {
                    StorageVolumesContent(
                        volumes = uiState.storageVolumes,
                        onScan = { path -> viewModel.startScan(path) },
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
    volumes: List<StorageVolumeUi>,
    onScan: (String) -> Unit,
    onVolumeClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Storage Volumes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(volumes) { volume ->
            StorageVolumeCard(
                volume = volume,
                onScan = { onScan(volume.path) },
                onClick = { onVolumeClick(volume.path) }
            )
        }
        
        // Empty state when no external storage
        if (volumes.size <= 1) {
            item {
                EmptyStorageState()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageVolumeCard(
    volume: StorageVolumeUi,
    onScan: () -> Unit,
    onClick: () -> Unit
) {
    val usedPercentage = if (volume.totalBytes > 0) {
        (volume.usedBytes.toFloat() / volume.totalBytes.toFloat()) * 100
    } else 0f
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
            
            // Storage bar - improved visibility
            StorageBar(
                usedBytes = volume.usedBytes,
                freeBytes = volume.freeBytes,
                totalBytes = volume.totalBytes,
                usedPercentage = usedPercentage
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Labels below bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used ${FileSizeFormatter.format(volume.usedBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Free ${FileSizeFormatter.format(volume.freeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total ${FileSizeFormatter.format(volume.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Last scanned time
            if (volume.lastScanTime != null) {
                Text(
                    text = "Last scanned: ${formatTimestamp(volume.lastScanTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Scan button
            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (volume.lastScanTime != null) "Rescan" else "Scan Now")
            }
        }
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
