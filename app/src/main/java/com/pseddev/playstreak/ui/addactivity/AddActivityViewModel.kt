package com.pseddev.playstreak.ui.addactivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.analytics.AnalyticsManager
import com.pseddev.playstreak.crashlytics.CrashlyticsManager
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.TextNormalizer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddActivityViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val analyticsManager = AnalyticsManager(context)
    private val crashlyticsManager = CrashlyticsManager(context)
    private val proUserManager = ProUserManager.getInstance(context)
    
    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _editMode = MutableLiveData<Boolean>(false)
    val editMode: LiveData<Boolean> = _editMode
    
    private val _editActivity = MutableLiveData<Activity?>()
    val editActivity: LiveData<Activity?> = _editActivity
    
    fun getPiecesAndTechniques(activityType: ActivityType): LiveData<List<PieceOrTechnique>> {
        return if (activityType == ActivityType.PERFORMANCE) {
            repository.getPieces().asLiveData()
        } else {
            repository.getAllPiecesAndTechniques().asLiveData()
        }
    }
    
    fun getFavorites(): LiveData<List<PieceOrTechnique>> {
        return repository.getFavorites().asLiveData()
    }
    
    fun insertPieceOrTechnique(name: String, type: ItemType, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // Check piece limit before adding
                val currentPieceCount = repository.getAllPiecesAndTechniques().first().size
                if (!proUserManager.canAddMorePieces(currentPieceCount)) {
                    val limit = proUserManager.getPieceLimit()
                    _errorMessage.value = "You have reached the piece limit of $limit pieces and techniques. Cannot add more pieces."
                    return@launch
                }
                
                val id = repository.insertPieceOrTechnique(
                    PieceOrTechnique(
                        name = TextNormalizer.normalizePieceName(name),
                        type = type
                    )
                )
                onComplete(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add piece: ${e.message}"
            }
        }
    }
    
    fun saveActivity(
        pieceId: Long,
        activityType: ActivityType,
        level: Int,
        performanceType: String,
        minutes: Int,
        notes: String
    ) {
        saveActivity(pieceId, activityType, level, performanceType, minutes, notes, System.currentTimeMillis())
    }
    
    fun saveActivity(
        pieceId: Long,
        activityType: ActivityType,
        level: Int,
        performanceType: String,
        minutes: Int,
        notes: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            try {
                // Check activity limit before adding
                val currentActivityCount = repository.getActivityCount()
                val activityLimit = proUserManager.getActivityLimit()
                
                if (currentActivityCount >= activityLimit) {
                    _errorMessage.value = "You have reached the activity limit of $activityLimit activities. Cannot add more activities."
                    return@launch
                }
                
                // Get piece information for analytics
                val piece = repository.getPieceOrTechniqueById(pieceId)
                
                repository.insertActivity(
                    Activity(
                        timestamp = timestamp,
                        pieceOrTechniqueId = pieceId,
                        activityType = activityType,
                        level = level,
                        performanceType = performanceType,
                        minutes = minutes,
                        notes = TextNormalizer.normalizeUserInput(notes)
                    )
                )
                
                // Track analytics event for activity logged
                piece?.let {
                    analyticsManager.trackActivityLogged(
                        activityType = activityType,
                        pieceType = it.type,
                        hasDuration = minutes > 0
                    )
                }
                
                _navigateToMain.value = true
            } catch (e: Exception) {
                // Record crash context for activity creation error
                crashlyticsManager.setCustomKey("activity_type", activityType.name)
                crashlyticsManager.setCustomKey("piece_id", pieceId.toInt())
                crashlyticsManager.setCustomKey("activity_level", level)
                crashlyticsManager.setCustomKey("activity_minutes", minutes)
                crashlyticsManager.recordDatabaseError("insert_activity", e)
                // Re-throw to let UI handle the error
                throw e
            }
        }
    }
    
    fun setEditMode(activity: Activity) {
        _editMode.value = true
        _editActivity.value = activity
    }
    
    fun updateActivity(
        activityId: Long,
        pieceId: Long,
        activityType: ActivityType,
        level: Int,
        performanceType: String,
        minutes: Int,
        notes: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            repository.updateActivity(
                Activity(
                    id = activityId,
                    timestamp = timestamp,
                    pieceOrTechniqueId = pieceId,
                    activityType = activityType,
                    level = level,
                    performanceType = performanceType,
                    minutes = minutes,
                    notes = TextNormalizer.normalizeUserInput(notes)
                )
            )
            _navigateToMain.value = true
        }
    }
    
    fun doneNavigating() {
        _navigateToMain.value = false
    }
    
    fun clearEditMode() {
        _editMode.value = false
        _editActivity.value = null
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

class AddActivityViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddActivityViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}