package com.pseddev.practicetracker.data.repository

import android.util.Log
import com.pseddev.practicetracker.data.daos.ActivityDao
import com.pseddev.practicetracker.data.daos.PieceOrTechniqueDao
import com.pseddev.practicetracker.data.entities.Activity
import com.pseddev.practicetracker.data.entities.ItemType
import com.pseddev.practicetracker.data.entities.PieceOrTechnique
import com.pseddev.practicetracker.ui.progress.ActivityWithPiece
import com.pseddev.practicetracker.utils.CsvHandler
import com.pseddev.practicetracker.utils.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.io.Writer
import java.io.Reader
import java.util.Calendar

class PianoRepository(
    private val pieceOrTechniqueDao: PieceOrTechniqueDao,
    private val activityDao: ActivityDao
) {
    
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getAllPiecesAndTechniques()
    
    fun getFavorites(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getFavorites()
    
    fun getPieces(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getByType(ItemType.PIECE)
    
    fun getTechniques(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getByType(ItemType.TECHNIQUE)
    
    suspend fun insertPieceOrTechnique(item: PieceOrTechnique): Long = 
        pieceOrTechniqueDao.insert(item)
    
    suspend fun updatePieceOrTechnique(item: PieceOrTechnique) = 
        pieceOrTechniqueDao.update(item)
    
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
    
    suspend fun insertActivity(activity: Activity) = 
        activityDao.insert(activity)
    
    suspend fun deleteAllActivities() = 
        activityDao.deleteAll()
    
    suspend fun getStreakCount(startTime: Long): Int = 
        activityDao.getStreakCount(startTime)
    
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
        val existingPieces = getAllPiecesAndTechniques().first()
        val favoritesByName = existingPieces
            .filter { it.isFavorite }
            .associate { it.name to it.isFavorite }
        
        // Always clear existing data to prevent duplicates, even if there are errors
        deleteAllActivities()
        deleteAllPiecesAndTechniques()
        
        // Only proceed with import if we have activities to import
        if (result.activities.isNotEmpty()) {
            // Create piece/technique map
            val pieceMap = mutableMapOf<String, Long>()
            
            // Insert unique pieces/techniques
            Log.d("ImportRepo", "Creating pieces from ${result.uniquePieceNames.size} unique names: ${result.uniquePieceNames}")
            result.uniquePieceNames.forEach { pieceName ->
                // Determine if it's a piece or technique based on common patterns
                val itemType = when {
                    pieceName.contains("Scale", ignoreCase = true) ||
                    pieceName.contains("Arpeggio", ignoreCase = true) ||
                    pieceName.contains("Exercise", ignoreCase = true) ||
                    pieceName.contains("Technique", ignoreCase = true) -> ItemType.TECHNIQUE
                    else -> ItemType.PIECE
                }
                
                // Preserve favorite status if this piece was previously a favorite
                val isFavorite = favoritesByName[pieceName] ?: false
                
                val piece = PieceOrTechnique(
                    name = pieceName,
                    type = itemType,
                    isFavorite = isFavorite
                )
                
                val id = insertPieceOrTechnique(piece)
                pieceMap[pieceName] = id
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
    
}