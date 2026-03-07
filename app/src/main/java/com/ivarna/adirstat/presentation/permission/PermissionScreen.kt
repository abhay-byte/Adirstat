package com.ivarna.adirstat.presentation.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ivarna.adirstat.util.PermissionManager

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Use SharedPreferences to track first launch - always show permission screen on first run
    val prefs = remember {
        context.getSharedPreferences("adirstat_prefs", Context.MODE_PRIVATE)
    }
    var hasVisitedPermission by remember { mutableStateOf(prefs.getBoolean("permission_screen_visited", false)) }
    
    // Check permission states - but only use for UI display, not auto-navigation
    var hasManageStorage by remember { mutableStateOf(checkManageStoragePermission()) }
    var hasUsageAccess by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    
    // Always show permission screen first time - check if already visited and permissions granted
    val shouldSkipPermission = hasVisitedPermission && hasManageStorage
    
    // Launchers for settings intents
    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check after returning
        hasManageStorage = checkManageStoragePermission()
    }
    
    val usageAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check after returning
        hasUsageAccess = checkUsageStatsPermission(context)
    }
    
    // Re-check permissions when resuming from Settings - only skip if already visited and granted
    DisposableEffect(lifecycleOwner, shouldSkipPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && shouldSkipPermission) {
                hasManageStorage = checkManageStoragePermission()
                hasUsageAccess = checkUsageStatsPermission(context)
                // Auto-navigate if permissions granted and already visited
                if (hasManageStorage) {
                    // Save that user has been through permission screen
                    prefs.edit().putBoolean("permission_screen_visited", true).apply()
                    onPermissionsGranted()
                } else {
                    // Permission revoked - reset the visited flag
                    prefs.edit().putBoolean("permission_screen_visited", false).apply()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Storage Access Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Adirstat needs the following permissions to show your complete storage breakdown:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // All Files Access permission
        PermissionItem(
            icon = Icons.Default.FolderOpen,
            title = "All Files Access",
            description = "Required to scan all files and folders on your device",
            isGranted = hasManageStorage
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Usage Access permission
        PermissionItem(
            icon = Icons.Default.Apps,
            title = "Usage Access",
            description = "Required to show per-app storage breakdown (app data & cache sizes)",
            isGranted = hasUsageAccess
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Grant All Files Access button
        if (!hasManageStorage) {
            Button(
                onClick = {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant All Files Access")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Grant Usage Access button
        if (!hasUsageAccess) {
            OutlinedButton(
                onClick = {
                    val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    usageAccessLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Usage Access (for App Sizes)")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Continue button
        Button(
            onClick = {
                // Save that user has been through permission screen
                prefs.edit().putBoolean("permission_screen_visited", true).apply()
                onPermissionsGranted()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasManageStorage)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                if (hasManageStorage) "Continue" else "Continue with Limited Access"
            )
        }
        
        if (!hasManageStorage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Limited mode: Some storage categories won't be visible",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
