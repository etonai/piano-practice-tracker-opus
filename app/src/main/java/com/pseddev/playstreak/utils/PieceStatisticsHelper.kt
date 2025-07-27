package com.pseddev.playstreak.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Helper class for managing piece statistics operations
 */
object PieceStatisticsHelper {
    private val gson = Gson()
    
    /**
     * Converts a list of timestamps to JSON string for storage
     */
    fun timestampsToJson(timestamps: List<Long>): String? {
        return if (timestamps.isEmpty()) null else gson.toJson(timestamps)
    }
    
    /**
     * Converts JSON string back to list of timestamps
     */
    fun jsonToTimestamps(json: String?): List<Long> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<Long>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Adds a new timestamp to existing list, keeping only the last 3
     */
    fun addTimestampToRecent(existingJson: String?, newTimestamp: Long): String? {
        val existing = jsonToTimestamps(existingJson).toMutableList()
        existing.add(newTimestamp)
        
        // Sort by timestamp descending and keep only last 3
        val recent = existing.sortedDescending().take(3)
        return timestampsToJson(recent)
    }
    
    /**
     * Calculates average practice level from a list of levels
     */
    fun calculateAverageLevel(levels: List<Int>): Float? {
        val validLevels = levels.filter { it > 0 }
        return if (validLevels.isEmpty()) null else validLevels.average().toFloat()
    }
}