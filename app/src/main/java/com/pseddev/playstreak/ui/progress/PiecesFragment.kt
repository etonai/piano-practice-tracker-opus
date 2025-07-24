package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentPiecesBinding
import com.pseddev.playstreak.ui.progress.QuickAddActivityDialogFragment
import com.pseddev.playstreak.utils.ProUserManager

class PiecesFragment : Fragment() {
    
    private var _binding: FragmentPiecesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PiecesViewModel by viewModels {
        PiecesViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private lateinit var adapter: PiecesAdapter
    private var shouldScrollToTop = false
    
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
        
        setupClickListeners()
        setupSortingControls()
        
        val proUserManager = ProUserManager.getInstance(requireContext())
        
        adapter = PiecesAdapter(
            onPieceClick = { pieceWithStats ->
                viewModel.selectPiece(pieceWithStats.piece.id)
            },
            onFavoriteToggle = { pieceWithStats ->
                val success = viewModel.toggleFavorite(pieceWithStats)
                if (!success) {
                    showFavoriteLimitPrompt()
                }
            },
            onAddActivityClick = { pieceWithStats ->
                // Show quick add activity dialog with piece pre-filled
                val dialog = QuickAddActivityDialogFragment.newInstance(
                    pieceWithStats.piece.id,
                    pieceWithStats.piece.name
                )
                dialog.show(parentFragmentManager, "QuickAddActivityDialog")
            },
            onDeleteClick = { pieceWithStats ->
                showDeleteConfirmationDialog(pieceWithStats)
            },
            proUserManager
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        viewModel.piecesWithStats.observe(viewLifecycleOwner) { pieces ->
            if (pieces.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(pieces) {
                    // Scroll to top after the new data is submitted if a sort change happened
                    if (shouldScrollToTop) {
                        binding.recyclerView.scrollToPosition(0)
                        shouldScrollToTop = false
                    }
                }
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
        
        val practiceCount = details.activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PRACTICE }
        val performanceCount = details.activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PERFORMANCE }
        
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
    
    private fun setupClickListeners() {
        binding.buttonAddPiece.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addPieceFragment)
        }
    }
    
    private fun setupSortingControls() {
        // Set up chip selection listener
        binding.sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val sortType = when (checkedIds[0]) {
                    R.id.chipAlphabetical -> SortType.ALPHABETICAL
                    R.id.chipLastDate -> SortType.LAST_DATE
                    R.id.chipActivityCount -> SortType.ACTIVITY_COUNT
                    else -> SortType.ALPHABETICAL
                }
                viewModel.setSortType(sortType)
                shouldScrollToTop = true
            }
        }
        
        // Set up sort direction button
        binding.buttonSortDirection.setOnClickListener {
            viewModel.toggleSortDirection()
            updateSortDirectionButton()
            shouldScrollToTop = true
        }
        
        // Initialize sort direction button
        updateSortDirectionButton()
    }
    
    private fun updateSortDirectionButton() {
        val direction = viewModel.getCurrentSortDirection()
        binding.buttonSortDirection.text = if (direction == SortDirection.ASCENDING) "↑" else "↓"
    }
    
    private fun showFavoriteLimitPrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Favorite Limit")
            .setMessage("You can have up to ${ProUserManager.FREE_USER_FAVORITE_LIMIT} favorite pieces.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showDeleteConfirmationDialog(pieceWithStats: PieceWithStats) {
        val activityText = if (pieceWithStats.activityCount == 1) {
            "1 activity"
        } else {
            "${pieceWithStats.activityCount} activities"
        }
        
        val message = "Delete \"${pieceWithStats.piece.name}\" and all its $activityText?\n\n" +
                "This action cannot be undone."
        
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Piece")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePiece(pieceWithStats)
                Toast.makeText(requireContext(), "Piece deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}