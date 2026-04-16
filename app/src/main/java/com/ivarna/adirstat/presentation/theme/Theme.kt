package com.ivarna.adirstat.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// "The Precision Mosaic" defines an editorial layout, primarily designed with light-theme elements.
// While dark theme is supported via Material 3, the exact design uses light colors for its 'architectural' feel.
// We define both using the standard material map.

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    onError = OnError,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest
)

// For dark theme, we adapt the token architecture slightly to ensure dark contrast but keep the soul.
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryContainer, // swap for dark
    onPrimary = OnPrimaryContainer,
    primaryContainer = Primary,
    onPrimaryContainer = OnPrimary,
    secondary = SecondaryContainer,
    onSecondary = OnSecondaryContainer,
    secondaryContainer = Secondary,
    onSecondaryContainer = OnSecondary,
    tertiary = TertiaryContainer,
    onTertiary = OnTertiaryContainer,
    tertiaryContainer = Tertiary,
    onTertiaryContainer = OnTertiary,
    background = OnSurface, // Invert
    onBackground = Surface,
    surface = OnSurface,
    onSurface = Surface,
    surfaceVariant = OnSurfaceVariant,
    onSurfaceVariant = SurfaceVariant,
    outline = OutlineVariant,
    outlineVariant = Outline,
    error = Error,
    onError = OnError,
    surfaceContainerLowest = androidx.compose.ui.graphics.Color(0xFF0D1112),
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFF161D1E),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF1E2526),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF262E2F),
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF2E3738)
)

@Composable
fun AdirstatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // "The Precision Mosaic" strongly prefers its custom palette over Dynamic Color.
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
