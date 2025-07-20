package com.pseddev.practicetracker.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pseddev.practicetracker.data.entities.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE pieceOrTechniqueId = :pieceId ORDER BY timestamp DESC")
    fun getActivitiesForPiece(pieceId: Long): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getActivitiesForDateRange(startTime: Long, endTime: Long): Flow<List<Activity>>
    
    @Insert
    suspend fun insert(activity: Activity)
    
    @Query("DELETE FROM activities")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(DISTINCT date(timestamp/1000, 'unixepoch', 'localtime')) as streak FROM activities WHERE timestamp >= :startTime")
    suspend fun getStreakCount(startTime: Long): Int
}