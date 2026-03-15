package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.aidriven.notificationdetector.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DELAY = 2000L // 2 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check permission after delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkPermissionAndNavigate()
        }, SPLASH_DELAY)
    }
    
    private fun checkPermissionAndNavigate() {
        if (isNotificationAccessGranted()) {
            // Permission granted, go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // Permission not granted, go to Permission screen
            startActivity(Intent(this, PermissionActivity::class.java))
        }
        finish()
    }
    
    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledListeners.contains(packageName)
    }
}
