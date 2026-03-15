package com.aidriven.notificationdetector.models

import com.google.gson.annotations.SerializedName

data class NotificationAnalysisRequest(
    @SerializedName("message") val message: String,
    @SerializedName("app_name") val appName: String,
    @SerializedName("title") val title: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("timestamp") val timestamp: Long
)

data class NotificationAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("prediction") val prediction: String, // "spam" or "ham"
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("risk_score") val riskScore: Float,
    @SerializedName("risk_level") val riskLevel: String, // "SAFE", "SUSPICIOUS", "HIGH_RISK"
    @SerializedName("detected_issues") val detectedIssues: List<String>,
    @SerializedName("explanation") val explanation: String
)
