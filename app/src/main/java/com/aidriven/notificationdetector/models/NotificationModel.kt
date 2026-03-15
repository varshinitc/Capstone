package com.aidriven.notificationdetector.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val title: String,
    val content: String,
    val riskLevel: String, // "SAFE", "SUSPICIOUS", "HIGH_RISK"
    val riskScore: Float,
    val detectedIssues: String, // Store as JSON string
    val timestamp: Long,
    var userFeedback: String? = null // "MARKED_SAFE", "MARKED_PHISHING", null
)

data class RiskAnalysisResult(
    val riskLevel: String,
    val score: Float,
    val issues: List<String>,
    val confidenceScore: Float,
    val explanation: String
)
