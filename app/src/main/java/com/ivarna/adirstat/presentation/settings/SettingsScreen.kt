package com.ivarna.adirstat.presentation.settings

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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(onBack = onNavigateBack)
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
            
            // Hero Section
            SettingsHero()
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Appearance Section
            SettingsSectionHeader(Icons.Default.Palette, "APPEARANCE")
            Spacer(modifier = Modifier.height(24.dp))
            ThemeSelectionGrid(uiState.theme) { viewModel.setTheme(it) }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Scanning Section
            SettingsSectionHeader(Icons.Default.ManageSearch, "SCANNING")
            Spacer(modifier = Modifier.height(24.dp))
            FileSizeSlider(uiState.minimumFileSize) { viewModel.setMinimumFileSize(it) }
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(24.dp)) {
                Column {
                    SettingsListButton(
                        icon = Icons.Default.FolderOff,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        title = "Exclusion paths",
                        subtitle = "Skip specific directories during scan",
                        badgeText = uiState.excludedPaths.size.toString(),
                        onClick = {}
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    SettingsSwitchItem(
                        icon = Icons.Default.Sync,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        title = "Background scan",
                        subtitle = "Update data while app is closed",
                        checked = true,
                        onCheckedChange = {}
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    SettingsSelectItem(
                        icon = Icons.Default.Schedule,
                        title = "Scan interval",
                        subtitle = "Frequency of automated updates",
                        options = listOf("Daily", "Weekly", "Monthly"),
                        selectedOption = "Weekly",
                        onOptionSelected = {}
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Data Section
            SettingsSectionHeader(Icons.Default.Storage, "DATA")
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DataActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Delete,
                    label = "Clear scan cache",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = { viewModel.clearCache() }
                )
                DataActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Share,
                    label = "Export prefs",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { viewModel.exportToCsv() }
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // About Section
            SettingsSectionHeader(Icons.Default.Info, "ABOUT")
            Spacer(modifier = Modifier.height(24.dp))
            AboutCard()
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
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
private fun SettingsHero() {
    Column {
        Text(
            text = "CORE",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
            modifier = Modifier.offset(y = (-40).dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure how Adirstat manages and visualizes your storage ecosystem.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.offset(y = (-32).dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun ThemeSelectionGrid(selectedTheme: ThemeOption, onThemeSelected: (ThemeOption) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ThemeOptionCard(Modifier.weight(1f), "System", selectedTheme == ThemeOption.SYSTEM) { onThemeSelected(ThemeOption.SYSTEM) }
            ThemeOptionCard(Modifier.weight(1f), "Light", selectedTheme == ThemeOption.LIGHT) { onThemeSelected(ThemeOption.LIGHT) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ThemeOptionCard(Modifier.weight(1f), "Dark", selectedTheme == ThemeOption.DARK) { onThemeSelected(ThemeOption.DARK) }
            ThemeOptionCard(Modifier.weight(1f), "Dynamic Color", false, isGradient = true) { /* Dynamic color action */ }
        }
    }
}

@Composable
private fun ThemeOptionCard(modifier: Modifier, label: String, isActive: Boolean, isGradient: Boolean = false, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                RadioButton(selected = isActive, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
            }
            if (isGradient) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Primary, SemanticColors.Audio, SemanticColors.Images))))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) {
                    if (isActive) Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
private fun FileSizeSlider(currentSize: MinimumFileSize, onSizeChange: (MinimumFileSize) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Minimum file size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Hide smaller files from analysis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = currentSize.displayName,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Slider(
                value = MinimumFileSize.entries.indexOf(currentSize).toFloat(),
                onValueChange = { onSizeChange(MinimumFileSize.entries[it.toInt()]) },
                valueRange = 0f..(MinimumFileSize.entries.size - 1).toFloat(),
                steps = MinimumFileSize.entries.size - 2
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0 KB", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Text("100 MB", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun SettingsListButton(icon: ImageVector, iconContainerColor: Color, title: String, subtitle: String, badgeText: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = iconContainerColor, shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                Text(badgeText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Black)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun SettingsSwitchItem(icon: ImageVector, iconContainerColor: Color, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = iconContainerColor, shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSelectItem(icon: ImageVector, title: String, subtitle: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(selectedOption, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }
    }
}

@Composable
private fun DataActionButton(modifier: Modifier, icon: ImageVector, label: String, containerColor: Color, contentColor: Color, onClick: () -> Unit) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = contentColor)
            Text(label, style = MaterialTheme.typography.labelLarge, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AboutCard() {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Adirstat", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        Text("Version 1.0.1", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.GridView, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AboutLinkItem("Open source licenses")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    AboutLinkItem("Privacy policy")
                }
            }
        }
    }
}

@Composable
private fun AboutLinkItem(label: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SettingsBottomBar() {
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
            BottomNavItem(Icons.Default.Apps, "Apps", false)
            BottomNavItem(Icons.Default.History, "History", false)
            BottomNavItem(Icons.Default.Settings, "Settings", true)
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
