# Software Requirements Specification (SRS)

## 1. Introduction

### 1.1 Purpose
This document defines the software requirements for **Adirstat**, a disk space analyzer for Android that provides WizTree/WinDirStat-equivalent functionality. The app scans all storage partitions and presents an interactive breakdown of file/folder space consumption through treemap visualization and detailed lists.

### 1.2 Scope
Adirstat is a standalone Android application that:
- Enumerates all storage partitions (internal, SD card, OTG)
- Recursively scans files and folders to calculate sizes
- Displays results via interactive treemap and sortable lists
- Detects duplicate files
- Shows per-app storage breakdown
- Exports results to CSV
- Operates fully offline with no network permissions

### 1.3 Definitions, Acronyms, and Abbreviations

| Term | Definition |
|------|------------|
| Treemap | A visualization method showing hierarchical data as nested rectangles |
| MANAGE_EXTERNAL_STORAGE | Special Android permission for full file system access |
| MediaStore | Android ContentProvider for media file metadata |
| StorageStatsManager | Android API for querying partition and app storage statistics |
| Room | Local SQLite database for scan history and cache |
| UseCase | Business logic layer in Clean Architecture |
| MVVM | Model-View-ViewModel architecture pattern |

---

## 2. Overall Description

### 2.1 Product Perspective
Adirstat is a self-contained Android application. It does not connect to any backend service, requires no authentication, and stores all data locally on the device.

### 2.2 Product Features Summary

| Feature Category | Key Capabilities |
|-----------------|-------------------|
| Storage Scanning | Full recursive scan, MediaStore fallback, background scan support |
| Visualization | Interactive treemap, color-coded by file type, full-fit wrapped labels, drill-down navigation |
| File Analysis | Large files list, file type breakdown, duplicate detection |
| App Analysis | Per-app storage breakdown (APK + data + cache) with direct App Info shortcuts |
| Data Management | Delete files, batch operations, export to CSV |
| History | Scan history with storage change comparison |
| Customization | Theme selection, exclusion paths, minimum file size filter |

### 2.3 Target Users
- Android power users wanting detailed storage insight
- Developers managing device storage
- General users wanting to reclaim space
- Users on devices with limited storage

---

## 3. Functional Requirements

### 3.1 SCAN Module

#### FR-SCAN-01: Enumerate Storage Volumes
The app shall enumerate all available storage volumes using `StorageManager.getStorageVolumes()`, including:
- Primary internal storage
- SD card (if inserted)
- USB OTG storage (if connected)

#### FR-SCAN-02: Recursive File System Traversal
The app shall perform recursive file system traversal using the File API when MANAGE_EXTERNAL_STORAGE is granted, calculating:
- Total size of each directory (sum of all contained files)
- File count per directory
- Modification timestamps

#### FR-SCAN-03: MediaStore Fallback Scan
When full storage access is not granted, the app shall query MediaStore ContentProvider to retrieve available file metadata, grouped by:
- Media category (Images, Video, Audio, Documents)
- File size and date

#### FR-SCAN-04: Progress Reporting with Cancellation
The app shall report scan progress via a Kotlin Flow, emitting:
- Files scanned count
- Current directory path being scanned
- Percentage complete (based on estimated total)
- Estimated time remaining

The scan shall be cancellable via a coroutine cancellation mechanism.

#### FR-SCAN-05: Scan Result Caching
The app shall cache scan results in Room database to avoid re-scanning unchanged partitions. Cache shall be invalidated when:
- User explicitly requests rescan
- Partition is unmounted and remounted
- 7 days have elapsed since cache creation

#### FR-SCAN-06: Rescan/Refresh
The app shall provide a refresh button on each partition to force a new scan, bypassing the cache.

#### FR-SCAN-07: Background Scan via WorkManager
The app shall support scheduling background scans via WorkManager for:
- Automatic periodic scanning (user-configurable interval: daily/weekly/monthly)
- Notification when significant storage change detected

#### FR-SCAN-08: Scan Performance Target
The app shall complete a full scan of a 64GB partition in under 30 seconds on a mid-range device (e.g., Pixel 6a equivalent).

---

### 3.2 TREEMAP Module

#### FR-TREE-01: Squarified Treemap Rendering
The app shall render a squarified treemap (Ben Shneiderman algorithm) using Compose Canvas API, where:
- Rectangle area is proportional to file/folder size
- Aspect ratio is near-square for optimal readability

#### FR-TREE-02: Color Coding by File Type
The app shall color treemap blocks by file type category:
- Images: Green (#4CAF50)
- Video: Red (#F44336)
- Audio: Purple (#9C27B0)
- Documents: Orange (#FF9800)
- Archives/APK: Brown (#795548)
- Code/Text: Cyan (#00BCD4)
- Other: Blue-grey (#607D8B)

#### FR-TREE-03: Tap to Drill Down
Tapping a treemap block representing a folder or virtual app-storage node shall navigate into that node, loading its children into the treemap.

#### FR-TREE-04: Breadcrumb Navigation
The app shall display a breadcrumb bar showing current path (e.g., "Storage > Downloads > Videos"). Tapping any segment shall navigate directly to that level.

#### FR-TREE-05: Pinch-to-Zoom
The treemap shall support pinch-to-zoom gestures to zoom in/out of the current view.

#### FR-TREE-06: Full-Fit Labels Only
Treemap labels shall obey the following readability rules:
- Node titles may wrap across up to 3 lines when space allows
- No treemap label may be ellipsized or partially truncated
- Size and percentage metadata shall only render when the full node title also fits
- Nodes too small to present useful content shall be grouped into `Others` instead of rendering as empty blocks

---

### 3.3 FILELIST Module

#### FR-LIST-01: Sortable File List
The app shall display a browsable list of scanned files, folders, and virtual app-storage directories sorted by size (largest first by default). Sorting options shall include:
- Size (ascending/descending)
- Name (A-Z/Z-A)
- Date modified (newest/oldest)
- File extension

#### FR-LIST-02: Filters
The list shall support filtering by:
- File extension (e.g., .mp4, .jpg)
- Size range (minimum/maximum bytes)
- Date modified range

#### FR-LIST-03: Wildcard and Regex Search
The app shall support search with:
- Wildcard pattern (e.g., `*.mp4`, `backup_*`)
- Regular expression toggle for advanced users
- Matches over both cached scan nodes and virtual app-storage nodes

#### FR-LIST-04: Large Files Quick View
The app shall provide a quick-access view showing the top 100 largest files across the scanned partition.

#### FR-LIST-05: File Type Grouping
The app shall display file type groupings with percentage bars showing space consumption by category (Images, Videos, Audio, Documents, etc.).

#### FR-LIST-06: List Item Details
Each list row shall display:
- File type icon
- File/folder name
- Size (formatted: KB/MB/GB)
- Date modified
- Full path (truncated with ellipsis)

---

### 3.4 DUPLICATE Module

#### FR-DUP-01: Detect Duplicates by Name + Size
The app shall identify duplicate files by matching:
- Identical file name
- Identical file size

This is a fast, WizTree-style detection method.

#### FR-DUP-02: Optional MD5 Hash Comparison
For more accurate duplicate detection, the app shall compute MD5 hashes for candidate duplicates and group only files with matching hashes.

#### FR-DUP-03: Display Duplicate Groups
The app shall display duplicate file groups showing:
- Original file (marked)
- Duplicate files with size
- Total wasted space per group

#### FR-DUP-04: Selective Delete from Duplicates
From a duplicate group, users shall be able to select which files to delete, with clear indication of which is the "original" vs. "duplicate."

---

### 3.5 PARTITION Module

#### FR-PART-01: List All Partitions
The app shall display all available partitions:
- Internal storage (primary)
- SD card (if present)
- OTG USB storage (if connected)

#### FR-PART-02: Partition Usage Display
Each partition card shall show:
- Used space (formatted)
- Free space (formatted)
- Total space (formatted)
- Visual usage bar (used/free ratio)

#### FR-PART-03: Tap to Scan
Tapping a partition card shall initiate a scan of that partition.

#### FR-PART-04: Last Scan Timestamp
Each partition shall display the timestamp of the last successful scan.

---

### 3.6 APPS Module

#### FR-APP-01: List Installed Apps
The app shall list all installed apps using `QUERY_ALL_PACKAGES` permission, displaying:
- App icon
- App name
- Package name
- Total storage (APK + data + cache)

#### FR-APP-02: Sort by Total Size
The app list shall be sortable by:
- Total size (largest first default)
- APK size
- Data size
- Cache size

#### FR-APP-03: Open App Settings
Tapping an app, choosing the app-details action from a virtual app-storage node in the treemap or file list, or using the dedicated visible App Info shortcut shown on virtual app rows in list mode shall open the system App Info page for that app, allowing users to clear data/cache or uninstall.

---

### 3.7 DELETE Module

#### FR-DEL-01: Delete Single File
The app shall support deleting a single file with confirmation dialog showing file name and size.

#### FR-DEL-02: Batch Delete
The app shall support multi-select mode where users can select multiple files/folders for batch deletion.

#### FR-DEL-03: Delete via MediaStore
On API 30+, the app shall use MediaStore.delete() to request file deletion, which moves files to a pending deletion state handled by the MediaStore.

---

### 3.8 EXPORT Module

#### FR-EXP-01: Export Scan Results to CSV
The app shall export the current scan results to CSV format via Android ShareSheet. CSV shall include:
- File/folder path
- Size in bytes
- Size formatted
- Type (file/folder)
- Last modified date

#### FR-EXP-02: Export File Type Summary
The app shall optionally export a summary of space usage by file type (category, total size, file count, percentage).

---

### 3.9 HISTORY Module

#### FR-HIST-01: Store Past Scans
The app shall store scan history in Room database, recording for each scan:
- Partition path
- Scan date/time
- Total bytes
- Free bytes at scan time
- File count
- Folder count
- Scan duration

#### FR-HIST-02: Compare with Previous Scan
The app shall calculate and display storage change between current and previous scan, showing:
- Total change (GB up/down)
- Per-folder change (if folder still exists)

---

### 3.10 SETTINGS Module

#### FR-SET-01: Theme Selection
The app shall allow users to select theme:
- System (follows device setting)
- Light
- Dark
- Dynamic Color (Android 12+ Material You)

#### FR-SET-02: Scan Exclusion List
The app shall allow users to add paths to exclude from scanning (e.g., /storage/emulated/0/Android/data).

#### FR-SET-03: Minimum File Size Filter
The app shall allow users to set a minimum file size for display in treemap and lists (e.g., hide files < 1MB).

#### FR-SET-04: Clear Scan Cache
The app shall provide a button to clear all cached scan results, forcing a fresh scan on next access.

---

## 4. Non-Functional Requirements

### 4.1 Performance
- Scan 64GB in < 30 seconds (mid-range device)
- Scan 128GB in < 60 seconds
- Treemap render < 500ms
- App launch < 2 seconds (cold start)

### 4.2 Memory
- Peak memory during scan < 300MB
- Handle 500,000+ files without crash

### 4.3 Reliability
- No data loss during scan (atomic writes)
- Graceful handling of storage removal (SD card ejected)
- Crash recovery with state preservation

### 4.4 Security
- No network permissions
- No data transmission
- No analytics SDKs
- All data local only

### 4.5 Compatibility
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36 (Android 14+)
- Tested on: Pixel, Samsung, OnePlus, Xiaomi, Motorola devices

---

## 5. User Interface Requirements

### 5.1 Screens
The app shall include the following screens:
1. Permission Rationale Screen
2. All Files Access Screen
3. Dashboard / Partition Overview Screen
4. Scan Progress Screen
5. Treemap Screen
6. File List Screen
7. File Type Breakdown Screen
8. Duplicate Files Screen
9. App Storage Screen
10. File Detail Bottom Sheet
11. Scan History Screen
12. Search Screen
13. Settings Screen

(See [UI_UX_DOCUMENTATION.md](UI_UX_DOCUMENTATION.md) for detailed screen specifications)

### 5.2 Navigation
- Bottom Navigation for main sections (Dashboard, Apps, History, Settings)
- Navigation from Dashboard → Partition → Scan → Treemap → Drill-down
- Global search accessible from all screens

---

## 6. Data Requirements

### 6.1 Local Storage
- Room database for scan history and cache
- DataStore for user preferences
- No external storage requirements

### 6.2 Database Schema
- `scan_history`: id, partition_path, scan_date, total_bytes, free_bytes, file_count, folder_count, duration_ms
- `scan_cache`: id, scan_history_id (FK), serialized_tree_json, created_at
- `user_exclusions`: id, path, created_at

---

## 7. Dependencies

### 7.1 Android Libraries
- Jetpack Compose with Material 3
- Room Database
- Hilt Dependency Injection
- Navigation Compose
- DataStore Preferences
- WorkManager

### 7.2 No External Dependencies
- No network libraries
- No analytics
- No advertising SDKs
- Treemap implemented with pure Kotlin + Compose Canvas

---

## 8. Constraints

- Must not require root access
- Must work with MANAGE_EXTERNAL_STORAGE permission only (not root)
- Must be fully offline
- Must not include any advertising
- Must not require account creation

---

## 9. Appendix: Requirement ID Reference

| Module | Requirements |
|--------|--------------|
| SCAN | FR-SCAN-01 through FR-SCAN-08 |
| TREEMAP | FR-TREE-01 through FR-TREE-06 |
| FILELIST | FR-LIST-01 through FR-LIST-06 |
| DUPLICATE | FR-DUP-01 through FR-DUP-04 |
| PARTITION | FR-PART-01 through FR-PART-04 |
| APPS | FR-APP-01 through FR-APP-03 |
| DELETE | FR-DEL-01 through FR-DEL-03 |
| EXPORT | FR-EXP-01 through FR-EXP-02 |
| HISTORY | FR-HIST-01 through FR-HIST-02 |
| SETTINGS | FR-SET-01 through FR-SET-04 |

Total: **44 functional requirements**
