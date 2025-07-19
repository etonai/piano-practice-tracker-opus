package com.example.pianotrackopus.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pianotrackopus.databinding.FragmentTimeInputBinding

class TimeInputFragment : Fragment() {
    
    private var _binding: FragmentTimeInputBinding? = null
    private val binding get() = _binding!!
    
    private val args: TimeInputFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeInputBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.buttonContinue.setOnClickListener {
            val minutesText = binding.editTextMinutes.text.toString()
            val minutes = if (minutesText.isNotEmpty()) {
                try {
                    minutesText.toInt()
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                -1
            }
            
            navigateToSummary(minutes)
        }
        
        binding.buttonSkip.setOnClickListener {
            navigateToSummary(-1)
        }
    }
    
    private fun navigateToSummary(minutes: Int) {
        val action = TimeInputFragmentDirections
            .actionTimeInputFragmentToSummaryFragment(
                activityType = args.activityType,
                pieceId = args.pieceId,
                pieceName = args.pieceName,
                level = args.level,
                performanceType = args.performanceType,
                minutes = minutes
            )
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}