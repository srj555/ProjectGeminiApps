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
            appId = BuildConfig.APPLICATION_ID,
            baseUrl = BuildConfig.BASE_URL,
            enableAnalytics = BuildConfig.ENABLE_ANALYTICS,
            enablePushNotifications = BuildConfig.ENABLE_PUSH_NOTIFICATIONS,
            enableDeepLinking = true,
            enableJavaScriptBridge = true,
            allowedDomains = extractDomainsFromUrl(BuildConfig.BASE_URL),
            customUserAgent = "${BuildConfig.APP_NAME}/${BuildConfig.VERSION_NAME}",
            theme = AppTheme(
                primaryColor = "#6200EE",
                splashScreenBackground = "#FFFFFF",
                statusBarColor = "#6200EE"
            )
        )
    }

    private fun extractDomainsFromUrl(url: String): List<String> {
        return try {
            val uri = android.net.Uri.parse(url)
            listOfNotNull(uri.host)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
