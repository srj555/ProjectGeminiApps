package com.projectduoplay.shared

import com.projectduoplay.core.analytics.Analytics
import com.projectduoplay.core.analytics.PlatformAnalyticsTracker
import com.projectduoplay.core.common.AppConfig
import com.projectduoplay.core.common.AppConfigProvider
import com.projectduoplay.core.common.AppMode
import com.projectduoplay.core.common.AppTheme
import com.projectduoplay.shared.di.commonModule
import com.projectduoplay.shared.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSBundle

fun startKoinIOS() = startKoin {
    modules(
        iosAppModule(),
        commonModule(),
        platformModule()
    )
}.also {
    // Initialize analytics
    Analytics.initialize(PlatformAnalyticsTracker())
}

private fun iosAppModule() = module {
    single<AppConfigProvider> { IOSAppConfigProvider() }
}

private class IOSAppConfigProvider(
    private val bundle: NSBundle = NSBundle.mainBundle
) : AppConfigProvider {
    override fun getConfig(): AppConfig {
        val appName = requiredString("AppName")
        val version = string("CFBundleShortVersionString") ?: "1.0"

        return AppConfig(
            appName = appName,
            appSubtitle = string("AppSubtitle").orEmpty(),
            appId = bundle.bundleIdentifier ?: error("Missing iOS bundle identifier"),
            mode = if (string("AppMode") == "launcher") AppMode.LAUNCHER else AppMode.FIXED,
            baseUrl = string("AppStartURL").orEmpty(),
            enableAnalytics = false,
            enablePushNotifications = false,
            enableDeepLinking = true,
            enableJavaScriptBridge = string("AppEnableJavaScriptBridge")
                ?.equals("true", ignoreCase = true) == true,
            allowedOrigins = string("AppAllowedOrigins").orEmpty()
                .split('|')
                .map(String::trim)
                .filter(String::isNotEmpty),
            customUserAgent = "$appName/$version",
            theme = AppTheme(
                primaryColor = "#FFFFFF",
                splashScreenBackground = "#000000",
                statusBarColor = "#000000"
            )
        )
    }

    private fun requiredString(key: String): String =
        string(key)?.takeIf(String::isNotBlank)
            ?: error("Missing required Info.plist value: $key")

    private fun string(key: String): String? =
        bundle.objectForInfoDictionaryKey(key) as? String
}
