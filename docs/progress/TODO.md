# TODO - Task List

This document contains all tasks to be completed for Adirstat.

---

## Setup Tasks

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-000 | Project documentation initialized | - | ✅ Complete |
| TASK-001 | Project scaffold (Hilt, Room, Compose, Navigation, DataStore setup) | - | ✅ Complete |
| TASK-002 | PermissionManager.kt — full multi-API-level permission logic | - | ✅ Complete |
| TASK-003 | StorageVolume enumeration using StorageManager | F-001 | ✅ Complete |
| TASK-004 | FileSystemDataSource recursive scan with coroutine + Flow | F-006 | ✅ Complete |
| TASK-005 | MediaStoreDataSource fallback | F-007 | ✅ Complete |
| TASK-006 | StorageStatsDataSource (StorageStatsManager wrapper) | F-001 | ✅ Complete |
| TASK-007 | Room DB setup (3 tables: scan_history, scan_cache, user_exclusions) | F-011 | ✅ Complete |
| TASK-008 | Squarified Treemap layout algorithm (pure Kotlin, unit testable) | F-013 | ✅ Complete |
| TASK-009 | Treemap Canvas renderer in Compose | F-013 | ✅ Complete |
| TASK-010 | Unit tests for TreemapLayoutEngine | F-013 | 🔴 Not Started |
| TASK-011 | Unit tests for ScanStorageUseCase | F-006 | 🔴 Not Started |
| TASK-012 | Fastlane setup + all lanes | - | ✅ Complete |
| TASK-013 | Google Play Permissions Declaration Form — document requirement for MANAGE_EXTERNAL_STORAGE and QUERY_ALL_PACKAGES | - | 🔴 Not Started |
| TASK-014 | Complete app storage breakdown in treemap (F-037) - Apps category with APK/Data/Cache + individual apps | F-037 | ✅ Complete |

---

## Feature Tasks

### Partition & Overview (F-001 to F-005)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-014 | Enumerate all storage volumes | F-001 | ✅ Complete |
| TASK-015 | Partition usage bar component | F-002 | ✅ Complete |
| TASK-016 | Partition pie/donut chart | F-003 | 🔴 Not Started |
| TASK-017 | Tap partition to scan | F-004 | ✅ Complete |
| TASK-018 | Last scan timestamp display | F-005 | ✅ Complete |

### Scanning Engine (F-006 to F-012)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-019 | Recursive file traversal with File API | F-006 | ✅ Complete |
| TASK-020 | MediaStore fallback implementation | F-007 | ✅ Complete |
| TASK-021 | Real-time scan progress via Flow | F-008 | ✅ Complete |
| TASK-022 | Cancellable scan with coroutine | F-009 | ✅ Complete |
| TASK-023 | Background scan via WorkManager | F-010 | 🔴 Not Started |
| TASK-024 | Scan result caching in Room | F-011 | ✅ Complete |
| TASK-025 | Rescan/refresh functionality | F-012 | 🔴 Not Started |

### Treemap Visualization (F-013 to F-017)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-026 | Implement squarified treemap algorithm | F-013 | ✅ Complete |
| TASK-027 | Color coding by file type | F-014 | ✅ Complete |
| TASK-028 | Tap to drill down | F-015 | ✅ Complete |
| TASK-029 | Breadcrumb navigation | F-016 | ✅ Complete |
| TASK-030 | Pinch-to-zoom gesture | F-017 | 🔴 Not Started |

### File List View (F-018 to F-022)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-031 | Sortable file list | F-018 | ✅ Complete |
| TASK-032 | Sort options (size/name/date/type) | F-019 | 🔴 Not Started |
| TASK-033 | Filter by extension/size/date | F-020 | 🔴 Not Started |
| TASK-034 | Wildcard and regex search | F-021 | ✅ Complete |
| TASK-035 | File type grouping view | F-022 | 🔴 Not Started |

### Duplicate Detection (F-023 to F-025)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-036 | Duplicate detection by name + size | F-023 | 🔴 Not Started |
| TASK-037 | Optional MD5 hash comparison | F-024 | 🔴 Not Started |
| TASK-038 | Duplicate group display with delete | F-025 | 🔴 Not Started |

### App Storage (F-026 to F-027)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-039 | Expose virtual app-storage entries with sizes and APK/Data/Cache drill-down | F-026, F-036, F-037 | ✅ Complete |
| TASK-040 | Open App Info from virtual app-storage entries | F-027 | ✅ Complete |

### File Actions (F-028 to F-030)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-041 | Delete single file with confirmation | F-028 | 🔴 Not Started |
| TASK-042 | Batch delete functionality | F-029 | 🔴 Not Started |
| TASK-043 | Share file via ShareSheet | F-030 | 🔴 Not Started |

### Export & History (F-031 to F-032)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-044 | Export scan results to CSV | F-031 | ✅ Complete |
| TASK-045 | Scan history with comparison | F-032 | ✅ Complete |

### Settings & UX (F-033 to F-035)

| Task ID | Description | Feature ID | Status |
|---------|-------------|------------|--------|
| TASK-046 | Theme selection (system/light/dark/dynamic) | F-033 | ✅ Complete |
| TASK-047 | Scan exclusion list | F-034 | ✅ Complete |
| TASK-048 | Minimum file size filter | F-035 | ✅ Complete |

---

## Task Count Summary

Task tables above are the authoritative source of status. The March 8 storage-visualization, search, list, and virtual app-storage work is now complete; remaining backlog items stay marked `🔴 Not Started` until implementation begins.

---

## How to Update This File

When a task status changes:
1. Find the task in this file
2. Update the status column
3. Move the task to ONGOING.md (if in progress) or FINISHED.md (if complete)
4. Update the README.md master index if the task is significant

---

## Notes

- Feature IDs (F-001 to F-037) correspond to FEATURES.md
- TASK-000 is documentation-only and is already complete
- TASK-001 is the main project setup task
- All other tasks are feature implementation tasks
