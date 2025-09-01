package com.pseddev.playstreak.ui.addactivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.databinding.FragmentSummaryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryFragment : Fragment() {
    
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    
    private val args: SummaryFragmentArgs by navArgs()
    
    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private var currentTimestamp: Long = System.currentTimeMillis()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize timestamp - use edit activity timestamp if in edit mode, otherwise current time
        val editActivity = viewModel.editActivity.value
        val isEditMode = com.pseddev.playstreak.ui.progress.EditActivityStorage.isEditMode()
        currentTimestamp = if (editActivity != null && isEditMode) {
            editActivity.timestamp
        } else {
            System.currentTimeMillis()
        }
        
        // Handle back navigation in edit mode
        if (editActivity != null) {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // In edit mode, go back to Timeline (progressFragment)
                    findNavController().popBackStack(com.pseddev.playstreak.R.id.progressFragment, false)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
        
        setupSummary()
        setupDateTimeEditing()
        
        binding.buttonSave.setOnClickListener {
            val editActivity = viewModel.editActivity.value
            if (editActivity != null) {
                // Edit mode - update existing activity
                viewModel.updateActivity(
                    activityId = editActivity.id,
                    pieceId = args.pieceId,
                    activityType = args.activityType,
                    level = args.level,
                    performanceType = args.performanceType,
                    minutes = args.minutes,
                    notes = args.notes,
                    timestamp = currentTimestamp  // Use potentially modified timestamp
                )
            } else {
                // Add mode - save new activity
                viewModel.saveActivity(
                    pieceId = args.pieceId,
                    activityType = args.activityType,
                    level = args.level,
                    performanceType = args.performanceType,
                    minutes = args.minutes,
                    notes = args.notes,
                    timestamp = currentTimestamp
                )
            }
        }
        
        binding.buttonCancel.setOnClickListener {
            val editActivity = viewModel.editActivity.value
            if (editActivity != null) {
                // In edit mode, go back to Timeline (progressFragment)
                findNavController().popBackStack(com.pseddev.playstreak.R.id.progressFragment, false)
            } else {
                // In add mode, navigate back to the screen that started the add activity flow
                findNavController().popBackStack(com.pseddev.playstreak.R.id.addActivityFragment, true)
            }
        }
        
        viewModel.navigateToMain.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                val editActivity = viewModel.editActivity.value
                if (editActivity != null) {
                    // In edit mode, go back to Timeline (progressFragment)
                    findNavController().popBackStack(com.pseddev.playstreak.R.id.progressFragment, false)
                } else {
                    // In add mode, navigate back to the screen that started the add activity flow
                    findNavController().popBackStack(com.pseddev.playstreak.R.id.addActivityFragment, true)
                }
                viewModel.doneNavigating()
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
        
        // Observe celebration events
        viewModel.showCelebration.observe(viewLifecycleOwner) { achievementType ->
            if (achievementType != null) {
                val achievement = com.pseddev.playstreak.utils.AchievementDefinitions.getAllAchievementDefinitions()
                    .find { it.type == achievementType }
                if (achievement != null) {
                    viewModel.getCelebrationManager().showCelebration(binding.root, achievement)
                }
                viewModel.onCelebrationHandled()
            }
        }
    }
    
    private fun setupSummary() {
        binding.textPiece.text = "Piece: ${args.pieceName}"
        binding.textType.text = "Type: ${args.activityType.name.lowercase().replaceFirstChar { it.uppercase() }}"
        
        val levelText = when (args.activityType) {
            ActivityType.PRACTICE -> {
                when (args.level) {
                    1 -> "Level: 1 - Essentials"
                    2 -> "Level: 2 - Incomplete"
                    3 -> "Level: 3 - Complete with Review"
                    4 -> "Level: 4 - Perfect Complete"
                    else -> "Level: ${args.level}"
                }
            }
            ActivityType.PERFORMANCE -> {
                val performanceTypeText = when (args.performanceType) {
                    "online" -> "Online"
                    "live" -> "Live"
                    else -> ""
                }
                when (args.level) {
                    1 -> "Level: 1 - Failed ($performanceTypeText)"
                    2 -> "Level: 2 - Unsatisfactory ($performanceTypeText)"
                    3 -> "Level: 3 - Satisfactory ($performanceTypeText)"
                    else -> "Level: ${args.level} ($performanceTypeText)"
                }
            }
        }
        binding.textLevel.text = levelText
        
        binding.textTime.text = if (args.minutes > 0) {
            "Time: ${args.minutes} minutes"
        } else {
            "Time: Not recorded"
        }
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
        val editActivity = viewModel.editActivity.value
        if (editActivity != null) {
            // Edit mode - show edit buttons and current timestamp
            binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
            binding.buttonSave.text = "Update"
            binding.buttonEditDate.visibility = View.VISIBLE
            binding.buttonEditTime.visibility = View.VISIBLE
        } else {
            // Add mode - show current date, no edit buttons
            binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
            binding.buttonSave.text = "Save"
            binding.buttonEditDate.visibility = View.GONE
            binding.buttonEditTime.visibility = View.GONE
        }
        
        if (args.notes.isNotEmpty()) {
            binding.textNotes.text = "Notes: ${args.notes}"
            binding.textNotes.visibility = View.VISIBLE
        } else {
            binding.textNotes.visibility = View.GONE
        }
    }
    
    private fun setupDateTimeEditing() {
        binding.buttonEditDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.buttonEditTime.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimestamp
        
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Update the date part of currentTimestamp
                val newCalendar = Calendar.getInstance()
                newCalendar.timeInMillis = currentTimestamp
                newCalendar.set(Calendar.YEAR, year)
                newCalendar.set(Calendar.MONTH, month)
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                currentTimestamp = newCalendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePicker.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimestamp
        
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // Update the time part of currentTimestamp
                val newCalendar = Calendar.getInstance()
                newCalendar.timeInMillis = currentTimestamp
                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCalendar.set(Calendar.MINUTE, minute)
                newCalendar.set(Calendar.SECOND, 0)
                newCalendar.set(Calendar.MILLISECOND, 0)
                
                currentTimestamp = newCalendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // Use 12-hour format
        )
        
        timePicker.show()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
        binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}