package com.pseddev.playstreak.data.repository

import android.util.Log
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.ui.progress.ActivityWithPiece
import com.pseddev.playstreak.utils.CsvHandler
import com.pseddev.playstreak.utils.JsonExporter
import com.pseddev.playstreak.utils.JsonImporter
import com.pseddev.playstreak.utils.StreakCalculator
import com.pseddev.playstreak.data.models.JsonImportResult
import com.pseddev.playstreak.data.models.JsonValidationResult
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
    
    suspend fun insertActivity(activity: Activity) {
        activityDao.insert(activity)
        updatePieceStatistics(activity.pieceOrTechniqueId)
    }
    
    suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity)
        updatePieceStatistics(activity.pieceOrTechniqueId)
    }
    
    suspend fun deleteActivity(activity: Activity) {
        val pieceId = activity.pieceOrTechniqueId
        activityDao.delete(activity)
        updatePieceStatistics(pieceId)
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
    
    // Statistics-based query methods for improved performance
    fun getPiecesWithPracticeHistory(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getPiecesWithPracticeHistory()
    
    fun getPiecesWithPerformanceHistory(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getPiecesWithPerformanceHistory()
    
    fun getRecentlyPracticedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getRecentlyPracticedPieces(limit)
    
    fun getRecentlyPerformedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getRecentlyPerformedPieces(limit)
    
    fun getPiecesWithSatisfactoryPractice(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getPiecesWithSatisfactoryPractice()
    
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
    
    suspend fun exportToJson(writer: Writer) {
        Log.d("JsonExport", "Repository.exportToJson called")
        try {
            Log.d("JsonExport", "Getting pieces and activities from database...")
            val pieces = getAllPiecesAndTechniques().first()
            val activities = getAllActivities().first().sortedBy { it.timestamp }
            
            Log.d("JsonExport", "Got ${pieces.size} pieces and ${activities.size} activities")
            Log.d("JsonExport", "Calling JsonExporter.exportToJson")
            
            JsonExporter.exportToJson(writer, pieces, activities)
            
            Log.d("JsonExport", "JsonExporter.exportToJson completed")
        } catch (e: Exception) {
            Log.e("JsonExport", "Exception in Repository.exportToJson: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }
    
    suspend fun validateJsonForImport(reader: java.io.Reader, pieceLimit: Int, activityLimit: Int): JsonValidationResult {
        return JsonImporter.validateJson(reader, pieceLimit, activityLimit)
    }
    
    suspend fun importFromJson(reader: java.io.Reader): JsonImportResult {
        Log.d("JsonImport", "Repository.importFromJson called")
        
        try {
            // First validate and parse the JSON
            val importResult = JsonImporter.importFromJson(reader)
            
            if (!importResult.success) {
                return importResult
            }
            
            // Get the imported data
            val importedPieces = JsonImporter.getLastImportedPieces()
            val importedActivities = JsonImporter.getLastImportedActivities()
            
            Log.d("JsonImport", "Importing ${importedPieces.size} pieces and ${importedActivities.size} activities")
            
            // Clear existing data
            deleteAllActivities()
            deleteAllPiecesAndTechniques()
            
            // Create piece name to new ID mapping
            val pieceNameToIdMap = mutableMapOf<String, Long>()
            
            // Insert pieces and build mapping
            importedPieces.forEach { piece ->
                val newId = insertPieceOrTechnique(piece)
                pieceNameToIdMap[piece.name] = newId
                Log.d("JsonImport", "Inserted piece '${piece.name}' with new ID $newId")
            }
            
            // Create mapping from original piece IDs to new piece IDs
            val originalPieceIdToNewIdMap = mutableMapOf<Long, Long>()
            val originalPieceIds = JsonImporter.getLastOriginalPieceIds()
            
            originalPieceIds.forEach { (pieceName, originalId) ->
                val newId = pieceNameToIdMap[pieceName]
                if (newId != null) {
                    originalPieceIdToNewIdMap[originalId] = newId
                }
            }
            
            // Insert activities with correct piece IDs
            importedActivities.forEach { activity ->
                val newPieceId = originalPieceIdToNewIdMap[activity.pieceOrTechniqueId]
                if (newPieceId != null) {
                    val activityWithCorrectId = activity.copy(pieceOrTechniqueId = newPieceId)
                    insertActivity(activityWithCorrectId)
                } else {
                    Log.w("JsonImport", "Could not find new piece ID for original piece ID ${activity.pieceOrTechniqueId}")
                }
            }
            
            Log.d("JsonImport", "JSON import completed successfully")
            
            return JsonImportResult(
                success = true,
                piecesImported = importedPieces.size,
                activitiesImported = importedActivities.size,
                errors = emptyList(),
                warnings = emptyList()
            )
            
        } catch (e: Exception) {
            Log.e("JsonImport", "Exception in Repository.importFromJson: ${e.javaClass.simpleName} - ${e.message}", e)
            return JsonImportResult(
                success = false,
                piecesImported = 0,
                activitiesImported = 0,
                errors = listOf("Import failed: ${e.message}"),
                warnings = emptyList()
            )
        }
    }
    
    private suspend fun updatePieceStatistics(pieceId: Long) {
        try {
            val piece = pieceOrTechniqueDao.getById(pieceId) ?: return
            val activities = getActivitiesForPiece(pieceId).first()
            
            // Separate activities by type
            val practices = activities.filter { it.activityType == ActivityType.PRACTICE }
                .sortedByDescending { it.timestamp }
            val performances = activities.filter { it.activityType == ActivityType.PERFORMANCE }
                .sortedByDescending { it.timestamp }
            
            // Calculate statistics
            val practiceCount = practices.size
            val performanceCount = performances.size
            
            // Get last dates for practices
            val lastPracticeDate = practices.getOrNull(0)?.timestamp
            val secondLastPracticeDate = practices.getOrNull(1)?.timestamp
            val thirdLastPracticeDate = practices.getOrNull(2)?.timestamp
            
            // Get last dates for performances
            val lastPerformanceDate = performances.getOrNull(0)?.timestamp
            val secondLastPerformanceDate = performances.getOrNull(1)?.timestamp
            val thirdLastPerformanceDate = performances.getOrNull(2)?.timestamp
            
            // Find last satisfactory activities (level 4 for practice, level 3 for performance)
            val lastSatisfactoryPractice = practices.firstOrNull { it.level >= 4 }?.timestamp
            val lastSatisfactoryPerformance = performances.firstOrNull { it.level >= 3 }?.timestamp
            
            // Update piece with calculated statistics
            val updatedPiece = piece.copy(
                practiceCount = practiceCount,
                performanceCount = performanceCount,
                lastPracticeDate = lastPracticeDate,
                secondLastPracticeDate = secondLastPracticeDate,
                thirdLastPracticeDate = thirdLastPracticeDate,
                lastPerformanceDate = lastPerformanceDate,
                secondLastPerformanceDate = secondLastPerformanceDate,
                thirdLastPerformanceDate = thirdLastPerformanceDate,
                lastSatisfactoryPractice = lastSatisfactoryPractice,
                lastSatisfactoryPerformance = lastSatisfactoryPerformance,
                lastUpdated = System.currentTimeMillis()
            )
            
            pieceOrTechniqueDao.update(updatedPiece)
            Log.d("PieceStats", "Updated statistics for piece ${piece.name}: practices=$practiceCount, performances=$performanceCount")
        } catch (e: Exception) {
            Log.e("PieceStats", "Error updating piece statistics for piece $pieceId", e)
        }
    }
    
}