package com.projectduoplay.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue

private const val PortalShakeNotification = "ProjectGeminiPortalShake"

@Composable
actual fun PortalSettingsGesture(onShowSettings: () -> Unit) {
    DisposableEffect(onShowSettings) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = PortalShakeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) {
            onShowSettings()
        }
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }
}
