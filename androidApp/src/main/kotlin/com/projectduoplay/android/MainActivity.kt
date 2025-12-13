package com.projectduoplay.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.projectduoplay.core.analytics.Analytics
import com.projectduoplay.feature.webview.WebViewViewModel
import com.projectduoplay.shared.App
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val webViewViewModel: WebViewViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle deep links
        handleIntent(intent)

        setContent {
            App(
                appName = BuildConfig.APP_NAME,
                appSubtitle = BuildConfig.APP_SUBTITLE
            )
        }

        Analytics.logScreenView("MainActivity")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    handleDeepLink(uri)
                }
            }
        }
    }

    private fun handleDeepLink(uri: Uri) {
        // Convert deep link to web URL if needed
        val webUrl = uri.toString()
        webViewViewModel.handleDeepLink(webUrl)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Back button handling is done via Compose navigation
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
