package com.pseddev.playstreak.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pseddev.playstreak.data.entities.PieceFavorite
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceFavoriteDao {
    @Query("""
        SELECT pt.* FROM pieces_techniques pt 
        INNER JOIN piece_favorites pf ON pt.id = pf.pieceOrTechniqueId 
        ORDER BY pf.dateAdded DESC
    """)
    fun getFavorites(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT COUNT(*) FROM piece_favorites")
    suspend fun getFavoriteCount(): Int
    
    @Query("SELECT * FROM piece_favorites WHERE pieceOrTechniqueId = :pieceId")
    suspend fun getFavorite(pieceId: Long): PieceFavorite?
    
    @Query("SELECT EXISTS(SELECT 1 FROM piece_favorites WHERE pieceOrTechniqueId = :pieceId)")
    suspend fun isFavorite(pieceId: Long): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: PieceFavorite)
    
    @Query("DELETE FROM piece_favorites WHERE pieceOrTechniqueId = :pieceId")
    suspend fun removeFavorite(pieceId: Long)
    
    @Delete
    suspend fun delete(favorite: PieceFavorite)
    
    @Query("DELETE FROM piece_favorites")
    suspend fun deleteAll()
}