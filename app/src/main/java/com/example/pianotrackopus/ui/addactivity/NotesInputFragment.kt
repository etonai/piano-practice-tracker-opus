package com.example.pianotrackopus.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pianotrackopus.databinding.FragmentNotesInputBinding

class NotesInputFragment : Fragment() {
    
    private var _binding: FragmentNotesInputBinding? = null
    private val binding get() = _binding!!
    
    private val args: NotesInputFragmentArgs by navArgs()
    
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