package com.example.pianotrackopus.ui.importexport

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pianotrackopus.data.repository.PianoRepository
import com.example.pianotrackopus.utils.CsvHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Reader
import java.io.Writer

class ImportExportViewModel(
    private val repository: PianoRepository
) : ViewModel() {
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _exportResult = MutableLiveData<Event<Result<String>>>()
    val exportResult: LiveData<Event<Result<String>>> = _exportResult
    
    private val _importResult = MutableLiveData<Event<Result<CsvHandler.ImportResult>>>()
    val importResult: LiveData<Event<Result<CsvHandler.ImportResult>>> = _importResult
    
    suspend fun exportToCsv(writer: Writer) {
        Log.d("ExportDebug", "ViewModel.exportToCsv called")
        _isLoading.value = true
        try {
            Log.d("ExportDebug", "Starting suspend function")
            withContext(Dispatchers.IO) {
                Log.d("ExportDebug", "Switched to IO dispatcher, calling repository.exportToCsv")
                repository.exportToCsv(writer)
                Log.d("ExportDebug", "repository.exportToCsv completed")
            }
            Log.d("ExportDebug", "Back on main dispatcher, setting success result")
            _exportResult.value = Event(Result.success("Export successful!"))
        } catch (e: Exception) {
            Log.e("ExportDebug", "Exception in ViewModel: ${e.javaClass.simpleName} - ${e.message}", e)
            // Add more detailed error information
            val errorMessage = "Export failed: ${e.javaClass.simpleName} - ${e.message}"
            _exportResult.value = Event(Result.failure(Exception(errorMessage, e)))
        } finally {
            Log.d("ExportDebug", "ViewModel.exportToCsv finally block")
            _isLoading.value = false
        }
    }
    
    fun importFromCsv(reader: Reader) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.importFromCsv(reader)
                _importResult.value = Event(Result.success(result))
            } catch (e: Exception) {
                _importResult.value = Event(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Event wrapper to handle one-time events
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false
    
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

class ImportExportViewModelFactory(
    private val repository: PianoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportExportViewModel::class.java)) {
            return ImportExportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}