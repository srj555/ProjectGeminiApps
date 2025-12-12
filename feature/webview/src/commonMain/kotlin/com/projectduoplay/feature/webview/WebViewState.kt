package com.projectduoplay.feature.webview

import kotlinx.coroutines.flow.StateFlow

/**
 * WebView loading state
 */
sealed class WebViewLoadingState {
    data object Idle : WebViewLoadingState()
    data class Loading(val progress: Int) : WebViewLoadingState()
    data class Loaded(val url: String) : WebViewLoadingState()
    data class Error(val errorCode: Int, val description: String, val failingUrl: String) :
        WebViewLoadingState()
}

/**
 * WebView navigation event
 */
sealed class WebViewNavigationEvent {
    data class PageStarted(val url: String) : WebViewNavigationEvent()
    data class PageFinished(val url: String) : WebViewNavigationEvent()
    data class ProgressChanged(val progress: Int) : WebViewNavigationEvent()
    data class ReceivedError(val errorCode: Int, val description: String, val failingUrl: String) :
        WebViewNavigationEvent()
    data class ShouldOverrideUrlLoading(val url: String) : WebViewNavigationEvent()
}

/**
 * JavaScript message from WebView
 */
data class JavaScriptMessage(
    val name: String,
    val data: String
)

/**
 * WebView controller interface
 */
interface WebViewController {
    val loadingState: StateFlow<WebViewLoadingState>
    val currentUrl: StateFlow<String?>
    val canGoBack: StateFlow<Boolean>
    val canGoForward: StateFlow<Boolean>

    fun loadUrl(url: String)
    fun reload()
    fun goBack()
    fun goForward()
    fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)? = null)
    fun setJavaScriptMessageHandler(handler: (JavaScriptMessage) -> Unit)
    fun clearCache()
}

/**
 * Expect platform-specific WebView wrapper
 */
expect class PlatformWebViewController(
    config: WebViewConfig
) : WebViewController
