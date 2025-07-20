package com.pseddev.practicetracker.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.R
import com.pseddev.practicetracker.data.entities.ActivityType
import com.pseddev.practicetracker.databinding.FragmentCalendarBinding
import java.util.*

class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            viewModel.selectDate(calendar.timeInMillis)
        }
        
        viewModel.selectedDateActivities.observe(viewLifecycleOwner) { activities ->
            updateSelectedDateView(activities)
        }
        
        // Set initial date to today
        val today = Calendar.getInstance()
        viewModel.selectDate(today.timeInMillis)
    }
    
    private fun updateSelectedDateView(activities: List<ActivityWithPiece>) {
        // Update color indicator
        val color = getCalendarColorForActivities(activities)
        binding.activityColorIndicator.setBackgroundColor(color)
        
        if (activities.isEmpty()) {
            binding.selectedDateText.text = "No activities on this date"
            binding.selectedDateActivities.text = ""
            binding.activityColorIndicator.visibility = View.GONE
        } else {
            binding.selectedDateText.text = "${activities.size} activities"
            binding.activityColorIndicator.visibility = View.VISIBLE
            
            val summary = activities.joinToString("\n") { item ->
                val time = android.text.format.DateFormat.format("h:mm a", item.activity.timestamp)
                val type = item.activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                val level = "(${item.activity.level})"
                val minutes = if (item.activity.minutes > 0) " - ${item.activity.minutes} min" else ""
                "$time - ${item.pieceOrTechnique.name} - $type $level$minutes"
            }
            binding.selectedDateActivities.text = summary
        }
    }
    
    private fun getCalendarColorForActivities(activities: List<ActivityWithPiece>): Int {
        if (activities.isEmpty()) {
            return ContextCompat.getColor(requireContext(), R.color.calendar_no_activity)
        }
        
        val activityCount = activities.size
        val hasPerformance = activities.any { it.activity.activityType == ActivityType.PERFORMANCE }
        
        return if (hasPerformance) {
            // Performance days use green colors
            when {
                activityCount >= 11 -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_dark)
                activityCount >= 5 -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_light)
            }
        } else {
            // Practice-only days use blue colors
            when {
                activityCount >= 11 -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_dark)
                activityCount >= 5 -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_light)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}