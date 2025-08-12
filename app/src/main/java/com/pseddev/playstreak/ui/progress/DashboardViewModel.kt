package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.StreakCalculator
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import android.util.Log
import java.util.*

class DashboardViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    private val suggestionsService = SuggestionsService(proUserManager)
    private val streakCalculator = StreakCalculator()
    
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
    
    // Rolling 7-day period calculation (last 7 days from today)
    private val sevenDaysAgoStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -6) // -6 to include today as day 7
    }.timeInMillis
    
    val todayActivities: LiveData<List<ActivityWithPiece>> = 
        combine(
            repository.getActivitiesForDateRange(todayStart, todayEnd).onEach { 
                Log.d("DashboardFlow", "Activities Flow emitted: ${it.size} activities")
            },
            repository.getAllPiecesAndTechniques().onEach {
                Log.d("DashboardFlow", "Pieces Flow emitted: ${it.size} pieces")
            }
        ) { activities, pieces ->
            Log.d("DashboardFlow", "Combining flows - ${activities.size} activities with pieces")
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }.also { result ->
                Log.d("DashboardFlow", "Final result: ${result.size} ActivityWithPiece items")
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
        repository.getActivitiesForDateRange(sevenDaysAgoStart, todayEnd)
            .map { activities ->
                val practiceCount = activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PRACTICE }
                val performanceCount = activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PERFORMANCE }
                val totalMinutes = activities.filter { it.minutes > 0 }.sumOf { it.minutes }
                
                // Format total time in a user-friendly way
                val timeFormatted = when {
                    totalMinutes >= 60 -> {
                        val hours = totalMinutes / 60
                        val mins = totalMinutes % 60
                        if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
                    }
                    totalMinutes > 0 -> "${totalMinutes}m"
                    else -> "0m"
                }
                
                buildString {
                    append("• $practiceCount practice activit${if (practiceCount != 1) "ies" else "y"}\n")
                    append("• $performanceCount performance activit${if (performanceCount != 1) "ies" else "y"}\n")
                    append("• $timeFormatted total tracked time")
                }
            }
            .asLiveData()
    
    val performanceSuggestions: LiveData<List<SuggestionItem>> = 
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                suggestionsService.generatePerformanceSuggestions(pieces, activities)
            }
            .asLiveData()
    
    val suggestions: LiveData<List<SuggestionItem>> = 
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                // Generate practice suggestions with dashboard limits (same as original combined limits)
                val practiceSuggestions = suggestionsService.generatePracticeSuggestions(pieces, activities)
                
                // Apply dashboard-specific limits (same as generateAllSuggestions used)
                val dashboardFavoriteLimit = if (proUserManager.isProUser()) 
                    ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS 
                    else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS
                val dashboardNonFavoriteLimit = if (proUserManager.isProUser()) 
                    ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS 
                    else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
                    
                val favoritesPractice = practiceSuggestions.filter { it.piece.isFavorite }.take(dashboardFavoriteLimit)
                val nonFavoritesPractice = practiceSuggestions.filter { !it.piece.isFavorite }.take(dashboardNonFavoriteLimit)
                
                favoritesPractice + nonFavoritesPractice
            }
            .asLiveData()
    
    val currentStreak: LiveData<Int> = repository.getAllActivities()
        .map { activities ->
            streakCalculator.calculateCurrentStreak(activities)
        }
        .asLiveData()
    
    suspend fun calculateStreak(): Int = repository.calculateCurrentStreak()
}

class DashboardViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}