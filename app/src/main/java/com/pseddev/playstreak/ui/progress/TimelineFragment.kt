package com.pseddev.playstreak.ui.progress

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentTimelineBinding
import com.pseddev.playstreak.ui.progress.EditActivityStorage
import java.text.SimpleDateFormat
import java.util.*

class TimelineFragment : Fragment() {
    
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels {
        TimelineViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
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
        
        setupRecyclerView()
        setupFilterUI()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = TimelineAdapter(
            onDeleteClick = { activityWithPiece ->
                showDeleteConfirmationDialog(activityWithPiece)
            },
            onEditClick = { activityWithPiece ->
                editActivity(activityWithPiece)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        // Enable nested scrolling for proper interaction with ViewPager2
        binding.recyclerView.isNestedScrollingEnabled = true
    }
    
    private fun setupFilterUI() {
        // No filter UI for now
    }
    
    private fun observeViewModel() {
        viewModel.activitiesWithPieces.observe(viewLifecycleOwner) { activities ->
            updateEmptyView(activities)
            adapter.submitList(activities)
        }
    }
    
    private fun updateEmptyView(activities: List<ActivityWithPiece>) {
        if (activities.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.text = "No activities recorded yet"
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun editActivity(activityWithPiece: ActivityWithPiece) {
        // Store the activity data for edit mode
        EditActivityStorage.setEditActivity(
            activityWithPiece.activity, 
            activityWithPiece.pieceOrTechnique.name,
            activityWithPiece.pieceOrTechnique.type
        )
        
        // Navigate directly to select level fragment for editing
        findNavController().navigate(
            R.id.action_viewProgressFragment_to_selectLevelFragment,
            bundleOf(
                "activityType" to activityWithPiece.activity.activityType,
                "pieceId" to activityWithPiece.activity.pieceOrTechniqueId,
                "pieceName" to activityWithPiece.pieceOrTechnique.name,
                "itemType" to activityWithPiece.pieceOrTechnique.type
            )
        )
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