package com.projectduoplay.feature.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebStorage
import java.io.ByteArrayInputStream
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
    private val deferredLoads = DeferredUrlQueue()
    private val navigationPolicy = NavigationPolicy(
        config.allowedOrigins,
        config.allowAnyHttpsOrigin
    )

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView(webView: WebView) {
        this.webView = webView

        webView.settings.apply {
            javaScriptEnabled = config.enableJavaScript
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = config.enableDomStorage
            setSupportMultipleWindows(false)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = config.enableSafeBrowsing
            }

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

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = createWebViewClient()
        webView.webChromeClient = createWebChromeClient()

        // Add JavaScript interface for bridge
        if (config.enableJavaScript && config.enableJavaScriptBridge) {
            webView.addJavascriptInterface(
                JavaScriptBridgeInterface(),
                config.javaScriptBridgeName
            )
        }

        deferredLoads.attach(webView::loadUrl)
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
            if (request.isForMainFrame.not()) return false

            return when (navigationPolicy.decide(url)) {
                NavigationDecision.ALLOW_IN_WEB_VIEW -> false
                NavigationDecision.OPEN_EXTERNALLY -> {
                    openExternal(view, url)
                    true
                }
                NavigationDecision.BLOCK -> true
            }
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

            // shouldOverrideUrlLoading is not invoked for POST. This request-level fallback
            // enforces the origin boundary for main-frame loads only; it is not a subresource
            // firewall and deliberately leaves normal page assets alone.
            if (!navigationPolicy.shouldBlockMainFrameRequest(url, request.isForMainFrame)) {
                return super.shouldInterceptRequest(view, request)
            }

            view?.post {
                _currentUrl.value = url
                _loadingState.value = WebViewLoadingState.Error(
                    errorCode = ERROR_BLOCKED_NAVIGATION,
                    description = "This main-frame request is not allowed inside the app.",
                    failingUrl = url
                )
            }
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                403,
                "Blocked by app navigation policy",
                mapOf("Cache-Control" to "no-store"),
                ByteArrayInputStream("Blocked by app navigation policy".encodeToByteArray())
            )
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
        if (navigationPolicy.decide(url) == NavigationDecision.ALLOW_IN_WEB_VIEW) {
            _loadingState.value = WebViewLoadingState.Loading(0)
            deferredLoads.load(url, webView != null) { destination ->
                webView?.loadUrl(destination)
            }
        } else {
            _loadingState.value = WebViewLoadingState.Error(
                errorCode = ERROR_BLOCKED_NAVIGATION,
                description = "This link is not allowed inside the app.",
                failingUrl = url
            )
        }
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
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
        webView?.apply {
            clearCache(true)
            clearHistory()
        }
    }

    override fun dispose() {
        webView?.apply {
            stopLoading()
            removeJavascriptInterface(config.javaScriptBridgeName)
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            destroy()
        }
        webView = null
        deferredLoads.clear()
        messageHandler = null
    }

    private fun openExternal(view: WebView?, url: String) {
        val context = view?.context ?: return
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    inner class JavaScriptBridgeInterface {
        @JavascriptInterface
        fun postMessage(name: String, data: String) {
            messageHandler?.invoke(JavaScriptMessage(name, data))
        }
    }

    private companion object {
        const val ERROR_BLOCKED_NAVIGATION = -1000
    }
}

actual fun getPlatformName(): String = "Android"
