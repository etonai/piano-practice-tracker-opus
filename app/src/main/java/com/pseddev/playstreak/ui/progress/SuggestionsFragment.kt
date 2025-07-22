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
    
    private lateinit var adapter: SuggestionsAdapter
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
        
        adapter = SuggestionsAdapter({ suggestionItem ->
            // Show quick add activity dialog with piece pre-filled
            val dialog = QuickAddActivityDialogFragment.newInstance(
                suggestionItem.piece.id,
                suggestionItem.piece.name
            )
            dialog.show(parentFragmentManager, "QuickAddActivityDialog")
        }, proUserManager)
        
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