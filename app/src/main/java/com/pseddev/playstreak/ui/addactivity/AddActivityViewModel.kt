package com.pseddev.playstreak.ui.addactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.TextNormalizer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AddActivityViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain
    
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
            val id = repository.insertPieceOrTechnique(
                PieceOrTechnique(
                    name = TextNormalizer.normalizePieceName(name),
                    type = type,
                    isFavorite = false
                )
            )
            onComplete(id)
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
            _navigateToMain.value = true
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
}

class AddActivityViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}