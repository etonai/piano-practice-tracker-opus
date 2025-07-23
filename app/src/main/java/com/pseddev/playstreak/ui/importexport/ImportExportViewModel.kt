package com.pseddev.playstreak.ui.importexport

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.crashlytics.CrashlyticsManager
import com.pseddev.playstreak.data.models.SyncOperationResult
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.CsvHandler
import com.pseddev.playstreak.utils.GoogleDriveHelper
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.SyncManager
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
    private val proUserManager = ProUserManager.getInstance(context)
    private val crashlyticsManager = CrashlyticsManager(context)
    private val sharedPrefs = context.getSharedPreferences("play_streak_export_prefs", Context.MODE_PRIVATE)
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _exportResult = MutableLiveData<Event<Result<String>>>()
    val exportResult: LiveData<Event<Result<String>>> = _exportResult
    
    private val _importResult = MutableLiveData<Event<Result<CsvHandler.ImportResult>>>()
    val importResult: LiveData<Event<Result<CsvHandler.ImportResult>>> = _importResult
    
    private val _validationResult = MutableLiveData<Event<CsvHandler.CsvValidationResult>>()
    val validationResult: LiveData<Event<CsvHandler.CsvValidationResult>> = _validationResult
    
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
            // Update last export time
            sharedPrefs.edit().putLong("last_export_time", System.currentTimeMillis()).apply()
            _exportResult.value = Event(Result.success("Export successful!"))
        } catch (e: Exception) {
            Log.e("ExportDebug", "Exception in ViewModel: ${e.javaClass.simpleName} - ${e.message}", e)
            // Record crash context for CSV export error
            crashlyticsManager.recordCsvError("export", 0, e) // Activity count not available at this level
            // Add more detailed error information
            val errorMessage = "Export failed: ${e.javaClass.simpleName} - ${e.message}"
            _exportResult.value = Event(Result.failure(Exception(errorMessage, e)))
        } finally {
            Log.d("ExportDebug", "ViewModel.exportToCsv finally block")
            _isLoading.value = false
        }
    }
    
    fun importFromCsv(csvContent: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // First validate the CSV against user limits
                val pieceLimit = proUserManager.getPieceLimit()
                val activityLimit = proUserManager.getActivityLimit()
                
                val validationResult = withContext(Dispatchers.IO) {
                    CsvHandler.validateCsv(csvContent.reader(), pieceLimit, activityLimit)
                }
                
                if (!validationResult.isValid) {
                    // Validation failed - show error dialog
                    _validationResult.value = Event(validationResult)
                } else {
                    // Validation passed - proceed with actual import using fresh reader
                    val importResult = withContext(Dispatchers.IO) {
                        repository.importFromCsv(csvContent.reader())
                    }
                    
                    _importResult.value = Event(Result.success(importResult))
                }
                
            } catch (e: Exception) {
                // Record crash context for CSV import error
                crashlyticsManager.recordCsvError("import", 0, e) // Activity count not available at this level
                _validationResult.value = Event(
                    CsvHandler.CsvValidationResult(
                        isValid = false,
                        activityCount = 0,
                        uniquePieceCount = 0,
                        errors = listOf("Failed to validate CSV: ${e.message}")
                    )
                )
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
                // Record crash context for database purge error
                crashlyticsManager.recordDatabaseError("purge_all_data", e)
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
    
    fun getLastExportTime(): Long {
        return sharedPrefs.getLong("last_export_time", 0L)
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