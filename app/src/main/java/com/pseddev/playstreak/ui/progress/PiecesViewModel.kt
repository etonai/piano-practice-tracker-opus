package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val lastActivityDate: Long?,
    val isFavorite: Boolean = false
)

data class PieceDetails(
    val piece: PieceOrTechnique,
    val activities: List<Activity>,
    val lastActivity: Activity?
)

@OptIn(ExperimentalCoroutinesApi::class)
class PiecesViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    
    private val selectedPieceId = MutableStateFlow<Long?>(null)
    private val sortType = MutableStateFlow(SortType.ALPHABETICAL)
    private val sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    
    val piecesWithStats: LiveData<List<PieceWithStats>> = 
        combine(
            repository.getPiecesWithFavoriteStatus(),
            repository.getAllActivities(),
            sortType,
            sortDirection
        ) { piecesWithFavorites, activities, currentSortType, currentSortDirection ->
            val piecesWithStats = piecesWithFavorites
                .map { pieceWithFavorite ->
                    val piece = pieceWithFavorite.piece
                    val pieceActivities = activities.filter { it.pieceOrTechniqueId == piece.id }
                    PieceWithStats(
                        piece = piece,
                        activityCount = pieceActivities.size,
                        lastActivityDate = pieceActivities.maxByOrNull { it.timestamp }?.timestamp,
                        isFavorite = pieceWithFavorite.isFavorite
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
    
    suspend fun toggleFavorite(pieceWithStats: PieceWithStats): Boolean {
        val currentlyFavorite = repository.isFavorite(pieceWithStats.piece.id)
        
        // If trying to add a favorite (not currently favorite), check limits for Free users
        if (!currentlyFavorite) {
            // Get current favorite count from repository
            val currentFavoriteCount = repository.getFavoriteCount()
            
            if (!proUserManager.canAddMoreFavorites(currentFavoriteCount)) {
                return false // Cannot add more favorites - caller should show upgrade prompt
            }
        }
        
        // Proceed with toggle (either removing favorite or adding within limits)
        if (currentlyFavorite) {
            repository.removeFavorite(pieceWithStats.piece.id)
        } else {
            repository.addFavorite(pieceWithStats.piece.id)
        }
        
        return true // Toggle was allowed and performed
    }
    
    fun deletePiece(pieceWithStats: PieceWithStats) {
        viewModelScope.launch {
            repository.deletePieceAndActivities(pieceWithStats.piece.id)
        }
    }
}

class PiecesViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PiecesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PiecesViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}