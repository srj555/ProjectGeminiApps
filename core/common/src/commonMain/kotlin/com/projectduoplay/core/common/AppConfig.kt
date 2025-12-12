package com.projectduoplay.core.common

import kotlinx.serialization.Serializable

/**
 * Application configuration data class
 * This is populated from flavor-specific configuration
 */
@Serializable
data class AppConfig(
    val appName: String,
    val appId: String,
    val baseUrl: String,
    val enableAnalytics: Boolean = true,
    val enablePushNotifications: Boolean = true,
    val enableDeepLinking: Boolean = true,
    val enableJavaScriptBridge: Boolean = true,
    val allowedDomains: List<String> = emptyList(),
    val customUserAgent: String? = null,
    val theme: AppTheme = AppTheme()
)

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
