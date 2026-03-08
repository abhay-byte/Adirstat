# Adirstat Documentation

> **AGENT STRICT RULE: Whenever ANY feature is added, modified, or removed — ANY screen layout changes — ANY database schema changes — ANY permission strategy changes — ANY architecture changes — ANY task status changes — you MUST immediately update EVERY affected document. Do NOT leave any document stale. After every change, re-read this README and confirm all listed documents reflect the exact current state of the app.**

## Master Index

| File | Description | Last Updated |
|------|-------------|--------------|
| [README.md](README.md) | Master index and change tracking rules | 2026-03-08 |
| [PROBLEM_STATEMENT.md](PROBLEM_STATEMENT.md) | Background, problem definition, goals, and success metrics | 2026-03-07 |
| [PERMISSIONS.md](PERMISSIONS.md) | Complete permission strategy, API-level handling, Play Store compliance | 2026-03-07 |
| [SRS.md](SRS.md) | Software Requirements Specification with all functional requirements | 2026-03-08 |
| [SDD.md](SDD.md) | Software Design Document with architecture and technical decisions | 2026-03-08 |
| [FEATURES.md](FEATURES.md) | Complete feature list with IDs F-001 through F-037 | 2026-03-08 |
| [UI_UX_DOCUMENTATION.md](UI_UX_DOCUMENTATION.md) | All 13 screen specifications | 2026-03-08 |
| [UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) | Design values, typography, colors, components | 2026-03-07 |
| [DIAGRAMS.md](DIAGRAMS.md) | All 13 Mermaid diagrams | 2026-03-08 |
| [CRASH_HANDLER.md](CRASH_HANDLER.md) | Crash handling implementation for Android 16 | 2026-03-07 |
| [progress/TODO.md](progress/TODO.md) | Task tracking for all features and setup | 2026-03-08 |
| [progress/ONGOING.md](progress/ONGOING.md) | Currently in-progress tasks | 2026-03-08 |
| [progress/FINISHED.md](progress/FINISHED.md) | Completed tasks | 2026-03-08 |
| [release/v1.0.1.md](release/v1.0.1.md) | GitHub release notes for version 1.0.1 | 2026-03-08 |
| [fastlane/README.md](../fastlane/README.md) | Fastlane CI/CD setup guide | 2026-03-07 |
| [fastlane/Appfile](../fastlane/Appfile) | Fastlane package configuration | 2026-03-07 |
| [fastlane/Fastfile](../fastlane/Fastfile) | Fastlane lanes for CI/CD | 2026-03-07 |

---

## Change Type → Documents Mapping

When making changes to the app, immediately update ALL affected documents:

| Change Type | Documents to Update |
|-------------|---------------------|
| New Feature | FEATURES.md (add new feature with next ID), SRS.md (add FR-* requirement), UI_UX_DOCUMENTATION.md (if new screen), DIAGRAMS.md (if new flow/entity), progress/TODO.md (add TASK) |
| Feature Changed | FEATURES.md (update status), SRS.md (update FR-* requirement), SDD.md (if architecture affected) |
| Feature Removed | FEATURES.md (mark as Removed), SRS.md (mark FR-* as obsolete) |
| New Screen | UI_UX_DOCUMENTATION.md (add screen section), DIAGRAMS.md (update flow), FEATURES.md (if feature-driven) |
| Screen Changed | UI_UX_DOCUMENTATION.md (update screen specs), DIAGRAMS.md (if flow affected) |
| Database Schema Change | SDD.md (update Room schema section), SRS.md (if requirement affected), DIAGRAMS.md (update ER diagram) |
| Permission Strategy Change | PERMISSIONS.md (update permission table and flows), SRS.md (update FR-*), AndroidManifest.xml |
| Architecture Change | SDD.md (update architecture section), DIAGRAMS.md (update class/sequence diagrams), SRS.md |
| Bug Fixed | FEATURES.md (if affects feature behavior), SRS.md (if requirement affected), progress/TODO.md |
| Task Started | progress/ONGOING.md (move from TODO), progress/TODO.md (mark in-progress) |
| Task Completed | progress/FINISHED.md (add completed task), progress/ONGOING.md (remove), progress/TODO.md (mark complete) |
| Major Change / Release | README.md (release checklist + version), release/*.md (new notes), progress/FINISHED.md, progress/ONGOING.md, build.gradle.kts (version), GitHub release/tag |
| CI/CD Change | fastlane/Fastfile, fastlane/README.md, fastlane/metadata/ (update lanes, env vars, changelogs) |
| Crash Handler Change | CRASH_HANDLER.md (update implementation details) |

---

## Quick Reference

### Package Name
`com.ivarna.adirstat`

### App Details
- **Name:** Adirstat
- **Tagline:** "See exactly what's eating your storage."
- **Type:** Android Disk Space Analyzer (WizTree/WinDirStat equivalent)
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 36 (Android 14+)
- **Architecture:** MVVM + Clean Architecture + Repository Pattern
- **UI:** Jetpack Compose + Material Design 3

### Key Technologies
- Kotlin 1.9.x
- Jetpack Compose with Material 3
- Room Database
- Hilt for Dependency Injection
- Coroutines + Flow
- DataStore for Preferences
- WorkManager for Background Scans

### Distribution
- Google Play Store
- F-Droid
- GitHub Releases

### Primary Locales
- en-US (primary)
- Structure ready for: hi-IN, de-DE, fr-FR, ja-JP

---

## Documentation Version
This documentation is current for Adirstat version **1.0.1**.

---

## Required Release Process For Major Changes

Any major user-visible change must end with a release like the v1.0.1 process.

### Treat these as release-triggering changes
- Search/navigation behavior changes
- Treemap/list/dashboard UI changes
- Launcher icon or branding changes
- Permission flow changes
- Storage scan, cache, or release-build behavior changes
- Any major bug-fix bundle that changes user workflows

### Mandatory release checklist
1. Update every affected document listed in this README.
2. Bump `versionCode` and `versionName` in the Android app module.
3. Rebuild debug and release artifacts.
4. Optimize release size when possible (`minify`, resource shrinking, packaging cleanup).
5. Ensure a signing keystore and properties file exist under `/home/flux/repos/key/`.
6. Generate/update release notes under `docs/release/`.
7. Use an absolute GitHub-hosted image URL in release notes; relative local image paths do not render on GitHub releases.
8. Create or update the Git tag and GitHub release.
9. Upload the signed release APK to the GitHub release.
10. Commit and push all code and documentation changes.

### Standard local signing layout
- Keystore: `/home/flux/repos/key/release.jks`
- Properties: `/home/flux/repos/key/release.properties`
- Credentials reference: `/home/flux/repos/key/credentials.txt`

The properties file must contain:
- `storeFile`
- `storePassword`
- `keyAlias`
- `keyPassword`

For questions or clarifications, refer to the relevant document or consult the [PROBLEM_STATEMENT.md](PROBLEM_STATEMENT.md) for context.
