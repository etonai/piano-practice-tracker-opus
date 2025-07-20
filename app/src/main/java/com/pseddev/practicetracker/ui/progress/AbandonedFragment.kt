package com.pseddev.practicetracker.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.practicetracker.PianoTrackerApplication
import com.pseddev.practicetracker.databinding.FragmentAbandonedBinding

class AbandonedFragment : Fragment() {
    
    private var _binding: FragmentAbandonedBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AbandonedViewModel by viewModels {
        AbandonedViewModelFactory(
            (requireActivity().application as PianoTrackerApplication).repository
        )
    }
    
    private lateinit var adapter: AbandonedAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbandonedBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = AbandonedAdapter()
        binding.abandonedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.abandonedRecyclerView.adapter = adapter
        
        // Enable nested scrolling for proper interaction with ViewPager2
        binding.abandonedRecyclerView.isNestedScrollingEnabled = true
        
        viewModel.abandonedPieces.observe(viewLifecycleOwner) { pieces ->
            if (pieces.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.abandonedRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.abandonedRecyclerView.visibility = View.VISIBLE
                adapter.submitList(pieces)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}