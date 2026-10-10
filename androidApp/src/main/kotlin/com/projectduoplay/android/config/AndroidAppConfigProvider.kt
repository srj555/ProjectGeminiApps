package com.projectduoplay.android.config

import android.content.Context
import com.projectduoplay.android.BuildConfig
import com.projectduoplay.core.common.AppConfig
import com.projectduoplay.core.common.AppConfigProvider
import com.projectduoplay.core.common.AppTheme

/**
 * Android implementation of AppConfigProvider
 * Reads configuration from BuildConfig (generated from flavor)
 */
class AndroidAppConfigProvider(
    private val context: Context
) : AppConfigProvider {

    override fun getConfig(): AppConfig {
        return AppConfig(
            appName = BuildConfig.APP_NAME,
            appSubtitle = BuildConfig.APP_SUBTITLE,
            appId = BuildConfig.APPLICATION_ID,
            baseUrl = BuildConfig.BASE_URL,
            enableAnalytics = BuildConfig.ENABLE_ANALYTICS,
            enablePushNotifications = BuildConfig.ENABLE_PUSH_NOTIFICATIONS,
            enableDeepLinking = true,
            enableJavaScriptBridge = BuildConfig.ENABLE_JAVASCRIPT_BRIDGE,
            allowedOrigins = BuildConfig.ALLOWED_ORIGINS
                .split('|')
                .map(String::trim)
                .filter(String::isNotEmpty),
            customUserAgent = "${BuildConfig.APP_NAME}/${BuildConfig.VERSION_NAME}",
            theme = AppTheme(
                primaryColor = "#6200EE",
                splashScreenBackground = "#FFFFFF",
                statusBarColor = "#6200EE"
            )
        )
    }

}
