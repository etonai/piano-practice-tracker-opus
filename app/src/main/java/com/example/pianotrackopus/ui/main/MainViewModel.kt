package com.example.pianotrackopus.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pianotrackopus.data.repository.PianoRepository
import com.example.pianotrackopus.utils.StreakCalculator
import kotlinx.coroutines.flow.map

class MainViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val streakCalculator = StreakCalculator()
    
    val currentStreak: LiveData<Int> = repository.getAllActivities()
        .map { activities ->
            streakCalculator.calculateCurrentStreak(activities)
        }
        .asLiveData()
}

class MainViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}