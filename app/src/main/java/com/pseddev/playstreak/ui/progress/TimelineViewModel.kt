package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.repository.PianoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

class TimelineViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    val activitiesWithPieces: LiveData<List<ActivityWithPiece>> = 
        repository.getAllActivitiesWithPieces()
            .map { activities ->
                activities.sortedByDescending { it.activity.timestamp }
            }
            .asLiveData()
    
    fun deleteActivity(activityWithPiece: ActivityWithPiece) {
        viewModelScope.launch {
            repository.deleteActivity(activityWithPiece.activity)
        }
    }
}

class TimelineViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimelineViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}