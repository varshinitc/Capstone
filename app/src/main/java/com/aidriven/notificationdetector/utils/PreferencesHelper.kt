package com.aidriven.notificationdetector.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
    
    private const val PREFS_NAME = "app_preferences"
    
    // Keys
    private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
    private const val KEY_POPUP_ALERTS = "popup_alerts"
    private const val KEY_RUN_ON_STARTUP = "run_on_startup"
    private const val KEY_ML_DETECTION = "ml_detection"
    private const val KEY_RULE_BASED = "rule_based"
    private const val KEY_URL_ANALYSIS = "url_analysis"
    private const val KEY_ALERT_TYPE = "alert_type"
    private const val KEY_ALERT_SOUND = "alert_sound"
    private const val KEY_VIBRATION = "vibration"
    private const val KEY_STORE_LOCALLY = "store_locally"
    private const val KEY_CLOUD_BACKUP = "cloud_backup"
    private const val KEY_FEEDBACK_COUNT = "feedback_count"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // General Settings
    fun isMonitoringEnabled(context: Context) = getPrefs(context).getBoolean(KEY_MONITORING_ENABLED, false)
    fun setMonitoringEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply()
    
    fun isPopupAlertsEnabled(context: Context) = getPrefs(context).getBoolean(KEY_POPUP_ALERTS, true)
    fun setPopupAlertsEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_POPUP_ALERTS, enabled).apply()
    
    fun isRunOnStartupEnabled(context: Context) = getPrefs(context).getBoolean(KEY_RUN_ON_STARTUP, false)
    fun setRunOnStartupEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_RUN_ON_STARTUP, enabled).apply()
    
    // Detection Settings
    fun isMlDetectionEnabled(context: Context) = getPrefs(context).getBoolean(KEY_ML_DETECTION, true)
    fun setMlDetectionEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_ML_DETECTION, enabled).apply()
    
    fun isRuleBasedEnabled(context: Context) = getPrefs(context).getBoolean(KEY_RULE_BASED, true)
    fun setRuleBasedEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_RULE_BASED, enabled).apply()
    
    fun isUrlAnalysisEnabled(context: Context) = getPrefs(context).getBoolean(KEY_URL_ANALYSIS, true)
    fun setUrlAnalysisEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_URL_ANALYSIS, enabled).apply()
    
    // Alert Customization
    fun getAlertType(context: Context) = getPrefs(context).getString(KEY_ALERT_TYPE, "popup") ?: "popup"
    fun setAlertType(context: Context, type: String) = 
        getPrefs(context).edit().putString(KEY_ALERT_TYPE, type).apply()
    
    fun isAlertSoundEnabled(context: Context) = getPrefs(context).getBoolean(KEY_ALERT_SOUND, true)
    fun setAlertSoundEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_ALERT_SOUND, enabled).apply()
    
    fun isVibrationEnabled(context: Context) = getPrefs(context).getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_VIBRATION, enabled).apply()
    
    // Privacy Settings
    fun isStoreLocallyEnabled(context: Context) = getPrefs(context).getBoolean(KEY_STORE_LOCALLY, true)
    fun setStoreLocallyEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_STORE_LOCALLY, enabled).apply()
    
    fun isCloudBackupEnabled(context: Context) = getPrefs(context).getBoolean(KEY_CLOUD_BACKUP, false)
    fun setCloudBackupEnabled(context: Context, enabled: Boolean) = 
        getPrefs(context).edit().putBoolean(KEY_CLOUD_BACKUP, enabled).apply()
    
    // Feedback
    fun getFeedbackCount(context: Context) = getPrefs(context).getInt(KEY_FEEDBACK_COUNT, 0)
    fun incrementFeedbackCount(context: Context) {
        val current = getFeedbackCount(context)
        getPrefs(context).edit().putInt(KEY_FEEDBACK_COUNT, current + 1).apply()
    }
    fun resetFeedbackCount(context: Context) = 
        getPrefs(context).edit().putInt(KEY_FEEDBACK_COUNT, 0).apply()
}
