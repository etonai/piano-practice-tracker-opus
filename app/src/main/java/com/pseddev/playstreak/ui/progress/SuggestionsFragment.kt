package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentSuggestionsBinding
import com.pseddev.playstreak.utils.ProUserManager

class SuggestionsFragment : Fragment() {
    
    private var _binding: FragmentSuggestionsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SuggestionsViewModel by viewModels {
        SuggestionsViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private lateinit var practiceAdapter: SuggestionsAdapter
    private lateinit var performanceAdapter: SuggestionsAdapter
    private lateinit var proUserManager: ProUserManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        proUserManager = ProUserManager.getInstance(requireContext())
        
        val onAddActivityClick = { suggestionItem: SuggestionItem ->
            // Show quick add activity dialog with piece pre-filled
            val dialog = QuickAddActivityDialogFragment.newInstance(
                suggestionItem.piece.id,
                suggestionItem.piece.name
            )
            dialog.show(parentFragmentManager, "QuickAddActivityDialog")
        }
        
        practiceAdapter = SuggestionsAdapter(onAddActivityClick, proUserManager)
        performanceAdapter = SuggestionsAdapter(onAddActivityClick, proUserManager)
        
        binding.practiceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.practiceRecyclerView.adapter = practiceAdapter
        binding.practiceRecyclerView.setHasFixedSize(false)
        binding.practiceRecyclerView.isNestedScrollingEnabled = false
        
        binding.performanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.performanceRecyclerView.adapter = performanceAdapter
        
        // Force RecyclerView to measure properly inside ScrollView
        binding.performanceRecyclerView.setHasFixedSize(false)
        binding.performanceRecyclerView.isNestedScrollingEnabled = false
        
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            val practiceSuggestions = suggestions.filter { it.suggestionType == SuggestionType.PRACTICE }
            val performanceSuggestions = suggestions.filter { it.suggestionType == SuggestionType.PERFORMANCE }
            
            // Debug logging
            android.util.Log.d("SuggestionsFragment", "Total suggestions: ${suggestions.size}")
            android.util.Log.d("SuggestionsFragment", "Practice suggestions: ${practiceSuggestions.size}")
            android.util.Log.d("SuggestionsFragment", "Performance suggestions: ${performanceSuggestions.size}")
            suggestions.forEach { suggestion ->
                android.util.Log.d("SuggestionsFragment", "Suggestion: ${suggestion.piece.name} - Type: ${suggestion.suggestionType} - Reason: ${suggestion.suggestionReason}")
            }
            
            // Update practice suggestions
            practiceAdapter.submitList(practiceSuggestions)
            if (practiceSuggestions.isEmpty()) {
                binding.practiceRecyclerView.visibility = View.GONE
                binding.noPracticeSuggestionsText.visibility = View.VISIBLE
            } else {
                binding.practiceRecyclerView.visibility = View.VISIBLE
                binding.noPracticeSuggestionsText.visibility = View.GONE
            }
            
            // Update performance suggestions (Pro only)
            if (proUserManager.isProUser() && performanceSuggestions.isNotEmpty()) {
                android.util.Log.d("SuggestionsFragment", "Submitting ${performanceSuggestions.size} performance suggestions to adapter")
                binding.performanceSuggestionsHeader.visibility = View.VISIBLE
                binding.performanceRecyclerView.visibility = View.VISIBLE
                binding.noPerformanceSuggestionsText.visibility = View.GONE
                performanceAdapter.submitList(performanceSuggestions)
            } else if (proUserManager.isProUser()) {
                binding.performanceSuggestionsHeader.visibility = View.VISIBLE
                binding.performanceRecyclerView.visibility = View.GONE
                binding.noPerformanceSuggestionsText.visibility = View.VISIBLE
            } else {
                binding.performanceSuggestionsHeader.visibility = View.GONE
                binding.performanceRecyclerView.visibility = View.GONE
                binding.noPerformanceSuggestionsText.visibility = View.GONE
            }
            
            // Show empty state only if no suggestions at all
            if (suggestions.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}