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
    
    @Query("SELECT * FROM pieces_techniques WHERE isFavorite = 1 ORDER BY name COLLATE NOCASE ASC")
    fun getFavorites(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE type = :type ORDER BY name COLLATE NOCASE ASC")
    fun getByType(type: ItemType): Flow<List<PieceOrTechnique>>
    
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
    
    // Synchronous methods for migration
    @Query("SELECT * FROM pieces_techniques ORDER BY name COLLATE NOCASE ASC")
    fun getAllPiecesAndTechniquesSync(): List<PieceOrTechnique>
    
    // Statistics-based query methods
    @Query("SELECT * FROM pieces_techniques WHERE practiceCount > 0 ORDER BY lastPracticeDate DESC")
    fun getPiecesWithPracticeHistory(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE performanceCount > 0 ORDER BY lastPerformanceDate DESC")
    fun getPiecesWithPerformanceHistory(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE lastPracticeDate IS NOT NULL ORDER BY lastPracticeDate DESC LIMIT :limit")
    fun getRecentlyPracticedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE lastPerformanceDate IS NOT NULL ORDER BY lastPerformanceDate DESC LIMIT :limit")
    fun getRecentlyPerformedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE lastSatisfactoryPractice IS NOT NULL ORDER BY lastSatisfactoryPractice DESC")
    fun getPiecesWithSatisfactoryPractice(): Flow<List<PieceOrTechnique>>
}