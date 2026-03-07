# UI Design System

This document defines the visual design language for Adirstat, including colors, typography, spacing, and components.

---

## Design Philosophy

**Data-First, Minimal Chrome.** The treemap and file lists ARE the UI. Every pixel of non-data UI should justify its existence.

Adirstat is a utility tool — users come to see their storage, not to admire the app's design. The UI should:
- Get out of the way and let data shine
- Use high information density where appropriate
- Provide clear visual hierarchy
- Support dark mode as the default/recommended feel

---

## Seed Color

**Primary Seed:** `#1B6CA8` (Deep Tech Blue)

This blue communicates:
- Reliability and trust
- Technology and data
- Professionalism

---

## Color Palette

### Material 3 Color Scheme

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| Primary | #1B6CA8 | #8BC4F5 | FAB, buttons, links |
| On Primary | #FFFFFF | #003258 | Text on primary |
| Primary Container | #D4E3FF | #00497D | Selected states |
| On Primary Container | #001C3A | #D4E3FF | Text on primary container |
| Secondary | #545F71 | #ADC6E0 | Secondary actions |
| On Secondary | #FFFFFF | #213141 | Text on secondary |
| Secondary Container | #D8E3F8 | #3C4858 | Cards, surfaces |
| Tertiary | #6B5778 | #D6BEE4 | Accent elements |
| Surface | #FDFCFF | #1A1C1E | Background |
| Surface Variant | #E1E2EC | #44474E | Cards, containers |
| On Surface | #1A1C1E | #E3E2E6 | Primary text |
| On Surface Variant | #44474E | #C4C6D0 | Secondary text |
| Outline | #74777F | #8E9099 | Borders, dividers |
| Error | #BA1A1A | #FFB4AB | Errors |
| Background | #FDFCFF | #1A1C1E | Screen background |

### Treemap Color Palette (Fixed)

These colors are NOT derived from Material theme — they are fixed to ensure consistent visualization regardless of user theme.

| Category | Color | Hex Code | Examples |
|----------|-------|----------|----------|
| Images | Green | #4CAF50 | jpg, png, gif, webp, bmp, heic |
| Video | Red | #F44336 | mp4, mkv, avi, mov, webm, 3gp |
| Audio | Purple | #9C27B0 | mp3, wav, flac, aac, ogg, m4a |
| Documents | Orange | #FF9800 | pdf, doc, docx, xls, xlsx, ppt, txt |
| Archives/APK | Brown | #795548 | apk, zip, rar, 7z, tar, gz |
| Code | Cyan | #00BCD4 | kt, java, js, py, html, css, xml |
| Unknown | Blue-grey | #607D8B | Everything else |

---

## Typography

### Font Family
- **System default:** Roboto (Android default)
- **Monospace:** Roboto Mono (for file paths, sizes)

### Type Scale

| Style | Font | Size | Weight | Line Height | Usage |
|-------|------|------|--------|-------------|-------|
| Display Large | Roboto | 57sp | 400 | 64sp | Not used |
| Display Medium | Roboto | 45sp | 400 | 52sp | Not used |
| Display Small | Roboto | 36sp | 400 | 44sp | Not used |
| Headline Large | Roboto | 32sp | 400 | 40sp | Screen titles |
| Headline Medium | Roboto | 28sp | 400 | 36sp | Section headers |
| Headline Small | Roboto | 24sp | 400 | 32sp | Card titles |
| Title Large | Roboto | 22sp | 500 | 28sp | App bar title |
| Title Medium | Roboto | 16sp | 500 | 24sp | List item titles |
| Title Small | Roboto | 14sp | 500 | 20sp | Chip text |
| Body Large | Roboto | 16sp | 400 | 24sp | Primary body text |
| Body Medium | Roboto | 14sp | 400 | 20sp | Secondary text |
| Body Small | Roboto | 12sp | 400 | 16sp | Captions, timestamps |
| Label Large | Roboto | 14sp | 500 | 20sp | Buttons |
| Label Medium | Roboto | 12sp | 500 | 16sp | Filter chips |
| Label Small | Roboto | 11sp | 500 | 16sp | Small labels |

### Compact Density Adjustments

For a data-dense tool app, use compact density:
- List item height: 56dp (vs default 72dp)
- Card padding: 12dp (vs default 16dp)
- Icon size: 20dp (vs default 24dp)
- Text: Body Medium as default (vs Body Large)

---

## Spacing

### Base Unit
4dp baseline grid

### Common Spacing Values

| Token | Value | Usage |
|-------|-------|-------|
| xxs | 4dp | Tight spacing, icon margins |
| xs | 8dp | Inline spacing, chip gaps |
| sm | 12dp | Card internal padding |
| md | 16dp | Standard padding |
| lg | 24dp | Section spacing |
| xl | 32dp | Large gaps |
| xxl | 48dp | Screen margins top/bottom |

### Screen Padding
- Horizontal: 16dp
- Vertical: 16dp (or 24dp on large screens)

### Card Spacing
- Between cards: 8dp
- Card internal padding: 16dp
- Card corner radius: 12dp

---

## Shape

### Corner Radii

| Token | Value | Usage |
|-------|-------|-------|
| None | 0dp | N/A |
| Extra Small | 4dp | Chips, small buttons |
| Small | 8dp | Text fields |
| Medium | 12dp | Cards, dialogs |
| Large | 16dp | Bottom sheets |
| Extra Large | 28dp | FAB |

### Component Shapes

| Component | Shape |
|-----------|-------|
| Cards | Medium (12dp) |
| Buttons | Extra Small (4dp) |
| Chips | Extra Small (4dp) |
| Bottom Sheet | Large (16dp) top corners |
| Dialog | Large (16dp) |
| FAB | Extra Large (28dp) |

---

## Elevation

### Shadow Levels

| Level | Elevation | Usage |
|-------|-----------|-------|
| Level 0 | 0dp | Flat surfaces |
| Level 1 | 1dp | Cards at rest |
| Level 2 | 3dp | Cards raised |
| Level 3 | 6dp | FAB |
| Level 4 | 8dp | Bottom sheet |
| Level 5 | 12dp | Dialog |

---

## Motion

### Animation Durations

| Duration | Value | Usage |
|----------|-------|-------|
| Short | 150ms | Button press, toggle |
| Medium | 300ms | Screen transitions |
| Long | 500ms | Treemap layout change |

### Animation Easing

| Easing | Usage |
|--------|-------|
| Standard (decelerate) | Enter animations |
| Standard (accelerate) | Exit animations |
| Emphasized | FAB, important elements |
| Emphasized Decelerate | Bottom sheet open |
| Emphasized Accelerate | Bottom sheet close |

### Reduce Motion

- Respect `android:reduceMotion` accessibility setting
- Use instant transitions when enabled

---

## Components

### Storage Bar (Usage Bar)

**Purpose:** Show used/free space ratio on a partition

```
┌─────────────────────────────────────────┐
│ ████████████░░░░░░░░░░░░░░░░░░░░░░░░░░  │
│ 32.5 GB used of 128 GB    95.5 GB free  │
└─────────────────────────────────────────┘
```

| Property | Value |
|----------|-------|
| Height | 8dp |
| Corner Radius | 4dp |
| Used Color | Primary |
| Free Color | Surface Variant |
| Animation | None (static) |

### File Type Icon

| Type | Icon Name | Color |
|------|-----------|-------|
| Image | `image` | #4CAF50 |
| Video | `videocam` | #F44336 |
| Audio | `music_note` | #9C27B0 |
| Document | `description` | #FF9800 |
| Archive | `folder_zip` | #795548 |
| Code | `code` | #00BCD4 |
| Folder | `folder` | Primary |
| Unknown | `insert_drive_file` | #607D8B |

### Bottom Sheet

| Property | Value |
|----------|-------|
| Corner Radius | 16dp (top) |
| Handle | 32dp x 4dp, centered |
| Background | Surface |
| Scrim | 32% black |

### Navigation Bar

| Property | Value |
|----------|-------|
| Style | Bottom Navigation |
| Items | 4: Dashboard, Apps, History, Settings |
| Icon Size | 24dp |
| Label | Visible, 12sp |

---

## Accessibility

### Touch Targets
- Minimum: 48dp x 48dp
- Recommended: 56dp x 56dp for primary actions

### Color Contrast
- Primary text: 7:1 ratio (WCAG AAA)
- Secondary text: 4.5:1 ratio (WCAG AA)
- UI components: 3:1 ratio (WCAG AA)

### Screen Reader
- All icons have content descriptions
- All buttons have semantic labels
- List items announce count and position

### Dynamic Type
- Support text scaling up to 200%
- Minimum touch targets maintained at all sizes

---

## Dark Mode

Dark mode is the recommended/default feel for this utility app.

### Dark Theme Values

| Property | Light | Dark | Notes |
|----------|-------|------|-------|
| Background | #FDFCFF | #1A1C1E | Not pure black |
| Surface | #FDFCFF | #252629 | Elevated surface |
| Surface Variant | #E1E2EC | #44474E | Cards |
| Primary | #1B6CA8 | #8BC4F5 | Lighter for visibility |
| On Background | #1A1C1E | #E3E2E6 | High contrast |

### Dark Mode Rationale
- Reduces eye strain for a tool app used frequently
- Makes colorful treemap blocks pop more
- Saves battery on OLED screens
- Standard practice for developer/power user tools

---

## Icon Set

Use Material Symbols (Material Icons) from the default Android icon set:

| Icon | Name | Usage |
|------|------|-------|
| 📁 | `folder` | Folders |
| 📄 | `insert_drive_file` | Files |
| 🖼️ | `image` | Images |
| 🎬 | `videocam` | Videos |
| 🎵 | `music_note` | Audio |
| 📄 | `description` | Documents |
| 📦 | `folder_zip` | Archives |
| 💻 | `code` | Code files |
| 📱 | `smartphone` | Apps |
| 💾 | `sd_storage` | Storage |
| 🔍 | `search` | Search |
| ⚙️ | `settings` | Settings |
| 🗑️ | `delete` | Delete |
| 📤 | `share` | Share |
| ⬅️ | `arrow_back` | Back |
| ℹ️ | `info` | Info |

---

## Responsive Layout

### Phone (Compact)
- Single column layouts
- Full-width cards
- Bottom navigation

### Tablet (Medium/Expanded)
- Two-column lists (if useful)
- Navigation rail instead of bottom nav (optional)
- Larger touch targets (optional)

---

## Implementation Notes

### Compose Theme Setup
```kotlin
// Theme.kt
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6CA8),
    onPrimary = Color.White,
    // ...
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BC4F5),
    onPrimary = Color(0xFF003258),
    // ...
)
```

### Treemap Colors (Non-Material)
```kotlin
object TreemapColors {
    val Images = Color(0xFF4CAF50)
    val Video = Color(0xFFF44336)
    val Audio = Color(0xFF9C27B0)
    val Documents = Color(0xFFFF9800)
    val Archives = Color(0xFF795548)
    val Code = Color(0xFF00BCD4)
    val Other = Color(0xFF607D8B)
}
```

---

## Summary

| Category | Value |
|----------|-------|
| Seed Color | #1B6CA8 |
| Primary Text Size | 14sp (Body Medium) |
| List Item Height | 56dp |
| Screen Padding | 16dp |
| Card Radius | 12dp |
| Minimum Touch | 48dp |
| Default Theme | Dark (recommended) |
