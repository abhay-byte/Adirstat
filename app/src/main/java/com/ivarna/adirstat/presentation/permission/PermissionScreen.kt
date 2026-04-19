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
import androidx.compose.ui.draw.clip
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
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            // Header Section
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_adirstat_logo),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Welcome to Adirstat",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "To visualize and manage your storage ecosystem, we need the following permissions.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Rationale Cards
            PermissionRationaleCard(
                icon = Icons.Default.FolderSpecial,
                title = "All Files Access",
                description = "Required to scan your internal storage and visualize large files consuming space.",
                isGranted = hasManageStorage,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PermissionRationaleCard(
                icon = Icons.Default.Apps,
                title = "Usage Access",
                description = "Required to calculate exactly how much storage each installed application is using.",
                isGranted = hasUsageAccess,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(48.dp))

            // CTA Section
            Button(
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (!hasManageStorage || !hasUsageAccess) "GRANT PERMISSIONS" else "GET STARTED",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
            
            TextButton(
                onClick = {
                    prefs.edit().putBoolean("permission_screen_visited", true).apply()
                    onPermissionsGranted()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "CONTINUE IN LIMITED MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp, 
            if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            if (isGranted) {
                Icon(
                    Icons.Default.CheckCircle, 
                    null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
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
