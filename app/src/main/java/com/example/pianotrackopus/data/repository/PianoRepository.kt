package com.example.pianotrackopus.data.repository

import com.example.pianotrackopus.data.daos.ActivityDao
import com.example.pianotrackopus.data.daos.PieceOrTechniqueDao
import com.example.pianotrackopus.data.entities.Activity
import com.example.pianotrackopus.data.entities.ItemType
import com.example.pianotrackopus.data.entities.PieceOrTechnique
import kotlinx.coroutines.flow.Flow
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
        val endTime = calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
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
        val endTime = calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
        return getActivitiesForDateRange(startTime, endTime)
    }
    
    suspend fun insertActivity(activity: Activity) = 
        activityDao.insert(activity)
    
    suspend fun deleteAllActivities() = 
        activityDao.deleteAll()
    
    suspend fun getStreakCount(startTime: Long): Int = 
        activityDao.getStreakCount(startTime)
}