package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.databinding.ActivityTopBannerAlertBinding

class TopBannerAlertActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTopBannerAlertBinding
    private val autoCloseHandler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure window to appear as top banner overlay ON TOP OF ANY APP
        window.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            
            // Show when locked and turn screen on
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
            
            // Critical flags for overlay on top of other apps
            addFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            
            // Position at top of screen
            setGravity(Gravity.TOP)
            
            // Set window attributes for system overlay
            attributes = attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                
                // Use TYPE_APPLICATION_OVERLAY for Android O+ (shows on top of everything)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                
                format = PixelFormat.TRANSLUCENT
                
                // Don't take focus so user can interact with app below
                flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                
                // Position at top with margin
                gravity = Gravity.TOP
                y = 0 // Start from very top
            }
        }
        
        binding = ActivityTopBannerAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
        val appName = intent.getStringExtra("app_name") ?: "Unknown"
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val riskLevel = intent.getStringExtra("risk_level") ?: "SAFE"
        val riskScore = intent.getFloatExtra("risk_score", 0f)
        val packageName = intent.getStringExtra("package_name") ?: ""
        val detectedIssues = intent.getStringArrayListExtra("detected_issues") ?: ArrayList()
        val explanation = intent.getStringExtra("explanation") ?: ""
        
        setupUI(appName, title, content, riskLevel, riskScore)
        setupButtons(appName, packageName, title, content, riskLevel, riskScore, detectedIssues, explanation)
        
        // Auto-close after 10 seconds
        autoCloseHandler.postDelayed({
            finish()
        }, 10000)
    }
    
    private fun setupUI(
        appName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float
    ) {
        // Set background color and title based on risk level
        when (riskLevel) {
            "HIGH_RISK" -> {
                binding.alertContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.high_risk_red))
                binding.tvAlertTitle.text = "🚨 SPAM DETECTED"
            }
            "SUSPICIOUS" -> {
                binding.alertContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.suspicious_yellow))
                binding.tvAlertTitle.text = "⚠️ SUSPICIOUS"
                binding.tvAlertTitle.setTextColor(Color.BLACK)
                binding.tvAppName.setTextColor(Color.BLACK)
                binding.tvNotificationTitle.setTextColor(Color.BLACK)
                binding.tvNotificationContent.setTextColor(Color.BLACK)
                binding.tvRiskScore.setTextColor(Color.BLACK)
                binding.btnClose.setColorFilter(Color.BLACK)
            }
            else -> {
                binding.alertContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.safe_green))
                binding.tvAlertTitle.text = "✓ SAFE"
            }
        }
        
        // Set content
        binding.tvAppName.text = appName
        binding.tvNotificationTitle.text = title
        binding.tvNotificationContent.text = content
        binding.tvRiskScore.text = "Risk: ${(riskScore * 100).toInt()}%"
    }
    
    private fun setupButtons(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: ArrayList<String>,
        explanation: String
    ) {
        // Close button
        binding.btnClose.setOnClickListener {
            autoCloseHandler.removeCallbacksAndMessages(null)
            finish()
        }
        
        // View details button - opens DetailActivity
        binding.btnViewDetails.setOnClickListener {
            autoCloseHandler.removeCallbacksAndMessages(null)
            
            val intent = Intent(this, DetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("app_name", appName)
                putExtra("package_name", packageName)
                putExtra("title", title)
                putExtra("content", content)
                putExtra("risk_level", riskLevel)
                putExtra("risk_score", riskScore)
                putStringArrayListExtra("detected_issues", detectedIssues)
                putExtra("explanation", explanation)
            }
            startActivity(intent)
            finish()
        }
        
        // Dismiss on tap outside (on the banner itself)
        binding.alertContainer.setOnClickListener {
            // Do nothing - prevent closing when tapping the banner
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        autoCloseHandler.removeCallbacksAndMessages(null)
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
