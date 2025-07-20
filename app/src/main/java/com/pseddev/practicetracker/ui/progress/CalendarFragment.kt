package com.pseddev.practicetracker.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pseddev.practicetracker.PianoTrackerApplication
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
        if (activities.isEmpty()) {
            binding.selectedDateText.text = "No activities on this date"
            binding.selectedDateActivities.text = ""
        } else {
            binding.selectedDateText.text = "${activities.size} activities"
            val summary = activities.joinToString("\n") { item ->
                val time = android.text.format.DateFormat.format("h:mm a", item.activity.timestamp)
                val type = item.activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                val minutes = if (item.activity.minutes > 0) " (${item.activity.minutes} min)" else ""
                "• $time - ${item.pieceOrTechnique.name} - $type$minutes"
            }
            binding.selectedDateActivities.text = summary
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}