package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
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
        
        setupClickListeners()
        
        viewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
            binding.todayCountText.text = "${activities.size} activities"
            
            if (activities.isNotEmpty()) {
                binding.todayActivitiesGroup.visibility = View.VISIBLE
                val activitySummary = activities.joinToString("\n") { activityWithPiece ->
                    val activity = activityWithPiece.activity
                    val piece = activityWithPiece.pieceOrTechnique
                    val time = android.text.format.DateFormat.format("h:mm a", activity.timestamp)
                    val level = "(${activity.level})"
                    val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
                    val type = activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                    "• $time - ${piece.name} - $type $level$minutes"
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
                    val level = "(${activity.level})"
                    val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
                    val type = activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                    "• $time - ${piece.name} - $type $level$minutes"
                }
                binding.yesterdayActivitiesList.text = activitySummary
            } else {
                binding.yesterdayActivitiesGroup.visibility = View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            val streak = viewModel.calculateStreak()
            binding.currentStreakText.text = if (streak >= 6) {
                "$streak day${if (streak != 1) "s" else ""} 🔥"
            } else {
                "$streak day${if (streak != 1) "s" else ""}"
            }
        }
        
        viewModel.weekSummary.observe(viewLifecycleOwner) { summary ->
            binding.weekSummaryText.text = summary
        }
        
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isNotEmpty()) {
                binding.suggestionsCard.visibility = View.VISIBLE
                val suggestionText = suggestions.joinToString("\n") { suggestion ->
                    val favoriteIndicator = if (suggestion.piece.isFavorite) "⭐ " else ""
                    "• $favoriteIndicator${suggestion.piece.name} (${suggestion.suggestionReason})"
                }
                binding.suggestionsList.text = suggestionText
            } else {
                binding.suggestionsCard.visibility = View.GONE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addActivityFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}