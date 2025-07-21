package com.pseddev.practicetracker.ui.progress

import androidx.lifecycle.*
import com.pseddev.practicetracker.data.entities.ActivityType
import com.pseddev.practicetracker.data.entities.ItemType
import com.pseddev.practicetracker.data.entities.PieceOrTechnique
import com.pseddev.practicetracker.data.repository.PianoRepository
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
                
                val suggestions = mutableListOf<SuggestionItem>()
                
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
                            suggestions.add(
                                SuggestionItem(
                                    piece = piece,
                                    lastActivityDate = lastActivityDate,
                                    daysSinceLastActivity = daysSince,
                                    suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                                        val activityTypeText = when (lastActivity!!.activityType) {
                                            ActivityType.PRACTICE -> "Last practice"
                                            ActivityType.PERFORMANCE -> "Last performance"
                                        }
                                        "Favorite piece - $activityTypeText $daysSince days ago"
                                    }
                                )
                            )
                        }
                    } else {
                        // Non-favorites that haven't been practiced in 7+ days but have been practiced in last 31 days
                        if (lastActivityDate != null && lastActivityDate < sevenDaysAgo && lastActivityDate >= thirtyOneDaysAgo) {
                            suggestions.add(
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
                
                // Sort favorites first (by last activity, least recent first), then non-favorites (by last activity, least recent first)
                suggestions.sortedWith(compareBy<SuggestionItem> { !it.piece.isFavorite }
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