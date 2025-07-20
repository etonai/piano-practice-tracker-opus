package com.pseddev.practicetracker.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.databinding.FragmentSuggestionsBinding

class SuggestionsFragment : Fragment() {
    
    private var _binding: FragmentSuggestionsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SuggestionsViewModel by viewModels {
        SuggestionsViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    private lateinit var adapter: SuggestionsAdapter
    
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
        
        adapter = SuggestionsAdapter()
        
        binding.suggestionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.suggestionsRecyclerView.adapter = adapter
        
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.suggestionsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.suggestionsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(suggestions)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}