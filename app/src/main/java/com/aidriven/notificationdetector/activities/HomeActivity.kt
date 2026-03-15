package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.databinding.ActivityHomeBinding
import com.aidriven.notificationdetector.service.AlertPreferences
import com.aidriven.notificationdetector.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        setupUI()
        observeData()
    }
    
    private fun setupUI() {
        // Monitoring Switch
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleMonitoring(isChecked)
            val message = if (isChecked) "Monitoring enabled" else "Monitoring disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        // Real-Time Alert Switch
        binding.switchRealTimeAlert.isChecked = AlertPreferences.isRealTimeAlertsEnabled(this)
        binding.switchRealTimeAlert.setOnCheckedChangeListener { _, isChecked ->
            AlertPreferences.setRealTimeAlertsEnabled(this, isChecked)
            val message = if (isChecked) "Real-time alerts enabled" else "Real-time alerts disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        // Refresh button
        binding.btnRefresh.setOnClickListener {
            viewModel.loadDashboardData()
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeData() {
        // Today's count
        viewModel.todayCount.observe(this) { count ->
            binding.tvTodayCount.text = count.toString()
        }
        
        // Suspicious count
        viewModel.suspiciousCount.observe(this) { count ->
            binding.tvSuspiciousCount.text = count.toString()
        }
        
        // High risk count
        viewModel.highRiskCount.observe(this) { count ->
            binding.tvHighRiskCount.text = count.toString()
        }
        
        // Last notification
        viewModel.lastNotification.observe(this) { notification ->
            if (notification != null) {
                binding.tvLastAppName.text = notification.appName
                binding.tvLastTitle.text = notification.title
                binding.tvLastTime.text = formatTime(notification.timestamp)
                binding.tvLastRiskLevel.text = notification.riskLevel
                
                // Set risk level color
                val color = when (notification.riskLevel) {
                    "SAFE" -> getColor(R.color.safe_green)
                    "SUSPICIOUS" -> getColor(R.color.suspicious_yellow)
                    "HIGH_RISK" -> getColor(R.color.high_risk_red)
                    else -> getColor(R.color.black)
                }
                binding.tvLastRiskLevel.setTextColor(color)
            } else {
                binding.tvLastAppName.text = "No notifications yet"
                binding.tvLastTitle.text = ""
                binding.tvLastTime.text = ""
                binding.tvLastRiskLevel.text = ""
            }
        }
        
        // Monitoring status
        viewModel.isMonitoring.observe(this) { isEnabled ->
            binding.switchMonitoring.isChecked = isEnabled
        }
    }
    
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadDashboardData()
        viewModel.checkMonitoringStatus()
    }
}
