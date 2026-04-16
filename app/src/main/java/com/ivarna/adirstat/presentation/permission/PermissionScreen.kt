package com.ivarna.adirstat.presentation.permission

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ivarna.adirstat.R
import com.ivarna.adirstat.presentation.theme.*

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val prefs = remember { context.getSharedPreferences("adirstat_prefs", Context.MODE_PRIVATE) }
    var hasManageStorage by remember { mutableStateOf(checkManageStoragePermission()) }
    var hasUsageAccess by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    
    val manageStorageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasManageStorage = checkManageStoragePermission()
    }
    
    val usageAccessLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasUsageAccess = checkUsageStatsPermission(context)
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasManageStorage = checkManageStoragePermission()
                hasUsageAccess = checkUsageStatsPermission(context)
                if (hasManageStorage && hasUsageAccess) {
                    prefs.edit().putBoolean("permission_screen_visited", true).apply()
                    onPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Background Decoration
        Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 48.dp, y = 48.dp).size(256.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape).blur(64.dp))
            Box(modifier = Modifier.align(Alignment.BottomStart).offset(x = (-48).dp, y = 48.dp).size(320.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), CircleShape).blur(80.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                modifier = Modifier.padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(40.dp, CircleShape, spotColor = Primary)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Primary, PrimaryContainer)))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Storage, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Adirstat",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Visualize your storage",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Cards Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionRationaleCard(
                    icon = Icons.Default.FolderSpecial,
                    title = "All Files Access",
                    description = "We need access to all files on your device to show you exactly what's consuming storage — just like WizTree on Windows.",
                    isGranted = hasManageStorage,
                    color = MaterialTheme.colorScheme.primary
                )
                
                PermissionRationaleCard(
                    icon = Icons.Default.Apps,
                    title = "Usage Access",
                    description = "We need usage access to show you how much storage each installed app is using.",
                    isGranted = hasUsageAccess,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Footer CTA
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GradientButton(
                    onClick = {
                        if (!hasManageStorage) {
                            val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            manageStorageLauncher.launch(intent)
                        } else if (!hasUsageAccess) {
                            val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            usageAccessLauncher.launch(intent)
                        } else {
                            prefs.edit().putBoolean("permission_screen_visited", true).apply()
                            onPermissionsGranted()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp).shadow(24.dp, CircleShape, spotColor = Primary),
                    shape = CircleShape,
                    brush = Brush.linearGradient(listOf(Primary, PrimaryContainer))
                ) {
                    Text(
                        text = if (!hasManageStorage || !hasUsageAccess) "Grant Access" else "Get Started",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                TextButton(
                    onClick = {
                        prefs.edit().putBoolean("permission_screen_visited", true).apply()
                        onPermissionsGranted()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Use Limited Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRationaleCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    color: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = CircleShape,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (isGranted) SemanticColors.Images else color, modifier = Modifier.size(28.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            if (isGranted) {
                Icon(Icons.Default.CheckCircle, null, tint = SemanticColors.Images)
            }
        }
    }
}

// Re-using check functions from previous impl
private fun checkManageStoragePermission(): Boolean {
    return try {
        android.os.Environment.isExternalStorageManager()
    } catch (e: Exception) {
        false
    }
}

private fun checkUsageStatsPermission(context: Context): Boolean {
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        mode == android.app.AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }
}

// Extension for Button brush
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    brush: Brush? = null,
    content: @Composable RowScope.() -> Unit
) {
    if (brush != null) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(brush)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(ButtonDefaults.ContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    } else {
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            content = content
        )
    }
}
