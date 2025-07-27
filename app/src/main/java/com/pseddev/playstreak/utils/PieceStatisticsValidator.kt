package com.pseddev.playstreak.utils

import android.util.Log
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.ActivityType
import kotlinx.coroutines.flow.first

/**
 * Utility class for validating that piece statistics match activity data
 */
class PieceStatisticsValidator(
    private val pieceDao: PieceOrTechniqueDao,
    private val activityDao: ActivityDao
) {
    
    /**
     * Validates that piece statistics are consistent with actual activity data
     * @return ValidationResult containing any inconsistencies found
     */
    suspend fun validateStatistics(): ValidationResult {
        val pieces = pieceDao.getAllPiecesAndTechniques().first()
        val activities = activityDao.getAllActivities().first()
        val inconsistencies = mutableListOf<String>()
        
        pieces.forEach { piece ->
            val pieceActivities = activities.filter { it.pieceOrTechniqueId == piece.id }
            val practices = pieceActivities.filter { it.activityType == ActivityType.PRACTICE }
            val performances = pieceActivities.filter { it.activityType == ActivityType.PERFORMANCE }
            
            // Validate practice count
            val actualPracticeCount = practices.size
            if (piece.practiceCount != actualPracticeCount) {
                inconsistencies.add("Piece '${piece.name}': practice count mismatch (stored: ${piece.practiceCount}, actual: $actualPracticeCount)")
            }
            
            // Validate performance count
            val actualPerformanceCount = performances.size
            if (piece.performanceCount != actualPerformanceCount) {
                inconsistencies.add("Piece '${piece.name}': performance count mismatch (stored: ${piece.performanceCount}, actual: $actualPerformanceCount)")
            }
            
            // Validate last practice date
            val actualLastPracticeDate = practices.maxByOrNull { it.timestamp }?.timestamp
            if (piece.lastPracticeDate != actualLastPracticeDate) {
                inconsistencies.add("Piece '${piece.name}': last practice date mismatch (stored: ${piece.lastPracticeDate}, actual: $actualLastPracticeDate)")
            }
            
            // Validate last performance date
            val actualLastPerformanceDate = performances.maxByOrNull { it.timestamp }?.timestamp
            if (piece.lastPerformanceDate != actualLastPerformanceDate) {
                inconsistencies.add("Piece '${piece.name}': last performance date mismatch (stored: ${piece.lastPerformanceDate}, actual: $actualLastPerformanceDate)")
            }
            
            // Validate total practice duration
            val actualTotalDuration = practices.sumOf { if (it.minutes > 0) it.minutes else 0 }
            if (piece.totalPracticeDuration != actualTotalDuration) {
                inconsistencies.add("Piece '${piece.name}': total practice duration mismatch (stored: ${piece.totalPracticeDuration}, actual: $actualTotalDuration)")
            }
            
            // Validate average practice level
            val practicesWithLevel = practices.filter { it.level > 0 }
            val actualAvgLevel = if (practicesWithLevel.isNotEmpty()) {
                practicesWithLevel.map { it.level }.average().toFloat()
            } else null
            
            val levelDifference = if (piece.averagePracticeLevel != null && actualAvgLevel != null) {
                kotlin.math.abs(piece.averagePracticeLevel!! - actualAvgLevel)
            } else 0f
            
            if ((piece.averagePracticeLevel == null) != (actualAvgLevel == null) || levelDifference > 0.01f) {
                inconsistencies.add("Piece '${piece.name}': average practice level mismatch (stored: ${piece.averagePracticeLevel}, actual: $actualAvgLevel)")
            }
        }
        
        return ValidationResult(
            isValid = inconsistencies.isEmpty(),
            inconsistencies = inconsistencies
        )
    }
    
    /**
     * Logs validation results for debugging
     */
    suspend fun logValidationResults() {
        val result = validateStatistics()
        if (result.isValid) {
            Log.i("PieceStatistics", "All piece statistics are consistent with activity data")
        } else {
            Log.w("PieceStatistics", "Found ${result.inconsistencies.size} inconsistencies:")
            result.inconsistencies.forEach { inconsistency ->
                Log.w("PieceStatistics", "  - $inconsistency")
            }
        }
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val inconsistencies: List<String>
    )
}