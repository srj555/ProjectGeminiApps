package com.projectduoplay.core.common

import kotlinx.serialization.Serializable

/**
 * Application configuration data class
 * This is populated from flavor-specific configuration
 */
@Serializable
data class AppConfig(
    val appName: String,
    val appSubtitle: String = "",
    val appId: String,
    val mode: AppMode = AppMode.FIXED,
    val baseUrl: String,
    val enableAnalytics: Boolean = true,
    val enablePushNotifications: Boolean = true,
    val enableDeepLinking: Boolean = true,
    val enableJavaScriptBridge: Boolean = false,
    val allowedOrigins: List<String>,
    val customUserAgent: String? = null,
    val theme: AppTheme = AppTheme()
)

@Serializable
enum class AppMode {
    FIXED,
    LAUNCHER
}

@Serializable
data class AppTheme(
    val primaryColor: String = "#6200EE",
    val splashScreenBackground: String = "#FFFFFF",
    val statusBarColor: String = "#6200EE"
)

/**
 * Interface for providing app configuration
 * Platform-specific implementations will provide flavor-specific configs
 */
interface AppConfigProvider {
    fun getConfig(): AppConfig
}
