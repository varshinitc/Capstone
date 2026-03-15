package com.aidriven.notificationdetector.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aidriven.notificationdetector.databinding.ActivitySettingsBinding
import com.aidriven.notificationdetector.utils.PreferencesHelper

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadSettings()
    }
    
    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // General Settings
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setMonitoringEnabled(this, isChecked)
            showToast("Monitoring ${if (isChecked) "enabled" else "disabled"}")
        }
        
        binding.switchPopupAlerts.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setPopupAlertsEnabled(this, isChecked)
        }
        
        binding.switchRunOnStartup.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setRunOnStartupEnabled(this, isChecked)
        }
        
        // Detection Settings
        binding.switchMlDetection.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setMlDetectionEnabled(this, isChecked)
        }
        
        binding.switchRuleBased.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setRuleBasedEnabled(this, isChecked)
        }
        
        binding.switchUrlAnalysis.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setUrlAnalysisEnabled(this, isChecked)
        }
        
        // Alert Customization
        binding.radioPopup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) PreferencesHelper.setAlertType(this, "popup")
        }
        
        binding.radioNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) PreferencesHelper.setAlertType(this, "notification")
        }
        
        binding.switchAlertSound.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setAlertSoundEnabled(this, isChecked)
        }
        
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setVibrationEnabled(this, isChecked)
        }
        
        // Privacy Settings
        binding.switchStoreLocally.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setStoreLocallyEnabled(this, isChecked)
        }
        
        binding.switchCloudBackup.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setCloudBackupEnabled(this, isChecked)
        }
    }
    
    private fun loadSettings() {
        // General
        binding.switchMonitoring.isChecked = PreferencesHelper.isMonitoringEnabled(this)
        binding.switchPopupAlerts.isChecked = PreferencesHelper.isPopupAlertsEnabled(this)
        binding.switchRunOnStartup.isChecked = PreferencesHelper.isRunOnStartupEnabled(this)
        
        // Detection
        binding.switchMlDetection.isChecked = PreferencesHelper.isMlDetectionEnabled(this)
        binding.switchRuleBased.isChecked = PreferencesHelper.isRuleBasedEnabled(this)
        binding.switchUrlAnalysis.isChecked = PreferencesHelper.isUrlAnalysisEnabled(this)
        
        // Alert
        val alertType = PreferencesHelper.getAlertType(this)
        binding.radioPopup.isChecked = alertType == "popup"
        binding.radioNotification.isChecked = alertType == "notification"
        binding.switchAlertSound.isChecked = PreferencesHelper.isAlertSoundEnabled(this)
        binding.switchVibration.isChecked = PreferencesHelper.isVibrationEnabled(this)
        
        // Privacy
        binding.switchStoreLocally.isChecked = PreferencesHelper.isStoreLocallyEnabled(this)
        binding.switchCloudBackup.isChecked = PreferencesHelper.isCloudBackupEnabled(this)
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
