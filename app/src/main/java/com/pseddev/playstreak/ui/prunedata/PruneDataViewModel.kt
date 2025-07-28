package com.pseddev.playstreak.ui.prunedata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pseddev.playstreak.data.repository.PianoRepository

class PruneDataViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    // Phase 1: Basic ViewModel foundation
    // Future phases will add:
    // - Activity count LiveData
    // - Pruning functionality
    // - Lifetime counter management
    // - Error handling and validation
    
    init {
        // Initialize ViewModel for Phase 1
        // Functionality will be added in subsequent phases
    }
}

class PruneDataViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PruneDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PruneDataViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}