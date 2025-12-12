package com.projectduoplay.android

import android.app.Application
import com.projectduoplay.android.config.AndroidAppConfigProvider
import com.projectduoplay.core.analytics.Analytics
import com.projectduoplay.core.analytics.PlatformAnalyticsTracker
import com.projectduoplay.core.common.AppConfigProvider
import com.projectduoplay.shared.di.commonModule
import com.projectduoplay.shared.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                appModule(),
                commonModule(),
                platformModule()
            )
        }

        // Initialize analytics
        Analytics.initialize(PlatformAnalyticsTracker())
    }

    private fun appModule() = module {
        single<AppConfigProvider> {
            AndroidAppConfigProvider(this@MainApplication)
        }
    }
}
