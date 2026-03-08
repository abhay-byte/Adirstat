# FINISHED - Completed Tasks

This document tracks all completed tasks for Adirstat.

---

## Completed Tasks

| Task ID | Description | Completed Date | Notes |
|---------|-------------|----------------|-------|
| TASK-000 | Project documentation initialized | 2026-03-07 | Created all documentation files in /docs folder |
| TASK-001 | Project scaffold (Hilt, Room, Compose, Navigation, DataStore setup) | 2026-03-07 | Android project with SDK 35/36, AGP 8.13.2, Kotlin 1.9.22, Hilt 2.50 |
| TASK-012 | Fastlane setup + all lanes | 2026-03-07 | Created Appfile, Fastfile, Pluginfile, metadata, README |
| TASK-014 | Complete app storage breakdown in treemap (F-037) | 2026-03-08 | Apps category with APK/Data/Cache + top 15 individual apps |
| TASK-031 | Sortable file list | 2026-03-08 | Dedicated list browsing supports direct drill-in and sorting |
| TASK-034 | Wildcard and regex search | 2026-03-08 | Search covers cached scan nodes and virtual app-storage nodes |
| TASK-039 | Expose virtual app-storage entries with sizes and APK/Data/Cache drill-down | 2026-03-08 | Root treemap/list/search now include protected app-storage nodes |
| TASK-040 | Open App Info from virtual app-storage entries | 2026-03-08 | Treemap and file-list bottom sheets open Android App Info |
| ROUND-13 | Visible inline App Info buttons | 2026-03-08 | Virtual app rows now show direct settings buttons in dedicated list mode and treemap list mode |
| ROUND-12 | Dedicated app-details shortcut component | 2026-03-08 | Shared `AppDetailsShortcutCard` now powers virtual app actions in treemap and file-list sheets |
| ROUND-7 | Root/data accuracy fixes | 2026-03-08 | Removed fake root nodes, fixed media totals, added virtual app layer, fixed list navigation, removed duplicate scan button |
| ROUND-8 | Treemap layout and breadcrumb fixes | 2026-03-08 | Correct root title, true totals, grouped density, and breadcrumb path |
| ROUND-9 | Dashboard/search/list improvements | 2026-03-08 | Added internal-storage spotlight, restored search, and completed app-aware browsing |
| ROUND-10 | Home search and tap reliability fixes | 2026-03-08 | Added home search CTA, refreshed search on resume, fixed list taps, enforced solid-surface styling |
| ROUND-11 | Treemap label priority and App Info actions | 2026-03-08 | Restored full-fit wrapped titles and added App Info shortcuts from virtual app nodes |
| TASK-CRASH | Crash Handler implementation | 2026-03-07 | CrashHandler + CrashActivity in separate process |

---

## How to Update This File

When a task is completed:
1. Move the task from TODO.md or ONGOING.md to FINISHED.md
2. Add the completion date
3. Add any relevant notes (commit hash, related tasks, etc.)

---

## Progress Summary

| Metric | Count |
|--------|-------|
| Total Tasks | Backlog tracked in TODO.md |
| Completed | See completed table above |
| In Progress | 0 |
| Remaining | See TODO.md for backlog |

---

## Notes

- TASK-000 is complete
- TASK-001 is complete
- TASK-012 is complete
- TASK-CRASH is complete
- Round 7 through Round 11 fixes are complete and documented
- This file will grow as more features are implemented
