package com.pseddev.playstreak.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val timestamp: Long,
    val pieceOrTechniqueId: Long,
    val activityType: ActivityType,
    val level: Int,
    val performanceType: String,
    val minutes: Int = -1,
    val notes: String = ""
)

enum class ActivityType { 
    PRACTICE, 
    PERFORMANCE 
}