package com.example.pianotrackopus.ui.progress

import androidx.lifecycle.*
import com.example.pianotrackopus.data.repository.PianoRepository
import kotlinx.coroutines.flow.map

class TimelineViewModel(private val repository: PianoRepository) : ViewModel() {
    
    val activitiesWithPieces: LiveData<List<ActivityWithPiece>> = 
        repository.getAllActivitiesWithPieces()
            .map { list ->
                list.sortedByDescending { it.activity.timestamp }
            }
            .asLiveData()
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