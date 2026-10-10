package com.projectduoplay.feature.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
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
        modifier = modifier,
        properties = UIKitInteropProperties(
            // A fullscreen WKWebView owns its gestures. Cooperative interop delays touch delivery
            // and can let Compose consume the pan before WKWebView's UIScrollView sees it.
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
