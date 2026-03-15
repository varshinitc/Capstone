package com.aidriven.notificationdetector.database

import androidx.room.*
import com.aidriven.notificationdetector.models.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationModel): Long

    @Update
    suspend fun update(notification: NotificationModel)

    @Delete
    suspend fun delete(notification: NotificationModel)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationModel>>

    @Query("SELECT * FROM notifications WHERE riskLevel = :riskLevel ORDER BY timestamp DESC")
    fun getNotificationsByRisk(riskLevel: String): Flow<List<NotificationModel>>

    @Query("SELECT COUNT(*) FROM notifications WHERE date(timestamp/1000, 'unixepoch') = date('now')")
    suspend fun getTodayCount(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE riskLevel = 'HIGH_RISK'")
    suspend fun getHighRiskCount(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE riskLevel = 'SUSPICIOUS'")
    suspend fun getSuspiciousCount(): Int

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastNotification(): NotificationModel?
    
    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
