# Problem Statement

## Background

Android users accumulate gigabytes of files over time — downloads, cached media from apps, duplicate photos, old APK files, large videos, WhatsApp media, screenshots, and system caches. Modern smartphones ship with 128GB, 256GB, or even 512GB of storage, but users frequently find themselves running out of space with no clear understanding of what's consuming it.

Android's built-in Storage Settings page (Settings > Storage) shows only top-level categories like "Apps," "Images," "Videos," "Audio," "Documents," and "Other." This provides a high-level overview but offers no drill-down capability. Users cannot see which specific folder or file is the actual space hog. For example, a user might see "Videos: 15GB" but has no way to know that 10GB is consumed by a single downloaded movie in the Downloads folder.

On desktop operating systems like Windows, tools like **WizTree** and **WinDirStat** have existed for years, providing instant treemap visualization of disk space consumption. These tools recursively scan directories, calculate file sizes, and display the results as an interactive treemap where larger files appear as larger rectangles. Users can instantly spot the largest files and navigate into folders to find space hogs.

**No equivalent tool exists for Android.** Existing "storage analyzer" apps on the Google Play Store suffer from:
- Excessive advertising (interstitial ads, banner ads, native ads)
- Mandatory account creation or cloud sync requirements
- Limited functionality (only scan media files, not all files)
- Poor performance (slow scans, memory-intensive)
- Privacy concerns (uploading data, requiring unnecessary permissions)

This gap leaves Android power users without a essential tool that Windows and macOS users have enjoyed for decades.

---

## Problem Definition

**Primary Problem:** Android users have no efficient way to visualize and understand what files and folders are consuming their device storage. The built-in Settings app provides only top-level categorization with no drill-down capability.

**Secondary Problems:**
1. Existing third-party storage apps are cluttered with ads, require accounts, or only show media files
2. No Android app provides WizTree-style treemap visualization of the entire file system
3. Duplicate files accumulate silently with no built-in detection or removal tool
4. Users cannot easily identify large files (>100MB) that could be deleted or moved
5. Per-app storage breakdown requires navigating through multiple system Settings screens
6. No scan history to track storage changes over time

---

## Goals

### Primary Goal
Provide a fast, ad-free, offline, no-account storage analyzer that scans all storage partitions on an Android device and presents a complete, interactive breakdown of which files and folders consume space — equivalent to WizTree on Windows.

### Secondary Goals
1. **Visual Clarity:** Use treemap visualization to show file/folder sizes at a glance
2. **Comprehensive Coverage:** Scan ALL files and folders, not just media (requires MANAGE_EXTERNAL_STORAGE permission)
3. **Performance:** Complete a full scan of a 64GB partition in under 30 seconds on a mid-range device
4. **Drill-Down:** Allow users to navigate from the root of a partition into any folder hierarchy
5. **Duplicate Detection:** Identify duplicate files to help users reclaim space
6. **Per-App Stats:** Show storage consumption by installed application (APK + data + cache)
7. **Export:** Allow users to export scan results for further analysis
8. **Privacy:** Remain fully offline with no network permissions, no accounts, no data upload

---

## Target Users

| User Type | Description | Needs |
|-----------|-------------|-------|
| Power Users | Tech enthusiasts who install many apps, customize their device | Deep insight into storage, ability to delete any file |
| Developers | Android developers testing apps, generating large build artifacts | Identify large build files, cached data, APKs |
| Content Creators | Photographers, videographers with large media files | Find large videos, duplicate photos, backup files |
| Budget Users | Users on devices with limited storage (16GB/32GB) | Maximize available space, identify bloat |
| General Users | Anyone curious about where their storage went | Simple, visual understanding of storage |

---

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Scan Speed (64GB) | < 30 seconds | Benchmark on Pixel 6a / Samsung A52 |
| Scan Speed (128GB) | < 60 seconds | Benchmark on mid-range device |
| App Launch Time | < 2 seconds | Cold start to interactive UI |
| Memory Usage During Scan | < 300MB | Peak RSS during active scan |
| Treemap Render Time | < 500ms | Time to render after scan completes |
| File Count Limit | Handle 500,000+ files | Without crashing or excessive memory |
| APK Size | < 15MB | Final debug APK size |
| Play Store Rating | 4.5+ | Target after 1000+ ratings |
| Crash Rate | < 0.1% | ANR + Crash rate on Play Console |

---

## Out of Scope

The following are explicitly NOT part of this project:

1. **Cloud Storage Analysis** — App will not scan or analyze cloud storage (Google Drive, Dropbox, OneDrive)
2. **Network Shares** — App will not scan network-attached storage (NAS) or SMB shares
3. **Internal App Data** — App will not analyze or display other apps' private internal data (only total size via StorageStatsManager)
4. **File Editing** — App will not rename, move, or modify files (only delete)
5. **Automatic Cleaning** — App will not automatically delete files; all deletions require user confirmation
6. **Root Required** — App will function without root using MANAGE_EXTERNAL_STORAGE permission
7. **Ads / Monetization** — App will remain free and ad-free indefinitely
8. **Accounts / Cloud Sync** — No account required; all data stays local on device

---

## Constraints

### Technical Constraints
- Must work on Android 7.0+ (API 24+) — covers 99%+ of active devices
- Must target Android 14+ (API 36) for latest platform features
- Must comply with Google Play Store policies, especially for MANAGE_EXTERNAL_STORAGE and QUERY_ALL_PACKAGES
- Must handle device heterogeneity: different manufacturers, storage sizes, SD card support, OTG support

### Regulatory Constraints
- Must not collect or transmit any user data
- Must not include any analytics SDKs that send data externally
- Must declare all permissions clearly in the app and Play Store listing
- Must comply with GDPR (no data collection means compliant by design)

### Resource Constraints
- Must not consume excessive battery during scans
- Must not hold wakelocks unnecessarily
- Must handle low-storage situations gracefully
- Must handle external storage removal (SD card ejected) gracefully

---

## User Journey

1. **First Launch:** User sees Permission Rationale screen explaining why permissions are needed
2. **Permission Grant:** User grants MANAGE_EXTERNAL_STORAGE (or limited media permissions)
3. **Dashboard:** App shows all storage partitions with usage bars
4. **Scan:** User taps a partition to initiate scan
5. **Treemap:** Scan completes, treemap visualization renders
6. **Exploration:** User taps treemap blocks to drill into folders
7. **Action:** User identifies large files, duplicates, or unwanted content
8. **Delete:** User deletes files to reclaim space
9. **Export:** User optionally exports results to CSV for reporting
10. **History:** User can revisit past scans to track storage changes
