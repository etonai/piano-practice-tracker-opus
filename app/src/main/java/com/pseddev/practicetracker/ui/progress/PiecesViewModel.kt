package com.pseddev.practicetracker.ui.progress

import androidx.lifecycle.*
import com.pseddev.practicetracker.data.entities.Activity
import com.pseddev.practicetracker.data.entities.ItemType
import com.pseddev.practicetracker.data.entities.PieceOrTechnique
import com.pseddev.practicetracker.data.repository.PianoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

enum class SortType {
    ALPHABETICAL,
    LAST_DATE,
    ACTIVITY_COUNT
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class PieceWithStats(
    val piece: PieceOrTechnique,
    val activityCount: Int,
    val lastActivityDate: Long?
)

data class PieceDetails(
    val piece: PieceOrTechnique,
    val activities: List<Activity>,
    val lastActivity: Activity?
)

class PiecesViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val selectedPieceId = MutableStateFlow<Long?>(null)
    private val sortType = MutableStateFlow(SortType.ALPHABETICAL)
    private val sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    
    val piecesWithStats: LiveData<List<PieceWithStats>> = 
        combine(
            repository.getAllPiecesAndTechniques(),
            repository.getAllActivities(),
            sortType,
            sortDirection
        ) { pieces, activities, currentSortType, currentSortDirection ->
            val piecesWithStats = pieces
                .filter { it.type == ItemType.PIECE }
                .map { piece ->
                    val pieceActivities = activities.filter { it.pieceOrTechniqueId == piece.id }
                    PieceWithStats(
                        piece = piece,
                        activityCount = pieceActivities.size,
                        lastActivityDate = pieceActivities.maxByOrNull { it.timestamp }?.timestamp
                    )
                }
            
            // Apply sorting
            val sorted = when (currentSortType) {
                SortType.ALPHABETICAL -> piecesWithStats.sortedBy { it.piece.name.lowercase() }
                SortType.LAST_DATE -> piecesWithStats.sortedBy { it.lastActivityDate ?: 0L }
                SortType.ACTIVITY_COUNT -> piecesWithStats.sortedBy { it.activityCount }
            }
            
            // Apply direction
            if (currentSortDirection == SortDirection.DESCENDING) {
                sorted.reversed()
            } else {
                sorted
            }
        }
        .asLiveData()
    
    val selectedPieceDetails: LiveData<PieceDetails?> = 
        selectedPieceId.flatMapLatest { pieceId ->
            if (pieceId == null) {
                kotlinx.coroutines.flow.flowOf(null)
            } else {
                repository.getActivitiesForPiece(pieceId)
                    .map { activities ->
                        val piece = repository.getPieceOrTechniqueById(pieceId)
                        piece?.let {
                            PieceDetails(
                                piece = it,
                                activities = activities,
                                lastActivity = activities.maxByOrNull { it.timestamp }
                            )
                        }
                    }
            }
        }
        .asLiveData()
    
    fun selectPiece(pieceId: Long) {
        selectedPieceId.value = pieceId
    }
    
    fun clearSelection() {
        selectedPieceId.value = null
    }
    
    fun setSortType(type: SortType) {
        sortType.value = type
        // Set appropriate default direction based on sort type
        sortDirection.value = when (type) {
            SortType.ALPHABETICAL -> SortDirection.ASCENDING  // A-Z makes sense
            SortType.LAST_DATE -> SortDirection.DESCENDING    // Newest first makes sense
            SortType.ACTIVITY_COUNT -> SortDirection.DESCENDING // Highest count first makes sense
        }
    }
    
    fun toggleSortDirection() {
        sortDirection.value = if (sortDirection.value == SortDirection.ASCENDING) {
            SortDirection.DESCENDING
        } else {
            SortDirection.ASCENDING
        }
    }
    
    fun getCurrentSortType(): SortType = sortType.value
    fun getCurrentSortDirection(): SortDirection = sortDirection.value
    
    fun toggleFavorite(pieceWithStats: PieceWithStats) {
        viewModelScope.launch {
            val updatedPiece = pieceWithStats.piece.copy(isFavorite = !pieceWithStats.piece.isFavorite)
            repository.updatePieceOrTechnique(updatedPiece)
        }
    }
}

class PiecesViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PiecesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PiecesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}