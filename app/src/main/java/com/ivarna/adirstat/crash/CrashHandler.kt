package com.ivarna.adirstat.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.outputStream

class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val CRASH_LOG_FILE = "crash_log.txt"
        private const val LAST_CRASH_TIME = "last_crash_time"
        private const val CRASH_LOOP_THRESHOLD_MS = 3000L
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crashLog = buildCrashLog(thread, throwable)
            
            // Save to file for persistence
            saveCrashLog(crashLog)
            
            // Check for crash loop
            if (!isCrashLoop()) {
                launchCrashActivity(crashLog)
                updateLastCrashTime()
            }
            
            // Give the crash activity time to start before killing the process
            Thread.sleep(500)
        } catch (e: Exception) {
            // If CrashHandler itself fails, fall back to system handler
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildCrashLog(thread: Thread, throwable: Throwable): String {
        return buildString {
            appendLine("=== CRASH REPORT ===")
            appendLine("Timestamp   : ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}")
            appendLine("App Version : ${getVersionName()} (${getVersionCode()})")
            appendLine("Device      : ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android     : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Thread      : ${thread.name} (id=${thread.id})")
            appendLine()
            appendLine("=== STACK TRACE ===")
            appendLine(throwable.stackTraceToString())
            
            var cause = throwable.cause
            while (cause != null) {
                appendLine()
                appendLine("=== CAUSED BY ===")
                appendLine(cause.stackTraceToString())
                cause = cause.cause
            }
        }
    }

    private fun getVersionName(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getVersionCode(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun saveCrashLog(crashLog: String) {
        try {
            val file = File(context.filesDir, CRASH_LOG_FILE)
            file.writeText(crashLog)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun isCrashLoop(): Boolean {
        val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
        val lastCrashTime = prefs.getLong(LAST_CRASH_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastCrashTime) < CRASH_LOOP_THRESHOLD_MS
    }

    private fun updateLastCrashTime() {
        try {
            val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong(LAST_CRASH_TIME, System.currentTimeMillis()).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun launchCrashActivity(crashLog: String) {
        // Use FLAG_ACTIVITY_NEW_TASK — required when starting from non-Activity context
        // Use FLAG_ACTIVITY_CLEAR_TASK to clear back stack
        // IMPORTANT: use an EXPLICIT intent to avoid Android 16's Intent redirection blocks
        val intent = Intent(context, CrashActivity::class.java).apply {
            putExtra("crash_log", crashLog)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}

// Helper functions for CrashActivity
object CrashHelpers {
    
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Crash Log", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Crash log copied", Toast.LENGTH_SHORT).show()
    }
    
    fun shareLog(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Adirstat Crash Report")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Crash Log"))
    }
    
    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        if (intent != null) {
            context.startActivity(intent)
        }
        // Force close current process
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
