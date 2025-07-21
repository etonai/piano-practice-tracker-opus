package com.pseddev.playstreak.ui.progress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.playstreak.R
import java.text.SimpleDateFormat
import java.util.*

class AbandonedAdapter : ListAdapter<AbandonedItem, AbandonedAdapter.AbandonedViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbandonedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_abandoned, parent, false)
        return AbandonedViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbandonedViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class AbandonedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pieceNameText: TextView = itemView.findViewById(R.id.pieceNameText)
        private val daysSinceText: TextView = itemView.findViewById(R.id.daysSinceText)
        private val lastPracticedText: TextView = itemView.findViewById(R.id.lastPracticedText)

        fun bind(item: AbandonedItem) {
            pieceNameText.text = item.piece.name
            
            if (item.lastActivityDate != null) {
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val dateString = dateFormat.format(Date(item.lastActivityDate))
                lastPracticedText.text = "Last practiced: $dateString"
                
                val daysText = when {
                    item.daysSinceLastActivity == 1 -> "1 day ago"
                    item.daysSinceLastActivity < 365 -> "${item.daysSinceLastActivity} days ago"
                    else -> {
                        val years = item.daysSinceLastActivity / 365
                        val remainingDays = item.daysSinceLastActivity % 365
                        when {
                            years == 1 && remainingDays == 0 -> "1 year ago"
                            years == 1 -> "1 year, $remainingDays days ago"
                            remainingDays == 0 -> "$years years ago"
                            else -> "$years years, $remainingDays days ago"
                        }
                    }
                }
                daysSinceText.text = daysText
            } else {
                lastPracticedText.text = "Never practiced"
                daysSinceText.text = "Never"
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AbandonedItem>() {
            override fun areItemsTheSame(oldItem: AbandonedItem, newItem: AbandonedItem): Boolean {
                return oldItem.piece.id == newItem.piece.id
            }

            override fun areContentsTheSame(oldItem: AbandonedItem, newItem: AbandonedItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}