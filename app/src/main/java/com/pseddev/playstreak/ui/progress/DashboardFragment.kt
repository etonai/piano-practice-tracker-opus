package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import android.util.Log
import com.pseddev.playstreak.BuildConfig
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentDashboardBinding

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
            Log.d("DashboardUI", "Observer called with ${activities.size} activities")
            binding.todayCountText.text = "${activities.size} activities"
            
            if (activities.isNotEmpty()) {
                Log.d("DashboardUI", "Showing activities group with ${activities.size} activities")
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
                Log.d("DashboardUI", "Set todayActivitiesList text to: $activitySummary")
            } else {
                Log.d("DashboardUI", "Hiding activities group - should be empty now")
                binding.todayActivitiesGroup.visibility = View.GONE
                binding.todayActivitiesList.text = ""
                Log.d("DashboardUI", "Set todayActivitiesGroup visibility to GONE and cleared text")
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
        
        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            val emojiSuffix = when {
                streak >= 14 -> " 🔥🔥🔥"
                streak >= 8 -> " 🔥" 
                streak >= 5 -> " 🎶"
                streak >= 3 -> " 🎵"
                else -> ""
            }
            binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}$emojiSuffix"
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
        
        viewModel.performanceSuggestions.observe(viewLifecycleOwner) { suggestions ->
            // Conditional debug logging for development builds only
            if (BuildConfig.DEBUG) {
                android.util.Log.d("DashboardFragment", "Performance suggestions: ${suggestions.size}")
                if (suggestions.size <= 5) { // Limit detailed logging to avoid log spam
                    suggestions.forEach { suggestion ->
                        android.util.Log.d("DashboardFragment", "Performance Suggestion: ${suggestion.piece.name} - Type: ${suggestion.suggestionType} - Reason: ${suggestion.suggestionReason}")
                    }
                } else {
                    android.util.Log.d("DashboardFragment", "Too many suggestions (${suggestions.size}) - detailed logging skipped to avoid spam")
                }
            }
            
            if (suggestions.isNotEmpty()) {
                binding.performanceSuggestionsCard.visibility = View.VISIBLE
                val suggestionText = suggestions.joinToString("\n") { suggestion ->
                    val favoriteIndicator = if (suggestion.piece.isFavorite) "⭐ " else ""
                    "• $favoriteIndicator${suggestion.piece.name} (${suggestion.suggestionReason})"
                }
                binding.performanceSuggestionsList.text = suggestionText
            } else {
                binding.performanceSuggestionsCard.visibility = View.GONE
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