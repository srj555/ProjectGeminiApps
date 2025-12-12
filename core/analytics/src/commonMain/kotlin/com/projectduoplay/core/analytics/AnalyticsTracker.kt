package com.projectduoplay.core.analytics

/**
 * Analytics event data class
 */
data class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, Any> = emptyMap()
)

/**
 * Analytics tracker interface
 */
interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun logEvent(name: String, parameters: Map<String, Any> = emptyMap())
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String)
}

/**
 * Platform-specific analytics implementation
 */
expect class PlatformAnalyticsTracker : AnalyticsTracker

/**
 * Analytics helper for common events
 */
object Analytics {
    private lateinit var tracker: AnalyticsTracker

    fun initialize(analyticsTracker: AnalyticsTracker) {
        tracker = analyticsTracker
    }

    fun logScreenView(screenName: String) {
        if (::tracker.isInitialized) {
            tracker.logEvent("screen_view", mapOf("screen_name" to screenName))
        }
    }

    fun logWebViewLoad(url: String) {
        if (::tracker.isInitialized) {
            tracker.logEvent("webview_load", mapOf("url" to url))
        }
    }

    fun logWebViewError(url: String, errorCode: Int) {
        if (::tracker.isInitialized) {
            tracker.logEvent(
                "webview_error",
                mapOf("url" to url, "error_code" to errorCode)
            )
        }
    }

    fun logDeepLink(url: String) {
        if (::tracker.isInitialized) {
            tracker.logEvent("deep_link_opened", mapOf("url" to url))
        }
    }

    fun logPushNotification(title: String, action: String) {
        if (::tracker.isInitialized) {
            tracker.logEvent(
                "push_notification",
                mapOf("title" to title, "action" to action)
            )
        }
    }
}
