package com.ivarna.adirstat

import android.app.Application
import com.ivarna.adirstat.crash.CrashHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AdirstatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Set up global exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(applicationContext, defaultHandler)
        )
    }
}
