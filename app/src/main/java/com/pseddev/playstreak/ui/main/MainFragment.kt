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
import com.pseddev.playstreak.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((requireActivity().application as PlayStreakApplication).repository)
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
        setupClickListeners()
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
        
        // Set version text
        binding.textVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }
    
    private fun setupClickListeners() {
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