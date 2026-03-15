package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.aidriven.notificationdetector.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPermissionBinding
    private var notificationPermissionGranted = false
    private var overlayPermissionGranted = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        // Grant notification permission button
        binding.btnGrantPermission.setOnClickListener {
            openNotificationSettings()
        }
        
        // Grant overlay permission button
        binding.btnGrantOverlay.setOnClickListener {
            openOverlaySettings()
        }
        
        // Skip button
        binding.btnSkip.setOnClickListener {
            binding.tvWarning.visibility = View.VISIBLE
            Toast.makeText(this, "App needs both permissions to work!", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
    
    private fun checkPermissions() {
        notificationPermissionGranted = isNotificationAccessGranted()
        overlayPermissionGranted = canDrawOverlays()
        
        // Update UI based on permissions
        if (notificationPermissionGranted) {
            binding.btnGrantPermission.text = "✓ Notification Access Granted"
            binding.btnGrantPermission.isEnabled = false
            binding.btnGrantOverlay.visibility = View.VISIBLE
        }
        
        if (overlayPermissionGranted) {
            binding.btnGrantOverlay.text = "✓ Display Permission Granted"
            binding.btnGrantOverlay.isEnabled = false
        }
        
        // If both granted, navigate to home
        if (notificationPermissionGranted && overlayPermissionGranted) {
            Toast.makeText(this, "All permissions granted! Starting app...", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }
    
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Enable 'Notification Detector' in the list", Toast.LENGTH_LONG).show()
    }
    
    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Enable 'Display over other apps'", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
    
    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledListeners.contains(packageName)
    }
    
    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
