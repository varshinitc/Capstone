package com.aidriven.notificationdetector.service

import android.content.Context
import android.content.SharedPreferences

object MonitoringManager {
    
    private const val PREFS_NAME = "notification_monitoring_prefs"
    private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
    private const val KEY_LAST_STARTED = "last_started"
    private const val KEY_NOTIFICATIONS_ANALYZED = "notifications_analyzed"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Enable notification monitoring
     */
    fun startMonitoring(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_MONITORING_ENABLED, true)
            putLong(KEY_LAST_STARTED, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Disable notification monitoring
     */
    fun stopMonitoring(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_MONITORING_ENABLED, false)
            apply()
        }
    }
    
    /**
     * Check if monitoring is currently enabled
     */
    fun isMonitoringEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_MONITORING_ENABLED, false)
    }
    
    /**
     * Get the timestamp when monitoring was last started
     */
    fun getLastStartedTime(context: Context): Long {
        return getPreferences(context).getLong(KEY_LAST_STARTED, 0)
    }
    
    /**
     * Increment the count of analyzed notifications
     */
    fun incrementAnalyzedCount(context: Context) {
        val prefs = getPreferences(context)
        val currentCount = prefs.getInt(KEY_NOTIFICATIONS_ANALYZED, 0)
        prefs.edit().putInt(KEY_NOTIFICATIONS_ANALYZED, currentCount + 1).apply()
    }
    
    /**
     * Get the total count of analyzed notifications
     */
    fun getAnalyzedCount(context: Context): Int {
        return getPreferences(context).getInt(KEY_NOTIFICATIONS_ANALYZED, 0)
    }
    
    /**
     * Reset all monitoring statistics
     */
    fun resetStatistics(context: Context) {
        getPreferences(context).edit().apply {
            putInt(KEY_NOTIFICATIONS_ANALYZED, 0)
            putLong(KEY_LAST_STARTED, 0)
            apply()
        }
    }
    
    /**
     * Get monitoring status summary
     */
    fun getMonitoringStatus(context: Context): MonitoringStatus {
        val prefs = getPreferences(context)
        return MonitoringStatus(
            isEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false),
            lastStarted = prefs.getLong(KEY_LAST_STARTED, 0),
            notificationsAnalyzed = prefs.getInt(KEY_NOTIFICATIONS_ANALYZED, 0)
        )
    }
}

/**
 * Data class to hold monitoring status information
 */
data class MonitoringStatus(
    val isEnabled: Boolean,
    val lastStarted: Long,
    val notificationsAnalyzed: Int
)
