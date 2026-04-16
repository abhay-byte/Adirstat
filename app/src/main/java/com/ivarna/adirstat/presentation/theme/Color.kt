package com.ivarna.adirstat.presentation.theme

import androidx.compose.ui.graphics.Color

// "The Precision Mosaic" Colors - Refined from Design Documentation

// Primary & Tonal Architecture
val Primary = Color(0xFF00687A)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF5CB7CE)
val OnPrimaryContainer = Color(0xFF004653)

val Secondary = Color(0xFF466270)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFC6E4F4)
val OnSecondaryContainer = Color(0xFF4A6774)

val Tertiary = Color(0xFF006E1C)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF5DC05F)
val OnTertiaryContainer = Color(0xFF004B10)

val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

// Surface Hierarchy & Nesting (No-Line Rule)
val Surface = Color(0xFFF4FAFC)                 // Base Layer
val OnSurface = Color(0xFF161D1E)               // Text/Icons on surface
val SurfaceVariant = Color(0xFFDDE3E5)
val OnSurfaceVariant = Color(0xFF3C494C)

val Background = Color(0xFFF4FAFC)
val OnBackground = Color(0xFF161D1E)

val SurfaceContainerLowest = Color(0xFFFFFFFF)  // Content Cards
val SurfaceContainerLow = Color(0xFFEFF5F6)     // Sectional Wrappers
val SurfaceContainer = Color(0xFFE9EFF0)        // Base Sectioning
val SurfaceContainerHigh = Color(0xFFE3E9EB)
val SurfaceContainerHighest = Color(0xFFDDE3E5) // Content Cards / Tonal Shifts

val Outline = Color(0xFF6C797C)
val OutlineVariant = Color(0xFFBBC9CC)          // 20% Opacity Ghost Border Fallback if used with alpha

val InverseSurface = Color(0xFF2B3133)
val InverseOnSurface = Color(0xFFECF2F3)
val InversePrimary = Color(0xFF79D3EA)

// Ambient Shadow (6% opacity of OnSurface)
val AmbientShadow = Color(0x0F161D1E)

// Semantic File Palette (Treemap / Cards)
object SemanticColors {
    val Images = Color(0xFF4CAF50)       // Green
    val Videos = Color(0xFFF44336)       // Red
    val Audio = Color(0xFF9C27B0)        // Purple
    val Documents = Color(0xFFFF9800)    // Orange
    val Apk = Color(0xFF795548)          // Brown
    val Code = Color(0xFF00BCD4)         // Cyan
    val SystemOther = Color(0xFF607D8B)  // Blue-grey
    
    fun getColorForCategory(category: com.ivarna.adirstat.domain.model.FileCategory): Color {
        return when (category) {
            com.ivarna.adirstat.domain.model.FileCategory.IMAGES -> Images
            com.ivarna.adirstat.domain.model.FileCategory.VIDEOS -> Videos
            com.ivarna.adirstat.domain.model.FileCategory.AUDIO -> Audio
            com.ivarna.adirstat.domain.model.FileCategory.DOCUMENTS -> Documents
            com.ivarna.adirstat.domain.model.FileCategory.ARCHIVES -> SystemOther
            com.ivarna.adirstat.domain.model.FileCategory.APK -> Apk
            com.ivarna.adirstat.domain.model.FileCategory.CODE -> Code
            com.ivarna.adirstat.domain.model.FileCategory.OTHER -> SystemOther
        }
    }
}
