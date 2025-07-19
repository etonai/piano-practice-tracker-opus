package com.example.pianotrackopus.ui.progress

import androidx.lifecycle.*
import com.example.pianotrackopus.data.entities.Activity
import com.example.pianotrackopus.data.entities.ItemType
import com.example.pianotrackopus.data.entities.PieceOrTechnique
import com.example.pianotrackopus.data.repository.PianoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

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
    
    val piecesWithStats: LiveData<List<PieceWithStats>> = 
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                pieces
                    .filter { it.type == ItemType.PIECE }
                    .map { piece ->
                        val pieceActivities = activities.filter { it.pieceOrTechniqueId == piece.id }
                        PieceWithStats(
                            piece = piece,
                            activityCount = pieceActivities.size,
                            lastActivityDate = pieceActivities.maxByOrNull { it.timestamp }?.timestamp
                        )
                    }
                    .sortedByDescending { it.lastActivityDate ?: 0 }
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