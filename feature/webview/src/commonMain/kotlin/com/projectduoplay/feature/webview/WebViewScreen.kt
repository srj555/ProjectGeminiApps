package com.projectduoplay.feature.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
expect fun WebViewContainer(
    controller: WebViewController,
    modifier: Modifier = Modifier
)

// Simple black and white theme
private val DarkBackground = Color.Black
private val AccentColor = Color.White
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFAAAAAA)

@Composable
fun WebViewScreen(
    viewModel: WebViewViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val controller = remember(viewModel.webViewConfig) {
        PlatformWebViewController(viewModel.webViewConfig)
    }
    val canGoBack by controller.canGoBack.collectAsState()

    LaunchedEffect(viewModel, controller) {
        viewModel.initializeWebView(controller)
    }

    PlatformBackHandler(enabled = canGoBack) {
        viewModel.goBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // WebView always present (hidden during loading/error)
        WebViewContainer(
            controller = controller,
            modifier = Modifier.fillMaxSize()
        )

        // Overlays
        when (uiState) {
            is WebViewUiState.Error -> {
                // Full screen error overlay
                val errorState = uiState as WebViewUiState.Error
                ErrorView(
                    message = errorState.message,
                    onRetry = { viewModel.reload() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // WebView is visible, no overlay
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Oops!",
                fontSize = 28.sp,
                color = TextPrimary
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "RETRY",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
        }
    }
}
