package com.ivarna.adirstat.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val Primary = Color(0xFF1B6CA8)
val OnPrimary = Color.White
val PrimaryContainer = Color(0xFFD4E3FF)
val OnPrimaryContainer = Color(0xFF001C3A)

// Secondary colors
val Secondary = Color(0xFF545F71)
val OnSecondary = Color.White
val SecondaryContainer = Color(0xFFD8E3F8)
val OnSecondaryContainer = Color(0xFF213141)

// Tertiary colors
val Tertiary = Color(0xFF6B5778)
val OnTertiary = Color.White
val TertiaryContainer = Color(0xFFF3DAFF)
val OnTertiaryContainer = Color(0xFF251432)

// Light theme colors
val BackgroundLight = Color(0xFFFDFCFF)
val OnBackgroundLight = Color(0xFF1A1C1E)
val SurfaceLight = Color(0xFFFDFCFF)
val OnSurfaceLight = Color(0xFF1A1C1E)
val SurfaceVariantLight = Color(0xFFE1E2EC)
val OnSurfaceVariantLight = Color(0xFF44474E)
val OutlineLight = Color(0xFF74777F)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color.White

// Dark theme colors (recommended default for tool app)
val BackgroundDark = Color(0xFF1A1C1E)
val OnBackgroundDark = Color(0xFFE3E2E6)
val SurfaceDark = Color(0xFF252629)
val OnSurfaceDark = Color(0xFFE3E2E6)
val SurfaceVariantDark = Color(0xFF44474E)
val OnSurfaceVariantDark = Color(0xFFC4C6D0)
val OutlineDark = Color(0xFF8E9099)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)

// Treemap colors (fixed, NOT Material theme colors)
object TreemapColors {
    val images = Color(0xFF4CAF50)       // Green
    val videos = Color(0xFFF44336)       // Red
    val audio = Color(0xFF9C27B0)        // Purple
    val documents = Color(0xFFFF9800)    // Orange
    val archives = Color(0xFF795548)     // Brown
    val apk = Color(0xFFE91E63)          // Pink
    val code = Color(0xFF00BCD4)         // Cyan
    val other = Color(0xFF607D8B)       // Blue-grey
    
    fun getColorForCategory(category: com.ivarna.adirstat.domain.model.FileCategory): Color {
        return when (category) {
            com.ivarna.adirstat.domain.model.FileCategory.IMAGES -> images
            com.ivarna.adirstat.domain.model.FileCategory.VIDEOS -> videos
            com.ivarna.adirstat.domain.model.FileCategory.AUDIO -> audio
            com.ivarna.adirstat.domain.model.FileCategory.DOCUMENTS -> documents
            com.ivarna.adirstat.domain.model.FileCategory.ARCHIVES -> archives
            com.ivarna.adirstat.domain.model.FileCategory.APK -> apk
            com.ivarna.adirstat.domain.model.FileCategory.CODE -> code
            com.ivarna.adirstat.domain.model.FileCategory.OTHER -> other
        }
    }
}
