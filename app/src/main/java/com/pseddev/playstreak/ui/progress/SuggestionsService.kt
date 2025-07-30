package com.pseddev.playstreak.ui.progress

import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.utils.ProUserManager
import java.util.*

/**
 * Centralized service for generating practice and performance suggestions.
 * This consolidates the suggestion algorithms used by both DashboardViewModel and SuggestionsViewModel
 * to ensure consistency and eliminate code duplication.
 */
class SuggestionsService(
    private val proUserManager: ProUserManager
) {
    
    private val now = System.currentTimeMillis()
    private val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)
    private val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
    private val thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)
    private val twentyEightDaysAgo = now - (28 * 24 * 60 * 60 * 1000L)
    
    // Calculate start of today (midnight) to exclude pieces practiced today
    private val startOfToday = run {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }
    
    /**
     * Generate all suggestions (practice + performance) for the given pieces and activities.
     * This is the main entry point used by both ViewModels.
     */
    fun generateAllSuggestions(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>
    ): List<SuggestionItem> {
        val practiceSuggestions = generatePracticeSuggestions(pieces, activities)
        val performanceSuggestions = generatePerformanceSuggestions(pieces, activities)
        
        // Combine practice and performance suggestions while respecting favorite limits
        val allSuggestions = practiceSuggestions + performanceSuggestions
        
        // Apply favorite limits across all suggestion types
        val finalFavoriteLimit = if (proUserManager.isProUser()) 
            ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS 
            else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS
        val finalNonFavoriteLimit = if (proUserManager.isProUser()) 
            ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS + ProUserManager.PRO_USER_PERFORMANCE_SUGGESTIONS 
            else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
        val favoriteSuggestionsInAll = allSuggestions.filter { it.piece.isFavorite }
        val nonFavoriteSuggestionsInAll = allSuggestions.filter { !it.piece.isFavorite }
        
        // Limit favorites to user limit and preserve order
        val limitedFavorites = favoriteSuggestionsInAll.take(finalFavoriteLimit)
        val limitedNonFavorites = nonFavoriteSuggestionsInAll.take(finalNonFavoriteLimit)
        
        return limitedFavorites + limitedNonFavorites
    }
    
    /**
     * Generate performance suggestions for Pro users only.
     * Returns empty list for non-Pro users.
     */
    fun generatePerformanceSuggestions(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>
    ): List<SuggestionItem> {
        if (!proUserManager.isProUser()) {
            return emptyList()
        }
        
        val pieceActivities = activities.groupBy { it.pieceOrTechniqueId }
        val performanceActivities = activities.filter { it.activityType == ActivityType.PERFORMANCE }
        val piecePerformanceActivities = performanceActivities.groupBy { it.pieceOrTechniqueId }
        
        val firstTierSuggestions = mutableListOf<SuggestionItem>()
        val secondTierSuggestions = mutableListOf<SuggestionItem>()
        
        pieces.filter { it.type == ItemType.PIECE }.forEach { piece ->
            val allPieceActivities = pieceActivities[piece.id] ?: emptyList()
            val piecePerformances = piecePerformanceActivities[piece.id] ?: emptyList()
            
            // Get practice activities in last 28 days
            val recentPractices = allPieceActivities.filter { 
                it.activityType == ActivityType.PRACTICE && it.timestamp >= twentyEightDaysAgo 
            }
            
            // Check if practiced at least 3 times in last 28 days AND has at least one Level 4 practice
            val hasLevel4Practice = recentPractices.any { it.level == 4 }
            if (recentPractices.size >= 3 && hasLevel4Practice) {
                // Use piece fields for last activity dates instead of expensive database queries
                val lastPerformanceDate = piece.lastPerformanceDate
                val lastPracticeDate = piece.lastPracticeDate
                
                val daysSinceLastPerformance = if (lastPerformanceDate != null) {
                    ((now - lastPerformanceDate) / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    Int.MAX_VALUE
                }
                
                val daysSinceLastPractice = if (lastPracticeDate != null) {
                    ((now - lastPracticeDate) / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    Int.MAX_VALUE
                }
                
                // First tier: Practiced ≥3 times in 28 days BUT not performed in 28 days
                if (lastPerformanceDate == null || lastPerformanceDate < twentyEightDaysAgo) {
                    val favoritePrefix = if (piece.isFavorite) "⭐ " else ""
                    firstTierSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastPracticeDate, // Use practice date for sorting
                            daysSinceLastActivity = daysSinceLastPractice,
                            suggestionReason = if (lastPerformanceDate == null) {
                                "${favoritePrefix}${recentPractices.size} practices, never performed"
                            } else {
                                "${favoritePrefix}${recentPractices.size} practices, last performance $daysSinceLastPerformance days ago"
                            },
                            suggestionType = SuggestionType.PERFORMANCE
                        )
                    )
                } else if (lastPerformanceDate!! < startOfToday) {
                    // Second tier: Practiced ≥3 times in 28 days AND performed in 28 days BUT not today
                    val favoritePrefix = if (piece.isFavorite) "⭐ " else ""
                    secondTierSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastPerformanceDate, // Use performance date for sorting
                            daysSinceLastActivity = daysSinceLastPerformance,
                            suggestionReason = "${favoritePrefix}${recentPractices.size} practices, last performance $daysSinceLastPerformance days ago",
                            suggestionType = SuggestionType.PERFORMANCE
                        )
                    )
                }
            }
        }
        
        // Sort first tier by most recent practice (lowest daysSinceLastPractice)
        val sortedFirstTier = firstTierSuggestions.sortedWith(
            compareBy<SuggestionItem> { it.daysSinceLastActivity } // Most recent practice first
            .thenBy { it.piece.name.lowercase() }
        )
        
        // Sort second tier by least recent performance (highest daysSinceLastPerformance)
        // For ties, prioritize pieces with more practices
        val sortedSecondTier = secondTierSuggestions.sortedWith(
            compareByDescending<SuggestionItem> { it.daysSinceLastActivity } // Least recent performance first
            .thenByDescending { suggestion -> 
                // Count practices for tiebreaker
                val allPieceActivities = pieceActivities[suggestion.piece.id] ?: emptyList()
                allPieceActivities.filter { 
                    it.activityType == ActivityType.PRACTICE && it.timestamp >= twentyEightDaysAgo 
                }.size
            }
            .thenBy { it.piece.name.lowercase() }
        )
        
        // Combine tiers and take Pro user performance suggestions limit
        return (sortedFirstTier + sortedSecondTier).take(ProUserManager.PRO_USER_PERFORMANCE_SUGGESTIONS)
    }
    
    /**
     * Generate practice suggestions based on favorites and non-favorites logic.
     */
    fun generatePracticeSuggestions(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>
    ): List<SuggestionItem> {
        val favoriteSuggestions = mutableListOf<SuggestionItem>()
        val nonFavoriteSuggestions = mutableListOf<SuggestionItem>()
        
        pieces.filter { it.type == ItemType.PIECE }.forEach { piece ->
            // Use piece fields instead of expensive database queries
            val lastPracticeDate = piece.lastPracticeDate
            val lastPerformanceDate = piece.lastPerformanceDate
            
            // Determine most recent activity and its type
            val (lastActivityDate, isLastActivityPerformance) = when {
                lastPracticeDate != null && lastPerformanceDate != null -> {
                    if (lastPracticeDate > lastPerformanceDate) {
                        lastPracticeDate to false
                    } else {
                        lastPerformanceDate to true
                    }
                }
                lastPracticeDate != null -> lastPracticeDate to false
                lastPerformanceDate != null -> lastPerformanceDate to true
                else -> null to false
            }
            
            val daysSince = if (lastActivityDate != null) {
                ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
            } else {
                Int.MAX_VALUE
            }
            
            if (piece.isFavorite) {
                // Favorites that haven't been practiced in 2+ days AND haven't been practiced today
                if ((lastActivityDate == null || lastActivityDate < twoDaysAgo) && 
                    (lastActivityDate == null || lastActivityDate < startOfToday)) {
                    favoriteSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince,
                            suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                                val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                                val dayText = if (daysSince == 1) "day" else "days"
                                "Favorite piece - $activityTypeText $daysSince $dayText ago"
                            }
                        )
                    )
                }
            } else {
                // Non-favorites that haven't been practiced in 7+ days but have been practiced in last 31 days
                if (lastActivityDate != null && lastActivityDate < sevenDaysAgo && lastActivityDate >= thirtyOneDaysAgo) {
                    nonFavoriteSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince,
                            suggestionReason = {
                                val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                                val dayText = if (daysSince == 1) "day" else "days"
                                "$activityTypeText $daysSince $dayText ago"
                            }()
                        )
                    )
                }
            }
        }
        
        // Determine favorite limit based on Pro status
        val favoriteLimit = if (proUserManager.isProUser()) 
            ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS 
            else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS
        
        // Always aim for up to limit favorites total
        val finalFavoriteSuggestions = if (favoriteSuggestions.size < favoriteLimit) {
            val neededCount = favoriteLimit - favoriteSuggestions.size
            val usedPieceIds = favoriteSuggestions.map { it.piece.id }.toSet()
            
            // Get fallback favorites (excluding already used pieces and pieces practiced today)
            val allFavorites = pieces.filter { it.type == ItemType.PIECE && it.isFavorite && it.id !in usedPieceIds }
            val fallbackFavorites = if (allFavorites.isNotEmpty()) {
                val favoritesWithActivity = allFavorites.mapNotNull { piece ->
                    // Use piece fields instead of expensive database queries
                    val lastPracticeDate = piece.lastPracticeDate
                    val lastPerformanceDate = piece.lastPerformanceDate
                    
                    // Determine most recent activity and its type
                    val (lastActivityDate, isLastActivityPerformance) = when {
                        lastPracticeDate != null && lastPerformanceDate != null -> {
                            if (lastPracticeDate > lastPerformanceDate) {
                                lastPracticeDate to false
                            } else {
                                lastPerformanceDate to true
                            }
                        }
                        lastPracticeDate != null -> lastPracticeDate to false
                        lastPerformanceDate != null -> lastPerformanceDate to true
                        else -> null to false
                    }
                    
                    // Exclude pieces practiced today
                    if (lastActivityDate != null && lastActivityDate >= startOfToday) {
                        return@mapNotNull null
                    }
                    
                    val daysSince = if (lastActivityDate != null) {
                        ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                    } else {
                        Int.MAX_VALUE
                    }
                    
                    SuggestionItem(
                        piece = piece,
                        lastActivityDate = lastActivityDate,
                        daysSinceLastActivity = daysSince,
                        suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                            val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                            val dayText = if (daysSince == 1) "day" else "days"
                            "Favorite piece - $activityTypeText $daysSince $dayText ago"
                        }
                    )
                }
                // Get the least recently practiced favorites (highest daysSince)
                if (favoritesWithActivity.isNotEmpty()) {
                    val maxDays = favoritesWithActivity.maxOf { it.daysSinceLastActivity }
                    val candidates = favoritesWithActivity.filter { it.daysSinceLastActivity == maxDays }
                    // Sort by time (earliest first for tie-breaking), then alphabetically
                    candidates.sortedWith(compareBy<SuggestionItem> { it.lastActivityDate ?: 0L }
                        .thenBy { it.piece.name.lowercase() })
                        .take(neededCount)
                } else emptyList()
            } else emptyList()
            
            favoriteSuggestions + fallbackFavorites
        } else favoriteSuggestions
        
        // Determine non-favorite limit based on Pro status
        val nonFavoriteLimit = if (proUserManager.isProUser()) 
            ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS 
            else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
        
        // Always aim for up to limit non-favorites total
        val finalNonFavoriteSuggestions = if (nonFavoriteSuggestions.size < nonFavoriteLimit) {
            val neededCount = nonFavoriteLimit - nonFavoriteSuggestions.size
            val usedPieceIds = nonFavoriteSuggestions.map { it.piece.id }.toSet()
            
            // Get fallback non-favorites (excluding already used pieces)
            val allNonFavorites = pieces.filter { it.type == ItemType.PIECE && !it.isFavorite && it.id !in usedPieceIds }
            val fallbackNonFavorites = if (allNonFavorites.isNotEmpty()) {
                val nonFavoritesWithActivity = allNonFavorites.mapNotNull { piece ->
                    // Use piece fields instead of expensive database queries
                    val lastPracticeDate = piece.lastPracticeDate
                    val lastPerformanceDate = piece.lastPerformanceDate
                    
                    // Determine most recent activity and its type
                    val (lastActivityDate, isLastActivityPerformance) = when {
                        lastPracticeDate != null && lastPerformanceDate != null -> {
                            if (lastPracticeDate > lastPerformanceDate) {
                                lastPracticeDate to false
                            } else {
                                lastPerformanceDate to true
                            }
                        }
                        lastPracticeDate != null -> lastPracticeDate to false
                        lastPerformanceDate != null -> lastPerformanceDate to true
                        else -> null to false
                    }
                    
                    // Only include abandoned/inactive pieces (31+ days or never practiced)
                    if (lastActivityDate == null || lastActivityDate < thirtyOneDaysAgo) {
                        val daysSince = if (lastActivityDate != null) {
                            ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                        } else {
                            Int.MAX_VALUE
                        }
                        
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince,
                            suggestionReason = if (lastActivityDate == null) "Never practiced" else {
                                val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                                val dayText = if (daysSince == 1) "day" else "days"
                                "$activityTypeText $daysSince $dayText ago"
                            }
                        )
                    } else null
                }
                // Get the most recently practiced abandoned pieces (lowest daysSince among abandoned)
                if (nonFavoritesWithActivity.isNotEmpty()) {
                    // Sort abandoned pieces: 1) most recent among abandoned (lowest daysSince), 2) latest timestamp, 3) alphabetical
                    val sortedCandidates = nonFavoritesWithActivity.sortedWith(
                        compareBy<SuggestionItem> { it.daysSinceLastActivity } // Most recent among abandoned (lowest days)
                        .thenByDescending { it.lastActivityDate ?: 0L } // Latest timestamp for tie-breaking
                        .thenBy { it.piece.name.lowercase() } // Alphabetical
                    )
                    sortedCandidates.take(neededCount)
                } else emptyList()
            } else emptyList()
            
            nonFavoriteSuggestions + fallbackNonFavorites
        } else nonFavoriteSuggestions
        
        // PRACTICE SUGGESTIONS: Combine favorites first, then non-favorites
        return (finalFavoriteSuggestions + finalNonFavoriteSuggestions).map {
            it.copy(suggestionType = SuggestionType.PRACTICE)
        }
    }
}