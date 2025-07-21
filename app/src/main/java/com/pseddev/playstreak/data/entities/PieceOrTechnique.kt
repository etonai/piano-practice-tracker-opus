package com.pseddev.playstreak.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pieces_techniques")
data class PieceOrTechnique(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val name: String,
    val type: ItemType,
    val isFavorite: Boolean = false
)

enum class ItemType { 
    PIECE, 
    TECHNIQUE 
}