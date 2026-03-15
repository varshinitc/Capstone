package com.aidriven.notificationdetector.activities

import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.databinding.ActivityFullscreenAlertBinding

class FullscreenAlertActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFullscreenAlertBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure window for overlay display
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
            
            addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            )
            
            // Make it appear on top
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                attributes = attributes.apply {
                    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
            }
        }
        
        binding = ActivityFullscreenAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
        val appName = intent.getStringExtra("app_name") ?: "Unknown"
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val riskLevel = intent.getStringExtra("risk_level") ?: "SAFE"
        val riskScore = intent.getFloatExtra("risk_score", 0f)
        val detectedIssues = intent.getStringArrayListExtra("detected_issues") ?: ArrayList()
        
        setupUI(appName, title, content, riskLevel, riskScore, detectedIssues)
        setupButtons()
    }
    
    private fun setupUI(
        appName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: ArrayList<String>
    ) {
        // Set background color based on risk level
        when (riskLevel) {
            "HIGH_RISK" -> {
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.high_risk_red))
                binding.tvAlertTitle.text = "⚠️ SPAM DETECTED"
                binding.tvAlertTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            "SUSPICIOUS" -> {
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.suspicious_yellow))
                binding.tvAlertTitle.text = "⚠️ SUSPICIOUS"
                binding.tvAlertTitle.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            else -> {
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.safe_green))
                binding.tvAlertTitle.text = "✓ SAFE"
                binding.tvAlertTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
        
        // Set app name
        binding.tvAppName.text = appName
        
        // Set notification title
        binding.tvNotificationTitle.text = title
        
        // Set notification content
        binding.tvNotificationContent.text = content
        
        // Set risk score
        binding.tvRiskScore.text = "Risk: ${(riskScore * 100).toInt()}%"
        
        // Set detected issues
        if (detectedIssues.isNotEmpty()) {
            binding.tvDetectedIssues.text = detectedIssues.joinToString("\n") { "• $it" }
        }
    }
    
    private fun setupButtons() {
        // Dismiss button
        binding.btnDismiss.setOnClickListener {
            finish()
        }
        
        // Block button (for future implementation)
        binding.btnBlock.setOnClickListener {
            // TODO: Add to block list
            finish()
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
