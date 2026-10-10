package com.projectduoplay.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.projectduoplay.feature.webview.WebViewScreen
import com.projectduoplay.feature.webview.WebViewViewModel
import com.projectduoplay.core.common.AppConfigProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// Simple black and white color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    error = Color(0xFFFF4444),
    onError = Color.Black
)

@Composable
fun App(
    deepLinkUrl: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    MaterialTheme(
        colorScheme = DarkColorScheme
    ) {
        val appConfigProvider: AppConfigProvider = koinInject()
        val appConfig = remember(appConfigProvider) { appConfigProvider.getConfig() }
        val viewModel: WebViewViewModel = koinViewModel()
        var splashCompleted by remember { mutableStateOf(false) }

        LaunchedEffect(deepLinkUrl) {
            deepLinkUrl?.let {
                viewModel.handleDeepLink(it)
                onDeepLinkConsumed()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // Keep the WebView mounted while the branded splash is visible.
                WebViewScreen(viewModel = viewModel)

                if (!splashCompleted) {
                    SplashScreen(
                        appName = appConfig.appName,
                        appSubtitle = appConfig.appSubtitle,
                        onSplashComplete = {
                            splashCompleted = true
                        }
                    )
                }
            }
        }
    }
}
