package com.pseddev.playstreak.ui.pieces

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.FragmentAddPieceBinding

class AddPieceFragment : Fragment() {
    
    private var _binding: FragmentAddPieceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddPieceViewModel by viewModels {
        AddPieceViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPieceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            val name = binding.pieceNameEditText.text?.toString()?.trim()
            
            if (name.isNullOrBlank()) {
                binding.pieceNameInputLayout.error = "Please enter a piece name"
                return@setOnClickListener
            }
            
            binding.pieceNameInputLayout.error = null
            
            val type = if (binding.radioPiece.isChecked) ItemType.PIECE else ItemType.TECHNIQUE
            val isFavorite = binding.favoriteSwitch.isChecked
            
            viewModel.savePiece(name, type, isFavorite)
        }
        
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddPieceResult.Success -> {
                    Toast.makeText(requireContext(), "Piece added successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is AddPieceResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is AddPieceResult.PieceLimitReached -> {
                    showPieceLimitDialog(result.currentCount, result.limit, result.isProUser)
                }
            }
        }
    }
    
    private fun showPieceLimitDialog(currentCount: Int, limit: Int, isProUser: Boolean) {
        val message = "Piece Limit Reached\n\n" +
                "You currently have $currentCount pieces and techniques. " +
                "This app supports up to $limit pieces total to ensure good performance.\n\n" +
                "You can keep all your existing pieces, but cannot add new ones until you remove some pieces."

        AlertDialog.Builder(requireContext())
            .setTitle("Cannot Add Piece")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}