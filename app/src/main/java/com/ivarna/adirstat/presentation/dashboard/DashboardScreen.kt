package com.ivarna.adirstat.presentation.dashboard

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ivarna.adirstat.data.source.StorageCategories
import com.ivarna.adirstat.presentation.common.components.AdirstatTopBar
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTreemap: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            AdirstatTopBar(
                title = "Adirstat"
            )
        },
        floatingActionButton = {
            if (uiState.permissionState.hasManageExternalStorage && uiState.storageVolumes.isNotEmpty()) {
                DashboardFAB(onClick = {
                    uiState.storageVolumes.firstOrNull()?.let { onNavigateToTreemap(it.path) }
                })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !uiState.permissionState.hasManageExternalStorage -> {
                    PermissionRequestState(onRequestPermission = {
                        try {
                            context.startActivity(viewModel.getPermissionIntent())
                        } catch (e: Exception) {}
                    })
                }
                uiState.storageVolumes.isEmpty() -> {
                    LoadingState()
                }
                else -> {
                    DashboardContent(
                        volumes = uiState.storageVolumes,
                        onVolumeClick = onNavigateToTreemap
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestState(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Storage Access Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Adirstat needs permission to scan your storage and visualize large files.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("GRANT PERMISSION", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DashboardContent(
    volumes: List<DashboardViewModel.StorageVolumeInfo>,
    onVolumeClick: (String) -> Unit
) {
    val primaryVolume = volumes.firstOrNull { it.path == "/storage/emulated/0" } ?: volumes.firstOrNull()
    val otherVolumes = volumes.filter { it != primaryVolume }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        primaryVolume?.let { volume ->
            item {
                InternalStorageSpotlightCard(
                    volume = volume,
                    onClick = { onVolumeClick(volume.path) }
                )
            }

            item {
                StorageBreakdownSection(volume = volume)
            }
        }

        if (otherVolumes.isNotEmpty()) {
            item {
                Text(
                    text = "External Storage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            items(otherVolumes) { volume ->
                OtherPartitionCard(
                    volume = volume,
                    onScanClick = { onVolumeClick(volume.path) }
                )
            }
        }
    }
}

@Composable
private fun InternalStorageSpotlightCard(
    volume: DashboardViewModel.StorageVolumeInfo,
    onClick: () -> Unit
) {
    val usedGB = String.format("%.1f", volume.usedBytes.toDouble() / (1024 * 1024 * 1024))
    val totalGB = (volume.totalBytes / (1024 * 1024 * 1024)).toString()
    val freeGB = String.format("%.1f", volume.freeBytes.toDouble() / (1024 * 1024 * 1024))
    val usedPercent = if (volume.totalBytes > 0) (volume.usedBytes * 100 / volume.totalBytes).toInt() else 0

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "INTERNAL STORAGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Device Partition",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storage, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = usedGB,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "GB",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$usedPercent%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Used",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { usedPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "$totalGB GB Total",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$freeGB GB Free",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageBreakdownSection(volume: DashboardViewModel.StorageVolumeInfo) {
    val categories = volume.storageCategories ?: StorageCategories(
        appsBytes = 0L, appDataBytes = 0L, cacheBytes = 0L, filesBytes = 0L,
        mediaBytes = 0L, imageBytes = 0L, videoBytes = 0L, audioBytes = 0L,
        systemBytes = 0L, freeBytes = volume.freeBytes, totalBytes = volume.totalBytes, usedBytes = volume.usedBytes
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "STORAGE BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            val items = listOf(
                BreakdownItemData("Apps", FileSizeFormatter.format(categories.appsBytes + categories.appDataBytes + categories.cacheBytes), MaterialTheme.colorScheme.primary),
                BreakdownItemData("Images", FileSizeFormatter.format(categories.imageBytes), SemanticColors.Images),
                BreakdownItemData("Videos", FileSizeFormatter.format(categories.videoBytes), SemanticColors.Videos),
                BreakdownItemData("Audio", FileSizeFormatter.format(categories.audioBytes), SemanticColors.Audio),
                BreakdownItemData("Documents", FileSizeFormatter.format(categories.filesBytes), SemanticColors.Documents),
                BreakdownItemData("System", FileSizeFormatter.format(categories.systemBytes), SemanticColors.SystemOther),
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BreakdownCard(items[0], modifier = Modifier.weight(1f))
                    BreakdownCard(items[1], modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BreakdownCard(items[2], modifier = Modifier.weight(1f))
                    BreakdownCard(items[3], modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BreakdownCard(items[4], modifier = Modifier.weight(1f))
                    BreakdownCard(items[5], modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class BreakdownItemData(val label: String, val value: String, val color: Color)

@Composable
private fun BreakdownCard(data: BreakdownItemData, modifier: Modifier = Modifier) {
    Surface(
        color = data.color.copy(alpha = 0.04f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, data.color.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(data.color, RoundedCornerShape(2.dp)))
                Text(
                    text = data.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = data.value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OtherPartitionCard(
    volume: DashboardViewModel.StorageVolumeInfo,
    onScanClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (volume.name.contains("SD", true)) Icons.Default.SdCard else Icons.Default.Usb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = volume.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (volume.lastScanTime != null) "Last: ${formatTimestamp(volume.lastScanTime)}" else "Never scanned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(
                onClick = onScanClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DashboardFAB(onClick: () -> Unit) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Analytics, null, modifier = Modifier.size(28.dp))
            Text(
                text = "START SCAN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
