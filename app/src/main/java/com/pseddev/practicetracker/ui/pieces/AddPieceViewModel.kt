package com.pseddev.practicetracker.ui.pieces

import androidx.lifecycle.*
import com.pseddev.practicetracker.data.entities.ItemType
import com.pseddev.practicetracker.data.entities.PieceOrTechnique
import com.pseddev.practicetracker.data.repository.PianoRepository
import kotlinx.coroutines.launch

sealed class AddPieceResult {
    object Success : AddPieceResult()
    data class Error(val message: String) : AddPieceResult()
}

class AddPieceViewModel(private val repository: PianoRepository) : ViewModel() {
    
    private val _saveResult = MutableLiveData<AddPieceResult>()
    val saveResult: LiveData<AddPieceResult> = _saveResult
    
    fun savePiece(name: String, type: ItemType, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                val piece = PieceOrTechnique(
                    name = name.trim(),
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

class AddPieceViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPieceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddPieceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}