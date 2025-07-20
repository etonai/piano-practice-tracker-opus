package com.pseddev.practicetracker.ui.progress

import androidx.lifecycle.*
import com.pseddev.practicetracker.data.entities.Activity
import com.pseddev.practicetracker.data.repository.PianoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.*

class DashboardViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    private val todayEnd = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    
    private val yesterdayStart = todayStart - 24 * 60 * 60 * 1000
    private val yesterdayEnd = todayEnd - 24 * 60 * 60 * 1000
    
    private val weekStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -7)
    }.timeInMillis
    
    val todayActivities: LiveData<List<ActivityWithPiece>> = 
        combine(
            repository.getActivitiesForDateRange(todayStart, todayEnd),
            repository.getAllPiecesAndTechniques()
        ) { activities, pieces ->
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }
        }.asLiveData()
    
    val yesterdayActivities: LiveData<List<ActivityWithPiece>> = 
        combine(
            repository.getActivitiesForDateRange(yesterdayStart, yesterdayEnd),
            repository.getAllPiecesAndTechniques()
        ) { activities, pieces ->
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }
        }.asLiveData()
    
    val weekSummary: LiveData<String> = 
        repository.getActivitiesForDateRange(weekStart, todayEnd)
            .map { activities ->
                val practiceCount = activities.count { it.activityType == com.pseddev.practicetracker.data.entities.ActivityType.PRACTICE }
                val performanceCount = activities.count { it.activityType == com.pseddev.practicetracker.data.entities.ActivityType.PERFORMANCE }
                val totalMinutes = activities.filter { it.minutes > 0 }.sumOf { it.minutes }
                
                // Calculate number of active days
                val activeDays = activities.groupBy { activity ->
                    val cal = Calendar.getInstance().apply { timeInMillis = activity.timestamp }
                    cal.get(Calendar.DAY_OF_YEAR)
                }.size
                
                buildString {
                    append("This week: ")
                    append("$practiceCount practice activit${if (practiceCount != 1) "ies" else "y"}, ")
                    append("$performanceCount performance${if (performanceCount != 1) "s" else ""}")
                    append(" across $activeDays day${if (activeDays != 1) "s" else ""}")
                    if (totalMinutes > 0) {
                        append("\nTotal tracked time: $totalMinutes minutes")
                    }
                }
            }
            .asLiveData()
    
    suspend fun calculateStreak(): Int = repository.calculateCurrentStreak()
}

class DashboardViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}