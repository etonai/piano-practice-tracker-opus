package com.pseddev.practicetracker.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.practicetracker.databinding.ItemPieceStatsBinding
import java.text.SimpleDateFormat
import java.util.*

class PiecesAdapter(
    private val onPieceClick: (PieceWithStats) -> Unit,
    private val onFavoriteToggle: (PieceWithStats) -> Unit
) : ListAdapter<PieceWithStats, PiecesAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPieceStatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onPieceClick, onFavoriteToggle)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemPieceStatsBinding,
        private val onPieceClick: (PieceWithStats) -> Unit,
        private val onFavoriteToggle: (PieceWithStats) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        
        fun bind(item: PieceWithStats) {
            binding.pieceNameText.text = item.piece.name
            binding.activityCountText.text = "${item.activityCount} activities"
            
            if (item.lastActivityDate != null) {
                binding.lastActivityText.text = "Last: ${dateFormat.format(Date(item.lastActivityDate))}"
            } else {
                binding.lastActivityText.text = "No activities yet"
            }
            
            // Set favorite icon based on favorite status
            if (item.piece.isFavorite) {
                binding.favoriteIcon.setImageResource(com.pseddev.practicetracker.R.drawable.ic_star_filled)
            } else {
                binding.favoriteIcon.setImageResource(com.pseddev.practicetracker.R.drawable.ic_star_outline)
            }
            
            binding.root.setOnClickListener {
                onPieceClick(item)
            }
            
            binding.favoriteIcon.setOnClickListener {
                onFavoriteToggle(item)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<PieceWithStats>() {
        override fun areItemsTheSame(oldItem: PieceWithStats, newItem: PieceWithStats): Boolean {
            return oldItem.piece.id == newItem.piece.id
        }
        
        override fun areContentsTheSame(oldItem: PieceWithStats, newItem: PieceWithStats): Boolean {
            return oldItem == newItem
        }
    }
}