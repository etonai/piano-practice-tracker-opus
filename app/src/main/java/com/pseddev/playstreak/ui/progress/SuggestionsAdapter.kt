package com.pseddev.playstreak.ui.progress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.playstreak.databinding.ItemSuggestionBinding
import com.pseddev.playstreak.utils.ProUserManager

class SuggestionsAdapter(
    private val onAddActivityClick: (SuggestionItem) -> Unit,
    private val proUserManager: ProUserManager
) : ListAdapter<SuggestionItem, SuggestionsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onAddActivityClick, proUserManager)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemSuggestionBinding,
        private val onAddActivityClick: (SuggestionItem) -> Unit,
        private val proUserManager: ProUserManager
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var currentItem: SuggestionItem? = null
        
        init {
            binding.addActivityIcon.setOnClickListener {
                currentItem?.let { onAddActivityClick(it) }
            }
        }
        
        fun bind(item: SuggestionItem) {
            currentItem = item
            binding.pieceNameText.text = item.piece.name
            binding.suggestionReasonText.text = item.suggestionReason
            
            // Show favorite icon for favorite pieces
            if (item.piece.isFavorite) {
                binding.favoriteIcon.visibility = View.VISIBLE
            } else {
                binding.favoriteIcon.visibility = View.GONE
            }
            
            // Show add activity icon only for Pro users
            if (proUserManager.isProUser()) {
                binding.addActivityIcon.visibility = View.VISIBLE
            } else {
                binding.addActivityIcon.visibility = View.GONE
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<SuggestionItem>() {
        override fun areItemsTheSame(oldItem: SuggestionItem, newItem: SuggestionItem): Boolean {
            return oldItem.piece.id == newItem.piece.id
        }
        
        override fun areContentsTheSame(oldItem: SuggestionItem, newItem: SuggestionItem): Boolean {
            return oldItem == newItem
        }
    }
}