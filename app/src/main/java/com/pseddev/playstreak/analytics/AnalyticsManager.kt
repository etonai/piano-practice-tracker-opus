package com.pseddev.playstreak.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType

/**
 * Centralized analytics manager for tracking user events in PlayStreak
 * Following Feature #36 implementation requirements
 */
class AnalyticsManager(private val context: Context) {
    
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        (context.applicationContext as PlayStreakApplication).firebaseAnalytics
    }
    
    companion object {
        // Event names - following Firebase naming conventions
        private const val EVENT_ACTIVITY_LOGGED = "activity_logged"
        private const val EVENT_STREAK_ACHIEVED = "streak_achieved"
        private const val EVENT_PIECE_ADDED = "piece_added"
        private const val EVENT_CSV_OPERATION = "csv_operation"
        
        // Parameter names
        private const val PARAM_ACTIVITY_TYPE = "activity_type"
        private const val PARAM_PIECE_TYPE = "piece_type"
        private const val PARAM_HAS_DURATION = "has_duration"
        private const val PARAM_STREAK_LENGTH = "streak_length"
        private const val PARAM_EMOJI_LEVEL = "emoji_level"
        private const val PARAM_TOTAL_PIECES = "total_pieces"
        private const val PARAM_OPERATION_TYPE = "operation_type"
        private const val PARAM_ACTIVITY_COUNT = "activity_count"
        private const val PARAM_SUCCESS = "success"
    }
    
    /**
     * Track when user logs a practice or performance activity
     */
    fun trackActivityLogged(
        activityType: ActivityType,
        pieceType: ItemType,
        hasDuration: Boolean
    ) {
        firebaseAnalytics.logEvent(EVENT_ACTIVITY_LOGGED) {
            param(PARAM_ACTIVITY_TYPE, activityType.name)
            param(PARAM_PIECE_TYPE, pieceType.name)
            param(PARAM_HAS_DURATION, if (hasDuration) 1L else 0L)
        }
    }
    
    /**
     * Track when user achieves a practice streak milestone
     */
    fun trackStreakAchieved(streakLength: Int, emojiLevel: String) {
        firebaseAnalytics.logEvent(EVENT_STREAK_ACHIEVED) {
            param(PARAM_STREAK_LENGTH, streakLength.toLong())
            param(PARAM_EMOJI_LEVEL, emojiLevel)
        }
    }
    
    /**
     * Track when user adds a new piece or technique
     */
    fun trackPieceAdded(pieceType: ItemType, totalPieceCount: Int) {
        firebaseAnalytics.logEvent(EVENT_PIECE_ADDED) {
            param(PARAM_PIECE_TYPE, pieceType.name)
            param(PARAM_TOTAL_PIECES, totalPieceCount.toLong())
        }
    }
    
    /**
     * Track CSV import/export operations
     */
    fun trackCsvOperation(
        operationType: String, // "import" or "export"
        activityCount: Int,
        success: Boolean
    ) {
        firebaseAnalytics.logEvent(EVENT_CSV_OPERATION) {
            param(PARAM_OPERATION_TYPE, operationType)
            param(PARAM_ACTIVITY_COUNT, activityCount.toLong())
            param(PARAM_SUCCESS, if (success) 1L else 0L)
        }
    }
    
    /**
     * Helper function to determine emoji level from streak length
     * Based on current emoji progression: 🎵 (3-4) → 🎶 (5-7) → 🔥 (8-13) → 🔥🔥🔥 (14+)
     */
    fun getEmojiLevelForStreak(streakLength: Int): String {
        return when {
            streakLength >= 14 -> "triple_fire"
            streakLength >= 8 -> "fire"
            streakLength >= 5 -> "musical_notes"
            streakLength >= 3 -> "musical_note"
            else -> "none"
        }
    }
}