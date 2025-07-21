package com.pseddev.playstreak.ui.favorites

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: PianoRepository) : ViewModel() {
    
    val allPiecesAndTechniques: LiveData<List<PieceOrTechnique>> = 
        repository.getAllPiecesAndTechniques().asLiveData()
    
    fun toggleFavorite(pieceOrTechnique: PieceOrTechnique) {
        viewModelScope.launch {
            val updated = pieceOrTechnique.copy(isFavorite = !pieceOrTechnique.isFavorite)
            repository.updatePieceOrTechnique(updated)
        }
    }
}

class FavoritesViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}