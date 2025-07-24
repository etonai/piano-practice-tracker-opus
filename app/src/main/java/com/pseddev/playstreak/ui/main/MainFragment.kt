package com.pseddev.playstreak.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.BuildConfig
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.analytics.AnalyticsManager
import com.pseddev.playstreak.crashlytics.CrashlyticsManager
import com.pseddev.playstreak.databinding.FragmentMainBinding
import com.pseddev.playstreak.utils.ProUserManager

class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((requireActivity().application as PlayStreakApplication).repository)
    }
    
    private lateinit var proUserManager: ProUserManager
    private lateinit var crashlyticsManager: CrashlyticsManager
    private lateinit var analyticsManager: AnalyticsManager
    
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
        
        proUserManager = ProUserManager.getInstance(requireContext())
        crashlyticsManager = CrashlyticsManager(requireContext())
        analyticsManager = AnalyticsManager(requireContext())
        
        setupObservers()
        setupClickListeners()
        updateAppTitle()
        updateToggleButtonText(proUserManager.isProUser())
        
        // Hide debug buttons in production builds
        binding.buttonTogglePro.visibility = if (BuildConfig.DEBUG) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        binding.buttonTestCrash.visibility = if (BuildConfig.DEBUG) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        binding.buttonForceAnalyticsSync.visibility = if (BuildConfig.DEBUG) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    private fun setupObservers() {
        viewModel.favoritesCount.observe(viewLifecycleOwner) { count ->
            val buttonText = if (count > 0) {
                "Manage Favorites ($count)"
            } else {
                "Manage Favorites"
            }
            binding.buttonManageFavorites.text = buttonText
        }
        
        viewModel.piecesCount.observe(viewLifecycleOwner) { count ->
            binding.buttonAddPiece.text = "Add Piece ($count)"
        }
        
        viewModel.activitiesCount.observe(viewLifecycleOwner) { count ->
            binding.buttonAddActivity.text = "Add Activity ($count)"
        }
        
        // Set version text
        binding.textVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }
    
    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addActivityFragment)
        }
        
        binding.buttonAddPiece.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addPieceFragment)
        }
        
        binding.buttonManageFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_favoritesFragment)
        }
        
        binding.buttonImportExport.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_syncFragment)
        }
        
        binding.buttonTogglePro.setOnClickListener {
            val newStatus = proUserManager.toggleProStatus()
            updateAppTitle()
            updateToggleButtonText(newStatus)
        }
        
        binding.buttonTestCrash.setOnClickListener {
            crashlyticsManager.forceCrashForTesting()
        }
        
        binding.buttonForceAnalyticsSync.setOnClickListener {
            analyticsManager.forceAnalyticsSyncForTesting()
        }
    }
    
    private fun updateAppTitle() {
        val title = if (proUserManager.isProUser()) {
            getString(R.string.app_name) + " Pro"
        } else {
            getString(R.string.app_name)
        }
        binding.textAppTitle.text = title
    }
    
    private fun updateToggleButtonText(isProUser: Boolean) {
        val buttonText = if (isProUser) {
            "Switch to Free (Testing)"
        } else {
            "Switch to Pro (Testing)"
        }
        binding.buttonTogglePro.text = buttonText
    }
    
    override fun onResume() {
        super.onResume()
        // Update title and button text when returning to the fragment in case Pro status changed
        updateAppTitle()
        updateToggleButtonText(proUserManager.isProUser())
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}