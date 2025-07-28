package com.pseddev.playstreak.ui.prunedata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.databinding.FragmentPruneDataBinding

class PruneDataFragment : Fragment() {
    
    private var _binding: FragmentPruneDataBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PruneDataViewModel by viewModels {
        PruneDataViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPruneDataBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Future phases will add functionality here
        // Phase 1 establishes the UI foundation
        setupUI()
    }
    
    private fun setupUI() {
        // Basic UI setup - future phases will add observers and click handlers
        // Currently displays placeholder content as designed in Phase 1
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}