package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidriven.notificationdetector.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAboutBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Version info
        binding.tvVersion.text = "Version 1.0 (1)"
        
        // Developer info
        binding.tvDeveloper.text = "Developed by: AI Security Team"
        
        // Disclaimer
        binding.tvDisclaimer.text = """
            ⚠️ DISCLAIMER
            
            This app assists users in identifying potentially harmful notifications but does not guarantee 100% threat prevention. 
            
            Users should exercise caution and verify suspicious content independently. The developers are not liable for any damages resulting from the use of this application.
            
            This is an educational project demonstrating AI-powered spam detection.
        """.trimIndent()
        
        // Contact button
        binding.btnContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@ainotificationdetector.com")
                putExtra(Intent.EXTRA_SUBJECT, "AI Notification Detector Feedback")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
        
        // Model Info button
        binding.btnModelInfo.setOnClickListener {
            startActivity(Intent(this, ModelInfoActivity::class.java))
        }
        
        // Feedback button
        binding.btnFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
    }
}
