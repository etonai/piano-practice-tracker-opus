package com.example.pianotrackopus.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pianotrackopus.PianoTrackerApplication
import com.example.pianotrackopus.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
            binding.todayCountText.text = "${activities.size} activities"
            
            if (activities.isNotEmpty()) {
                binding.todayActivitiesGroup.visibility = View.VISIBLE
                val activitySummary = activities.joinToString("\n") { activityWithPiece ->
                    val activity = activityWithPiece.activity
                    val piece = activityWithPiece.pieceOrTechnique
                    val time = android.text.format.DateFormat.format("h:mm a", activity.timestamp)
                    val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
                    val type = activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                    "• $time - ${piece.name} - $type$minutes"
                }
                binding.todayActivitiesList.text = activitySummary
            } else {
                binding.todayActivitiesGroup.visibility = View.GONE
            }
        }
        
        viewModel.yesterdayActivities.observe(viewLifecycleOwner) { activities ->
            binding.yesterdayCountText.text = "${activities.size} activities"
            
            if (activities.isNotEmpty()) {
                binding.yesterdayActivitiesGroup.visibility = View.VISIBLE
                val activitySummary = activities.joinToString("\n") { activityWithPiece ->
                    val activity = activityWithPiece.activity
                    val piece = activityWithPiece.pieceOrTechnique
                    val time = android.text.format.DateFormat.format("h:mm a", activity.timestamp)
                    val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
                    val type = activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                    "• $time - ${piece.name} - $type$minutes"
                }
                binding.yesterdayActivitiesList.text = activitySummary
            } else {
                binding.yesterdayActivitiesGroup.visibility = View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            val streak = viewModel.calculateStreak()
            binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}"
        }
        
        viewModel.weekSummary.observe(viewLifecycleOwner) { summary ->
            binding.weekSummaryText.text = summary
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}