package com.pseddev.playstreak.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceOrTechniqueDao {
    @Query("SELECT * FROM pieces_techniques ORDER BY name COLLATE NOCASE ASC")
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE type = :type ORDER BY name COLLATE NOCASE ASC")
    fun getByType(type: ItemType): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE lastPracticeDate IS NOT NULL ORDER BY lastPracticeDate DESC LIMIT :limit")
    fun getRecentlyPracticed(limit: Int): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE lastPracticeDate IS NULL OR lastPracticeDate < :beforeTimestamp ORDER BY COALESCE(lastPracticeDate, 0) ASC LIMIT :limit")
    fun getLeastRecentlyPracticed(beforeTimestamp: Long, limit: Int): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE practiceCount > 0 ORDER BY practiceCount DESC LIMIT :limit")
    fun getMostPracticed(limit: Int): Flow<List<PieceOrTechnique>>
    
    @Insert
    suspend fun insert(piece: PieceOrTechnique): Long
    
    @Update
    suspend fun update(piece: PieceOrTechnique)
    
    @Delete
    suspend fun delete(piece: PieceOrTechnique)
    
    @Query("DELETE FROM pieces_techniques")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM pieces_techniques WHERE id = :id")
    suspend fun getById(id: Long): PieceOrTechnique?
    
    @Query("UPDATE pieces_techniques SET practiceCount = practiceCount + 1, lastPracticeDate = :timestamp, totalPracticeDuration = totalPracticeDuration + :duration, lastUpdated = :timestamp, lastThreePractices = :lastThreePractices WHERE id = :id")
    suspend fun updatePracticeStats(id: Long, timestamp: Long, duration: Int, lastThreePractices: String?)
    
    @Query("UPDATE pieces_techniques SET performanceCount = performanceCount + 1, lastPerformanceDate = :timestamp, lastUpdated = :timestamp, lastThreePerformances = :lastThreePerformances WHERE id = :id")
    suspend fun updatePerformanceStats(id: Long, timestamp: Long, lastThreePerformances: String?)
    
    @Query("UPDATE pieces_techniques SET averagePracticeLevel = :avgLevel, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateAveragePracticeLevel(id: Long, avgLevel: Float, timestamp: Long)
}