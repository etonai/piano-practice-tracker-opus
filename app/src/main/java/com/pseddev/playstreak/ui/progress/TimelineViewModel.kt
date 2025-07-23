package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class TimelineViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    
    // Feature #33B-Safe: Filter state management (no data filtering yet)
    private val _showPerformancesOnly = MutableLiveData<Boolean>(false)
    val showPerformancesOnly: LiveData<Boolean> = _showPerformancesOnly
    
    val activitiesWithPieces: LiveData<List<ActivityWithPiece>> = 
        _showPerformancesOnly.switchMap { performancesOnly ->
            repository.getAllActivitiesWithPieces()
                .map { activities ->
                    android.util.Log.d("TimelineViewModel", "Feature #33C: Filtering activities - performancesOnly=$performancesOnly, total=${activities.size}")
                    
                    // Feature #33C: Apply filtering based on state
                    val filteredActivities = if (performancesOnly && proUserManager.isProUser()) {
                        val filtered = activities.filter { it.activity.activityType == ActivityType.PERFORMANCE }
                        android.util.Log.d("TimelineViewModel", "Feature #33C: Filtered to ${filtered.size} performance activities")
                        filtered
                    } else {
                        android.util.Log.d("TimelineViewModel", "Feature #33C: Showing all ${activities.size} activities")
                        activities
                    }
                    
                    filteredActivities.sortedByDescending { it.activity.timestamp }
                }
                .asLiveData()
        }
    
    val isProUser: Boolean
        get() = proUserManager.isProUser()
    
    // Feature #33B-Safe: Simple toggle method
    fun toggleFilter() {
        if (proUserManager.isProUser()) {
            val newValue = !(_showPerformancesOnly.value ?: false)
            android.util.Log.d("TimelineViewModel", "Feature #33B-Safe: Toggling filter from ${_showPerformancesOnly.value} to $newValue")
            _showPerformancesOnly.value = newValue
        } else {
            android.util.Log.d("TimelineViewModel", "Feature #33B-Safe: Toggle denied - user is not Pro")
        }
    }
    
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