package com.pseddev.playstreak.utils

import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.entities.AchievementType

object AchievementDefinitions {
    
    fun getAllAchievementDefinitions(): List<Achievement> {
        return listOf(
            // First Actions Achievements
            Achievement(
                type = AchievementType.FIRST_PIECE,
                title = "First Steps",
                description = "Added your first piece to PlayStreak",
                iconEmoji = "🎹"
            ),
            Achievement(
                type = AchievementType.FIRST_TECHNIQUE,
                title = "Skill Builder",
                description = "Added your first technique to PlayStreak",
                iconEmoji = "🛠️"
            ),
            Achievement(
                type = AchievementType.FIRST_PRACTICE,
                title = "Practice Makes Perfect",
                description = "Completed your first practice session",
                iconEmoji = "💪"
            ),
            Achievement(
                type = AchievementType.FIRST_PERFORMANCE,
                title = "Debut Performance",
                description = "Completed your first performance",
                iconEmoji = "🎭"
            ),
            Achievement(
                type = AchievementType.FIRST_ONLINE_PERFORMANCE,
                title = "Digital Debut",
                description = "Completed your first online performance",
                iconEmoji = "💻"
            ),
            Achievement(
                type = AchievementType.FIRST_LIVE_PERFORMANCE,
                title = "Stage Presence",
                description = "Completed your first live performance",
                iconEmoji = "🎤"
            ),
            
            // Streak Milestone Achievements
            Achievement(
                type = AchievementType.STREAK_3_DAYS,
                title = "Getting Started",
                description = "Maintained a 3-day practice streak",
                iconEmoji = "🎵"
            ),
            Achievement(
                type = AchievementType.STREAK_5_DAYS,
                title = "Building Momentum",
                description = "Maintained a 5-day practice streak",
                iconEmoji = "🎶"
            ),
            Achievement(
                type = AchievementType.STREAK_8_DAYS,
                title = "Consistency Counts",
                description = "Maintained an 8-day practice streak",
                iconEmoji = "🔥"
            ),
            Achievement(
                type = AchievementType.STREAK_14_DAYS,
                title = "Two Week Warrior",
                description = "Maintained a 14-day practice streak",
                iconEmoji = "🔥🔥🔥"
            ),
            Achievement(
                type = AchievementType.STREAK_30_DAYS,
                title = "Monthly Master",
                description = "Maintained a 30-day practice streak",
                iconEmoji = "⭐⭐⭐"
            ),
            Achievement(
                type = AchievementType.STREAK_61_DAYS,
                title = "Diamond Dedication",
                description = "Maintained a 61-day practice streak",
                iconEmoji = "💎💎💎"
            ),
            Achievement(
                type = AchievementType.STREAK_100_DAYS,
                title = "Elite Performer",
                description = "Maintained a 100-day practice streak",
                iconEmoji = "🚀🚀🚀"
            )
        )
    }
    
    fun getAchievementByType(type: AchievementType): Achievement? {
        return getAllAchievementDefinitions().find { it.type == type }
    }
    
    fun getTotalAchievementsCount(): Int {
        return getAllAchievementDefinitions().size
    }
}