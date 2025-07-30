package com.pseddev.playstreak.ui.progress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.playstreak.databinding.ItemPieceStatsBinding
import com.pseddev.playstreak.utils.ProUserManager
import java.text.SimpleDateFormat
import java.util.*

class PiecesAdapter(
    private val onPieceClick: (PieceWithStats) -> Unit,
    private val onFavoriteToggle: (PieceWithStats) -> Unit,
    private val onAddActivityClick: (PieceWithStats) -> Unit,
    private val onEditClick: (PieceWithStats) -> Unit,
    private val onDeleteClick: (PieceWithStats) -> Unit,
    private val proUserManager: ProUserManager
) : ListAdapter<PieceWithStats, PiecesAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPieceStatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onPieceClick, onFavoriteToggle, onAddActivityClick, onEditClick, onDeleteClick, proUserManager)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemPieceStatsBinding,
        private val onPieceClick: (PieceWithStats) -> Unit,
        private val onFavoriteToggle: (PieceWithStats) -> Unit,
        private val onAddActivityClick: (PieceWithStats) -> Unit,
        private val onEditClick: (PieceWithStats) -> Unit,
        private val onDeleteClick: (PieceWithStats) -> Unit,
        private val proUserManager: ProUserManager
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        
        fun bind(item: PieceWithStats) {
            // Show technique emoji indicator for techniques
            val displayName = if (item.piece.type == com.pseddev.playstreak.data.entities.ItemType.TECHNIQUE) {
                "🎼 ${item.piece.name}"
            } else {
                item.piece.name
            }
            binding.pieceNameText.text = displayName
            binding.activityCountText.text = "${item.activityCount} activities"
            
            if (item.lastActivityDate != null) {
                binding.lastActivityText.text = "Last: ${dateFormat.format(Date(item.lastActivityDate))}"
            } else {
                binding.lastActivityText.text = "No activities yet"
            }
            
            // Set favorite icon based on favorite status
            if (item.piece.isFavorite) {
                binding.favoriteIcon.setImageResource(com.pseddev.playstreak.R.drawable.ic_star_filled)
            } else {
                binding.favoriteIcon.setImageResource(com.pseddev.playstreak.R.drawable.ic_star_outline)
            }
            
            binding.root.setOnClickListener {
                onPieceClick(item)
            }
            
            binding.favoriteIcon.setOnClickListener {
                onFavoriteToggle(item)
            }
            
            binding.addActivityIcon.setOnClickListener {
                onAddActivityClick(item)
            }
            
            binding.editIcon.setOnClickListener {
                onEditClick(item)
            }
            
            binding.deleteIcon.setOnClickListener {
                onDeleteClick(item)
            }
            
            // Show add activity icon only for Pro users
            if (proUserManager.isProUser()) {
                binding.addActivityIcon.visibility = View.VISIBLE
            } else {
                binding.addActivityIcon.visibility = View.GONE
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