package com.pseddev.playstreak.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.playstreak.databinding.ItemSuggestionBinding

class SuggestionsAdapter : ListAdapter<SuggestionItem, SuggestionsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SuggestionItem) {
            binding.pieceNameText.text = item.piece.name
            binding.suggestionReasonText.text = item.suggestionReason
            
            // Show favorite icon for favorite pieces
            if (item.piece.isFavorite) {
                binding.favoriteIcon.visibility = android.view.View.VISIBLE
            } else {
                binding.favoriteIcon.visibility = android.view.View.GONE
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