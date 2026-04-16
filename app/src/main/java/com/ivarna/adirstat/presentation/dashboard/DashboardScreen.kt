package com.ivarna.adirstat.presentation.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.util.lerp
import androidx.compose.ui.geometry.Offset
import kotlin.math.ln
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ivarna.adirstat.R
import com.ivarna.adirstat.data.source.StorageCategories
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload data on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissions()
    }

    Scaffold(
        topBar = {
            DashboardTopBar(onSettingsClick = onNavigateToSettings)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingOverlay()
                }
                !uiState.permissionState.hasManageExternalStorage -> {
                    PermissionRequestContent(
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                permissionLauncher.launch(intent)
                            } else {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = android.net.Uri.parse("package:${context.packageName}")
                                permissionLauncher.launch(intent)
                            }
                        }
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Adirstat",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF607D8B),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun DashboardFAB(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 8.dp, bottom = 100.dp) // Offset from bottom bar
            .height(64.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = AmbientShadow
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            Text(
                text = "SCAN STORAGE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun DashboardBottomBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        shadowElevation = 0.dp // Custom shadow via modifier if needed
    ) {
        // Blur effect simulation via alpha + subtle shadow
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.GridView,
                    label = "Dashboard",
                    isActive = true
                )
                BottomNavItem(
                    icon = Icons.Default.Apps,
                    label = "Apps",
                    isActive = false
                )
                BottomNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    isActive = false
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isActive = false
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .then(
                if (isActive) Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 10.dp, horizontal = 18.dp)
                else Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 18.dp)
            )
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
private fun LoadingOverlay() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Loading storage summary…",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PermissionRequestContent(onRequestPermission: () -> Unit) {
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("OPEN SETTINGS")
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
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
                    text = "Other Partitions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(otherVolumes) { volume ->
                OtherPartitionCard(
                    volume = volume,
                    onScanClick = { onVolumeClick(volume.path) }
                )
            }
        }

        // Visual Mosaic Decoration
        item {
            Row(modifier = Modifier.height(128.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp)))
                Box(modifier = Modifier.weight(2f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp)))
            }
        }
    }
}

@Composable
private fun InternalStorageSpotlightCard(
    volume: DashboardViewModel.StorageVolumeInfo,
    onClick: () -> Unit
) {
    val categories = volume.storageCategories ?: StorageCategories(
        appsBytes = 0L, appDataBytes = 0L, cacheBytes = 0L, filesBytes = 0L,
        mediaBytes = 0L, imageBytes = 0L, videoBytes = 0L, audioBytes = 0L,
        systemBytes = 0L, freeBytes = volume.freeBytes, totalBytes = volume.totalBytes, usedBytes = volume.usedBytes
    )
    
    val usedGB = String.format("%.1f", volume.usedBytes.toDouble() / (1024 * 1024 * 1024))
    val totalGB = (volume.totalBytes / (1024 * 1024 * 1024)).toString()
    val freeGB = String.format("%.1f", volume.freeBytes.toDouble() / (1024 * 1024 * 1024))
    val usedPercent = if (volume.totalBytes > 0) (volume.usedBytes * 100 / volume.totalBytes).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = AmbientShadow)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Primary, PrimaryContainer),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .clickable(onClick = onClick)
            .padding(32.dp)
    ) {
        // Decorative blur circle simulation
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 64.dp, y = (-64).dp)
                .size(256.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .blur(48.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "PARTITION",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Internal Storage",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp)
                )
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = usedGB,
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GB used",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "$usedPercent% Capacity",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$freeGB GB free of $totalGB GB",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Multi-Segment Storage Bar
                MultiSegmentBar(categories = categories)
            }
        }
    }
}

@Composable
private fun MultiSegmentBar(categories: StorageCategories) {
    val total = categories.totalBytes.toFloat()
    if (total == 0f) return
    
    val freeBytes = categories.freeBytes.coerceAtLeast(0L)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.1f))
    ) {
        val appWeight = (categories.appsBytes + categories.appDataBytes + categories.cacheBytes) / total
        val imgWeight = categories.imageBytes / total
        val vidWeight = categories.videoBytes / total
        val audWeight = categories.audioBytes / total
        val docWeight = categories.filesBytes / total
        val sysWeight = categories.systemBytes / total
        
        if (appWeight > 0.001f) Box(modifier = Modifier.weight(appWeight).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
        if (imgWeight > 0.001f) Box(modifier = Modifier.weight(imgWeight).fillMaxHeight().background(SemanticColors.Images))
        if (vidWeight > 0.001f) Box(modifier = Modifier.weight(vidWeight).fillMaxHeight().background(SemanticColors.Videos))
        if (audWeight > 0.001f) Box(modifier = Modifier.weight(audWeight).fillMaxHeight().background(SemanticColors.Audio))
        if (docWeight > 0.001f) Box(modifier = Modifier.weight(docWeight).fillMaxHeight().background(SemanticColors.Documents))
        if (sysWeight > 0.001f) Box(modifier = Modifier.weight(sysWeight).fillMaxHeight().background(SemanticColors.SystemOther))
        
        val freeWeight = freeBytes / total
        if (freeWeight > 0.001f) Box(modifier = Modifier.weight(freeWeight).fillMaxHeight())
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "STORAGE BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            val items = listOf(
                BreakdownItemData("Apps", FileSizeFormatter.format(categories.appsBytes + categories.appDataBytes + categories.cacheBytes), MaterialTheme.colorScheme.primary),
                BreakdownItemData("Images", FileSizeFormatter.format(categories.imageBytes), SemanticColors.Images),
                BreakdownItemData("Videos", FileSizeFormatter.format(categories.videoBytes), SemanticColors.Videos),
                BreakdownItemData("Audio", FileSizeFormatter.format(categories.audioBytes), SemanticColors.Audio),
                BreakdownItemData("Documents", FileSizeFormatter.format(categories.filesBytes), SemanticColors.Documents),
                BreakdownItemData("System", FileSizeFormatter.format(categories.systemBytes), SemanticColors.SystemOther),
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Manual grid to avoid height issues in LazyColumn
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BreakdownCard(items[0], modifier = Modifier.weight(1f))
                    BreakdownCard(items[1], modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BreakdownCard(items[2], modifier = Modifier.weight(1f))
                    BreakdownCard(items[3], modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BreakdownCard(items[4], modifier = Modifier.weight(1f))
                    BreakdownCard(items[5], modifier = Modifier.weight(1f))
                }
                BreakdownCard(
                    BreakdownItemData("Free Space", "${FileSizeFormatter.format(categories.freeBytes)} Available", MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class BreakdownItemData(val label: String, val value: String, val color: Color)

@Composable
private fun BreakdownCard(data: BreakdownItemData, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).background(data.color, CircleShape))
            Column {
                Text(
                    text = data.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (data.label == "Videos") SemanticColors.Videos else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OtherPartitionCard(
    volume: DashboardViewModel.StorageVolumeInfo,
    onScanClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (volume.name.contains("SD", true)) Icons.Default.SdCard else Icons.Default.Usb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = volume.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (volume.lastScanTime != null) "Last scanned: ${formatTimestamp(volume.lastScanTime)}" else "Never scanned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "SCAN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
