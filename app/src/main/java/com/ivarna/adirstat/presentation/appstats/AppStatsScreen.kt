package com.ivarna.adirstat.presentation.appstats

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.ivarna.adirstat.data.source.InstalledAppStorageInfo
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }
    
    Scaffold(
        topBar = {
            AppStatsTopBar(onBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Total Managed Capacity Card
            TotalManagedCapacityCard(uiState)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Category Filter Row
            CategoryFilterRow()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Catalog Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SORTED BY SIZE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App List
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.apps.isEmpty() -> EmptyAppsState()
                else -> {
                    uiState.apps.forEachIndexed { index, app ->
                        AppCatalogItem(
                            app = app,
                            isLow = index % 2 == 0,
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${app.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppStatsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Apps Storage",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
    )
}

@Composable
private fun TotalManagedCapacityCard(uiState: AppStatsUiState) {
    val totalGB = String.format("%.1f", (uiState.totalAppSize + uiState.totalDataSize + uiState.totalCacheSize).toDouble() / (1024 * 1024 * 1024))
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(32.dp)) {
            // Background blur decoration
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 48.dp, y = (-48).dp)
                    .size(192.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape)
                    .blur(48.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TOTAL MANAGED CAPACITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = totalGB,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "GB",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Multi-segment Bar
                val total = (uiState.totalAppSize + uiState.totalDataSize + uiState.totalCacheSize).toFloat().coerceAtLeast(1f)
                val appWeight = (uiState.totalAppSize / total).coerceAtLeast(0.01f)
                val dataWeight = (uiState.totalDataSize / total).coerceAtLeast(0.01f)
                val cacheWeight = (uiState.totalCacheSize / total).coerceAtLeast(0.01f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Box(modifier = Modifier.weight(appWeight).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.weight(dataWeight).fillMaxHeight().background(SemanticColors.Images))
                    Box(modifier = Modifier.weight(cacheWeight).fillMaxHeight().background(SemanticColors.Documents))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItemSmall(MaterialTheme.colorScheme.primary, "APK")
                    LegendItemSmall(SemanticColors.Images, "DATA")
                    LegendItemSmall(SemanticColors.Documents, "CACHE")
                }
            }
        }
    }
}

@Composable
private fun LegendItemSmall(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CategoryFilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill("Total", isActive = true)
        FilterPill("APK", isActive = false)
        FilterPill("Data", isActive = false)
        FilterPill("Cache", isActive = false)
    }
}

@Composable
private fun FilterPill(label: String, isActive: Boolean) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = CircleShape,
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
private fun AppCatalogItem(
    app: InstalledAppStorageInfo,
    isLow: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isLow) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = FileSizeFormatter.format(app.totalSize),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Text(
                    text = app.packageName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Detailed breakdown bar for app
                val total = (app.apkSize + app.dataSize + app.cacheSize).toFloat().coerceAtLeast(1f)
                val apkWeight = (app.apkSize / total).coerceAtLeast(0.01f)
                val dataWeight = (app.dataSize / total).coerceAtLeast(0.01f)
                val cacheWeight = (app.cacheSize / total).coerceAtLeast(0.01f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Box(modifier = Modifier.weight(apkWeight).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.weight(dataWeight).fillMaxHeight().background(SemanticColors.Images))
                    Box(modifier = Modifier.weight(cacheWeight).fillMaxHeight().background(SemanticColors.Documents))
                }
            }
        }
    }
}

@Composable
private fun AppStatsBottomBar() {
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
            BottomNavItem(Icons.Default.Dashboard, "Dashboard", false)
            BottomNavItem(Icons.Default.Apps, "Apps", true)
            BottomNavItem(Icons.Default.History, "History", false)
            BottomNavItem(Icons.Default.Settings, "Settings", false)
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
    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyAppsState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Android, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Text("No apps found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
