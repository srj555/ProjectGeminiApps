package com.projectduoplay.feature.webview

/**
 * WebView configuration
 */
data class WebViewConfig(
    val initialUrl: String,
    val allowedDomains: List<String> = emptyList(),
    val enableJavaScript: Boolean = true,
    val enableDomStorage: Boolean = true,
    val enableCache: Boolean = true,
    val enableZoom: Boolean = false,
    val userAgentSuffix: String? = null,
    val javaScriptBridgeName: String = "NativeBridge",
    val enableSafeBrowsing: Boolean = true,
    val supportMultipleWindows: Boolean = false,
    val mixedContentMode: MixedContentMode = MixedContentMode.NEVER_ALLOW
)

enum class MixedContentMode {
    ALWAYS_ALLOW,
    NEVER_ALLOW,
    COMPATIBILITY_MODE
}

/**
 * JavaScript bridge configuration
 */
data class JavaScriptBridgeConfig(
    val bridgeName: String = "NativeBridge",
    val enabledCommands: Set<String> = setOf(
        "navigateTo",
        "share",
        "getDeviceInfo",
        "showToast"
    )
)
