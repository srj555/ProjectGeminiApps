package com.projectduoplay.feature.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebViewContainer(
    controller: WebViewController,
    modifier: Modifier
) {
    val platformController = remember(controller) { controller as PlatformWebViewController }

    DisposableEffect(controller) {
        onDispose {
            platformController.dispose()
        }
    }

    UIKitView(
        factory = {
            platformController.createWebView()
        },
        modifier = modifier
    )
}
