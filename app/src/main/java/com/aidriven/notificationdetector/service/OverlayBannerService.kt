package com.aidriven.notificationdetector.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.activities.DetailActivity

class OverlayBannerService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val autoCloseHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "OverlayBannerService"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_RISK_LEVEL = "risk_level"
        const val EXTRA_RISK_SCORE = "risk_score"
        const val EXTRA_DETECTED_ISSUES = "detected_issues"
        const val EXTRA_EXPLANATION = "explanation"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.e(TAG, "Overlay permission not granted!")
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Get data from intent
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Unknown"
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val riskLevel = intent.getStringExtra(EXTRA_RISK_LEVEL) ?: "SAFE"
        val riskScore = intent.getFloatExtra(EXTRA_RISK_SCORE, 0f)
        val detectedIssues = intent.getStringArrayListExtra(EXTRA_DETECTED_ISSUES) ?: ArrayList()
        val explanation = intent.getStringExtra(EXTRA_EXPLANATION) ?: ""

        showOverlay(appName, packageName, title, content, riskLevel, riskScore, detectedIssues, explanation)

        return START_NOT_STICKY
    }

    private fun showOverlay(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: ArrayList<String>,
        explanation: String
    ) {
        try {
            // Remove existing overlay if any
            removeOverlay()

            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            // Inflate the overlay layout
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_banner, null)

            // Configure window parameters
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP
            params.x = 0
            params.y = 0

            // Setup UI
            setupOverlayUI(overlayView!!, appName, title, content, riskLevel, riskScore)
            setupOverlayButtons(overlayView!!, appName, packageName, title, content, riskLevel, riskScore, detectedIssues, explanation)

            // Add view to window
            windowManager?.addView(overlayView, params)

            Log.i(TAG, "✓ Overlay banner shown on top of screen")

            // Auto-close after 10 seconds
            autoCloseHandler.postDelayed({
                removeOverlay()
                stopSelf()
            }, 10000)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay", e)
            stopSelf()
        }
    }

    private fun setupOverlayUI(
        view: View,
        appName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float
    ) {
        val alertContainer = view.findViewById<LinearLayout>(R.id.alertContainer)
        val tvAlertTitle = view.findViewById<TextView>(R.id.tvAlertTitle)
        val tvAppName = view.findViewById<TextView>(R.id.tvAppName)
        val tvNotificationTitle = view.findViewById<TextView>(R.id.tvNotificationTitle)
        val tvNotificationContent = view.findViewById<TextView>(R.id.tvNotificationContent)
        val tvRiskScore = view.findViewById<TextView>(R.id.tvRiskScore)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)

        // Set background color and title based on risk level
        when (riskLevel) {
            "HIGH_RISK" -> {
                alertContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.high_risk_red))
                tvAlertTitle.text = "🚨 SPAM DETECTED"
            }
            "SUSPICIOUS" -> {
                alertContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.suspicious_yellow))
                tvAlertTitle.text = "⚠️ SUSPICIOUS"
                tvAlertTitle.setTextColor(Color.BLACK)
                tvAppName.setTextColor(Color.BLACK)
                tvNotificationTitle.setTextColor(Color.BLACK)
                tvNotificationContent.setTextColor(Color.BLACK)
                tvRiskScore.setTextColor(Color.BLACK)
                btnClose.setColorFilter(Color.BLACK)
            }
            else -> {
                alertContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.safe_green))
                tvAlertTitle.text = "✓ SAFE"
            }
        }

        // Set content
        tvAppName.text = appName
        tvNotificationTitle.text = title
        tvNotificationContent.text = content
        tvRiskScore.text = "Risk: ${(riskScore * 100).toInt()}%"
    }

    private fun setupOverlayButtons(
        view: View,
        appName: String,
        packageName: String,
        title: String,
        content: String,
        riskLevel: String,
        riskScore: Float,
        detectedIssues: ArrayList<String>,
        explanation: String
    ) {
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val btnViewDetails = view.findViewById<Button>(R.id.btnViewDetails)

        btnClose.setOnClickListener {
            autoCloseHandler.removeCallbacksAndMessages(null)
            removeOverlay()
            stopSelf()
        }

        btnViewDetails.setOnClickListener {
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

            removeOverlay()
            stopSelf()
        }
    }

    private fun removeOverlay() {
        try {
            if (overlayView != null && windowManager != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        autoCloseHandler.removeCallbacksAndMessages(null)
        removeOverlay()
        Log.d(TAG, "Overlay service destroyed")
    }
}
