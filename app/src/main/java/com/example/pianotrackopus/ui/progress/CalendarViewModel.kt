package com.example.pianotrackopus.ui.progress

import androidx.lifecycle.*
import com.example.pianotrackopus.data.entities.Activity
import com.example.pianotrackopus.data.repository.PianoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import java.util.*

data class DayActivitySummary(
    val activities: List<Activity>
)

class CalendarViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val selectedDate = MutableStateFlow(System.currentTimeMillis())
    
    val selectedDateActivities: LiveData<List<ActivityWithPiece>> = 
        selectedDate.flatMapLatest { date ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            
            repository.getActivitiesForDateRange(startTime, endTime)
                .combine(repository.getAllPiecesAndTechniques()) { activities, pieces ->
                    activities.mapNotNull { activity ->
                        val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                        piece?.let { ActivityWithPiece(activity, it) }
                    }
                }
        }
        .asLiveData()
    
    fun selectDate(dateMillis: Long) {
        selectedDate.value = dateMillis
    }
}

class CalendarViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}