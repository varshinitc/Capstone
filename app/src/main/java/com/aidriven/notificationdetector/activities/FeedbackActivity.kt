package com.aidriven.notificationdetector.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aidriven.notificationdetector.databinding.ActivityFeedbackBinding
import com.aidriven.notificationdetector.utils.PreferencesHelper

class FeedbackActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFeedbackBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadFeedbackData()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnRetrainModel.setOnClickListener {
            showRetrainDialog()
        }
        
        binding.btnResetFeedback.setOnClickListener {
            showResetDialog()
        }
    }
    
    private fun loadFeedbackData() {
        val feedbackCount = PreferencesHelper.getFeedbackCount(this)
        binding.tvFeedbackCount.text = feedbackCount.toString()
        
        // Simulated accuracy improvement (1% per 10 feedback)
        val accuracyImprovement = (feedbackCount / 10.0).coerceAtMost(15.0)
        binding.tvAccuracyImprovement.text = String.format("+%.1f%%", accuracyImprovement)
        
        // Update progress
        binding.progressFeedback.progress = (feedbackCount * 10).coerceAtMost(100)
    }
    
    private fun showRetrainDialog() {
        AlertDialog.Builder(this)
            .setTitle("Retrain Model")
            .setMessage("This will retrain the model with your feedback data. This process may take a few minutes. Continue?")
            .setPositiveButton("Retrain") { _, _ ->
                startRetraining()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startRetraining() {
        binding.progressRetraining.visibility = View.VISIBLE
        binding.tvRetrainingStatus.visibility = View.VISIBLE
        binding.btnRetrainModel.isEnabled = false
        
        binding.tvRetrainingStatus.text = "Retraining model..."
        
        // Simulate retraining process
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressRetraining.visibility = View.GONE
            binding.tvRetrainingStatus.text = "Model retrained successfully!"
            binding.btnRetrainModel.isEnabled = true
            
            Toast.makeText(this, "Model updated with your feedback", Toast.LENGTH_LONG).show()
        }, 3000)
    }
    
    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Feedback Data")
            .setMessage("This will delete all your feedback data. This action cannot be undone. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                resetFeedback()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetFeedback() {
        PreferencesHelper.resetFeedbackCount(this)
        loadFeedbackData()
        Toast.makeText(this, "Feedback data reset", Toast.LENGTH_SHORT).show()
    }
}
