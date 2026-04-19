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
import androidx.compose.ui.res.painterResource
import com.ivarna.adirstat.R
import com.ivarna.adirstat.presentation.common.components.AdirstatTopBar
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
    AdirstatTopBar(
        title = "Settings",
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun SettingsHero() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Configure how Adirstat manages and visualizes your storage ecosystem.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ThemeSelectionGrid(selectedTheme: ThemeOption, onThemeSelected: (ThemeOption) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeOptionCard(Modifier.weight(1f), "System", selectedTheme == ThemeOption.SYSTEM) { onThemeSelected(ThemeOption.SYSTEM) }
            ThemeOptionCard(Modifier.weight(1f), "Light", selectedTheme == ThemeOption.LIGHT) { onThemeSelected(ThemeOption.LIGHT) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeOptionCard(Modifier.weight(1f), "Dark", selectedTheme == ThemeOption.DARK) { onThemeSelected(ThemeOption.DARK) }
            ThemeOptionCard(Modifier.weight(1f), "Dynamic", selectedTheme == ThemeOption.DYNAMIC, isGradient = true) { onThemeSelected(ThemeOption.DYNAMIC) }
        }
    }
}

@Composable
private fun ThemeOptionCard(modifier: Modifier, label: String, isActive: Boolean, isGradient: Boolean = false, onClick: () -> Unit) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                RadioButton(
                    selected = isActive,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
            }
            if (isGradient) {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Primary, Color(0xFF9C27B0), Color(0xFF4CAF50)))))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))) {
                    if (isActive) Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
private fun FileSizeSlider(currentSize: MinimumFileSize, onSizeChange: (MinimumFileSize) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Minimum file size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Filter from analysis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = currentSize.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = MinimumFileSize.entries.indexOf(currentSize).toFloat(),
                onValueChange = { onSizeChange(MinimumFileSize.entries[it.toInt()]) },
                valueRange = 0f..(MinimumFileSize.entries.size - 1).toFloat(),
                steps = MinimumFileSize.entries.size - 2,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun SettingsListButton(icon: ImageVector, iconContainerColor: Color, title: String, subtitle: String, badgeText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconContainerColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = iconContainerColor)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (badgeText != "0") {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SettingsSwitchItem(icon: ImageVector, iconContainerColor: Color, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconContainerColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = iconContainerColor)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsSelectItem(icon: ImageVector, title: String, subtitle: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().clickable { /* Show options */ }
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedOption, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DataActionButton(modifier: Modifier, icon: ImageVector, label: String, containerColor: Color, contentColor: Color, onClick: () -> Unit) {
    Surface(
        color = containerColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.2f)),
        modifier = modifier.height(56.dp).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = containerColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = containerColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AboutCard() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Adirstat", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    Text("Version 1.0.1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_adirstat_logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AboutLinkItem("Open source licenses")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                AboutLinkItem("Privacy policy")
            }
        }
    }
}

@Composable
private fun AboutLinkItem(label: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
    }
}

