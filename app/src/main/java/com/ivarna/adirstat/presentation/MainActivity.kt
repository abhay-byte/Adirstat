package com.ivarna.adirstat.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ivarna.adirstat.data.local.datastore.UserPreferencesRepository
import com.ivarna.adirstat.presentation.navigation.AdirstatNavHost
import com.ivarna.adirstat.presentation.theme.AdirstatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by preferencesRepository.userPreferences.collectAsState(initial = null)
            val systemDarkTheme = isSystemInDarkTheme()
            
            val (darkTheme, dynamicColor) = when (preferences?.theme) {
                "light" -> false to false
                "dark" -> true to false
                "dynamic" -> systemDarkTheme to true
                else -> systemDarkTheme to false
            }
            
            AdirstatTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdirstatNavHost()
                }
            }
        }
    }
}
