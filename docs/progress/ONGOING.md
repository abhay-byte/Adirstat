# ONGOING - In Progress Tasks

This document tracks tasks currently being worked on.

---

## Currently In Progress

| Task ID | Description | Feature ID | Notes |
|---------|-------------|------------|-------|
| - | UI Bug fixes Round 4 | - | Permission screen, treemap colors, breadcrumb, search fixes |

---

## Recently Completed (March 7, 2026)

### Bug Fix Round 4 - Permissions, Treemap, Search
- Fixed PermissionScreen - created new permission screen with sequential requests
- Fixed Navigation.kt - Permission as start destination, FileList folder navigation
- Fixed FileTypeColorMapper - recursive color detection for directories
- Fixed TreemapViewModel - breadcrumb duplicate prevention
- Fixed SearchScreen - improved empty states and icons
- Fixed TreemapScreen - color legend padding

### Bug Fix Round 3 - Storage + Android/data Restriction
- Fixed StorageStatsDataSource with queryStatsForPackage for accurate per-app storage
- Fixed StorageRepository to combine file scan + StorageStatsManager data
- Fixed Dashboard to show multi-segment storage bar (Apps/Media/Files/Free)
- Fixed Search to load from cached scan results
- Fixed FileList navigation: folder tap navigates, file tap shows sheet
- Added virtual Android/data nodes to treemap via ScanStorageUseCase
- Updated PERMISSIONS.md with Android/data restriction documentation
- Updated SDD.md with new data sources
- Updated UI_UX_DOCUMENTATION.md with new Dashboard and Treemap sections
- Updated FEATURES.md: F-026, F-027 now 🟡 In Progress, added F-036

### Bug Fixes Completed
- Fixed Compose version incompatibility causing crash on CircularProgressIndicator
- Fixed Dashboard UI: storage bar, empty state, FAB, scan staleness indicator
- Fixed Treemap colors at root level (all blocks same color issue)
- Fixed Treemap labels in subfolders  
- Fixed info bar styling and clickable breadcrumbs
- Fixed list view toggle functionality

### Features Completed
- F-001: Storage volume enumeration
- F-002: Partition usage bar
- F-004: Tap to scan
- F-005: Last scan timestamp
- F-006: Recursive file traversal
- F-007: MediaStore fallback
- F-008: Real-time progress
- F-009: Cancellable scan
- F-011: Scan result caching
- F-013: Squarified treemap algorithm
- F-014: Color by file type
- F-015: Tap to drill down
- F-016: Breadcrumb navigation
- F-018: Sortable file list
- F-022: File type grouping

---

## How to Update This File

When starting a new task:
1. Add it to "Currently In Progress" section with Task ID, Description, Feature ID, and Notes
2. Include your name/handle and start date

When completing a task:
1. Move it from "Currently In Progress" to "Recently Completed" with completion date
2. Update the status in TODO.md
3. If it's a feature, update the status in FEATURES.md

---

Last updated: March 7, 2026
