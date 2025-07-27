package com.pseddev.playstreak.data.repository

import android.util.Log
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.PieceFavoriteDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceFavorite
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.ui.progress.ActivityWithPiece
import com.pseddev.playstreak.utils.CsvHandler
import com.pseddev.playstreak.utils.PieceStatisticsCsvExporter
import com.pseddev.playstreak.utils.PieceStatisticsHelper
import com.pseddev.playstreak.utils.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.io.Writer
import java.io.Reader
import java.util.Calendar

/**
 * Data class to represent a piece with its favorite status
 * Used by UI components that need both piece data and favorite information
 */
data class PieceWithFavoriteStatus(
    val piece: PieceOrTechnique,
    val isFavorite: Boolean
)

class PianoRepository(
    private val pieceOrTechniqueDao: PieceOrTechniqueDao,
    private val activityDao: ActivityDao,
    private val pieceFavoriteDao: PieceFavoriteDao
) {
    
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getAllPiecesAndTechniques()
    
    fun getFavorites(): Flow<List<PieceOrTechnique>> = 
        pieceFavoriteDao.getFavorites()
    
    fun getPieces(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getByType(ItemType.PIECE)
    
    fun getTechniques(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getByType(ItemType.TECHNIQUE)
    
    suspend fun insertPieceOrTechnique(item: PieceOrTechnique): Long = 
        pieceOrTechniqueDao.insert(item)
    
    suspend fun updatePieceOrTechnique(item: PieceOrTechnique) = 
        pieceOrTechniqueDao.update(item)
    
    suspend fun deletePieceOrTechnique(item: PieceOrTechnique) = 
        pieceOrTechniqueDao.delete(item)
    
    suspend fun deletePieceAndActivities(pieceId: Long) {
        // First delete all activities for this piece
        activityDao.deleteActivitiesForPiece(pieceId)
        // Then delete the piece itself
        val piece = pieceOrTechniqueDao.getById(pieceId)
        piece?.let { pieceOrTechniqueDao.delete(it) }
    }
    
    suspend fun deleteAllPiecesAndTechniques() = 
        pieceOrTechniqueDao.deleteAll()
    
    fun getAllActivities(): Flow<List<Activity>> = 
        activityDao.getAllActivities()
    
    fun getActivitiesForPiece(pieceId: Long): Flow<List<Activity>> = 
        activityDao.getActivitiesForPiece(pieceId)
    
    fun getActivitiesForDateRange(startTime: Long, endTime: Long): Flow<List<Activity>> = 
        activityDao.getActivitiesForDateRange(startTime, endTime)
    
    fun getTodaysActivities(): Flow<List<Activity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = calendar.timeInMillis
        return getActivitiesForDateRange(startTime, endTime)
    }
    
    fun getYesterdaysActivities(): Flow<List<Activity>> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = calendar.timeInMillis
        return getActivitiesForDateRange(startTime, endTime)
    }
    
    // Favorites management
    suspend fun addFavorite(pieceId: Long) {
        pieceFavoriteDao.addFavorite(PieceFavorite(pieceId))
    }
    
    suspend fun removeFavorite(pieceId: Long) {
        pieceFavoriteDao.removeFavorite(pieceId)
    }
    
    suspend fun isFavorite(pieceId: Long): Boolean {
        return pieceFavoriteDao.isFavorite(pieceId)
    }
    
    suspend fun getFavoriteCount(): Int {
        return pieceFavoriteDao.getFavoriteCount()
    }
    
    // Enhanced activity operations with statistics maintenance
    suspend fun insertActivity(activity: Activity) {
        activityDao.insert(activity)
        updatePieceStatistics(activity)
    }
    
    suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity)
        // Note: For updates, we should recalculate all statistics for the piece
        // This is more complex but ensures accuracy
        recalculatePieceStatistics(activity.pieceOrTechniqueId)
    }
    
    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
        // Recalculate statistics after deletion
        recalculatePieceStatistics(activity.pieceOrTechniqueId)
    }
    
    suspend fun deleteAllActivities() = 
        activityDao.deleteAll()
    
    suspend fun getStreakCount(startTime: Long): Int = 
        activityDao.getStreakCount(startTime)
    
    suspend fun getActivityCount(): Int = 
        activityDao.getActivityCount()
    
    fun getAllActivitiesWithPieces(): Flow<List<ActivityWithPiece>> {
        return combine(
            getAllActivities(),
            getAllPiecesAndTechniques()
        ) { activities, pieces ->
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }
        }
    }
    
    suspend fun calculateCurrentStreak(): Int {
        val activities = getAllActivities().first()
        return StreakCalculator().calculateCurrentStreak(activities)
    }
    
    suspend fun getPieceOrTechniqueById(id: Long): PieceOrTechnique? {
        return pieceOrTechniqueDao.getById(id)
    }
    
    suspend fun exportToCsv(writer: Writer) {
        Log.d("ExportDebug", "Repository.exportToCsv called")
        try {
            Log.d("ExportDebug", "Getting activities from database...")
            val activities = getAllActivities().first().sortedBy { it.timestamp }
            Log.d("ExportDebug", "Got ${activities.size} activities")
            
            Log.d("ExportDebug", "Getting pieces from database...")
            val pieces = getAllPiecesAndTechniques().first().associateBy { it.id }
            Log.d("ExportDebug", "Got ${pieces.size} pieces")
            
            Log.d("ExportDebug", "Calling CsvHandler.exportActivitiesToCsv")
            CsvHandler.exportActivitiesToCsv(writer, activities, pieces)
            Log.d("ExportDebug", "CsvHandler.exportActivitiesToCsv completed")
        } catch (e: Exception) {
            Log.e("ExportDebug", "Exception in Repository.exportToCsv: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }
    
    suspend fun importFromCsv(reader: Reader): CsvHandler.ImportResult {
        val result = CsvHandler.importActivitiesFromCsv(reader)
        
        // Save current favorites before clearing data (do this regardless of errors)
        val existingFavorites = getFavorites().first()
        val favoritesByName = existingFavorites.associate { it.name to true }
        
        // Always clear existing data to prevent duplicates, even if there are errors
        deleteAllActivities()
        deleteAllPiecesAndTechniques()
        // Clear favorites as well since pieces are being recreated with new IDs
        pieceFavoriteDao.deleteAll()
        
        // Only proceed with import if we have activities to import
        if (result.activities.isNotEmpty()) {
            // Create piece/technique map
            val pieceMap = mutableMapOf<String, Long>()
            
            // Insert unique pieces/techniques
            Log.d("ImportRepo", "Creating pieces from ${result.uniquePieceNames.size} unique names: ${result.uniquePieceNames}")
            result.uniquePieceNames.forEach { pieceName ->
                // First check if any imported activity provides explicit type info for this piece
                val explicitType = result.activities
                    .firstOrNull { it.pieceName == pieceName && it.pieceType != null }
                    ?.pieceType
                
                // Determine if it's a piece or technique - use explicit type if available, otherwise use heuristic
                val itemType = explicitType ?: when {
                    pieceName.contains("Scale", ignoreCase = true) ||
                    pieceName.contains("Arpeggio", ignoreCase = true) ||
                    pieceName.contains("Exercise", ignoreCase = true) ||
                    pieceName.contains("Technique", ignoreCase = true) -> ItemType.TECHNIQUE
                    else -> ItemType.PIECE
                }
                
                // Create piece without favorite status (will be handled separately)
                val piece = PieceOrTechnique(
                    name = pieceName,
                    type = itemType
                )
                
                val id = insertPieceOrTechnique(piece)
                pieceMap[pieceName] = id
                
                // Restore favorite status if this piece was previously a favorite
                if (favoritesByName[pieceName] == true) {
                    addFavorite(id)
                }
                
                Log.d("ImportRepo", "Created piece: '$pieceName' with ID $id")
            }
            
            // Insert activities
            result.activities.forEach { importedActivity ->
                val pieceId = pieceMap[importedActivity.pieceName]
                if (pieceId != null) {
                    val activity = Activity(
                        timestamp = importedActivity.timestamp,
                        pieceOrTechniqueId = pieceId,
                        activityType = importedActivity.activityType,
                        level = importedActivity.level,
                        performanceType = importedActivity.performanceType,
                        minutes = importedActivity.minutes,
                        notes = importedActivity.notes
                    )
                    insertActivity(activity)
                }
            }
        }
        
        return result
    }
    
    /**
     * Updates piece statistics when a new activity is added
     */
    private suspend fun updatePieceStatistics(activity: Activity) {
        val currentTime = System.currentTimeMillis()
        
        when (activity.activityType) {
            ActivityType.PRACTICE -> {
                // Get current piece data for recent activities calculation
                val piece = pieceOrTechniqueDao.getById(activity.pieceOrTechniqueId)
                val updatedRecentPractices = PieceStatisticsHelper.addTimestampToRecent(
                    piece?.lastThreePractices, 
                    activity.timestamp
                )
                
                // Update practice statistics
                pieceOrTechniqueDao.updatePracticeStats(
                    id = activity.pieceOrTechniqueId,
                    timestamp = activity.timestamp,
                    duration = if (activity.minutes > 0) activity.minutes else 0,
                    lastThreePractices = updatedRecentPractices
                )
                
                // Update average practice level if level is valid
                if (activity.level > 0) {
                    updateAveragePracticeLevel(activity.pieceOrTechniqueId)
                }
            }
            
            ActivityType.PERFORMANCE -> {
                // Get current piece data for recent activities calculation
                val piece = pieceOrTechniqueDao.getById(activity.pieceOrTechniqueId)
                val updatedRecentPerformances = PieceStatisticsHelper.addTimestampToRecent(
                    piece?.lastThreePerformances, 
                    activity.timestamp
                )
                
                // Update performance statistics
                pieceOrTechniqueDao.updatePerformanceStats(
                    id = activity.pieceOrTechniqueId,
                    timestamp = activity.timestamp,
                    lastThreePerformances = updatedRecentPerformances
                )
            }
        }
    }
    
    /**
     * Recalculates all statistics for a piece (used after updates/deletions)
     */
    private suspend fun recalculatePieceStatistics(pieceId: Long) {
        val activities = activityDao.getActivitiesForPiece(pieceId).first()
        val currentTime = System.currentTimeMillis()
        
        // Separate activities by type
        val practices = activities.filter { it.activityType == ActivityType.PRACTICE }
        val performances = activities.filter { it.activityType == ActivityType.PERFORMANCE }
        
        // Calculate basic statistics
        val practiceCount = practices.size
        val performanceCount = performances.size
        val lastPracticeDate = practices.maxByOrNull { it.timestamp }?.timestamp
        val lastPerformanceDate = performances.maxByOrNull { it.timestamp }?.timestamp
        val totalPracticeDuration = practices.sumOf { if (it.minutes > 0) it.minutes else 0 }
        
        // Calculate average practice level
        val practicesWithLevel = practices.filter { it.level > 0 }
        val averagePracticeLevel = if (practicesWithLevel.isNotEmpty()) {
            practicesWithLevel.map { it.level }.average().toFloat()
        } else null
        
        // Calculate recent activities (last 3 of each type)
        val recentPractices = practices
            .sortedByDescending { it.timestamp }
            .take(3)
            .map { it.timestamp }
        val recentPerformances = performances
            .sortedByDescending { it.timestamp }
            .take(3)
            .map { it.timestamp }
        
        // Create updated piece with new statistics
        val currentPiece = pieceOrTechniqueDao.getById(pieceId)
        currentPiece?.let { piece ->
            val updatedPiece = piece.copy(
                practiceCount = practiceCount,
                performanceCount = performanceCount,
                lastPracticeDate = lastPracticeDate,
                lastPerformanceDate = lastPerformanceDate,
                totalPracticeDuration = totalPracticeDuration,
                averagePracticeLevel = averagePracticeLevel,
                lastUpdated = currentTime,
                lastThreePractices = PieceStatisticsHelper.timestampsToJson(recentPractices),
                lastThreePerformances = PieceStatisticsHelper.timestampsToJson(recentPerformances)
            )
            pieceOrTechniqueDao.update(updatedPiece)
        }
    }
    
    /**
     * Updates the average practice level for a piece
     */
    private suspend fun updateAveragePracticeLevel(pieceId: Long) {
        val practices = activityDao.getActivitiesForPiece(pieceId).first()
            .filter { it.activityType == ActivityType.PRACTICE && it.level > 0 }
        
        if (practices.isNotEmpty()) {
            val avgLevel = practices.map { it.level }.average().toFloat()
            pieceOrTechniqueDao.updateAveragePracticeLevel(pieceId, avgLevel, System.currentTimeMillis())
        }
    }
    
    // New query methods using piece statistics instead of complex joins
    fun getRecentlyPracticed(limit: Int = 10): Flow<List<PieceOrTechnique>> =
        pieceOrTechniqueDao.getRecentlyPracticed(limit)
    
    fun getLeastRecentlyPracticed(beforeDays: Int = 7, limit: Int = 10): Flow<List<PieceOrTechnique>> {
        val beforeTimestamp = System.currentTimeMillis() - (beforeDays * 24 * 60 * 60 * 1000L)
        return pieceOrTechniqueDao.getLeastRecentlyPracticed(beforeTimestamp, limit)
    }
    
    fun getMostPracticed(limit: Int = 10): Flow<List<PieceOrTechnique>> =
        pieceOrTechniqueDao.getMostPracticed(limit)
    
    // Helper method to get pieces with favorite status for UI components
    fun getPiecesWithFavoriteStatus(): Flow<List<PieceWithFavoriteStatus>> {
        return combine(
            getAllPiecesAndTechniques(),
            getFavorites()
        ) { allPieces, favorites ->
            val favoriteIds = favorites.map { it.id }.toSet()
            allPieces.map { piece ->
                PieceWithFavoriteStatus(
                    piece = piece,
                    isFavorite = favoriteIds.contains(piece.id)
                )
            }
        }
    }
    
    // Export methods for piece statistics (separate from activities per DevCycle 2025-0004)
    suspend fun exportPieceStatisticsToCsv(writer: Writer, includeRecentActivities: Boolean = true) {
        val pieces = getAllPiecesAndTechniques().first()
        PieceStatisticsCsvExporter.exportPieceStatistics(writer, pieces, includeRecentActivities)
    }
    
    suspend fun exportPieceFavoritesToCsv(writer: Writer) {
        val favorites = getFavorites().first()
        val favoritesWithDates = favorites.map { piece ->
            val favoriteData = pieceFavoriteDao.getFavorite(piece.id)
            piece to (favoriteData?.dateAdded ?: System.currentTimeMillis())
        }
        PieceStatisticsCsvExporter.exportPieceFavorites(writer, favoritesWithDates)
    }
    
}