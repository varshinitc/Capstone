package com.aidriven.notificationdetector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.activities.AlertPopupActivity
import com.aidriven.notificationdetector.service.OverlayBannerService
import com.aidriven.notificationdetector.database.AppDatabase
import com.aidriven.notificationdetector.repository.ApiRepository
import com.aidriven.notificationdetector.ml.OfflineSpamDetector
import com.aidriven.notificationdetector.models.NotificationModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {
    
    private val TAG = "NotificationListener"
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val CHANNEL_ID = "spam_alert_channel"
    private val NOTIFICATION_ID = 1001
    
    private lateinit var apiRepository: ApiRepository
    private lateinit var notificationManager: NotificationManager
    private val processedNotifications = mutableSetOf<String>()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListener service created")
        
        // Initialize repository
        val database = AppDatabase.getDatabase(applicationContext)
        apiRepository = ApiRepository(notificationDao = database.notificationDao())
        
        // Initialize notification manager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Spam Alert Notifications",
                NotificationManager.IMPORTANCE_MAX  // Changed to MAX for heads-up
            ).apply {
                description = "Critical alerts for spam and phishing notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                setBypassDnd(true)
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created with MAX importance")
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        // Log ALL notifications that arrive
        Log.d(TAG, "📱 Notification arrived from: ${sbn.packageName}")
        
        // Check if monitoring is enabled
        if (!MonitoringManager.isMonitoringEnabled(this)) {
            Log.d(TAG, "Monitoring is disabled, ignoring notification")
            return
        }
        
        try {
            // Extract notification details
            val packageName = sbn.packageName
            val notification = sbn.notification
            
            // Skip system notifications and our own app
            if (shouldSkipNotification(packageName)) {
                Log.d(TAG, "Skipping notification from: $packageName")
                return
            }
            
            // Get app name
            val appName = getAppName(packageName)
            
            // Extract title and content
            val extras = notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            
            // Use big text if available, otherwise use regular text
            val content = if (bigText.isNotEmpty()) bigText else text
            
            // Skip if no content
            if (title.isEmpty() && content.isEmpty()) {
                Log.d(TAG, "Skipping notification with no content")
                return
            }
            
            Log.d(TAG, "Processing notification from $appName: $title")
            
            // Analyze notification in background
            analyzeNotification(appName, packageName, title, content)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed: ${sbn.packageName}")
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListener connected")
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListener disconnected")
    }
    
    private fun analyzeNotification(
        appName: String,
        packageName: String,
        title: String,
        content: String
    ) {
        serviceScope.launch {
            try {
                Log.i(TAG, "=== ANALYZING NOTIFICATION (OFFLINE) ===")
                Log.i(TAG, "App: $appName")
                Log.i(TAG, "Package: $packageName")
                Log.i(TAG, "Title: $title")
                Log.i(TAG, "Content: $content")
                
                // Use offline spam detector
                val response = OfflineSpamDetector.analyzeNotification(
                    appName = appName,
                    title = title,
                    content = content
                )
                
                Log.i(TAG, "✓ Analysis SUCCESS: ${response.prediction} - ${response.riskLevel}")
                Log.i(TAG, "Risk Score: ${response.riskScore}")
                Log.i(TAG, "Issues: ${response.detectedIssues}")
                
                // Save to database
                saveToDatabase(appName, packageName, title, content, response)
                
                // Show alert for high risk or suspicious notifications
                if (response.riskLevel == "HIGH_RISK" || response.riskLevel == "SUSPICIOUS") {
                    Log.w(TAG, "⚠️ SHOWING ALERT for ${response.riskLevel}")
                    showRiskAlert(
                        appName = appName,
                        packageName = packageName,
                        title = title,
                        content = content,
                        riskLevel = response.riskLevel,
                        riskScore = response.riskScore,
                        detectedIssues = response.detectedIssues,
                        explanation = response.explanation
                    )
                } else {
                    Log.i(TAG, "✓ SAFE notification, no alert shown")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "✗ EXCEPTION in analyzeNotification", e)
            }
        }
    }
    
    private suspend fun saveToDatabase(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        response: com.aidriven.notificationdetector.ml.SpamDetectionResult
    ) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val notification = NotificationModel(
                appName = appName,
                packageName = packageName,
                title = title,
                content = content,
                riskLevel = response.riskLevel,
                riskScore = response.riskScore,
                detectedIssues = Gson().toJson(response.detectedIssues),
                timestamp = System.currentTimeMillis(),
                userFeedback = null
            )
            val id = database.notificationDao().insert(notification)
            Log.i(TAG, "✓ Saved to database with ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to save to database", e)
        }
    }
    
    private fun showRiskAlert(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: List<String>,
        explanation: String
    ) {
        // Check if real-time alerts are enabled
        if (!AlertPreferences.isRealTimeAlertsEnabled(applicationContext)) {
            Log.d(TAG, "Real-time alerts disabled, skipping alert")
            return
        }
        
        // Show BOTH overlay banner AND heads-up notification
        if (riskLevel == "HIGH_RISK" || riskLevel == "SUSPICIOUS") {
            // Launch overlay banner service immediately
            launchOverlayBanner(appName, packageName, title, content, riskLevel, riskScore, detectedIssues, explanation)
            
            // Also show heads-up notification
            showHeadsUpNotification(appName, title, content, riskLevel, packageName, riskScore, detectedIssues, explanation)
            
            Log.w(TAG, "ALERT SHOWN: $riskLevel - $appName: $title")
        }
    }
    
    private fun launchOverlayBanner(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: List<String>,
        explanation: String
    ) {
        try {
            Log.i(TAG, "Launching overlay banner for $appName")
            
            // Check overlay permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!android.provider.Settings.canDrawOverlays(applicationContext)) {
                    Log.e(TAG, "Overlay permission not granted!")
                    return
                }
            }
            
            // Wake up screen
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or 
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "NotificationDetector:AlertWakeLock"
            )
            wakeLock.acquire(5000)
            
            // Start overlay service
            val intent = Intent(applicationContext, OverlayBannerService::class.java).apply {
                putExtra(OverlayBannerService.EXTRA_APP_NAME, appName)
                putExtra(OverlayBannerService.EXTRA_PACKAGE_NAME, packageName)
                putExtra(OverlayBannerService.EXTRA_TITLE, title)
                putExtra(OverlayBannerService.EXTRA_CONTENT, content)
                putExtra(OverlayBannerService.EXTRA_RISK_LEVEL, riskLevel)
                putExtra(OverlayBannerService.EXTRA_RISK_SCORE, riskScore)
                putStringArrayListExtra(OverlayBannerService.EXTRA_DETECTED_ISSUES, ArrayList(detectedIssues))
                putExtra(OverlayBannerService.EXTRA_EXPLANATION, explanation)
            }
            
            startService(intent)
            Log.i(TAG, "✓ Overlay banner service started")
            
            wakeLock.release()
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to launch overlay banner", e)
        }
    }
    
    private fun showHeadsUpNotification(
        appName: String,
        title: String,
        content: String,
        riskLevel: String,
        packageName: String,
        riskScore: Float,
        detectedIssues: List<String>,
        explanation: String
    ) {
        val intent = Intent(applicationContext, com.aidriven.notificationdetector.activities.TopBannerAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("app_name", appName)
            putExtra("package_name", packageName)
            putExtra("title", title)
            putExtra("content", content)
            putExtra("risk_level", riskLevel)
            putExtra("risk_score", riskScore)
            putStringArrayListExtra("detected_issues", ArrayList(detectedIssues))
            putExtra("explanation", explanation)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (notifTitle, notifText, color) = when (riskLevel) {
            "HIGH_RISK" -> Triple(
                "🚨 SPAM DETECTED",
                "$appName: $title",
                android.graphics.Color.RED
            )
            "SUSPICIOUS" -> Triple(
                "⚠️ SUSPICIOUS MESSAGE",
                "$appName: $title",
                android.graphics.Color.rgb(255, 165, 0)
            )
            else -> Triple(
                "Alert",
                "$appName: $title",
                android.graphics.Color.GRAY
            )
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$content\n\n⚠️ Risk: ${(riskScore * 100).toInt()}%")
                .setBigContentTitle(notifTitle))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(color)
            .setColorized(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        
        Log.i(TAG, "✓ Notification shown: ID=$notificationId")
    }
    
    private fun showDialogActivity(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: List<String>,
        explanation: String
    ) {
        val intent = Intent(applicationContext, com.aidriven.notificationdetector.activities.TopBannerAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra("app_name", appName)
            putExtra("package_name", packageName)
            putExtra("title", title)
            putExtra("content", content)
            putExtra("risk_level", riskLevel)
            putExtra("risk_score", riskScore)
            putStringArrayListExtra("detected_issues", ArrayList(detectedIssues))
            putExtra("explanation", explanation)
        }
        startActivity(intent)
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = applicationContext.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
    private fun shouldSkipNotification(packageName: String): Boolean {
        // Skip our own app
        if (packageName == applicationContext.packageName) {
            return true
        }
        
        // Only skip system UI notifications
        val skipPackages = listOf(
            "com.android.systemui",
            "com.android.providers"
        )
        
        return skipPackages.any { packageName.startsWith(it) }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "NotificationListener service destroyed")
    }
}
