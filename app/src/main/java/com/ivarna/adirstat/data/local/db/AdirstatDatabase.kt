package com.ivarna.adirstat.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database for Adirstat
 * Contains tables for scan history, scan cache, and user exclusions
 */
@Database(
    entities = [
        ScanHistoryEntity::class,
        ScanCacheEntity::class,
        UserExclusionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AdirstatDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun scanCacheDao(): ScanCacheDao
    abstract fun userExclusionDao(): UserExclusionDao
}

/**
 * Entity for storing scan history metadata
 */
@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partitionPath: String,
    val partitionName: String,
    val scanDate: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val fileCount: Long,
    val folderCount: Long,
    val durationMs: Long,
    val scanType: String // "FULL", "PARTIAL", "MEDIA_ONLY"
)

/**
 * Entity for caching serialized scan results
 */
@Entity(tableName = "scan_cache")
data class ScanCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scanHistoryId: Long,
    val serializedTreeJson: String,
    val createdAt: Long,
    val partitionPath: String,
    val totalSize: Long,
    val fileCount: Long
)

/**
 * Entity for storing user-defined exclusion paths
 */
@Entity(tableName = "user_exclusions")
data class UserExclusionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val createdAt: Long,
    val isActive: Boolean = true
)
