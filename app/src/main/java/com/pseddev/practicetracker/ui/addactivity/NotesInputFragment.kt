package com.pseddev.practicetracker.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.databinding.FragmentNotesInputBinding

class NotesInputFragment : Fragment() {
    
    private var _binding: FragmentNotesInputBinding? = null
    private val binding get() = _binding!!
    
    private val args: NotesInputFragmentArgs by navArgs()
    
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
        _binding = FragmentNotesInputBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Handle back navigation in edit mode
        val editActivity = viewModel.editActivity.value
        if (editActivity != null) {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(com.pseddev.practicetracker.R.id.addActivityFragment, true)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
        
        // Pre-populate notes input in edit mode
        viewModel.editActivity.observe(viewLifecycleOwner) { editActivity ->
            if (editActivity != null && editActivity.notes.isNotEmpty()) {
                binding.editTextNotes.setText(editActivity.notes)
            }
        }
        
        binding.buttonContinue.setOnClickListener {
            val notes = binding.editTextNotes.text.toString().trim()
            navigateToSummary(notes)
        }
        
        binding.buttonSkip.setOnClickListener {
            navigateToSummary("")
        }
    }
    
    private fun navigateToSummary(notes: String) {
        val action = NotesInputFragmentDirections
            .actionNotesInputFragmentToSummaryFragment(
                activityType = args.activityType,
                pieceId = args.pieceId,
                pieceName = args.pieceName,
                level = args.level,
                performanceType = args.performanceType,
                notes = notes
            )
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}