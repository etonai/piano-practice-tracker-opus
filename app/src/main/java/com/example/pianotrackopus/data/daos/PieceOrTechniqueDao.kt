package com.example.pianotrackopus.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pianotrackopus.data.entities.ItemType
import com.example.pianotrackopus.data.entities.PieceOrTechnique
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceOrTechniqueDao {
    @Query("SELECT * FROM pieces_techniques ORDER BY name ASC")
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE type = :type ORDER BY name ASC")
    fun getByType(type: ItemType): Flow<List<PieceOrTechnique>>
    
    @Insert
    suspend fun insert(piece: PieceOrTechnique): Long
    
    @Update
    suspend fun update(piece: PieceOrTechnique)
    
    @Query("DELETE FROM pieces_techniques")
    suspend fun deleteAll()
}