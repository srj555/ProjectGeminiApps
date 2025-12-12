package com.projectduoplay.core.analytics

import platform.Foundation.NSLog

/**
 * iOS analytics implementation
 * In production, this would integrate with Firebase Analytics
 */
actual class PlatformAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) {
        logEvent(event.name, event.parameters)
    }

    override fun logEvent(name: String, parameters: Map<String, Any>) {
        // TODO: Implement Firebase Analytics
        NSLog("Analytics Event: $name, Parameters: $parameters")
    }

    override fun setUserId(userId: String?) {
        // TODO: Implement Firebase Analytics
        NSLog("Analytics Set User ID: $userId")
    }

    override fun setUserProperty(name: String, value: String) {
        // TODO: Implement Firebase Analytics
        NSLog("Analytics Set User Property: $name = $value")
    }
}
