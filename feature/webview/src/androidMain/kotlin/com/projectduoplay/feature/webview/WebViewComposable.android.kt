package com.projectduoplay.feature.webview

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun WebViewContainer(
    controller: WebViewController,
    modifier: Modifier
) {
    val context = LocalContext.current
    val webView = remember(controller) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Set black background
            setBackgroundColor(Color.parseColor("#000000"))
        }
    }

    DisposableEffect(controller) {
        if (controller is PlatformWebViewController) {
            controller.setupWebView(webView)
        }
        onDispose {
            controller.dispose()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
