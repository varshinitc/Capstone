package com.aidriven.notificationdetector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aidriven.notificationdetector.database.AppDatabase
import com.aidriven.notificationdetector.database.NotificationDao
import com.aidriven.notificationdetector.models.NotificationModel
import com.aidriven.notificationdetector.service.MonitoringManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val notificationDao: NotificationDao = AppDatabase.getDatabase(application).notificationDao()
    
    private val _todayCount = MutableLiveData<Int>()
    val todayCount: LiveData<Int> = _todayCount
    
    private val _suspiciousCount = MutableLiveData<Int>()
    val suspiciousCount: LiveData<Int> = _suspiciousCount
    
    private val _highRiskCount = MutableLiveData<Int>()
    val highRiskCount: LiveData<Int> = _highRiskCount
    
    private val _lastNotification = MutableLiveData<NotificationModel?>()
    val lastNotification: LiveData<NotificationModel?> = _lastNotification
    
    private val _isMonitoring = MutableLiveData<Boolean>()
    val isMonitoring: LiveData<Boolean> = _isMonitoring
    
    init {
        loadDashboardData()
        checkMonitoringStatus()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _todayCount.value = notificationDao.getTodayCount()
                _suspiciousCount.value = notificationDao.getSuspiciousCount()
                _highRiskCount.value = notificationDao.getHighRiskCount()
                _lastNotification.value = notificationDao.getLastNotification()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun checkMonitoringStatus() {
        _isMonitoring.value = MonitoringManager.isMonitoringEnabled(getApplication())
    }
    
    fun toggleMonitoring(enabled: Boolean) {
        if (enabled) {
            MonitoringManager.startMonitoring(getApplication())
        } else {
            MonitoringManager.stopMonitoring(getApplication())
        }
        _isMonitoring.value = enabled
    }
    
    fun getAnalyzedCount(): Int {
        return MonitoringManager.getAnalyzedCount(getApplication())
    }
}
