package com.pseddev.playstreak.ui.achievements

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.data.AppDatabase
import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.AchievementManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class AchievementCategory(
    val categoryName: String,
    val achievements: List<Achievement>
)

class AchievementsViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val database = AppDatabase.getDatabase(context)
    private val achievementDao = database.achievementDao()
    private val achievementManager = AchievementManager(context, repository)
    
    val categorizedAchievements: LiveData<List<AchievementCategory>> = 
        achievementDao.getAllAchievements().map { achievements ->
            val firstActions = achievements.filter { 
                it.type.name.startsWith("FIRST_") 
            }
            val streakMilestones = achievements.filter { 
                it.type.name.startsWith("STREAK_") 
            }
            
            listOf(
                AchievementCategory("First Actions", firstActions),
                AchievementCategory("Streak Milestones", streakMilestones)
            )
        }.asLiveData()
    
    val achievementCounts: LiveData<Pair<Int, Int>> = 
        achievementDao.getAllAchievements().map { achievements ->
            val unlockedCount = achievements.count { it.isUnlocked }
            val totalCount = achievements.size
            Pair(unlockedCount, totalCount)
        }.asLiveData()
    
    init {
        // Initialize achievements system if needed
        viewModelScope.launch {
            achievementManager.initializeAchievements()
        }
    }
}

class AchievementsViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementsViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}