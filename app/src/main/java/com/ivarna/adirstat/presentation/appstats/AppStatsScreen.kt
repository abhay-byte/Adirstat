package com.ivarna.adirstat.presentation.appstats

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.data.source.InstalledAppStorageInfo
import com.ivarna.adirstat.presentation.common.components.AdirstatTopBar
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileSizeFormatter

import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

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
            AppStatsTopBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            if (uiState.isLoading) {
                LoadingSummaryCardShimmer()
            } else {
                TotalManagedCapacityCard(uiState)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!uiState.isLoading) {
                    Text(
                        text = "SORTED BY SIZE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column {
                    when {
                        uiState.isLoading -> {
                            repeat(6) { AppCatalogItemShimmer() }
                        }
                        uiState.apps.isEmpty() -> EmptyAppsState()
                        else -> {
                            uiState.apps.forEach { app ->
                                AppCatalogItem(
                                    app = app,
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${app.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {}
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppStatsTopBar() {
    AdirstatTopBar(
        title = "Apps Storage"
    )
}

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@Composable
private fun LoadingSummaryCardShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(shimmerBrush())
    )
}

@Composable
private fun AppCatalogItemShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerBrush())
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
        }
    }
}

@Composable
private fun TotalManagedCapacityCard(uiState: AppStatsUiState) {
    val totalGB = String.format("%.1f", (uiState.totalAppSize + uiState.totalDataSize + uiState.totalCacheSize).toDouble() / (1024 * 1024 * 1024))
    
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = "TOTAL MANAGED CAPACITY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = totalGB,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "GB",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            val total = (uiState.totalAppSize + uiState.totalDataSize + uiState.totalCacheSize).toFloat().coerceAtLeast(1f)
            val appWeight = (uiState.totalAppSize / total).coerceAtLeast(0.01f)
            val dataWeight = (uiState.totalDataSize / total).coerceAtLeast(0.01f)
            val cacheWeight = (uiState.totalCacheSize / total).coerceAtLeast(0.01f)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.weight(appWeight).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.weight(dataWeight).fillMaxHeight().background(SemanticColors.Images))
                    Box(modifier = Modifier.weight(cacheWeight).fillMaxHeight().background(SemanticColors.Documents))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AppCatalogItem(
    app: com.ivarna.adirstat.data.source.AppStorageInfoBytes,
    onClick: () -> Unit
) {
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
                    val context = LocalContext.current
                    val icon = remember(app.packageName) {
                        try {
                            context.packageManager.getApplicationIcon(app.packageName)
                        } catch (e: Exception) { null }
                    }
                    if (icon != null) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.widget.ImageView(ctx).apply {
                                    setImageDrawable(icon)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(Icons.Default.Android, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = FileSizeFormatter.format(app.totalBytes),
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
