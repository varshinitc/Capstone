package com.aidriven.notificationdetector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aidriven.notificationdetector.database.AppDatabase
import com.aidriven.notificationdetector.database.NotificationDao
import com.aidriven.notificationdetector.models.NotificationModel
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val notificationDao: NotificationDao = AppDatabase.getDatabase(application).notificationDao()
    
    val allNotifications: LiveData<List<NotificationModel>> = 
        notificationDao.getAllNotifications().asLiveData()
    
    private val _filteredNotifications = MutableLiveData<List<NotificationModel>>()
    val filteredNotifications: LiveData<List<NotificationModel>> = _filteredNotifications
    
    private var currentFilter = "ALL"
    
    fun filterNotifications(filter: String) {
        currentFilter = filter
        viewModelScope.launch {
            when (filter) {
                "ALL" -> {
                    // Observe all notifications
                }
                "SAFE" -> {
                    notificationDao.getNotificationsByRisk("SAFE").collect { list ->
                        _filteredNotifications.value = list
                    }
                }
                "SUSPICIOUS" -> {
                    notificationDao.getNotificationsByRisk("SUSPICIOUS").collect { list ->
                        _filteredNotifications.value = list
                    }
                }
                "HIGH_RISK" -> {
                    notificationDao.getNotificationsByRisk("HIGH_RISK").collect { list ->
                        _filteredNotifications.value = list
                    }
                }
            }
        }
    }
    
    fun updateUserFeedback(notificationId: Long, feedback: String) {
        viewModelScope.launch {
            try {
                // Get notification and update feedback
                // This would require updating the DAO to support this operation
                // For now, we'll just log it
                android.util.Log.d("HistoryViewModel", "Feedback updated: $notificationId -> $feedback")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getCurrentFilter(): String = currentFilter
    
    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                notificationDao.deleteAll()
                android.util.Log.d("HistoryViewModel", "All notifications cleared")
            } catch (e: Exception) {
                android.util.Log.e("HistoryViewModel", "Error clearing notifications", e)
            }
        }
    }
}
