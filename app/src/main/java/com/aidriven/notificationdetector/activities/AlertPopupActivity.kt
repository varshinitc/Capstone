package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.databinding.ActivityAlertPopupBinding

class AlertPopupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlertPopupBinding
    
    private var appName: String = ""
    private var packageName: String = ""
    private var title: String = ""
    private var content: String = ""
    private var riskLevel: String = ""
    private var riskScore: Float = 0f
    private var detectedIssues: ArrayList<String> = ArrayList()
    private var explanation: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
        extractIntentData()
        
        // Setup UI
        setupUI()
        setupButtons()
    }
    
    private fun extractIntentData() {
        appName = intent.getStringExtra("app_name") ?: "Unknown App"
        packageName = intent.getStringExtra("package_name") ?: ""
        title = intent.getStringExtra("title") ?: ""
        content = intent.getStringExtra("content") ?: ""
        riskLevel = intent.getStringExtra("risk_level") ?: "SAFE"
        riskScore = intent.getFloatExtra("risk_score", 0f)
        detectedIssues = intent.getStringArrayListExtra("detected_issues") ?: ArrayList()
        explanation = intent.getStringExtra("explanation") ?: ""
    }
    
    private fun setupUI() {
        // App name with risk level
        binding.tvAppName.text = "$appName - ${riskLevel.replace("_", " ")}"
        
        // Notification title
        binding.tvNotificationTitle.text = title
        
        // Notification content
        binding.tvNotificationContent.text = content
        
        // Risk level - make it more prominent
        val riskText = when (riskLevel) {
            "HIGH_RISK" -> "⚠️ HIGH RISK DETECTED"
            "SUSPICIOUS" -> "⚠️ SUSPICIOUS CONTENT"
            else -> "SAFE"
        }
        binding.tvRiskLevel.text = riskText
        
        // Risk score
        binding.tvRiskScore.text = "Risk Score: ${(riskScore * 100).toInt()}%"
        
        // Set colors based on risk level
        when (riskLevel) {
            "SAFE" -> {
                binding.cardRiskIndicator.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.safe_green)
                )
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(this, R.color.white)
                )
            }
            "SUSPICIOUS" -> {
                binding.cardRiskIndicator.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.suspicious_yellow)
                )
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(this, R.color.black)
                )
            }
            "HIGH_RISK" -> {
                binding.cardRiskIndicator.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.high_risk_red)
                )
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(this, R.color.white)
                )
            }
        }
        
        // Detected issues
        if (detectedIssues.isNotEmpty()) {
            binding.tvIssuesTitle.visibility = View.VISIBLE
            binding.tvIssuesList.visibility = View.VISIBLE
            
            val issuesText = detectedIssues.joinToString("\n") { "• $it" }
            binding.tvIssuesList.text = issuesText
        } else {
            binding.tvIssuesTitle.visibility = View.GONE
            binding.tvIssuesList.visibility = View.GONE
        }
        
        // Explanation
        if (explanation.isNotEmpty()) {
            binding.tvExplanation.text = explanation
            binding.tvExplanation.visibility = View.VISIBLE
        } else {
            binding.tvExplanation.visibility = View.GONE
        }
    }
    
    private fun setupButtons() {
        // Open Notification button
        binding.btnOpenNotification.setOnClickListener {
            openOriginalApp()
        }
        
        // Ignore button
        binding.btnIgnore.setOnClickListener {
            finish()
        }
        
        // Mark as Safe/Unsafe button
        binding.btnMarkFeedback.setOnClickListener {
            showFeedbackDialog()
        }
    }
    
    private fun openOriginalApp() {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                finish()
            } else {
                binding.tvExplanation.text = "Cannot open app"
            }
        } catch (e: Exception) {
            binding.tvExplanation.text = "Error opening app: ${e.message}"
        }
    }
    
    private fun showFeedbackDialog() {
        val options = arrayOf("Mark as Safe", "Mark as Unsafe")
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Provide Feedback")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Mark as Safe
                    sendFeedback("MARKED_SAFE")
                }
                1 -> {
                    // Mark as Unsafe
                    sendFeedback("MARKED_PHISHING")
                }
            }
            dialog.dismiss()
            finish()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    
    private fun sendFeedback(feedback: String) {
        // TODO: Send feedback to backend
        // For now, just show a toast
        android.widget.Toast.makeText(
            this,
            "Feedback recorded: $feedback",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
