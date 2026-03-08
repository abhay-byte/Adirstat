# Features Documentation

This document lists all features for Adirstat with their status and IDs.

---

## Feature List

All features are grouped by module. Each feature has an ID (F-001 to F-037), name, description, and status.

---

## Partition & Overview (F-001 to F-005)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-001 | Enumerate Storage Volumes | Use StorageManager.getStorageVolumes() to list all partitions: internal, SD card, OTG | ✅ Complete |
| F-002 | Partition Usage Bar | Display horizontal usage bar showing used/free space ratio for each partition | ✅ Complete |
| F-003 | Partition Pie/Donut Chart | Visual donut chart showing partition usage breakdown | 🔴 Not Started |
| F-004 | Tap to Scan | Initiate scan when user taps on a partition card | ✅ Complete |
| F-005 | Last Scan Timestamp | Show last scan time for each partition on the dashboard | ✅ Complete |

---

## Scanning Engine (F-006 to F-012)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-006 | Recursive File Traversal | Use File API to recursively scan directories and calculate sizes | ✅ Complete |
| F-007 | MediaStore Fallback | Query MediaStore ContentProvider when MANAGE_EXTERNAL_STORAGE not granted | ✅ Complete |
| F-008 | Real-time Progress | Emit scan progress via Flow (files scanned, current path, %) | ✅ Complete |
| F-009 | Cancellable Scan | Allow user to cancel ongoing scan via coroutine cancellation | ✅ Complete |
| F-010 | Background Scan (WorkManager) | Schedule periodic scans via WorkManager | 🔴 Not Started |
| F-011 | Scan Result Caching | Cache scan results in Room database | ✅ Complete |
| F-012 | Rescan/Refresh | Button to force fresh scan, bypassing cache | 🔴 Not Started |

---

## Treemap Visualization (F-013 to F-017)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-013 | Squarified Treemap | Implement Ben Shneiderman's squarified treemap algorithm in pure Kotlin with grouped root density control, fewer empty tiny blocks, and readable labels | ✅ Complete |
| F-014 | Color by File Type | Color treemap blocks based on file category (images=green, video=red, etc.), including distinct app-data styling | ✅ Complete |
| F-015 | Tap to Drill Down | Navigate into folder when tapping its treemap block | ✅ Complete |
| F-016 | Breadcrumb Navigation | Show current path as breadcrumbs (Storage > Downloads > Videos) with correct root hiding, proper icons for virtual app nodes, and back-jump behavior | ✅ Complete |
| F-017 | Pinch-to-Zoom | Support pinch gestures to zoom treemap in/out | 🔴 Not Started |

---

## File List View (F-018 to F-022)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-018 | Sortable File List | Display flat list sorted by size (default: largest first) | ✅ Complete |
| F-019 | Sort Options | Sort by: size, name, date modified, extension (ascending/descending) | 🟡 In Progress |
| F-020 | Filter by Extension/Size/Date | Allow filtering by file extension, size range slider, date range | 🟡 In Progress |
| F-021 | Wildcard & Regex Search | Search with wildcards (*.mp4) and regex toggle from treemap/file-list scan screens across the active scanned scope plus virtual app-storage nodes | ✅ Complete |
| F-022 | File Type Grouping | Show file type breakdown with percentage bars | ✅ Complete |

---

## Duplicate Detection (F-023 to F-025)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-023 | Name + Size Duplicate Detection | Group files by (filename, size) for fast WizTree-style detection | 🔴 Not Started |
| F-024 | MD5 Hash Comparison | Optional second pass computing MD5 for content comparison | 🔴 Not Started |
| F-025 | Duplicate Group Display | Show duplicate groups with wasted space and batch delete option | 🔴 Not Started |

---

## App Storage Breakdown (F-026 to F-027)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-026 | Per-App Storage in Treemap | Show per-app protected storage as virtual nodes in treemap and list/search drill-down flows with APK/Data/Cache breakdown | ✅ Complete |
| F-027 | Open App Info | Open system App Info from app-storage entries in list and treemap detail sheets, plus visible list-row shortcuts, using a dedicated reusable shortcut component | ✅ Complete |
| F-036 | Virtual App Data Nodes | Display protected app-data nodes as virtual entries with lock styling and read-only drill-down | ✅ Complete |
| F-037 | Virtual App Breakdown Drill-Down | Drill into a virtual app node to inspect APK/Data/Cache segments | ✅ Complete |

---

## File Actions (F-028 to F-030)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-028 | Delete Single File | Delete file with confirmation dialog | 🔴 Not Started |
| F-029 | Batch Delete | Multi-select mode for deleting multiple files | 🔴 Not Started |
| F-030 | Share File | Share file via Android ShareSheet | 🔴 Not Started |

---

## Export & History (F-031 to F-032)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-031 | Export to CSV | Export scan results via ShareSheet | 🔴 Not Started |
| F-032 | Scan History | View past scans with storage change comparison | 🔴 Not Started |

---

## Settings & UX (F-033 to F-035)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-033 | Theme Selection | System/Light/Dark/Dynamic Color (Android 12+) | 🔴 Not Started |
| F-034 | Exclusion List | Add paths to exclude from scanning | 🔴 Not Started |
| F-035 | Minimum File Size Filter | Set minimum file size for treemap/list display | 🔴 Not Started |

---

## Feature Matrix

| Module | Features | Status |
|--------|----------|--------|
| Partition & Overview | F-001, F-002, F-003, F-004, F-005 | 5 🔴 |
| Scanning Engine | F-006, F-007, F-008, F-009, F-010, F-011, F-012 | 7 🔴 |
| Treemap Visualization | F-013, F-014, F-015, F-016, F-017 | 5 🔴 |
| File List View | F-018, F-019, F-020, F-021, F-022 | 5 🔴 |
| Duplicate Detection | F-023, F-024, F-025 | 3 🔴 |
| App Storage | F-026, F-027, F-036, F-037 | 4 ✅/🔴 mixed |
| File Actions | F-028, F-029, F-030 | 3 🔴 |
| Export & History | F-031, F-032 | 2 🔴 |
| Settings & UX | F-033, F-034, F-035 | 3 🔴 |

**Total: 37 features**

---

## Feature Dependencies

| Feature | Depends On |
|---------|-----------|
| F-013 (Squarified Treemap) | None - foundational |
| F-014 (Color by Type) | F-013 |
| F-015 (Tap to Drill Down) | F-013 |
| F-016 (Breadcrumb) | F-015 |
| F-017 (Pinch-to-Zoom) | F-013, F-015 |
| F-018 (Sortable List) | F-006 or F-007 |
| F-019 (Sort Options) | F-018 |
| F-020 (Filters) | F-018 |
| F-021 (Search) | F-018 |
| F-022 (File Type Grouping) | F-006 or F-007 |
| F-023 (Duplicate by Name+Size) | F-006 |
| F-024 (MD5 Duplicates) | F-023 |
| F-025 (Duplicate Display) | F-023, F-024 |
| F-026 (Per-App Storage in Treemap/List/Search) | QUERY_ALL_PACKAGES permission, StorageStatsManager |
| F-027 (Open App Info) | F-026 |
| F-036 (Virtual App Data Nodes) | F-026 |
| F-037 (Virtual App Breakdown Drill-Down) | F-036 |
| F-028 (Delete Single) | F-006 |
| F-029 (Batch Delete) | F-028 |
| F-030 (Share) | F-006 |
| F-031 (Export CSV) | F-006 or F-007 |
| F-032 (Scan History) | F-011 |
| F-033 (Theme) | None |
| F-034 (Exclusions) | F-006 |
| F-035 (Min File Size) | F-013, F-018 |

---

## Future Considerations (Out of Scope)

The following features are explicitly NOT in scope for version 1.0 but may be considered for future releases:

- Cloud storage scanning (Google Drive, Dropbox)
- Network share scanning (SMB/NAS)
- Automatic file cleaning suggestions
- Root mode for additional features
- File preview (images, videos)
- Compression/archiving features
- Sync with cloud backup services
