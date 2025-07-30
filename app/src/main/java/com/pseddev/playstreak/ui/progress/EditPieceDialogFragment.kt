package com.pseddev.playstreak.ui.progress

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.DialogEditPieceBinding

class EditPieceDialogFragment : DialogFragment() {
    
    private var _binding: DialogEditPieceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PiecesViewModel by viewModels({requireParentFragment()}) {
        PiecesViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private var pieceId: Long = -1
    private var currentName: String = ""
    private var currentType: ItemType = ItemType.PIECE
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditPieceBinding.inflate(layoutInflater)
        
        // Get arguments
        arguments?.let {
            pieceId = it.getLong(ARG_PIECE_ID, -1)
            currentName = it.getString(ARG_PIECE_NAME, "")
            currentType = ItemType.valueOf(it.getString(ARG_PIECE_TYPE, ItemType.PIECE.name))
        }
        
        // Pre-populate fields
        binding.pieceNameEditText.setText(currentName)
        when (currentType) {
            ItemType.PIECE -> binding.radioPiece.isChecked = true
            ItemType.TECHNIQUE -> binding.radioTechnique.isChecked = true
        }
        
        // Set up button listeners
        binding.saveButton.setOnClickListener {
            saveChanges()
        }
        
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }
    
    private fun saveChanges() {
        val newName = binding.pieceNameEditText.text.toString().trim()
        
        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a piece name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newType = if (binding.radioPiece.isChecked) ItemType.PIECE else ItemType.TECHNIQUE
        
        // Only update if something changed
        if (newName != currentName || newType != currentType) {
            viewModel.updatePiece(pieceId, newName, newType)
            Toast.makeText(requireContext(), "Piece updated", Toast.LENGTH_SHORT).show()
        }
        
        dismiss()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val ARG_PIECE_ID = "piece_id"
        private const val ARG_PIECE_NAME = "piece_name"
        private const val ARG_PIECE_TYPE = "piece_type"
        
        fun newInstance(pieceId: Long, pieceName: String, pieceType: ItemType): EditPieceDialogFragment {
            return EditPieceDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_PIECE_ID, pieceId)
                    putString(ARG_PIECE_NAME, pieceName)
                    putString(ARG_PIECE_TYPE, pieceType.name)
                }
            }
        }
    }
}