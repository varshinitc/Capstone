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
            val (backgroundColor, textColor) = when (notification.riskLevel) {
                "SAFE" -> Pair(
                    ContextCompat.getColor(binding.root.context, R.color.safe_green),
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                "SUSPICIOUS" -> Pair(
                    ContextCompat.getColor(binding.root.context, R.color.suspicious_yellow),
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
                "HIGH_RISK" -> Pair(
                    ContextCompat.getColor(binding.root.context, R.color.high_risk_red),
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                else -> Pair(
                    ContextCompat.getColor(binding.root.context, R.color.black),
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
            }
            
            binding.tvRiskLevel.setBackgroundColor(backgroundColor)
            binding.tvRiskLevel.setTextColor(textColor)
            
            // Set card background tint based on risk
            val cardTint = when (notification.riskLevel) {
                "SAFE" -> ContextCompat.getColor(binding.root.context, R.color.white)
                "SUSPICIOUS" -> ContextCompat.getColorStateList(binding.root.context, R.color.suspicious_yellow)?.withAlpha(30)?.defaultColor ?: ContextCompat.getColor(binding.root.context, R.color.white)
                "HIGH_RISK" -> ContextCompat.getColorStateList(binding.root.context, R.color.high_risk_red)?.withAlpha(30)?.defaultColor ?: ContextCompat.getColor(binding.root.context, R.color.white)
                else -> ContextCompat.getColor(binding.root.context, R.color.white)
            }
            binding.cardNotification.setCardBackgroundColor(cardTint)
            
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
