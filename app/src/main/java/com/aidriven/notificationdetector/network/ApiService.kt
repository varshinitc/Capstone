package com.aidriven.notificationdetector.network

import com.aidriven.notificationdetector.models.NotificationAnalysisRequest
import com.aidriven.notificationdetector.models.NotificationAnalysisResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    
    @POST("analyze")
    suspend fun analyzeNotification(
        @Body request: NotificationAnalysisRequest
    ): Response<NotificationAnalysisResponse>
    
    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>
    
    @GET("/")
    suspend fun getApiInfo(): Response<Map<String, Any>>
}
