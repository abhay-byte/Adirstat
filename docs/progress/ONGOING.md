# ONGOING - In Progress Tasks

This document tracks tasks currently being worked on.

---

## Currently In Progress

No active round tasks. The storage visualization, list browsing, search, and virtual app-storage fixes are complete and the remaining backlog has moved back to [progress/TODO.md](TODO.md).

---

## Recently Completed (March 8, 2026)

### Bug Fix Round 19 - Scan Back Confirmation
- Added confirmation dialogs before leaving active scan progress from dashboard and treemap screens

### Bug Fix Round 18 - List Multi-Select
- Added multi-select mode to the dedicated file list and treemap list view
- Added a top-bar `Select all` action for the current visible list while selection mode is active
- Highlighted selected rows and used back/close to exit selection mode cleanly

### Bug Fix Round 17 - Release Docs Polish
- Documented the mandatory major-change release workflow in the docs index
- Fixed GitHub release notes to use a GitHub-hosted icon URL instead of a local relative path

### Bug Fix Round 16 - Scoped Search + Release Shrink
- Removed the dashboard home search entry points so search is only launched from scan screens
- Scoped search to the active scanned path while keeping virtual app-storage results available inside internal storage scans
- Enlarged the dashboard top bar logo and enabled release shrinking/resource trimming for a much smaller APK

### Bug Fix Round 15 - Launcher Icon Refresh
- Replaced the default launcher artwork with the provided Downloads logo asset
- Generated legacy `mipmap-*` launcher icons plus adaptive foreground artwork for Android 8+
- Switched the adaptive launcher background to transparent so the white logo stays visible
- Replaced the dashboard top bar text title with the same app logo

### Bug Fix Round 14 - Icon Cleanup + Tiny Node Grouping
- Replaced emoji-based virtual app markers with proper icons in app bars and breadcrumbs
- Kept long app-bar titles to a single line with ellipsis instead of overflow
- Grouped tiny treemap nodes more aggressively so unreadable empty blocks are folded into `Others`

### Bug Fix Round 13 - Visible App Info Buttons
- Added direct trailing settings buttons on virtual app rows in dedicated list mode and treemap list mode so App Info is discoverable without long-press

### Bug Fix Round 12 - Dedicated App Details Component
- Added a reusable `AppDetailsShortcutCard` component for virtual app nodes in both treemap and file-list detail sheets

### Bug Fix Round 11 - Treemap Labels + App Info Actions
- Restored treemap node title priority with full-fit wrapped labels and no size-only labels
- Added `Open app details` actions for virtual app-storage nodes from both treemap and file-list bottom sheets

### Bug Fix Round 10 - Home Search + Reliable List Taps
- Added a direct dashboard home search action after scan completion
- Refreshed search index on resume so newly scanned content appears immediately
- Replaced conflicting row handlers with combined click handling for file-list and treemap list mode
- Switched dashboard spotlight styling back to solid Material surfaces per the design system

### Bug Fix Round 9 - Dashboard/Search/List Improvements
- Added the internal-storage spotlight section with summary pills and improved hierarchy
- Restored global search functionality and app indexing
- Completed app-aware browsing in both dedicated list view and treemap list mode

### Bug Fix Round 8 - Treemap Layout + Breadcrumb Accuracy
- Fixed treemap title, real total bytes, grouped density, and breadcrumb correctness

### Bug Fix Round 7 - Root/Data Accuracy
- Removed fake root `System` and `Apps` nodes
- Fixed dashboard media totals from `MediaStore`
- Added virtual protected app-data nodes with drill-down
- Fixed single-tap folder navigation and removed the duplicate scan button

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
- F-021: Wildcard and regex search
- F-022: File type grouping
- F-026: Per-app protected storage browsing
- F-027: Open App Info from virtual app-storage entries
- F-036: Virtual app data nodes
- F-037: Virtual app breakdown drill-down

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

Last updated: March 8, 2026
