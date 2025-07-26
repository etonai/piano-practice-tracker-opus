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
        
        val onAddActivityClick = { suggestionItem: SuggestionItem ->
            // Show quick add activity dialog with piece pre-filled
            val dialog = QuickAddActivityDialogFragment.newInstance(
                suggestionItem.piece.id,
                suggestionItem.piece.name
            )
            dialog.show(parentFragmentManager, "QuickAddActivityDialog")
        }
        
        practiceAdapter = SuggestionsAdapter(onAddActivityClick)
        
        binding.practiceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.practiceRecyclerView.adapter = practiceAdapter
        binding.practiceRecyclerView.setHasFixedSize(false)
        binding.practiceRecyclerView.isNestedScrollingEnabled = false
        
        
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            // Only show practice suggestions (no Pro features)
            val practiceSuggestions = suggestions.filter { it.suggestionType == SuggestionType.PRACTICE }
            
            // Update practice suggestions
            practiceAdapter.submitList(practiceSuggestions)
            if (practiceSuggestions.isEmpty()) {
                binding.practiceRecyclerView.visibility = View.GONE
                binding.noPracticeSuggestionsText.visibility = View.VISIBLE
            } else {
                binding.practiceRecyclerView.visibility = View.VISIBLE
                binding.noPracticeSuggestionsText.visibility = View.GONE
            }
            
            // Hide performance suggestions section completely (no Pro features)
            binding.performanceSuggestionsHeader.visibility = View.GONE
            binding.performanceRecyclerView.visibility = View.GONE
            binding.noPerformanceSuggestionsText.visibility = View.GONE
            
            // Show empty state only if no practice suggestions
            if (practiceSuggestions.isEmpty()) {
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