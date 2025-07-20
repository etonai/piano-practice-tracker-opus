package com.pseddev.practicetracker.ui.importexport

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.practicetracker.data.models.SyncOperationResult
import com.pseddev.practicetracker.data.repository.PianoRepository
import com.pseddev.practicetracker.utils.CsvHandler
import com.pseddev.practicetracker.utils.GoogleDriveHelper
import com.pseddev.practicetracker.utils.SyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Reader
import java.io.Writer

class ImportExportViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val driveHelper = GoogleDriveHelper(context)
    private val syncManager = SyncManager(context, repository, driveHelper)
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _exportResult = MutableLiveData<Event<Result<String>>>()
    val exportResult: LiveData<Event<Result<String>>> = _exportResult
    
    private val _importResult = MutableLiveData<Event<Result<CsvHandler.ImportResult>>>()
    val importResult: LiveData<Event<Result<CsvHandler.ImportResult>>> = _importResult
    
    private val _purgeResult = MutableLiveData<Event<Result<String>>>()
    val purgeResult: LiveData<Event<Result<String>>> = _purgeResult
    
    private val _syncResult = MutableLiveData<Event<SyncOperationResult>>()
    val syncResult: LiveData<Event<SyncOperationResult>> = _syncResult
    
    private val _driveConnectionState = MutableLiveData<Boolean>()
    val driveConnectionState: LiveData<Boolean> = _driveConnectionState
    
    init {
        // Check initial sign-in state
        _driveConnectionState.value = isSignedIn()
    }
    
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
    
    fun purgeAllData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteAllActivities()
                    repository.deleteAllPiecesAndTechniques()
                }
                _purgeResult.value = Event(Result.success("All data has been purged successfully."))
            } catch (e: Exception) {
                _purgeResult.value = Event(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
    
    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    fun getLastSyncTime(): Long {
        return syncManager.getLastSyncTime()
    }
    
    fun getSignInIntent(): Intent {
        return driveHelper.getSignInIntent()
    }
    
    fun handleSignInSuccess(account: GoogleSignInAccount) {
        syncManager.setGoogleEmail(account.email)
        syncManager.setSyncEnabled(true)
        _driveConnectionState.value = true
    }
    
    fun syncToDrive() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    syncManager.performSync()
                }
                _syncResult.value = Event(result)
            } catch (e: Exception) {
                _syncResult.value = Event(
                    SyncOperationResult(
                        success = false,
                        message = "Sync failed: ${e.message}"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun restoreFromDrive() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    syncManager.restoreFromDrive()
                }
                _syncResult.value = Event(result)
            } catch (e: Exception) {
                _syncResult.value = Event(
                    SyncOperationResult(
                        success = false,
                        message = "Restore failed: ${e.message}"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun disconnectDrive() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    driveHelper.signOut()
                }
                syncManager.clearSyncData()
                _driveConnectionState.value = false
            } catch (e: Exception) {
                Log.e("ImportExportViewModel", "Failed to disconnect from Drive", e)
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
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportExportViewModel::class.java)) {
            return ImportExportViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}