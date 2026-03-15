package com.aidriven.notificationdetector.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.databinding.ItemNotificationBinding
import com.aidriven.notificationdetector.models.NotificationModel
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onItemClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    
    private var notifications = listOf<NotificationModel>()
    
    fun submitList(list: List<NotificationModel>) {
        notifications = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }
    
    override fun getItemCount(): Int = notifications.size
    
    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(notification: NotificationModel) {
            binding.tvAppName.text = notification.appName
            binding.tvTitle.text = notification.title
            binding.tvContent.text = notification.content
            binding.tvTime.text = formatTime(notification.timestamp)
            binding.tvRiskLevel.text = notification.riskLevel.replace("_", " ")
            
            // Set risk level color

            
            // Apply pill drawable per risk level
            val pillDrawable = when (notification.riskLevel) {
                "SAFE" -> R.drawable.bg_pill_safe
                "SUSPICIOUS" -> R.drawable.bg_pill_warning
                "HIGH_RISK" -> R.drawable.bg_pill_danger
                else -> R.drawable.bg_pill_safe
            }
            binding.tvRiskLevel.background = ContextCompat.getDrawable(binding.root.context, pillDrawable)
            binding.tvRiskLevel.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            // Dark card background for all cards
            binding.cardNotification.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, R.color.bg_surface)
            )
            
            // Click listener
            binding.root.setOnClickListener {
                onItemClick(notification)
            }
        }
        
        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
