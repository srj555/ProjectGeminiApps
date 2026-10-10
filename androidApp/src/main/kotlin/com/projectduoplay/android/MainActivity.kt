package com.projectduoplay.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.projectduoplay.core.analytics.Analytics
import com.projectduoplay.shared.App

class MainActivity : ComponentActivity() {
    private var deepLinkUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        handleIntent(intent)

        setContent {
            App(
                deepLinkUrl = deepLinkUrl,
                onDeepLinkConsumed = { deepLinkUrl = null }
            )
        }

        Analytics.logScreenView("MainActivity")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (BuildConfig.APP_MODE == "launcher") return
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                deepLinkUrl = intent.dataString
            }
        }
    }
}
