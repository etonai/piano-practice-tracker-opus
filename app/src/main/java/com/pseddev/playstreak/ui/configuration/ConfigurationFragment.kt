package com.pseddev.playstreak.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentConfigurationBinding
import com.pseddev.playstreak.utils.PreferencesManager
import com.pseddev.playstreak.utils.ConfigurationManager
import com.pseddev.playstreak.utils.AchievementManager

class ConfigurationFragment : Fragment() {
    
    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ConfigurationViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var achievementManager: AchievementManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager.getInstance(requireContext())
        configurationManager = ConfigurationManager.getInstance(requireContext())
        achievementManager = AchievementManager(
            requireContext(),
            (requireActivity().application as PlayStreakApplication).repository
        )
        
        // Create ViewModel with custom factory
        viewModel = ViewModelProvider(this, ConfigurationViewModelFactory(preferencesManager))
            .get(ConfigurationViewModel::class.java)
        
        setupObservers()
        setupClickListeners()
        setupPruningToggle()
        setupAchievements()
    }
    
    private fun setupObservers() {
        viewModel.calendarDetailModeEnabled.observe(viewLifecycleOwner) { enabled ->
            // Update switch state without triggering listener
            binding.switchCalendarDetailMode.setOnCheckedChangeListener(null)
            binding.switchCalendarDetailMode.isChecked = enabled
            binding.switchCalendarDetailMode.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setCalendarDetailMode(isChecked)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.switchCalendarDetailMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setCalendarDetailMode(isChecked)
        }
        
        binding.viewAchievementsButton.setOnClickListener {
            findNavController().navigate(R.id.action_configurationFragment_to_achievementsFragment)
        }
    }
    
    private fun setupPruningToggle() {
        // Set initial state from ConfigurationManager
        binding.switchAllowPruning.isChecked = configurationManager.isPruningEnabled()
        
        // Set up toggle listener
        binding.switchAllowPruning.setOnCheckedChangeListener { _, isChecked ->
            configurationManager.setPruningEnabled(isChecked)
        }
    }
    
    private fun setupAchievements() {
        // Initialize achievements and update display
        lifecycleScope.launch {
            achievementManager.initializeAchievements()
            val (unlockedCount, totalCount) = achievementManager.getAchievementCounts()
            binding.achievementCountDisplay.text = "$unlockedCount/$totalCount achievements"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ConfigurationViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}