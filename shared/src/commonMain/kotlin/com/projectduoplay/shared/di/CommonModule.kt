package com.projectduoplay.shared.di

import com.projectduoplay.core.analytics.PlatformAnalyticsTracker
import com.projectduoplay.core.common.AppConfigProvider
import com.projectduoplay.core.network.NetworkMonitor
import com.projectduoplay.core.network.PlatformNetworkMonitor
import com.projectduoplay.feature.webview.WebViewViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Common Koin module for shared dependencies
 */
fun commonModule() = module {
    // Analytics
    single { PlatformAnalyticsTracker() }

    // WebView ViewModel
    viewModel {
        WebViewViewModel(
            appConfig = get<AppConfigProvider>().getConfig(),
            networkMonitor = get<NetworkMonitor>()
        )
    }
}

/**
 * Platform-specific module
 * Must be provided by each platform
 */
expect fun platformModule(): Module
