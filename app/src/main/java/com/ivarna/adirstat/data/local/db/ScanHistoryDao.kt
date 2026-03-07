package com.ivarna.adirstat.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for scan history operations
 */
@Dao
interface ScanHistoryDao {
    
    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>
    
    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanById(id: Long): ScanHistoryEntity?
    
    @Query("SELECT * FROM scan_history WHERE partitionPath = :path ORDER BY scanDate DESC LIMIT 1")
    suspend fun getLatestScanForPartition(path: String): ScanHistoryEntity?
    
    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC LIMIT :limit")
    fun getRecentScans(limit: Int): Flow<List<ScanHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity): Long
    
    @Update
    suspend fun updateScan(scan: ScanHistoryEntity)
    
    @Delete
    suspend fun deleteScan(scan: ScanHistoryEntity)
    
    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanById(id: Long)
    
    @Query("DELETE FROM scan_history")
    suspend fun deleteAllScans()
    
    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getScanCount(): Int
    
    @Query("SELECT SUM(totalBytes) FROM scan_history")
    suspend fun getTotalScannedSize(): Long?
}
