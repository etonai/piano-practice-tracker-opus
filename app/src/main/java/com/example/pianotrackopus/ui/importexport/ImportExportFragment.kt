package com.example.pianotrackopus.ui.importexport

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
import com.example.pianotrackopus.PianoTrackerApplication
import com.example.pianotrackopus.databinding.FragmentImportExportBinding
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
            (requireActivity().application as PianoTrackerApplication).repository
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
    }
    
    private fun setupButtons() {
        binding.exportToCsvButton.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
            val fileName = "piano_tracker_export_$timestamp.csv"
            exportCsvLauncher.launch(fileName)
        }
        
        binding.importFromCsvButton.setOnClickListener {
            binding.warningTextView.visibility = View.VISIBLE
            importCsvLauncher.launch("text/*")
        }
        
        binding.syncWithDriveButton.setOnClickListener {
            Toast.makeText(context, "Google Drive sync not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.exportToCsvButton.isEnabled = !isLoading
            binding.importFromCsvButton.isEnabled = !isLoading
        }
        
        viewModel.exportResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
                val reader = InputStreamReader(inputStream)
                viewModel.importFromCsv(reader)
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
    
    private fun showImportErrors(importResult: com.example.pianotrackopus.utils.CsvHandler.ImportResult) {
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
}