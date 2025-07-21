package com.pseddev.practicetracker.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.data.entities.ActivityType
import com.pseddev.practicetracker.ui.progress.EditActivityStorage
import com.pseddev.practicetracker.databinding.FragmentAddActivityBinding

class AddActivityFragment : Fragment() {
    
    private var _binding: FragmentAddActivityBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddActivityBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup button click listeners
        binding.buttonPractice.setOnClickListener {
            // Clear any previous edit mode when starting a new activity
            viewModel.clearEditMode()
            EditActivityStorage.clearEditActivity()
            val action = AddActivityFragmentDirections
                .actionAddActivityFragmentToSelectPieceFragment(ActivityType.PRACTICE)
            findNavController().navigate(action)
        }
        
        binding.buttonPerformance.setOnClickListener {
            // Clear any previous edit mode when starting a new activity
            viewModel.clearEditMode()
            EditActivityStorage.clearEditActivity()
            val action = AddActivityFragmentDirections
                .actionAddActivityFragmentToSelectPieceFragment(ActivityType.PERFORMANCE)
            findNavController().navigate(action)
        }
        
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}