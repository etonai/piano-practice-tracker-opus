package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.repository.PianoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimelineViewModel(private val repository: PianoRepository) : ViewModel() {
    
    val activitiesWithPieces: LiveData<List<ActivityWithPiece>> = 
        repository.getAllActivitiesWithPieces()
            .map { list ->
                list.sortedByDescending { it.activity.timestamp }
            }
            .asLiveData()
    
    fun deleteActivity(activityWithPiece: ActivityWithPiece) {
        viewModelScope.launch {
            repository.deleteActivity(activityWithPiece.activity)
        }
    }
}

class TimelineViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimelineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}