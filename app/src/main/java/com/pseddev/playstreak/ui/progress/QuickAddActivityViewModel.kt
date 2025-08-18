package com.pseddev.playstreak.ui.progress

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.analytics.AnalyticsManager
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QuickAddActivityViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    private val analyticsManager = AnalyticsManager(context)
    
    private val _addResult = MutableLiveData<Result<Unit>>()
    val addResult: LiveData<Result<Unit>> = _addResult
    
    fun addActivity(activity: Activity, source: String = "dashboard_quick") {
        GlobalScope.launch {
            try {
                // Check activity limit before adding
                val currentActivityCount = repository.getActivityCount()
                val activityLimit = proUserManager.getActivityLimit()
                
                if (currentActivityCount >= activityLimit) {
                    val limitMessage = "You have reached the activity limit of $activityLimit activities. Cannot add more activities."
                    _addResult.postValue(Result.failure(IllegalStateException(limitMessage)))
                    return@launch
                }
                
                repository.insertActivity(activity)
                
                // Track analytics event with provided source context
                val piece = repository.getPieceOrTechniqueById(activity.pieceOrTechniqueId)
                piece?.let {
                    analyticsManager.trackActivityLogged(
                        activityType = activity.activityType,
                        pieceType = it.type,
                        hasDuration = activity.minutes > 0,
                        source = source
                    )
                }
                
                // Check for streak achievements
                val newStreak = repository.calculateCurrentStreak()
                trackStreakAchievement(newStreak)
                
                _addResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _addResult.postValue(Result.failure(e))
            }
        }
    }
    
    /**
     * Track streak achievement milestones
     */
    private fun trackStreakAchievement(streakLength: Int) {
        // Only track milestones at specific levels (aligned with emoji progression system)
        if (streakLength in listOf(3, 5, 8, 14, 30, 61, 91)) {
            val emojiLevel = analyticsManager.getEmojiLevelForStreak(streakLength)
            analyticsManager.trackStreakAchieved(streakLength, emojiLevel)
        }
    }
}

class QuickAddActivityViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickAddActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickAddActivityViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}