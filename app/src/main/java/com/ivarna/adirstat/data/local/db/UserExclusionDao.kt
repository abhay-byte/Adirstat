package com.ivarna.adirstat.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user exclusion operations
 */
@Dao
interface UserExclusionDao {
    
    @Query("SELECT * FROM user_exclusions WHERE isActive = 1")
    fun getActiveExclusions(): Flow<List<UserExclusionEntity>>
    
    @Query("SELECT * FROM user_exclusions WHERE isActive = 1")
    suspend fun getActiveExclusionsList(): List<UserExclusionEntity>
    
    @Query("SELECT * FROM user_exclusions ORDER BY createdAt DESC")
    fun getAllExclusions(): Flow<List<UserExclusionEntity>>
    
    @Query("SELECT * FROM user_exclusions WHERE id = :id")
    suspend fun getExclusionById(id: Long): UserExclusionEntity?
    
    @Query("SELECT * FROM user_exclusions WHERE path = :path")
    suspend fun getExclusionByPath(path: String): UserExclusionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExclusion(exclusion: UserExclusionEntity): Long
    
    @Update
    suspend fun updateExclusion(exclusion: UserExclusionEntity)
    
    @Delete
    suspend fun deleteExclusion(exclusion: UserExclusionEntity)
    
    @Query("DELETE FROM user_exclusions WHERE id = :id")
    suspend fun deleteExclusionById(id: Long)
    
    @Query("DELETE FROM user_exclusions")
    suspend fun deleteAllExclusions()
    
    @Query("UPDATE user_exclusions SET isActive = :isActive WHERE id = :id")
    suspend fun setExclusionActive(id: Long, isActive: Boolean)
    
    @Query("SELECT COUNT(*) FROM user_exclusions WHERE isActive = 1")
    suspend fun getActiveExclusionCount(): Int
}
