package com.pseddev.playstreak.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pieces_techniques")
data class PieceOrTechnique(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val name: String,
    val type: ItemType,
    val dateCreated: Long = System.currentTimeMillis(),
    
    // Independent statistics fields
    val practiceCount: Int = 0,
    val performanceCount: Int = 0,
    val lastPracticeDate: Long? = null,
    val lastPerformanceDate: Long? = null,
    val totalPracticeDuration: Int = 0, // in minutes
    val averagePracticeLevel: Float? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    
    // Recent activity tracking for better suggestions (JSON arrays of timestamps)
    val lastThreePractices: String? = null, // JSON array of timestamps
    val lastThreePerformances: String? = null // JSON array of timestamps
)

@Entity(tableName = "piece_favorites")
data class PieceFavorite(
    @PrimaryKey val pieceOrTechniqueId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)

enum class ItemType { 
    PIECE, 
    TECHNIQUE 
}