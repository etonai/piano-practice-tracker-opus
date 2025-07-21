package com.pseddev.playstreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.FragmentAddNewPieceBinding

class AddNewPieceFragment : Fragment() {
    
    private var _binding: FragmentAddNewPieceBinding? = null
    private val binding get() = _binding!!
    
    private val args: AddNewPieceFragmentArgs by navArgs()
    
    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewPieceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set default based on activity type
        if (args.activityType == ActivityType.PERFORMANCE) {
            binding.radioPiece.isChecked = true
            binding.radioTechnique.isEnabled = false
        }
        
        binding.buttonOk.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val type = if (binding.radioPiece.isChecked) ItemType.PIECE else ItemType.TECHNIQUE
            
            viewModel.insertPieceOrTechnique(name, type) { pieceId ->
                requireActivity().runOnUiThread {
                    val action = AddNewPieceFragmentDirections
                        .actionAddNewPieceFragmentToSelectLevelFragment(
                            activityType = args.activityType,
                            pieceId = pieceId,
                            pieceName = name,
                            itemType = type
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}