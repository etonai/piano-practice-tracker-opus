package com.pseddev.playstreak.ui.importexport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.databinding.FragmentImportExportBinding
import com.pseddev.playstreak.utils.GoogleDriveHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class ImportExportFragment : Fragment() {
    
    private var _binding: FragmentImportExportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ImportExportViewModel by viewModels {
        ImportExportViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    
    private val exportCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { exportToCsv(it) }
    }
    
    private val importCsvLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { showImportConfirmation(it) }
    }
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignInResult(result.data)
        } else {
            Log.e("ImportExportFragment", "Sign-in failed with result code: ${result.resultCode}")
            Toast.makeText(context, "Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportExportBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
        observeViewModel()
        updateDriveUI()
    }
    
    private fun setupButtons() {
        binding.exportToCsvButton.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
            val fileName = "PlayStreak_activities_export_$timestamp.csv"
            exportCsvLauncher.launch(fileName)
        }
        
        binding.importFromCsvButton.setOnClickListener {
            binding.warningTextView.visibility = View.VISIBLE
            importCsvLauncher.launch("text/*")
        }
        
        binding.syncWithDriveButton.setOnClickListener {
            if (viewModel.isSignedIn()) {
                showSyncOptions()
            } else {
                startGoogleSignIn()
            }
        }
        
        binding.purgeDataButton.setOnClickListener {
            showPurgeConfirmation()
        }
        
        // Hide purge button in production builds
        binding.purgeDataButton.visibility = if (com.pseddev.playstreak.BuildConfig.DEBUG) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.exportToCsvButton.isEnabled = !isLoading
            binding.importFromCsvButton.isEnabled = !isLoading
            binding.purgeDataButton.isEnabled = !isLoading
            // Keep Google Drive button disabled since functionality is disabled
            binding.syncWithDriveButton.isEnabled = false
        }
        
        viewModel.exportResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        updateDriveUI() // Refresh UI to show updated export time
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            context,
                            "Export failed: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
        
        viewModel.importResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = { importResult ->
                        if (importResult.errors.isEmpty()) {
                            val message = "Import successful! Imported ${importResult.activities.size} activities."
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            // Navigate back to main screen
                            findNavController().navigateUp()
                        } else {
                            showImportErrors(importResult)
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            context,
                            "Import failed: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
                binding.warningTextView.visibility = View.GONE
            }
        }
        
        viewModel.validationResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { validationResult ->
                if (validationResult.isValid) {
                    // Show success message with counts
                    val message = "CSV file is valid!\n" +
                            "Contains: ${validationResult.activityCount} activities, ${validationResult.uniquePieceCount} unique pieces"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                } else {
                    // Show rejection dialog
                    showValidationErrorDialog(validationResult)
                }
                binding.warningTextView.visibility = View.GONE
            }
        }
        
        viewModel.purgeResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        // Navigate back to main screen
                        findNavController().navigateUp()
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            context,
                            "Purge failed: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
        
        viewModel.syncResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                if (result.success) {
                    Toast.makeText(
                        context,
                        "Sync successful! ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    updateDriveUI()
                } else {
                    Toast.makeText(
                        context,
                        "Sync failed: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        viewModel.driveConnectionState.observe(viewLifecycleOwner) { connected ->
            updateDriveUI()
        }
    }
    
    private fun exportToCsv(uri: Uri) {
        Log.d("ExportDebug", "Starting exportToCsv with URI: $uri")
        
        // Launch coroutine that will wait for the export to complete
        lifecycleScope.launch {
            try {
                Log.d("ExportDebug", "Opening output stream...")
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    Log.d("ExportDebug", "Output stream opened successfully")
                    OutputStreamWriter(outputStream).use { writer ->
                        Log.d("ExportDebug", "OutputStreamWriter created, calling viewModel.exportToCsv")
                        viewModel.exportToCsv(writer) // This will now wait for completion
                        Log.d("ExportDebug", "viewModel.exportToCsv completed")
                    }
                    Log.d("ExportDebug", "OutputStreamWriter closed")
                }
                Log.d("ExportDebug", "Output stream closed")
            } catch (e: Exception) {
                Log.e("ExportDebug", "Exception in exportToCsv: ${e.javaClass.simpleName} - ${e.message}", e)
                Toast.makeText(
                    context,
                    "Failed to create file: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun showImportConfirmation(uri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("Import CSV Data")
            .setMessage("This will replace all existing data in the app. Are you sure you want to continue?")
            .setPositiveButton("Import") { _, _ ->
                importFromCsv(uri)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                binding.warningTextView.visibility = View.GONE
            }
            .show()
    }
    
    private fun importFromCsv(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val csvContent = InputStreamReader(inputStream).readText()
                viewModel.importFromCsv(csvContent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to read file: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            binding.warningTextView.visibility = View.GONE
        }
    }
    
    private fun showImportErrors(importResult: com.pseddev.playstreak.utils.CsvHandler.ImportResult) {
        val message = buildString {
            appendLine("Import completed with errors:")
            appendLine("Imported ${importResult.activities.size} activities successfully.")
            appendLine()
            appendLine("Errors:")
            importResult.errors.take(10).forEach { error ->
                appendLine("• $error")
            }
            if (importResult.errors.size > 10) {
                appendLine("... and ${importResult.errors.size - 10} more errors")
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Import Errors")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun updateDriveUI() {
        if (viewModel.isSignedIn()) {
            val account = viewModel.getSignedInAccount()
            binding.driveStatusTextView.text = "Connected to Google Drive"
            binding.driveAccountTextView.text = account?.email ?: ""
            binding.driveAccountTextView.visibility = View.VISIBLE
            binding.syncWithDriveButton.text = "Sync Now"
            
            // Update last sync time
            val lastSyncTime = viewModel.getLastSyncTime()
            if (lastSyncTime > 0) {
                val date = Date(lastSyncTime)
                val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
                binding.lastSyncTextView.text = "Last sync: ${formatter.format(date)}"
            } else {
                binding.lastSyncTextView.text = "Last sync: Never"
            }
        } else {
            binding.driveStatusTextView.text = "Google Drive functionality coming soon"
            binding.driveAccountTextView.visibility = View.GONE
            binding.syncWithDriveButton.text = "Connect to Google Drive"
            binding.lastSyncTextView.text = "Last sync: Never"
        }
        
        // Update last export time (independent of Google Drive status)
        val lastExportTime = viewModel.getLastExportTime()
        if (lastExportTime > 0) {
            val date = Date(lastExportTime)
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
            binding.lastExportTextView.text = "Last export: ${formatter.format(date)}"
        } else {
            binding.lastExportTextView.text = "Last export: Never"
        }
    }
    
    private fun startGoogleSignIn() {
        val signInIntent = viewModel.getSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }
    
    private fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            Log.d("ImportExportFragment", "Sign-in successful: ${account.email}")
            viewModel.handleSignInSuccess(account)
            updateDriveUI()
            
            // Show sync dialog after successful sign-in
            showSyncOptions()
        } catch (e: ApiException) {
            Log.e("ImportExportFragment", "Sign-in failed with code: ${e.statusCode}, message: ${e.message}", e)
            
            val errorMessage = when (e.statusCode) {
                12500 -> "Sign-in cancelled by user"
                12501 -> "Sign-in currently in progress"
                12502 -> "Play Services not available or outdated"
                10 -> "Developer error - check Google Cloud Console configuration"
                else -> "Sign-in failed (code: ${e.statusCode}): ${e.message}"
            }
            
            Toast.makeText(
                context,
                errorMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showSyncOptions() {
        val options = arrayOf("Sync to Drive", "Restore from Drive", "Disconnect")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Google Drive Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.syncToDrive()
                    1 -> showRestoreConfirmation()
                    2 -> showDisconnectConfirmation()
                }
            }
            .show()
    }
    
    private fun showRestoreConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restore from Drive")
            .setMessage("This will replace all local data with data from Google Drive. Are you sure?")
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restoreFromDrive()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDisconnectConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Disconnect Google Drive")
            .setMessage("Are you sure you want to disconnect from Google Drive?")
            .setPositiveButton("Disconnect") { _, _ ->
                viewModel.disconnectDrive()
                updateDriveUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showValidationErrorDialog(validationResult: com.pseddev.playstreak.utils.CsvHandler.CsvValidationResult) {
        val message = buildString {
            appendLine("CSV File Rejected")
            appendLine()
            appendLine("File contents:")
            appendLine("• ${validationResult.activityCount} activities")
            appendLine("• ${validationResult.uniquePieceCount} unique pieces")
            appendLine()
            appendLine("Limit violations:")
            validationResult.errors.forEach { error ->
                appendLine("• $error")
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Import Not Allowed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showPurgeConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Purge All Data")
            .setMessage("This will permanently delete ALL practice activities and pieces from the app. This action cannot be undone.\n\nAre you absolutely sure you want to continue?")
            .setPositiveButton("YES, PURGE ALL") { _, _ ->
                showSecondPurgeConfirmation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSecondPurgeConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Final Confirmation")
            .setMessage("This is your final warning. All data will be permanently lost.\n\nDo you want to proceed with purging all data?")
            .setPositiveButton("PURGE NOW") { _, _ ->
                viewModel.purgeAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}