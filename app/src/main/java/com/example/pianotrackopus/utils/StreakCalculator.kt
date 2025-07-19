package com.example.pianotrackopus.utils

import com.example.pianotrackopus.data.entities.Activity
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
        
        var streak = 0
        val currentDate = today.clone() as Calendar
        
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
            } else if (currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1) {
                // Yesterday had no activity, check if we're still in grace period (today)
                break
            } else {
                break
            }
        }
        
        return streak
    }
}