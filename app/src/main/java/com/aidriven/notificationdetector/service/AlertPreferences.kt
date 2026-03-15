package com.aidriven.notificationdetector.service

import android.content.Context

object AlertPreferences {
    
    private const val PREFS_NAME = "alert_preferences"
    private const val KEY_REAL_TIME_ALERTS = "real_time_alerts_enabled"
    
    fun isRealTimeAlertsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REAL_TIME_ALERTS, true) // Default: enabled
    }
    
    fun setRealTimeAlertsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REAL_TIME_ALERTS, enabled).apply()
    }
}
