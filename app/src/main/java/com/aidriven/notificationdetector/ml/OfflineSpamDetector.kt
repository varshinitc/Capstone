package com.aidriven.notificationdetector.ml

import android.util.Log

/**
 * Offline spam detector using rule-based pattern matching
 * No backend server or ML model required
 */
object OfflineSpamDetector {
    
    private const val TAG = "OfflineSpamDetector"
    
    // Spam keywords by category
    private val financialScamKeywords = listOf(
        "bank", "account", "suspended", "verify", "payment", "credit card",
        "debit card", "transaction", "fraud", "unauthorized", "confirm identity"
    )
    
    private val urgencyKeywords = listOf(
        "urgent", "immediately", "now", "act fast", "expires", "limited time",
        "hurry", "quick", "asap", "today only", "last chance"
    )
    
    private val prizeScamKeywords = listOf(
        "won", "winner", "prize", "congratulations", "claim", "lottery",
        "jackpot", "reward", "free", "gift", "bonus"
    )
    
    private val phishingKeywords = listOf(
        "click here", "verify now", "confirm now", "update now", "login",
        "reset password", "security alert", "unusual activity"
    )
    
    private val spamKeywords = listOf(
        "click", "offer", "deal", "discount", "sale", "buy now",
        "limited offer", "exclusive", "special offer"
    )
    
    // Suspicious URL patterns
    private val suspiciousUrlPatterns = listOf(
        "bit.ly", "tinyurl", "goo.gl", "ow.ly", "t.co",
        "http://", "https://", ".tk", ".ml", ".ga", ".cf"
    )
    
    /**
     * Analyze notification content and return spam detection result
     */
    fun analyzeNotification(
        appName: String,
        title: String,
        content: String
    ): SpamDetectionResult {
        val fullText = "$title $content".lowercase()
        
        var riskScore = 0.0
        val detectedIssues = mutableListOf<String>()
        
        // Check for financial scam indicators
        val financialMatches = financialScamKeywords.filter { fullText.contains(it) }
        if (financialMatches.isNotEmpty()) {
            riskScore += 0.3
            detectedIssues.add("Financial scam indicators: ${financialMatches.joinToString(", ")}")
        }
        
        // Check for urgency tactics
        val urgencyMatches = urgencyKeywords.filter { fullText.contains(it) }
        if (urgencyMatches.isNotEmpty()) {
            riskScore += 0.2
            detectedIssues.add("Urgency tactics: ${urgencyMatches.joinToString(", ")}")
        }
        
        // Check for prize/lottery scams
        val prizeMatches = prizeScamKeywords.filter { fullText.contains(it) }
        if (prizeMatches.isNotEmpty()) {
            riskScore += 0.25
            detectedIssues.add("Prize/lottery scam: ${prizeMatches.joinToString(", ")}")
        }
        
        // Check for phishing patterns
        val phishingMatches = phishingKeywords.filter { fullText.contains(it) }
        if (phishingMatches.isNotEmpty()) {
            riskScore += 0.2
            detectedIssues.add("Phishing patterns: ${phishingMatches.joinToString(", ")}")
        }
        
        // Check for spam keywords
        val spamMatches = spamKeywords.filter { fullText.contains(it) }
        if (spamMatches.isNotEmpty()) {
            riskScore += 0.15
            detectedIssues.add("Spam keywords: ${spamMatches.joinToString(", ")}")
        }
        
        // Check for suspicious URLs
        val urlMatches = suspiciousUrlPatterns.filter { fullText.contains(it) }
        if (urlMatches.isNotEmpty()) {
            riskScore += 0.2
            detectedIssues.add("Contains suspicious URL")
        }
        
        // Check for multiple exclamation marks or all caps
        if (fullText.count { it == '!' } >= 3) {
            riskScore += 0.1
            detectedIssues.add("Excessive punctuation")
        }
        
        if (title.count { it.isUpperCase() } > title.length * 0.7 && title.length > 5) {
            riskScore += 0.1
            detectedIssues.add("Excessive capitalization")
        }
        
        // Determine risk level
        val riskLevel = when {
            riskScore >= 0.6 -> "HIGH_RISK"
            riskScore >= 0.3 -> "SUSPICIOUS"
            else -> "SAFE"
        }
        
        val prediction = if (riskScore >= 0.3) "spam" else "ham"
        
        val explanation = when {
            riskScore >= 0.6 -> "High confidence spam detection. Multiple spam indicators found."
            riskScore >= 0.3 -> "Moderate confidence spam detection. Some suspicious patterns detected."
            else -> "No significant spam indicators detected."
        }
        
        Log.d(TAG, "Analysis: $appName - $riskLevel (score: $riskScore)")
        
        return SpamDetectionResult(
            prediction = prediction,
            riskLevel = riskLevel,
            riskScore = riskScore.toFloat(),
            detectedIssues = detectedIssues,
            explanation = explanation,
            appName = appName,
            packageName = "",
            success = true
        )
    }
}

/**
 * Result of spam detection analysis
 */
data class SpamDetectionResult(
    val prediction: String,
    val riskLevel: String,
    val riskScore: Float,
    val detectedIssues: List<String>,
    val explanation: String,
    val appName: String,
    val packageName: String,
    val success: Boolean
)
