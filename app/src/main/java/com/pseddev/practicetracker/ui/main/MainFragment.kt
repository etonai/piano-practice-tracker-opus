package com.pseddev.practicetracker.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.practicetracker.BuildConfig
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.R
import com.pseddev.practicetracker.databinding.FragmentMainBinding
import com.pseddev.practicetracker.ui.progress.DashboardViewModel
import com.pseddev.practicetracker.ui.progress.DashboardViewModelFactory

class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((requireActivity().application as PianoTrackerApplication).repository)
    }
    
    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory((requireActivity().application as PianoTrackerApplication).repository)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupTodayActivities()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            binding.textCurrentStreak.text = if (streak > 0) {
                "Current Streak: $streak days 🔥"
            } else {
                "Current Streak: 0 days"
            }
        }
        
        viewModel.favoritesCount.observe(viewLifecycleOwner) { count ->
            val buttonText = if (count > 0) {
                "Manage Favorites ($count)"
            } else {
                "Manage Favorites"
            }
            binding.buttonManageFavorites.text = buttonText
        }
        
        // Set version text
        binding.textVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }
    
    private fun setupTodayActivities() {
        dashboardViewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
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
    }
    
    private fun setupClickListeners() {
        binding.buttonViewProgress.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_progressFragment)
        }
        
        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addActivityFragment)
        }
        
        binding.buttonManageFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_favoritesFragment)
        }
        
        binding.buttonImportExport.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_syncFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}