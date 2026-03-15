package com.aidriven.notificationdetector.repository

import android.content.Context
import com.aidriven.notificationdetector.database.NotificationDao
import com.aidriven.notificationdetector.models.NotificationAnalysisRequest
import com.aidriven.notificationdetector.models.NotificationAnalysisResponse
import com.aidriven.notificationdetector.models.NotificationModel
import com.aidriven.notificationdetector.network.ApiService
import com.aidriven.notificationdetector.network.NetworkUtils
import com.aidriven.notificationdetector.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ApiRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val notificationDao: NotificationDao
) {
    
    /**
     * Analyze notification and save to database
     */
    suspend fun analyzeNotification(
        context: Context,
        appName: String,
        packageName: String,
        title: String,
        content: String
    ): Result<NotificationAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            // Check internet connectivity
            if (!NetworkUtils.isInternetAvailable(context)) {
                return@withContext Result.failure(Exception("No internet connection"))
            }
            
            // Create request
            val request = NotificationAnalysisRequest(
                message = content,
                appName = appName,
                title = title,
                packageName = packageName,
                timestamp = System.currentTimeMillis()
            )
            
            // Make API call
            val response = apiService.analyzeNotification(request)
            
            if (response.isSuccessful && response.body() != null) {
                val analysisResponse = response.body()!!
                
                // Save to database
                saveNotificationToDatabase(
                    appName = appName,
                    packageName = packageName,
                    title = title,
                    content = content,
                    response = analysisResponse
                )
                
                Result.success(analysisResponse)
            } else {
                Result.failure(Exception("Server error: ${response.code()} - ${response.message()}"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save notification to local database
     */
    private suspend fun saveNotificationToDatabase(
        appName: String,
        packageName: String,
        title: String,
        content: String,
        response: NotificationAnalysisResponse
    ) {
        try {
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
            
            val id = notificationDao.insert(notification)
            android.util.Log.i("ApiRepository", "✓ Saved to database with ID: $id")
        } catch (e: Exception) {
            android.util.Log.e("ApiRepository", "✗ Failed to save to database", e)
            throw e
        }
    }
    
    /**
     * Check if server is reachable
     */
    suspend fun checkServerHealth(context: Context): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isInternetAvailable(context)) {
                return@withContext Result.failure(Exception("No internet connection"))
            }
            
            val response = apiService.checkHealth()
            
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Server not responding: ${response.code()}"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get API information
     */
    suspend fun getApiInfo(context: Context): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isInternetAvailable(context)) {
                return@withContext Result.failure(Exception("No internet connection"))
            }
            
            val response = apiService.getApiInfo()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get API info"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user feedback for a notification
     */
    suspend fun updateUserFeedback(
        notificationId: Long,
        feedback: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notification = notificationDao.getAllNotifications()
            // Update logic would go here
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get statistics from database
     */
    suspend fun getStatistics(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val todayCount = notificationDao.getTodayCount()
            val highRiskCount = notificationDao.getHighRiskCount()
            val suspiciousCount = notificationDao.getSuspiciousCount()
            
            val stats = mapOf(
                "today" to todayCount,
                "high_risk" to highRiskCount,
                "suspicious" to suspiciousCount
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
