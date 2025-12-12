package com.projectduoplay.shared

import com.projectduoplay.core.analytics.Analytics
import com.projectduoplay.core.analytics.PlatformAnalyticsTracker
import com.projectduoplay.core.common.AppConfig
import com.projectduoplay.core.common.AppConfigProvider
import com.projectduoplay.core.common.AppTheme
import com.projectduoplay.shared.di.commonModule
import com.projectduoplay.shared.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

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
    single<AppConfigProvider> {
        object : AppConfigProvider {
            override fun getConfig(): AppConfig {
                // Read from Info.plist or Config
                return AppConfig(
                    appName = "DuoPlay",
                    appId = "com.duoplay.app",
                    baseUrl = "https://duoplay-589720827506.us-west1.run.app",
                    enableAnalytics = true,
                    enablePushNotifications = true,
                    enableDeepLinking = true,
                    enableJavaScriptBridge = true,
                    allowedDomains = listOf("duoplay-589720827506.us-west1.run.app"),
                    customUserAgent = "DuoPlay/1.0.0",
                    theme = AppTheme(
                        primaryColor = "#6200EE",
                        splashScreenBackground = "#FFFFFF",
                        statusBarColor = "#6200EE"
                    )
                )
            }
        }
    }
}
