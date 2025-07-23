package com.pseddev.playstreak.ui.pieces

import android.content.Context
import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.TextNormalizer
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

sealed class AddPieceResult {
    object Success : AddPieceResult()
    data class Error(val message: String) : AddPieceResult()
    data class PieceLimitReached(val currentCount: Int, val limit: Int, val isProUser: Boolean) : AddPieceResult()
}

class AddPieceViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    private val _saveResult = MutableLiveData<AddPieceResult>()
    val saveResult: LiveData<AddPieceResult> = _saveResult
    
    fun savePiece(name: String, type: ItemType, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                // Check piece limit before saving
                val currentPieceCount = repository.getAllPiecesAndTechniques().first().size
                val limit = proUserManager.getPieceLimit()
                
                // Debug logging
                android.util.Log.d("AddPieceViewModel", "Current piece count: $currentPieceCount, Limit: $limit, Can add more: ${proUserManager.canAddMorePieces(currentPieceCount)}")
                
                if (!proUserManager.canAddMorePieces(currentPieceCount)) {
                    val limit = proUserManager.getPieceLimit()
                    _saveResult.value = AddPieceResult.PieceLimitReached(
                        currentCount = currentPieceCount,
                        limit = limit,
                        isProUser = proUserManager.isProUser()
                    )
                    return@launch
                }
                
                val piece = PieceOrTechnique(
                    name = TextNormalizer.normalizePieceName(name),
                    type = type,
                    isFavorite = isFavorite
                )
                
                repository.insertPieceOrTechnique(piece)
                _saveResult.value = AddPieceResult.Success
                
            } catch (e: Exception) {
                _saveResult.value = AddPieceResult.Error("Failed to save piece: ${e.message}")
            }
        }
    }
}

class AddPieceViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPieceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddPieceViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}