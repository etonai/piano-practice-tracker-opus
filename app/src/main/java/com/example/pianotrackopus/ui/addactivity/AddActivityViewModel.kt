package com.example.pianotrackopus.ui.addactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pianotrackopus.data.entities.Activity
import com.example.pianotrackopus.data.entities.ActivityType
import com.example.pianotrackopus.data.entities.ItemType
import com.example.pianotrackopus.data.entities.PieceOrTechnique
import com.example.pianotrackopus.data.repository.PianoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AddActivityViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain
    
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
                    name = name,
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
        viewModelScope.launch {
            repository.insertActivity(
                Activity(
                    timestamp = System.currentTimeMillis(),
                    pieceOrTechniqueId = pieceId,
                    activityType = activityType,
                    level = level,
                    performanceType = performanceType,
                    minutes = minutes,
                    notes = notes
                )
            )
            _navigateToMain.value = true
        }
    }
    
    fun doneNavigating() {
        _navigateToMain.value = false
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