package com.pseddev.practicetracker.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.R
import com.pseddev.practicetracker.databinding.FragmentPiecesBinding

class PiecesFragment : Fragment() {
    
    private var _binding: FragmentPiecesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PiecesViewModel by viewModels {
        PiecesViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    private lateinit var adapter: PiecesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPiecesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = PiecesAdapter { pieceWithStats ->
            viewModel.selectPiece(pieceWithStats.piece.id)
        }
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        viewModel.piecesWithStats.observe(viewLifecycleOwner) { pieces ->
            if (pieces.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(pieces)
            }
        }
        
        viewModel.selectedPieceDetails.observe(viewLifecycleOwner) { details ->
            details?.let {
                showPieceDetails(it)
            }
        }
    }
    
    private fun showPieceDetails(details: PieceDetails) {
        binding.pieceDetailsCard.visibility = View.VISIBLE
        binding.pieceNameText.text = details.piece.name
        binding.totalActivitiesText.text = "Total activities: ${details.activities.size}"
        
        val practiceCount = details.activities.count { it.activityType == com.pseddev.practicetracker.data.entities.ActivityType.PRACTICE }
        val performanceCount = details.activities.count { it.activityType == com.pseddev.practicetracker.data.entities.ActivityType.PERFORMANCE }
        
        binding.practiceCountText.text = "Practice activities: $practiceCount"
        binding.performanceCountText.text = "Performances: $performanceCount"
        
        val totalMinutes = details.activities.filter { it.minutes > 0 }.sumOf { it.minutes }
        if (totalMinutes > 0) {
            binding.totalTimeText.text = "Total tracked time: $totalMinutes minutes"
            binding.totalTimeText.visibility = View.VISIBLE
        } else {
            binding.totalTimeText.visibility = View.GONE
        }
        
        if (details.lastActivity != null) {
            val dateStr = android.text.format.DateFormat.format("MMM d, yyyy", details.lastActivity.timestamp)
            binding.lastActivityText.text = "Last activity: $dateStr"
        } else {
            binding.lastActivityText.text = "No activities recorded"
        }
        
        binding.closeDetailsButton.setOnClickListener {
            binding.pieceDetailsCard.visibility = View.GONE
            viewModel.clearSelection()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}