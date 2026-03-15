package com.aidriven.notificationdetector.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidriven.notificationdetector.databinding.ActivityModelInfoBinding

class ModelInfoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityModelInfoBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Set model information
        binding.tvAlgorithm.text = "Naïve Bayes (Multinomial)"
        binding.tvFeatureExtraction.text = "TF-IDF with N-grams (1-2)"
        binding.tvDetectionMethod.text = "Hybrid ML + Rule-Based"
        binding.tvProcessingType.text = "Cloud API (Flask Backend)"
        binding.tvDatasetInfo.text = "Trained on 40+ SMS, Email, YouTube, and Notification samples"
        binding.tvAccuracy.text = "87.5%"
        
        binding.tvExplanation.text = """
            Naïve Bayes performs exceptionally well for short text classification tasks like spam detection. 
            
            The model uses:
            • TF-IDF vectorization for feature extraction
            • N-gram analysis (1-2 words) for context
            • Stemming and stop word removal
            • URL detection and flagging
            • Spam keyword identification
            
            Combined with rule-based detection, this hybrid approach provides robust protection against:
            • Phishing attempts
            • Spam notifications
            • Malicious links
            • Social engineering attacks
        """.trimIndent()
    }
}
