package com.ivarna.adirstat.crash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ivarna.adirstat.presentation.common.components.AdirstatTopBar

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 16: edge-to-edge is MANDATORY
        enableEdgeToEdge()
        
        val crashLog = intent.getStringExtra("crash_log") ?: "No crash log available."
        
        setContent {
            MaterialTheme {
                CrashScreen(
                    crashLog = crashLog,
                    onRestart = { restartApp() },
                    onClose = { finishAffinity() }
                )
            }
        }
    }
    
    private fun restartApp() {
        CrashHelpers.restartApp(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen(
    crashLog: String,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showFullLog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            AdirstatTopBar(
                title = "App Crashed",
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Error icon + title
            Icon(
                imageVector = Icons.Filled.BugReport,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "An unexpected error occurred",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Crash summary (always visible)
            if (!showFullLog) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = crashLog.lines().take(20).joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                // Full log
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = crashLog,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle full log
            TextButton(
                onClick = { showFullLog = !showFullLog },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (showFullLog) "Show Less" else "Show Full Details")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Copy + Share row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { CrashHelpers.copyToClipboard(context, crashLog) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
                OutlinedButton(
                    onClick = { CrashHelpers.shareLog(context, crashLog) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Restart + Close row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart")
                }
                FilledTonalButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Close")
                }
            }
        }
    }
}
