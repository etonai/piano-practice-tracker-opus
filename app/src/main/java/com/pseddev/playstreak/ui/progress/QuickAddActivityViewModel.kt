package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.repository.PianoRepository
import kotlinx.coroutines.launch

class QuickAddActivityViewModel(
    private val repository: PianoRepository
) : ViewModel() {
    
    private val _addResult = MutableLiveData<Result<Unit>>()
    val addResult: LiveData<Result<Unit>> = _addResult
    
    suspend fun addActivity(activity: Activity) {
        try {
            repository.insertActivity(activity)
            _addResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _addResult.postValue(Result.failure(e))
        }
    }
}

class QuickAddActivityViewModelFactory(
    private val repository: PianoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickAddActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickAddActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}