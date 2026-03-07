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
    val Images = Color(0xFF4CAF50)      // Green
    val Video = Color(0xFFF44336)       // Red
    val Audio = Color(0xFF9C27B0)      // Purple
    val Documents = Color(0xFFFF9800)  // Orange
    val Archives = Color(0xFF795548)   // Brown
    val Code = Color(0xFF00BCD4)       // Cyan
    val Other = Color(0xFF607D8B)      // Blue-grey
    
    fun forCategory(category: FileCategory): Color = when (category) {
        FileCategory.IMAGES -> Images
        FileCategory.VIDEO -> Video
        FileCategory.AUDIO -> Audio
        FileCategory.DOCUMENTS -> Documents
        FileCategory.ARCHIVES -> Archives
        FileCategory.CODE -> Code
        FileCategory.OTHER -> Other
    }
}

enum class FileCategory {
    IMAGES,
    VIDEO,
    AUDIO,
    DOCUMENTS,
    ARCHIVES,
    CODE,
    OTHER
}
