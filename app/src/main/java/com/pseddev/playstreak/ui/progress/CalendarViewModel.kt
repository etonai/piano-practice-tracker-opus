package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.repository.PianoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import java.util.*

data class DayActivitySummary(
    val activities: List<Activity>
)

data class MonthlyActivitySummary(
    val activeDays: Int,
    val totalActivities: Int,
    val dailyActivities: Map<Long, List<ActivityWithPiece>>
)

class CalendarViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val currentMonth = MutableStateFlow(getCurrentMonthRange())
    
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
    
    val monthlyActivitySummary: LiveData<MonthlyActivitySummary> = 
        currentMonth.flatMapLatest { (startTime, endTime) ->
            repository.getActivitiesForDateRange(startTime, endTime)
                .combine(repository.getAllPiecesAndTechniques()) { activities, pieces ->
                    val activitiesWithPieces = activities.mapNotNull { activity ->
                        val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                        piece?.let { ActivityWithPiece(activity, it) }
                    }
                    
                    // Group activities by date
                    val activitiesByDate = activitiesWithPieces.groupBy { activityWithPiece ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = activityWithPiece.activity.timestamp
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        calendar.timeInMillis
                    }
                    
                    MonthlyActivitySummary(
                        activeDays = activitiesByDate.keys.size,
                        totalActivities = activitiesWithPieces.size,
                        dailyActivities = activitiesByDate
                    )
                }
        }
        .asLiveData()
    
    fun selectDate(dateMillis: Long) {
        selectedDate.value = dateMillis
        
        // Update current month if date changed to different month
        val newMonthRange = getMonthRangeForDate(dateMillis)
        if (newMonthRange != currentMonth.value) {
            currentMonth.value = newMonthRange
        }
    }
    
    private fun getCurrentMonthRange(): Pair<Long, Long> {
        return getMonthRangeForDate(System.currentTimeMillis())
    }
    
    private fun getMonthRangeForDate(dateMillis: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
        return Pair(startTime, endTime)
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