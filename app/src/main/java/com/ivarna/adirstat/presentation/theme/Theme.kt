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
import androidx.compose.ui.graphics.Color
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
    primary = Color(0xFF5CB7CE),
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF004D5B),
    onPrimaryContainer = Color(0xFFB4EBFF),
    secondary = Color(0xFFB1CBD9),
    onSecondary = Color(0xFF1B3441),
    secondaryContainer = Color(0xFF304A58),
    onSecondaryContainer = Color(0xFFCDE7F6),
    tertiary = Color(0xFF5DC05F),
    onTertiary = Color(0xFF00390A),
    tertiaryContainer = Color(0xFF005312),
    onTertiaryContainer = Color(0xFFBBFFB2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1112),
    onBackground = Color(0xFFDDE3E5),
    surface = Color(0xFF0D1112),
    onSurface = Color(0xFFDDE3E5),
    surfaceVariant = Color(0xFF3C494C),
    onSurfaceVariant = Color(0xFFBBC9CC),
    outline = Color(0xFF869296),
    outlineVariant = Color(0xFF3C494C),
    surfaceContainerLowest = Color(0xFF080D0E),
    surfaceContainerLow = Color(0xFF161D1E),
    surfaceContainer = Color(0xFF1E2526),
    surfaceContainerHigh = Color(0xFF262E2F),
    surfaceContainerHighest = Color(0xFF2E3738)
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
