package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.*

class DashboardViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    
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
                val practiceCount = activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PRACTICE }
                val performanceCount = activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PERFORMANCE }
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
    
    val suggestions: LiveData<List<SuggestionItem>> = 
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                val now = System.currentTimeMillis()
                val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)
                val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
                val thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)
                
                val pieceActivities = activities.groupBy { it.pieceOrTechniqueId }
                
                val favoriteSuggestions = mutableListOf<SuggestionItem>()
                val nonFavoriteSuggestions = mutableListOf<SuggestionItem>()
                
                pieces.filter { it.type == ItemType.PIECE }.forEach { piece ->
                    val pieceActivitiesForPiece = pieceActivities[piece.id] ?: emptyList()
                    val lastActivity = pieceActivitiesForPiece.maxByOrNull { it.timestamp }
                    val lastActivityDate = lastActivity?.timestamp
                    
                    val daysSince = if (lastActivityDate != null) {
                        ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                    } else {
                        Int.MAX_VALUE
                    }
                    
                    if (piece.isFavorite) {
                        // Favorites that haven't been practiced in 2+ days
                        if (lastActivityDate == null || lastActivityDate < twoDaysAgo) {
                            favoriteSuggestions.add(
                                SuggestionItem(
                                    piece = piece,
                                    lastActivityDate = lastActivityDate,
                                    daysSinceLastActivity = daysSince,
                                    suggestionReason = if (lastActivityDate == null) "Never practiced" else {
                                    val activityTypeText = when (lastActivity!!.activityType) {
                                        ActivityType.PRACTICE -> "Last practice"
                                        ActivityType.PERFORMANCE -> "Last performance"
                                    }
                                    val dayText = if (daysSince == 1) "day" else "days"
                                    "$activityTypeText $daysSince $dayText ago"
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
                                        val activityTypeText = when (lastActivity!!.activityType) {
                                            ActivityType.PRACTICE -> "Last practice"
                                            ActivityType.PERFORMANCE -> "Last performance"
                                        }
                                        "$activityTypeText $daysSince days ago"
                                    }()
                                )
                            )
                        }
                    }
                }
                
                // Get regular oldest favorites (highest daysSinceLastActivity)
                val regularFavorites = if (favoriteSuggestions.isNotEmpty()) {
                    val maxDays = favoriteSuggestions.maxOf { it.daysSinceLastActivity }
                    favoriteSuggestions.filter { it.daysSinceLastActivity == maxDays }
                } else emptyList()
                
                // Determine favorite limit based on Pro status
                val favoriteLimit = if (proUserManager.isProUser()) 4 else 1
                
                // Always aim for up to limit favorites total
                val finalFavorites = if (regularFavorites.size < favoriteLimit) {
                    val neededCount = favoriteLimit - regularFavorites.size
                    val usedPieceIds = regularFavorites.map { it.piece.id }.toSet()
                    
                    // Get fallback favorites (excluding already used pieces)
                    val allFavorites = pieces.filter { it.type == ItemType.PIECE && it.isFavorite && it.id !in usedPieceIds }
                    val fallbackFavorites = if (allFavorites.isNotEmpty()) {
                        val favoritesWithActivity = allFavorites.mapNotNull { piece ->
                            val pieceActivitiesForPiece = pieceActivities[piece.id] ?: emptyList()
                            val lastActivity = pieceActivitiesForPiece.maxByOrNull { it.timestamp }
                            val lastActivityDate = lastActivity?.timestamp
                            
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
                                    val activityTypeText = when (lastActivity!!.activityType) {
                                        ActivityType.PRACTICE -> "Last practice"
                                        ActivityType.PERFORMANCE -> "Last performance"
                                    }
                                    val dayText = if (daysSince == 1) "day" else "days"
                                    "$activityTypeText $daysSince $dayText ago"
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
                    
                    regularFavorites + fallbackFavorites
                } else regularFavorites
                
                // Get regular oldest non-favorites (highest daysSinceLastActivity)  
                val regularNonFavorites = if (nonFavoriteSuggestions.isNotEmpty()) {
                    val maxDays = nonFavoriteSuggestions.maxOf { it.daysSinceLastActivity }
                    nonFavoriteSuggestions.filter { it.daysSinceLastActivity == maxDays }
                } else emptyList()
                
                // Determine non-favorite limit based on Pro status  
                val nonFavoriteLimit = if (proUserManager.isProUser()) 4 else 2
                
                // Always aim for up to limit non-favorites total
                val finalNonFavorites = if (regularNonFavorites.size < nonFavoriteLimit) {
                    val neededCount = nonFavoriteLimit - regularNonFavorites.size
                    val usedPieceIds = regularNonFavorites.map { it.piece.id }.toSet()
                    
                    // Get fallback non-favorites (excluding already used pieces)
                    val allNonFavorites = pieces.filter { it.type == ItemType.PIECE && !it.isFavorite && it.id !in usedPieceIds }
                    val fallbackNonFavorites = if (allNonFavorites.isNotEmpty()) {
                        val nonFavoritesWithActivity = allNonFavorites.mapNotNull { piece ->
                            val pieceActivitiesForPiece = pieceActivities[piece.id] ?: emptyList()
                            val lastActivity = pieceActivitiesForPiece.maxByOrNull { it.timestamp }
                            val lastActivityDate = lastActivity?.timestamp
                            
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
                                        val activityTypeText = when (lastActivity!!.activityType) {
                                            ActivityType.PRACTICE -> "Last practice"
                                            ActivityType.PERFORMANCE -> "Last performance"
                                        }
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
                    
                    regularNonFavorites + fallbackNonFavorites
                } else regularNonFavorites
                
                // Combine and sort favorites first, then non-favorites
                finalFavorites + finalNonFavorites
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