package com.pseddev.practicetracker.ui.progress

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.databinding.FragmentTimelineBinding
import java.text.SimpleDateFormat
import java.util.*

class TimelineFragment : Fragment() {
    
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels {
        TimelineViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    private lateinit var adapter: TimelineAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = TimelineAdapter { activityWithPiece ->
            showDeleteConfirmationDialog(activityWithPiece)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        // Enable nested scrolling for proper interaction with ViewPager2
        binding.recyclerView.isNestedScrollingEnabled = true
        
        viewModel.activitiesWithPieces.observe(viewLifecycleOwner) { activities ->
            if (activities.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(activities)
            }
        }
    }
    
    private fun showDeleteConfirmationDialog(activityWithPiece: ActivityWithPiece) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val dateString = dateFormat.format(Date(activityWithPiece.activity.timestamp))
        val pieceName = activityWithPiece.pieceOrTechnique.name
        
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?\n\n$pieceName\n$dateString")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteActivity(activityWithPiece)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}