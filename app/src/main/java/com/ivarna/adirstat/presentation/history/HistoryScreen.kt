package com.ivarna.adirstat.presentation.history

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivarna.adirstat.presentation.theme.*
import com.ivarna.adirstat.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            HistoryTopBar(onBack = onNavigateBack)
        },
        floatingActionButton = {
            HistoryFAB(onClick = {})
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
            
            // Lifetime Optimized Stats Card
            HistorySummaryCard(uiState)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.history.isEmpty() -> EmptyHistoryState()
                else -> {
                    uiState.history.forEach { item ->
                        HistoryItemCard(
                            item = item,
                            onClick = { viewModel.loadScan(item.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Scan History",
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
private fun HistorySummaryCard(uiState: HistoryUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "LIFETIME OPTIMIZED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "42.8 GB", // Placeholder total
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TOTAL SCANS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = uiState.history.size.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(item: ScanHistoryItem, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = formatTimestampShort(item.scanDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        item.changeFromPrevious?.let { change ->
                            val isNegative = change.startsWith("-") || change.contains("↓")
                            Surface(
                                color = if (isNegative) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = change,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNegative) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Text(
                        text = item.partitionName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Default.SwipeLeft, null, tint = MaterialTheme.colorScheme.outlineVariant)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used: ${FileSizeFormatter.format(item.totalBytes)} / 128 GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${item.fileCount} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Simple capacity bar
            LinearProgressIndicator(
                progress = { 0.8f }, // item.totalBytes / capacity
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@Composable
private fun HistoryBottomBar() {
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
            BottomNavItem(Icons.Default.Storage, "Storage", false)
            BottomNavItem(Icons.Default.History, "History", true)
            BottomNavItem(Icons.Default.CleaningServices, "Cleaner", false)
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
private fun HistoryFAB(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).size(56.dp).shadow(12.dp, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AddChart, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = CircleShape, modifier = Modifier.size(192.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.HistoryToggleOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("No scan history yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Run a scan to see your storage over time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = {}, shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)) {
            Text("Start Initial Scan")
        }
    }
}

private fun formatTimestampShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

data class ScanHistoryItem(
    val id: Long,
    val partitionName: String,
    val partitionPath: String,
    val scanDate: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val fileCount: Int,
    val folderCount: Int,
    val changeFromPrevious: String? = null
)
