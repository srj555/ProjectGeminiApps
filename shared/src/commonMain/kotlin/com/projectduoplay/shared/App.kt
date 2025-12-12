package com.projectduoplay.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.projectduoplay.feature.webview.WebViewScreen
import com.projectduoplay.feature.webview.WebViewViewModel
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
fun App() {
    MaterialTheme(
        colorScheme = DarkColorScheme
    ) {
        val viewModel: WebViewViewModel = koinViewModel()
        var splashCompleted by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            // Always render WebView to start loading
            WebViewScreen(viewModel = viewModel)

            // Show splash screen for 2 seconds
            if (!splashCompleted) {
                SplashScreen(
                    onSplashComplete = {
                        splashCompleted = true
                    }
                )
            }
        }
    }
}
