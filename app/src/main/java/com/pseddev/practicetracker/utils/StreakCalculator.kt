package com.pseddev.practicetracker.utils

import com.pseddev.practicetracker.data.entities.Activity
import java.util.Calendar

class StreakCalculator {
    
    fun calculateCurrentStreak(activities: List<Activity>): Int {
        if (activities.isEmpty()) return 0
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Check if there was activity today
        val todayStart = today.timeInMillis
        val todayEndCalendar = today.clone() as Calendar
        todayEndCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val todayEnd = todayEndCalendar.timeInMillis
        
        val hasActivityToday = activities.any { 
            it.timestamp >= todayStart && it.timestamp < todayEnd 
        }
        
        // Start from today if there's activity today, otherwise start from yesterday
        val startDate = if (hasActivityToday) {
            today.clone() as Calendar
        } else {
            val yesterday = today.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            yesterday
        }
        
        var streak = 0
        val currentDate = startDate.clone() as Calendar
        
        while (true) {
            val dayStart = currentDate.timeInMillis
            val dayEndCalendar = currentDate.clone() as Calendar
            dayEndCalendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = dayEndCalendar.timeInMillis
            
            val hasActivity = activities.any { 
                it.timestamp >= dayStart && it.timestamp < dayEnd 
            }
            
            if (hasActivity) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }
}