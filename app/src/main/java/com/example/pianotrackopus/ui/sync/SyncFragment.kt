package com.example.pianotrackopus.ui.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pianotrackopus.databinding.FragmentSyncBinding

class SyncFragment : Fragment() {
    
    private var _binding: FragmentSyncBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Implement import/export functionality
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}