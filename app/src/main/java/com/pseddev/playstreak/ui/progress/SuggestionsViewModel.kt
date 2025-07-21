package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.*

data class SuggestionItem(
    val piece: PieceOrTechnique,
    val lastActivityDate: Long?,
    val daysSinceLastActivity: Int,
    val suggestionReason: String
)

class SuggestionsViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val now = System.currentTimeMillis()
    private val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)
    private val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
    private val thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)
    
    val suggestions: LiveData<List<SuggestionItem>> = 
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
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
                                    suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                                        val activityTypeText = when (lastActivity!!.activityType) {
                                            ActivityType.PRACTICE -> "Last practice"
                                            ActivityType.PERFORMANCE -> "Last performance"
                                        }
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
                                        val activityTypeText = when (lastActivity!!.activityType) {
                                            ActivityType.PRACTICE -> "Last practice"
                                            ActivityType.PERFORMANCE -> "Last performance"
                                        }
                                        val dayText = if (daysSince == 1) "day" else "days"
                                        "$activityTypeText $daysSince $dayText ago"
                                    }()
                                )
                            )
                        }
                    }
                }
                
                // Always aim for up to 4 favorites total
                val finalFavoriteSuggestions = if (favoriteSuggestions.size < 4) {
                    val neededCount = 4 - favoriteSuggestions.size
                    val usedPieceIds = favoriteSuggestions.map { it.piece.id }.toSet()
                    
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
                                suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                                    val activityTypeText = when (lastActivity!!.activityType) {
                                        ActivityType.PRACTICE -> "Last practice"
                                        ActivityType.PERFORMANCE -> "Last performance"
                                    }
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
                
                // Always aim for up to 4 non-favorites total
                val finalNonFavoriteSuggestions = if (nonFavoriteSuggestions.size < 4) {
                    val neededCount = 4 - nonFavoriteSuggestions.size
                    val usedPieceIds = nonFavoriteSuggestions.map { it.piece.id }.toSet()
                    
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
                    
                    nonFavoriteSuggestions + fallbackNonFavorites
                } else nonFavoriteSuggestions
                
                // Combine and sort favorites first (by last activity, least recent first), then non-favorites (by last activity, least recent first)
                val allSuggestions = finalFavoriteSuggestions + finalNonFavoriteSuggestions
                allSuggestions.sortedWith(compareBy<SuggestionItem> { !it.piece.isFavorite }
                    .thenByDescending { it.daysSinceLastActivity })
            }
            .asLiveData()
}

class SuggestionsViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SuggestionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SuggestionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}