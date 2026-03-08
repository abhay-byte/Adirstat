package com.ivarna.adirstat.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for scan cache operations
 */
@Dao
interface ScanCacheDao {
    
    @Query("SELECT * FROM scan_cache WHERE scanHistoryId = :scanHistoryId")
    suspend fun getCacheForScan(scanHistoryId: Long): ScanCacheEntity?
    
    @Query("SELECT * FROM scan_cache WHERE partitionPath = :path ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestCacheForPartition(path: String): ScanCacheEntity?

    @Query("SELECT * FROM scan_cache ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestCache(): ScanCacheEntity?
    
    @Query("SELECT * FROM scan_cache ORDER BY createdAt DESC")
    fun getAllCache(): Flow<List<ScanCacheEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: ScanCacheEntity): Long
    
    @Delete
    suspend fun deleteCache(cache: ScanCacheEntity)
    
    @Query("DELETE FROM scan_cache WHERE id = :id")
    suspend fun deleteCacheById(id: Long)
    
    @Query("DELETE FROM scan_cache WHERE scanHistoryId = :scanHistoryId")
    suspend fun deleteCacheForScan(scanHistoryId: Long)
    
    @Query("DELETE FROM scan_cache")
    suspend fun deleteAllCache()
    
    @Query("DELETE FROM scan_cache WHERE createdAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM scan_cache")
    suspend fun getCacheCount(): Int
}
