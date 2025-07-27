package com.pseddev.playstreak.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.databinding.FragmentFavoritesBinding
import com.pseddev.playstreak.utils.ProUserManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private lateinit var adapter: FavoritesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            onFavoriteToggle = { pieceOrTechnique ->
                lifecycleScope.launch {
                    val success = viewModel.toggleFavorite(pieceOrTechnique)
                    if (!success) {
                        showFavoriteLimitPrompt()
                    } else {
                        // In favorites list, pieces are always favorites, so clicking removes them
                        val message = "${pieceOrTechnique.name} removed from favorites"
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@FavoritesFragment.adapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.allPiecesAndTechniques.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(items)
            }
        }
    }
    
    private fun showFavoriteLimitPrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Favorite Limit")
            .setMessage("You can have up to ${ProUserManager.FREE_USER_FAVORITE_LIMIT} favorite pieces.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}