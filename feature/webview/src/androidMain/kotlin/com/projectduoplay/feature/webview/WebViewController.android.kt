package com.projectduoplay.feature.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformWebViewController actual constructor(
    private val config: WebViewConfig
) : WebViewController {

    private val _loadingState = MutableStateFlow<WebViewLoadingState>(WebViewLoadingState.Idle)
    override val loadingState: StateFlow<WebViewLoadingState> = _loadingState.asStateFlow()

    private val _currentUrl = MutableStateFlow<String?>(null)
    override val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    override val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    override val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private var messageHandler: ((JavaScriptMessage) -> Unit)? = null
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView(webView: WebView) {
        this.webView = webView

        webView.settings.apply {
            javaScriptEnabled = config.enableJavaScript
            domStorageEnabled = config.enableDomStorage
            setSupportZoom(config.enableZoom)
            builtInZoomControls = config.enableZoom
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true

            config.userAgentSuffix?.let { suffix ->
                userAgentString = "$userAgentString $suffix"
            }

            // Security settings
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false

            // Mixed content
            mixedContentMode = when (config.mixedContentMode) {
                MixedContentMode.ALWAYS_ALLOW -> android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                MixedContentMode.NEVER_ALLOW -> android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                MixedContentMode.COMPATIBILITY_MODE -> android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            // Cache settings
            cacheMode = if (config.enableCache) {
                android.webkit.WebSettings.LOAD_DEFAULT
            } else {
                android.webkit.WebSettings.LOAD_NO_CACHE
            }
        }

        webView.webViewClient = createWebViewClient()
        webView.webChromeClient = createWebChromeClient()

        // Add JavaScript interface for bridge
        if (config.enableJavaScript) {
            webView.addJavascriptInterface(
                JavaScriptBridgeInterface(),
                config.javaScriptBridgeName
            )
        }
    }

    private fun createWebViewClient() = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let {
                _currentUrl.value = it
                _loadingState.value = WebViewLoadingState.Loading(0)
                updateNavigationState()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let {
                _loadingState.value = WebViewLoadingState.Loaded(it)
                updateNavigationState()
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) {
                _loadingState.value = WebViewLoadingState.Error(
                    errorCode = error?.errorCode ?: -1,
                    description = error?.description?.toString() ?: "Unknown error",
                    failingUrl = request.url.toString()
                )
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false

            // Check if URL is in allowed domains
            if (config.allowedDomains.isNotEmpty()) {
                val isAllowed = config.allowedDomains.any { domain ->
                    url.contains(domain)
                }
                if (!isAllowed) {
                    // Open in external browser or handle differently
                    return true
                }
            }

            return false
        }
    }

    private fun createWebChromeClient() = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress < 100) {
                _loadingState.value = WebViewLoadingState.Loading(newProgress)
            }
        }
    }

    private fun updateNavigationState() {
        webView?.let {
            _canGoBack.value = it.canGoBack()
            _canGoForward.value = it.canGoForward()
        }
    }

    override fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    override fun reload() {
        webView?.reload()
    }

    override fun goBack() {
        webView?.goBack()
    }

    override fun goForward() {
        webView?.goForward()
    }

    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
        webView?.evaluateJavascript(script) { result ->
            callback?.invoke(result)
        }
    }

    override fun setJavaScriptMessageHandler(handler: (JavaScriptMessage) -> Unit) {
        messageHandler = handler
    }

    override fun clearCache() {
        webView?.clearCache(true)
    }

    inner class JavaScriptBridgeInterface {
        @JavascriptInterface
        fun postMessage(name: String, data: String) {
            messageHandler?.invoke(JavaScriptMessage(name, data))
        }
    }
}

actual fun getPlatformName(): String = "Android"
