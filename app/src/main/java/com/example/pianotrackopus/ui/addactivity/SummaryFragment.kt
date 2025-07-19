package com.example.pianotrackopus.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pianotrackopus.PianoTrackerApplication
import com.example.pianotrackopus.data.entities.ActivityType
import com.example.pianotrackopus.databinding.FragmentSummaryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryFragment : Fragment() {
    
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    
    private val args: SummaryFragmentArgs by navArgs()
    
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
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSummary()
        
        binding.buttonSave.setOnClickListener {
            viewModel.saveActivity(
                pieceId = args.pieceId,
                activityType = args.activityType,
                level = args.level,
                performanceType = args.performanceType,
                minutes = args.minutes,
                notes = args.notes
            )
        }
        
        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack(com.example.pianotrackopus.R.id.mainFragment, false)
        }
        
        viewModel.navigateToMain.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().popBackStack(com.example.pianotrackopus.R.id.mainFragment, false)
                viewModel.doneNavigating()
            }
        }
    }
    
    private fun setupSummary() {
        binding.textPiece.text = "Piece: ${args.pieceName}"
        binding.textType.text = "Type: ${args.activityType.name.lowercase().capitalize()}"
        
        val levelText = when (args.activityType) {
            ActivityType.PRACTICE -> {
                when (args.level) {
                    1 -> "Level: 1 - Essentials"
                    2 -> "Level: 2 - Incomplete"
                    3 -> "Level: 3 - Complete with Review"
                    4 -> "Level: 4 - Perfect Complete"
                    else -> "Level: ${args.level}"
                }
            }
            ActivityType.PERFORMANCE -> {
                val performanceTypeText = when (args.performanceType) {
                    "online" -> "Online"
                    "live" -> "Live"
                    else -> ""
                }
                when (args.level) {
                    1 -> "Level: 1 - Failed ($performanceTypeText)"
                    2 -> "Level: 2 - Unsatisfactory ($performanceTypeText)"
                    3 -> "Level: 3 - Satisfactory ($performanceTypeText)"
                    else -> "Level: ${args.level} ($performanceTypeText)"
                }
            }
        }
        binding.textLevel.text = levelText
        
        binding.textTime.text = if (args.minutes > 0) {
            "Time: ${args.minutes} minutes"
        } else {
            "Time: Not recorded"
        }
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
        binding.textDate.text = "Date: ${dateFormat.format(Date())}"
        
        if (args.notes.isNotEmpty()) {
            binding.textNotes.text = "Notes: ${args.notes}"
            binding.textNotes.visibility = View.VISIBLE
        } else {
            binding.textNotes.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}