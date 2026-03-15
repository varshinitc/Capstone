package com.aidriven.notificationdetector.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.databinding.ActivityDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDetailBinding
    
    private var notificationId: Long = 0
    private var appName: String = ""
    private var packageName: String = ""
    private var title: String = ""
    private var content: String = ""
    private var riskLevel: String = ""
    private var riskScore: Float = 0f
    private var detectedIssues: List<String> = emptyList()
    private var timestamp: Long = 0
    private var userFeedback: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        extractIntentData()
        setupUI()
        setupButtons()
    }
    
    private fun extractIntentData() {
        notificationId = intent.getLongExtra("notification_id", 0)
        appName = intent.getStringExtra("app_name") ?: "Unknown"
        packageName = intent.getStringExtra("package_name") ?: ""
        title = intent.getStringExtra("title") ?: ""
        content = intent.getStringExtra("content") ?: ""
        riskLevel = intent.getStringExtra("risk_level") ?: "SAFE"
        riskScore = intent.getFloatExtra("risk_score", 0f)
        timestamp = intent.getLongExtra("timestamp", 0)
        userFeedback = intent.getStringExtra("user_feedback")
        
        // Parse detected issues from JSON string
        val issuesJson = intent.getStringExtra("detected_issues") ?: "[]"
        try {
            val type = object : TypeToken<List<String>>() {}.type
            detectedIssues = Gson().fromJson(issuesJson, type) ?: emptyList()
        } catch (e: Exception) {
            detectedIssues = emptyList()
        }
    }
    
    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // App name
        binding.tvAppName.text = appName
        binding.tvPackageName.text = packageName
        
        // Notification content
        binding.tvTitle.text = title
        binding.tvContent.text = content
        
        // Timestamp
        binding.tvTimestamp.text = formatTime(timestamp)
        
        // Risk level
        binding.tvRiskLevel.text = riskLevel.replace("_", " ")
        
        // Risk score
        binding.tvRiskScore.text = "${(riskScore * 100).toInt()}%"
        binding.progressRiskScore.progress = (riskScore * 100).toInt()
        
        // Set colors based on risk level
        val (backgroundColor, textColor, progressColor) = when (riskLevel) {
            "SAFE" -> Triple(
                ContextCompat.getColor(this, R.color.safe_green),
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.safe_green)
            )
            "SUSPICIOUS" -> Triple(
                ContextCompat.getColor(this, R.color.suspicious_yellow),
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.suspicious_yellow)
            )
            "HIGH_RISK" -> Triple(
                ContextCompat.getColor(this, R.color.high_risk_red),
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.high_risk_red)
            )
            else -> Triple(
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.black)
            )
        }
        
        binding.cardRiskLevel.setCardBackgroundColor(backgroundColor)
        binding.tvRiskLevel.setTextColor(textColor)
        binding.tvRiskScore.setTextColor(textColor)
        binding.progressRiskScore.progressTintList = ContextCompat.getColorStateList(this, 
            when (riskLevel) {
                "SAFE" -> R.color.safe_green
                "SUSPICIOUS" -> R.color.suspicious_yellow
                else -> R.color.high_risk_red
            }
        )
        
        // Detected issues
        if (detectedIssues.isNotEmpty()) {
            val issuesText = detectedIssues.joinToString("\n") { "• $it" }
            binding.tvIssuesList.text = issuesText
        } else {
            binding.tvIssuesList.text = "No issues detected"
        }
        
        // User feedback
        if (userFeedback != null) {
            binding.tvFeedbackStatus.text = "Feedback: ${userFeedback?.replace("_", " ")}"
            binding.tvFeedbackStatus.visibility = android.view.View.VISIBLE
        } else {
            binding.tvFeedbackStatus.visibility = android.view.View.GONE
        }
    }
    
    private fun setupButtons() {
        binding.btnMarkSafe.setOnClickListener {
            markFeedback("MARKED_SAFE")
        }
        
        binding.btnMarkPhishing.setOnClickListener {
            markFeedback("MARKED_PHISHING")
        }
    }
    
    private fun markFeedback(feedback: String) {
        // TODO: Update database with feedback
        userFeedback = feedback
        binding.tvFeedbackStatus.text = "Feedback: ${feedback.replace("_", " ")}"
        binding.tvFeedbackStatus.visibility = android.view.View.VISIBLE
        
        Toast.makeText(this, "Feedback recorded: ${feedback.replace("_", " ")}", Toast.LENGTH_SHORT).show()
    }
    
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
