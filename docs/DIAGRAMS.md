# Diagrams

This document contains all Mermaid diagrams for Adirstat, covering architecture, data flows, and state machines.

---

## Diagram Index

1. [ER Diagram](#1-entity-relationship-diagram)
2. [App Flowchart](#2-app-flowchart)
3. [Class Diagram: Core Domain](#3-class-diagram-core-domain)
4. [Class Diagram: Architecture Layers](#4-class-diagram-architecture-layers)
5. [Sequence Diagram: Scan Initiation](#5-sequence-diagram-scan-initiation)
6. [Sequence Diagram: Treemap Interaction](#6-sequence-diagram-treemap-interaction)
7. [State Machine: Scan States](#7-state-machine-scan-states)
8. [Package Diagram](#8-package-diagram)
9. [Data Flow: File Deletion](#9-data-flow-file-deletion)
10. [Data Flow: Export to CSV](#10-data-flow-export-to-csv)
11. [Navigation Diagram](#11-navigation-diagram)
12. [Permission Flow](#12-permission-flow)
13. [Treemap Layout Algorithm](#13-treemap-layout-algorithm)

---

## 1. Entity Relationship Diagram

```mermaid
erDiagram
    SCAN_HISTORY ||--o{ SCAN_CACHE : "has"
    SCAN_HISTORY {
        int id PK
        string partition_path
        int scan_date
        long total_bytes
        long free_bytes
        int file_count
        int folder_count
        int duration_ms
    }
    SCAN_CACHE {
        int id PK
        int scan_history_id FK
        string serialized_tree_json
        int created_at
    }
    USER_EXCLUSION {
        int id PK
        string path
        int created_at
    }
```

---

## 2. App Flowchart

```mermaid
flowchart TD
    A[App Launch] --> B{First Launch?}
    B -->|Yes| C[Permission Rationale Screen]
    B -->|No| D{Permissions Granted?}
    
    C --> E{User Grants Full Access?}
    E -->|Yes| F[Dashboard]
    E -->|No| G[MediaStore Mode]
    
    D -->|Yes| F
    D -->|No| G
    
    G --> H[Limited Dashboard]
    H --> I[MediaStore Scan Only]
    
    F --> J[Dashboard - Full Features]
    J --> K[Select Partition]
    K --> L[Scan Progress]
    L --> M[Treemap Screen]
    
    M --> N{User Action}
    N -->|Tap Block| O[Drill Down]
    N -->|Tap Breadcrumb| P[Navigate Up]
    N -->|Search| Q[Search Screen]
    N -->|File List| R[File List Screen]
    N -->|App Details| AB[App Info]
    N -->|Duplicates| S[Duplicate Screen]
    N -->|Delete| T[Delete Flow]
    
    O --> M
    P --> M
    
    R -->|Virtual App Details| AB
    R --> U[Filter/Sort]
    U --> R
    
    T --> V{Confirm?}
    V -->|Yes| W[Delete Files]
    V -->|No| M
    
    W --> M
    
    F --> X[Bottom Navigation]
    X --> Y[App Storage]
    X --> Z[Scan History]
    X --> AA[Settings]
    
    Y --> AB[App Info]
    Z --> AC[View Past Scan]
    AA --> AD[Preferences]
```

---

## 3. Class Diagram: Core Domain

```mermaid
classDiagram
    class FileNode {
        <<sealed>>
        +name: String
        +path: String
        +size: Long
        +lastModified: Long
    }
    
    class File {
        +extension: String
        +mimeType: String?
    }
    
    class Directory {
        +children: List~FileNode~
        +childCount: Int
        +fileCount: Int
        +folderCount: Int
    }
    
    FileNode <|-- File
    FileNode <|-- Directory
    
    class PartitionInfo {
        +path: String
        +displayName: String
        +type: PartitionType
        +totalBytes: Long
        +freeBytes: Long
        +usedBytes: Long
        +isRemovable: Boolean
        +lastScanTime: Long?
    }
    
    class DuplicateGroup {
        +original: File
        +duplicates: List~File~
        +wastedSpace: Long
        +totalSpace: Long
    }
    
    class FileTypeGroup {
        +category: FileCategory
        +totalSize: Long
        +fileCount: Int
        +percentage: Float
        +extensions: List~String~
    }
    
    class ScanProgress {
        +state: ScanState
        +filesScanned: Int
        +currentPath: String
        +totalBytes: Long
        +percentage: Float
        +estimatedTimeRemaining: Long?
    }
    
    class ScanResult {
        +rootNode: Directory
        +partitionPath: String
        +totalSize: Long
        +fileCount: Int
        +folderCount: Int
        +scanDurationMs: Long
    }
    
    FileNode -- PartitionInfo
    ScanProgress -- ScanState
```

---

## 4. Class Diagram: Architecture Layers

```mermaid
classDiagram
    namespace UI_Layer {
        class DashboardScreen
        class TreemapScreen
        class FileListScreen
    }
    
    namespace ViewModel_Layer {
        class DashboardViewModel
        class TreemapViewModel
        class FileListViewModel
    }
    
    namespace UseCase_Layer {
        class ScanStorageUseCase
        class GetDuplicatesUseCase
        class DeleteFilesUseCase
    }
    
    namespace Repository_Layer {
        class StorageRepository
        class AppStatsRepository
    }
    
    namespace DataSource_Layer {
        class FileSystemDataSource
        class MediaStoreDataSource
        class StorageStatsDataSource
    }
    
    DashboardScreen --> DashboardViewModel
    TreemapScreen --> TreemapViewModel
    FileListScreen --> FileListViewModel
    
    DashboardViewModel --> ScanStorageUseCase
    TreemapViewModel --> ScanStorageUseCase
    FileListViewModel --> ScanStorageUseCase
    
    ScanStorageUseCase --> StorageRepository
    GetDuplicatesUseCase --> StorageRepository
    DeleteFilesUseCase --> StorageRepository
    
    StorageRepository --> FileSystemDataSource
    StorageRepository --> MediaStoreDataSource
    StorageRepository --> StorageStatsDataSource
```

---

## 5. Sequence Diagram: Scan Initiation

```mermaid
sequenceDiagram
    participant User
    participant DashboardScreen
    participant DashboardViewModel
    participant ScanStorageUseCase
    participant StorageRepository
    participant FileSystemDataSource
    participant Room
    
    User->>DashboardScreen: Tap "Scan" on partition
    DashboardScreen->>DashboardViewModel: scanPartition(partitionPath)
    DashboardViewModel->>ScanStorageUseCase: invoke(partitionPath)
    
    par Check Cache
        ScanStorageUseCase->>Room: getCachedScan(partitionPath)
        Room-->>ScanStorageUseCase: cachedResult?
    and Start New Scan
        ScanStorageUseCase->>StorageRepository: scanStorage(partitionPath)
        StorageRepository->>FileSystemDataSource: startScan(path)
        
        loop Scan Files
            FileSystemDataSource->>FileSystemDataSource: traverseRecursive()
            FileSystemDataSource-->>StorageRepository: Flow~ScanProgress~
            StorageRepository-->>ScanStorageUseCase: Flow~ScanProgress~
            ScanStorageUseCase-->>DashboardViewModel: Flow~ScanProgress~
            DashboardViewModel-->>DashboardScreen: Update UI
        end
        
        FileSystemDataSource-->>StorageRepository: FileNode tree
        StorageRepository-->>ScanStorageUseCase: ScanResult
        ScanStorageUseCase->>Room: cacheResult(scanResult)
    end
    
    ScanStorageUseCase-->>DashboardViewModel: ScanResult
    DashboardViewModel-->>DashboardScreen: Navigate to Treemap
    DashboardScreen->>User: Show Treemap Screen
```

---

## 6. Sequence Diagram: Treemap Interaction

```mermaid
sequenceDiagram
    participant User
    participant TreemapScreen
    participant TreemapViewModel
    participant TreemapLayoutEngine
    participant Canvas
    
    User->>TreemapScreen: Tap treemap block
    TreemapScreen->>TreemapViewModel: onBlockTapped(blockId)
    
    alt Block is File
        TreemapViewModel->>TreemapViewModel: showFileDetails(file)
        TreemapViewModel-->>TreemapScreen: Update state
        TreemapScreen->>User: Show Bottom Sheet
    end

    alt Block is Virtual App Node
        TreemapViewModel->>TreemapViewModel: navigateToDirectory(dir)
        TreemapViewModel-->>TreemapScreen: Update state with APK/Data/Cache children
        User->>TreemapScreen: Open app details from bottom sheet
        TreemapScreen->>User: Launch Android App Info
    end
    
    alt Block is Directory
        TreemapViewModel->>TreemapViewModel: navigateToDirectory(dir)
        
        par Update Breadcrumb
            TreemapViewModel-->>TreemapScreen: Update breadcrumb
        and Recompute Layout
            TreemapViewModel->>TreemapLayoutEngine: computeLayout(children, bounds)
            TreemapLayoutEngine-->>TreemapViewModel: List~Rect~
        and Render
            TreemapViewModel-->>TreemapScreen: Update state
            TreemapScreen->>Canvas: drawTreemap(rects)
        end
    end
    
    User->>TreemapScreen: Tap breadcrumb segment
    TreemapScreen->>TreemapViewModel: navigateToBreadcrumb(index)
    TreemapViewModel->>TreemapViewModel: popToIndex()
    TreemapViewModel->>TreemapLayoutEngine: computeLayout()
    TreemapLayoutEngine-->>TreemapViewModel: rects
    TreemapViewModel-->>TreemapScreen: Update treemap
```

---

## 7. State Machine: Scan States

```mermaid
stateDiagram-v2
    [*] --> Idle : App Starts
    Idle --> Scanning : User taps Scan
    Scanning --> Scanning : Files being scanned
    Scanning --> Scanning : Progress update
    Scanning --> Completed : Scan finishes
    Scanning --> Cancelled : User cancels
    Scanning --> Error : Exception occurs
    Completed --> Idle : Navigate away
    Cancelled --> Idle : Dismiss
    Error --> Idle : Dismiss or Retry
    Error --> Scanning : Retry
    
    note right of Scanning
        Emits progress updates
        Files: 0 → 50,000+
        Path: /storage/... → current
    end note
    
    note right of Completed
        Results cached
        Navigate to Treemap
    end note
    
    note right of Cancelled
        Partial results
        Not cached
    end note
    
    note right of Error
        Show error message
        Retry button
    end note
```

---

## 8. Package Diagram

```mermaid
graph TD
    subgraph "com.ivarna.adirstat"
        subgraph "di"
            AppModule
            DatabaseModule
            DataStoreModule
        end
        
        subgraph "data"
            subgraph "local"
                db
                datastore
            end
            source
            repository
        end
        
        subgraph "domain"
            model
            repository
            usecase
        end
        
        subgraph "presentation"
            dashboard
            treemap
            filelist
            duplicates
            appstats
            history
            settings
            permission
            common
        end
        
        util
    end
    
    di --> data
    di --> domain
    di --> presentation
    di --> util
    
    data --> domain
    domain --> presentation
```

---

## 9. Data Flow: File Deletion

```mermaid
flowchart TD
    A[User selects file(s) to delete] --> B[Show confirmation dialog]
    B --> C{User confirms?}
    
    C -->|No| D[Cancel and return]
    C -->|Yes| E{Full Access?}
    
    E -->|Yes| F[Use File API]
    E -->|No| G[Use MediaStore API]
    
    F --> H[file.delete()]
    G --> I[contentResolver.delete(uri)]
    
    H --> J{Delete successful?}
    I --> J
    
    J -->|Yes| K[Update UI]
    J -->|No| L[Show error toast]
    
    K --> M[Remove from treemap/list]
    M --> N[Recalculate sizes]
    
    L --> O[Show error message]
    
    N --> P[Navigate back or stay]
```

---

## 10. Data Flow: Export to CSV

```mermaid
sequenceDiagram
    participant User
    participant FileListScreen
    participant ExportToCsvUseCase
    participant StorageRepository
    participant ShareSheet
    
    User->>FileListScreen: Tap "Export to CSV"
    FileListScreen->>ExportToCsvUseCase: invoke(scanResult)
    
    ExportToCsvUseCase->>StorageRepository: generateCsv(scanResult)
    
    par Generate CSV
        StorageRepository->>StorageRepository: buildCsvString()
    and Create File
        StorageRepository->>StorageRepository: write to cache dir
    end
    
    StorageRepository-->>ExportToCsvUseCase: File
    ExportToCsvUseCase-->>FileListScreen: File
    
    FileListScreen->>ShareSheet: shareFile(file)
    ShareSheet->>User: Show Android Share Sheet
    
    User->>ShareSheet: Select target app
    ShareSheet-->User: Send file
```

---

## 11. Navigation Diagram

```mermaid
flowchart LR
    subgraph Main
        Dashboard
        AppStorage
        History
        Settings
    end
    
    subgraph SubScreens
        Treemap
        FileList
        FileTypeBreakdown
        Duplicates
        Search
    end
    
    Dashboard -->|Tap partition| ScanProgress
    ScanProgress -->|Complete| Treemap
    
    Treemap -->|Tap block| FileList
    Treemap -->|Menu| FileTypeBreakdown
    Treemap -->|Menu| Duplicates
    Treemap -->|Search| Search
    
    Treemap -->|Back| Dashboard
    
    AppStorage -->|Tap app| AppInfo
    History -->|Tap entry| Treemap
    Settings -->|Back| Dashboard
```

---

## 12. Permission Flow

```mermaid
flowchart TD
    A[App Launch] --> B{API Level?}
    
    B -->|API < 30| C[READ_EXTERNAL_STORAGE]
    B -->|API 30-32| D[MANAGE_EXTERNAL_STORAGE]
    B -->|API >= 33| E[Check MANAGE_EXTERNAL_STORAGE]
    
    C --> C1{Granted?}
    C1 -->|Yes| F[Full Access Mode]
    C1 -->|No| G[Limited Mode]
    
    D --> D1{isExternalStorageManager?}
    D1 -->|true| F
    D1 -->|false| H[Request in Settings]
    H --> D1
    
    E --> E1{isExternalStorageManager?}
    E1 -->|true| F
    E1 -->|false| I[Request Media Perms]
    
    I --> I1{All Granted?}
    I1 -->|Yes| F
    I1 -->|No| G
    
    F --> J[Full Feature Set]
    G --> K[Limited Features]
    
    J --> L[Dashboard]
    K --> L
    
    J --> M{PACKAGE_USAGE_STATS?}
    M -->|Not granted| N[Prompt to enable]
    M -->|Granted| L
    
    K --> L
```

---

## 13. Treemap Layout Algorithm

```mermaid
flowchart TD
    A[Input: List of items with sizes] --> B[Sort by size descending]
    B --> C[Initialize container bounds]
    C --> D[Calculate total size]
    
    D --> E{Total items > 0?}
    E -->|No| F[Return empty]
    
    E -->|Yes| G[Start row]
    
    G --> H{More items?}
    H -->|No| I[Layout final row]
    H -->|Yes| J[Add next item to row]
    
    J --> K{Aspect ratio improves?}
    K -->|Yes| H
    K -->|No| L[Remove last item]
    
    L --> M{Layout current row}
    M --> N[Alternating horizontal/vertical]
    N --> O[Update remaining bounds]
    O --> H
    
    I --> P[Return rectangles]
    
    subgraph "Aspect Ratio Calculation"
        Q[For each item in row]
        R[Calculate: max(w/h, h/w)]
        S[Find worst ratio in row]
    end
    
    J -.-> Q
    K -.-> R
    S -.-> K
```

---

## Summary

| Diagram | Type | Purpose |
|---------|------|---------|
| ER Diagram | Entity Relationship | Database schema |
| App Flowchart | Flowchart | User journey |
| Class Diagram (Core) | UML Class | Domain models |
| Class Diagram (Layers) | UML Class | Architecture |
| Scan Initiation | Sequence | Scan flow |
| Treemap Interaction | Sequence | UI interaction |
| Scan States | State Machine | State management |
| Package Diagram | Graph | Module organization |
| File Deletion | Flowchart | Delete flow |
| Export to CSV | Sequence | Export flow |
| Navigation | Flowchart | Screen navigation |
| Permission Flow | Flowchart | Permission handling |
| Treemap Algorithm | Flowchart | Algorithm steps |
