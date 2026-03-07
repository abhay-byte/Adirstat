# Play Store Image Assets

This document lists all required image assets for the Google Play Store listing.

---

## Required Assets

### App Icon
| Property | Value |
|----------|-------|
| Dimensions | 512 × 512 pixels |
| Format | PNG (32-bit with alpha) |
| Naming | `icon.png` |
| Location | `fastlane/metadata/android/images/` |

**Design Guidelines:**
- Transparent background
- Simple, recognizable design at small sizes
- Use the app's primary blue (#1B6CA8) as dominant color
- Include a folder/storage visualization element

### Feature Graphic
| Property | Value |
|----------|-------|
| Dimensions | 1024 × 500 pixels |
| Format | PNG or JPG |
| Naming | `featureGraphic.png` or `featureGraphic.jpg` |
| Location | `fastlane/metadata/android/images/` |

**Design Guidelines:**
- Showcase the treemap visualization
- Include app name "Adirstat"
- Use the app's blue (#1B6CA8) as background
- Text should be readable at small sizes

### Phone Screenshots
| Property | Value |
|----------|-------|
| Dimensions | 1080 × 1920 pixels (or 9:16 aspect ratio) |
| Minimum | 2 screenshots required |
| Maximum | 8 screenshots allowed |
| Format | PNG or JPG |
| Naming | `screenshot_1.png`, `screenshot_2.png`, etc. |
| Location | `fastlane/metadata/android/images/` |

#### Required Screenshots (5 total recommended)

1. **Dashboard Screen**
   - Shows partition overview with usage bars
   - Internal storage, SD card, OTG cards visible

2. **Treemap Screen**
   - Interactive treemap visualization
   - Color-coded blocks visible

3. **File List Screen**
   - Sortable file list with sizes
   - Filter chips visible

4. **Duplicate Detection Screen**
   - Duplicate file groups displayed
   - Wasted space shown

5. **App Storage Screen**
   - List of installed apps
   - Storage breakdown bars visible

---

## Screengrab Setup

To capture screenshots automatically using Fastlane Screengrab:

### 1. Add Screengrab to build.gradle

```kotlin
// app/build.gradle.kts
android {
    ...
    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}

dependencies {
    // Screengrab for automated screenshots
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
}
```

### 2. Create Screengrabfile

```ruby
# fastlane/Screengrabfile
app_package_name 'com.ivarna.adirstat'
tests_package_name 'com.ivarna.adirstat.test'
```

### 3. Create Test Class

```kotlin
// androidTest/java/com/ivarna/adirstat/ScreenshotTest.kt
package com.ivarna.adirstat

import androidx.test.core.app.takeScreenshot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenshotTest {
    
    @Test
    fun captureDashboard() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Navigate to dashboard
        takeScreenshot("1_dashboard")
    }
    
    // ... additional screenshot methods
}
```

### 4. Run Screengrab

```bash
fastlane screengrab
```

---

## Optional Assets

### TV Banner
| Property | Value |
|----------|-------|
| Dimensions | 1280 × 720 pixels |
| Format | PNG or JPG |
| Naming | `tvBanner.png` |

### Wear OS Screenshots
If supporting Wear OS, provide square screenshots at 576 × 576 pixels.

---

## Image Guidelines

### Do:
- Use high-contrast text
- Ensure readability at small sizes
- Use the app's brand colors consistently
- Show actual app UI, not mockups

### Don't:
- Use placeholder images
- Show device bezels or notches
- Include promotional text or pricing
- Use screenshots from other apps
- Add borders or frames

---

## Naming Convention

All image files must follow this naming:

```
fastlane/
├── metadata/
│   └── android/
│       └── images/
│           ├── icon.png              # 512x512 app icon
│           ├── featureGraphic.png    # 1024x500 feature graphic
│           ├── screenshot_1.png       # Dashboard
│           ├── screenshot_2.png       # Treemap
│           ├── screenshot_3.png       # File List
│           ├── screenshot_4.png       # Duplicates
│           └── screenshot_5.png       # App Storage
```

---

## Fastlane Upload

To upload all metadata including images:

```bash
# Upload to Play Store
fastlane supply --package_name com.ivarna.adirstat --metadata_path fastlane/metadata/android

# Or use the deploy lanes
fastlane deploy_internal
```

---

## Compliance Notes

- All screenshots must accurately represent the app's current UI
- No device frames or misleading imagery
- Text must be in the app's supported languages
- Feature graphic must not include device images
