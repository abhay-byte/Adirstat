# Crash Handler Implementation

This document describes the crash handling implementation for Adirstat, designed for Android 16 (API 36) compatibility.

## Overview

Adirstat implements a production-ready crash handling system that:
- Catches all unhandled exceptions on any thread
- Displays a polished crash screen with full stack trace
- Runs the crash activity in a separate process to survive crashes
- Prevents crash loops (two crashes within 3 seconds)
- Supports edge-to-edge display (mandatory on Android 16)
- Uses explicit intents to comply with Android 16's Intent redirection hardening

## Implementation Components

### 1. CrashHandler.kt

Located: `app/src/main/java/com/ivarna/adirstat/crash/CrashHandler.kt`

The `CrashHandler` class implements `Thread.UncaughtExceptionHandler` to intercept all uncaught exceptions:

```kotlin
class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler
```

**Key Features:**
- Builds comprehensive crash log with:
  - Timestamp (ISO-8601 format)
  - App version name and version code
  - Device model, manufacturer, Android version, API level
  - Thread name and ID
  - Full stack trace with all chained causes
- Saves crash log to file for persistence
- Checks for crash loop (prevents infinite restart cycle)
- Launches CrashActivity using explicit intent
- Chains to default handler after capturing crash

### 2. CrashActivity.kt

Located: `app/src/main/java/com/ivarna/adirstat/crash/CrashActivity.kt`

A Compose-based activity that displays the crash screen:

**UI Features:**
- Error icon with "An unexpected error occurred" message
- Scrollable crash log display (summary or full detail)
- "Show Full Details" toggle
- "Copy" button - copies crash log to clipboard
- "Share" button - opens Android share sheet
- "Restart App" button - relaunches the app
- "Close App" button - exits the application

**Android 16 Compatibility:**
- Calls `enableEdgeToEdge()` before `setContent()`
- Uses `WindowInsets.safeDrawing` for proper inset handling
- Uses explicit `Intent` for restart (Android 16 Intent redirection hardening)

### 3. AndroidManifest.xml Registration

```xml
<activity
    android:name=".crash.CrashActivity"
    android:process=":crash_process"
    android:excludeFromRecents="true"
    android:exported="false"
    android:taskAffinity=""
    android:launchMode="singleInstance"
    android:theme="@style/Theme.Adirstat" />
```

**Critical Points:**
- `android:process=":crash_process"` - Runs in separate process to survive the crashed process
- `android:exported="false"` - Not accessible from external apps
- `android:launchMode="singleInstance"` - Prevents multiple crash activities

### 4. Application Registration

In `AdirstatApplication.kt`:

```kotlin
override fun onCreate() {
    super.onCreate()
    
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler(
        CrashHandler(applicationContext, defaultHandler)
    )
}
```

## Crash Loop Prevention

The implementation includes a crash loop guard:
- Stores last crash timestamp in SharedPreferences
- If two crashes occur within 3 seconds (CRASH_LOOP_THRESHOLD_MS), skips showing CrashActivity
- This prevents infinite restart loops that could lock the user out

## Edge-to-Edge Support

Android 16 requires edge-to-edge for all apps targeting API 36:
- `enableEdgeToEdge()` is called in `CrashActivity.onCreate()`
- `WindowInsets.safeDrawing` padding ensures content doesn't go under navigation/status bars
- No deprecated `windowOptOutEdgeToEdgeEnforcement` attribute used

## Intent Hardening

Android 16 blocks implicit intent launching from untrusted contexts:
- Uses explicit `Intent(context, CrashActivity::class.java)`
- Uses `getLaunchIntentForPackage()` for restart functionality
- No action strings or implicit matching

## Testing

To test the crash screen during development, add test buttons:

```kotlin
// Test UI thread crash
Button(onClick = { throw RuntimeException("Test crash") }) {
    Text("Test Crash")
}

// Test background thread crash
Button(onClick = {
    Thread { throw IllegalStateException("Background crash") }.start()
}) {
    Text("Test BG Crash")
}
```

## Files Created

| File | Description |
|------|-------------|
| `app/src/main/java/com/ivarna/adirstat/crash/CrashHandler.kt` | Global exception handler |
| `app/src/main/java/com/ivarna/adirstat/crash/CrashActivity.kt` | Crash screen UI |
| `AndroidManifest.xml` | CrashActivity registration |

## Status

✅ Implemented - Ready for production use
