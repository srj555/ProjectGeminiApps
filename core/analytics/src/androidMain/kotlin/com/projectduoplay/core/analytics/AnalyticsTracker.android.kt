package com.projectduoplay.core.analytics

import android.util.Log

/**
 * Android analytics implementation
 * In production, this would integrate with Firebase Analytics
 */
actual class PlatformAnalyticsTracker : AnalyticsTracker {
    private val TAG = "Analytics"

    override fun logEvent(event: AnalyticsEvent) {
        logEvent(event.name, event.parameters)
    }

    override fun logEvent(name: String, parameters: Map<String, Any>) {
        // TODO: Implement Firebase Analytics
        Log.d(TAG, "Event: $name, Parameters: $parameters")
    }

    override fun setUserId(userId: String?) {
        // TODO: Implement Firebase Analytics
        Log.d(TAG, "Set User ID: $userId")
    }

    override fun setUserProperty(name: String, value: String) {
        // TODO: Implement Firebase Analytics
        Log.d(TAG, "Set User Property: $name = $value")
    }
}
