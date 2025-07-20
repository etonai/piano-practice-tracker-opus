package com.pseddev.practicetracker.ui.progress

import androidx.lifecycle.*
import com.pseddev.practicetracker.data.entities.ItemType
import com.pseddev.practicetracker.data.entities.PieceOrTechnique
import com.pseddev.practicetracker.data.repository.PianoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.*

data class AbandonedItem(
    val piece: PieceOrTechnique,
    val lastActivityDate: Long?,
    val daysSinceLastActivity: Int
)

class AbandonedViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val now = System.currentTimeMillis()
    private val thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)
    
    val abandonedPieces: LiveData<List<AbandonedItem>> = 
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                val pieceActivities = activities.groupBy { it.pieceOrTechniqueId }
                
                val abandoned = mutableListOf<AbandonedItem>()
                
                pieces.filter { 
                    it.type == ItemType.PIECE && !it.isFavorite 
                }.forEach { piece ->
                    val pieceActivitiesForPiece = pieceActivities[piece.id] ?: emptyList()
                    val lastActivity = pieceActivitiesForPiece.maxByOrNull { it.timestamp }
                    val lastActivityDate = lastActivity?.timestamp
                    
                    val daysSince = if (lastActivityDate != null) {
                        ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                    } else {
                        Int.MAX_VALUE
                    }
                    
                    // Include pieces that haven't been practiced in 31+ days (or never)
                    if (lastActivityDate == null || lastActivityDate < thirtyOneDaysAgo) {
                        abandoned.add(
                            AbandonedItem(
                                piece = piece,
                                lastActivityDate = lastActivityDate,
                                daysSinceLastActivity = daysSince
                            )
                        )
                    }
                }
                
                // Sort: pieces with activity first (most recent first), then never-practiced pieces (alphabetical)
                abandoned.sortedWith(
                    compareBy<AbandonedItem> { it.lastActivityDate == null }
                        .thenByDescending { it.lastActivityDate ?: 0 }
                        .thenBy { it.piece.name.lowercase() }
                )
            }
            .asLiveData()
}

class AbandonedViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AbandonedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AbandonedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}