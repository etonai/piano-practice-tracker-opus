package com.pseddev.playstreak.ui.progress

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import android.widget.ArrayAdapter
import android.widget.Toast
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.databinding.DialogQuickAddActivityBinding
import kotlinx.coroutines.launch
import java.util.*

class QuickAddActivityDialogFragment : DialogFragment() {
    
    private var _binding: DialogQuickAddActivityBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: QuickAddActivityViewModel by viewModels {
        QuickAddActivityViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository
        )
    }
    
    private var pieceId: Long = -1
    private var pieceName: String = ""
    
    companion object {
        private const val ARG_PIECE_ID = "piece_id"
        private const val ARG_PIECE_NAME = "piece_name"
        
        fun newInstance(pieceId: Long, pieceName: String): QuickAddActivityDialogFragment {
            val fragment = QuickAddActivityDialogFragment()
            val args = Bundle().apply {
                putLong(ARG_PIECE_ID, pieceId)
                putString(ARG_PIECE_NAME, pieceName)
            }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pieceId = it.getLong(ARG_PIECE_ID)
            pieceName = it.getString(ARG_PIECE_NAME, "")
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogQuickAddActivityBinding.inflate(layoutInflater)
        
        setupViews()
        setupObservers()
        
        return AlertDialog.Builder(requireContext())
            .setTitle("Add Activity for $pieceName")
            .setView(binding.root)
            .setPositiveButton("Add Activity") { _, _ ->
                addActivity()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
    
    private fun setupViews() {
        // Setup activity type spinner
        val activityTypes = listOf("Practice", "Performance")
        val activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityTypes)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityTypeSpinner.adapter = activityAdapter
        
        // Setup performance type spinner
        val performanceTypes = listOf("Online Performance", "Live Performance")
        val performanceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, performanceTypes)
        performanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.performanceTypeSpinner.adapter = performanceAdapter
        
        // Set piece name (read-only)
        binding.pieceNameText.text = pieceName
        
        // Setup activity type change listener to update level options
        binding.activityTypeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                setupLevelOptions(position == 1) // true if Performance selected
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Initialize with Practice levels (default)
        setupLevelOptions(false)
    }
    
    private fun setupLevelOptions(isPerformance: Boolean) {
        val levels = if (isPerformance) {
            // Performance levels (only 3 levels)
            listOf(
                "Level 1 - Failed",
                "Level 2 - Unsatisfactory", 
                "Level 3 - Satisfactory"
            )
        } else {
            // Practice levels (4 levels)
            listOf(
                "Level 1 - Essentials",
                "Level 2 - Incomplete",
                "Level 3 - Complete with Review",
                "Level 4 - Perfect Complete"
            )
        }
        
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, levels)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.levelSpinner.adapter = levelAdapter
        
        // Show/hide performance type selection
        if (isPerformance) {
            binding.performanceTypeLabel.visibility = android.view.View.VISIBLE
            binding.performanceTypeSpinner.visibility = android.view.View.VISIBLE
        } else {
            binding.performanceTypeLabel.visibility = android.view.View.GONE
            binding.performanceTypeSpinner.visibility = android.view.View.GONE
        }
    }
    
    private fun setupObservers() {
        viewModel.addResult.observe(this) { result ->
            result.fold(
                onSuccess = { 
                    Toast.makeText(context, "Activity added successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Failed to add activity: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
    
    private fun addActivity() {
        val activityType = when (binding.activityTypeSpinner.selectedItemPosition) {
            0 -> ActivityType.PRACTICE
            1 -> ActivityType.PERFORMANCE
            else -> ActivityType.PRACTICE
        }
        
        val level = binding.levelSpinner.selectedItemPosition + 1
        
        val performanceType = if (activityType == ActivityType.PERFORMANCE) {
            // Map spinner selection to proper values that match regular flow
            when (binding.performanceTypeSpinner.selectedItemPosition) {
                0 -> "online"  // "Online Performance" -> "online"
                1 -> "live"    // "Live Performance" -> "live"
                else -> "online"
            }
        } else {
            "practice" // Default for practice activities
        }
        
        val activity = Activity(
            id = 0, // Will be auto-generated
            pieceOrTechniqueId = pieceId,
            activityType = activityType,
            timestamp = System.currentTimeMillis(),
            level = level,
            minutes = if (activityType == ActivityType.PRACTICE) -1 else 0, // No default duration for practice
            notes = "",
            performanceType = performanceType
        )
        
        lifecycleScope.launch {
            viewModel.addActivity(activity)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}