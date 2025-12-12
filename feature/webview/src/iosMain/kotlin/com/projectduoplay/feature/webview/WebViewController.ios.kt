package com.projectduoplay.feature.webview

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegate
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandler
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
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
    var webView: WKWebView? = null
        private set

    fun createWebView(): WKWebView {
        val configuration = WKWebViewConfiguration()

        // Configure user content controller for JavaScript bridge
        val userContentController = WKUserContentController()

        // Add message handler for JavaScript bridge
        val scriptMessageHandler = object : NSObject(), WKScriptMessageHandler {
            override fun userContentController(
                userContentController: WKUserContentController,
                didReceiveScriptMessage: WKScriptMessage
            ) {
                val name = didReceiveScriptMessage.name
                val body = didReceiveScriptMessage.body.toString()
                messageHandler?.invoke(JavaScriptMessage(name, body))
            }
        }

        userContentController.addScriptMessageHandler(
            scriptMessageHandler,
            config.javaScriptBridgeName
        )

        configuration.userContentController = userContentController
        configuration.allowsInlineMediaPlayback = true

        val webView = WKWebView(frame = platform.CoreGraphics.CGRectZero, configuration = configuration)

        webView.navigationDelegate = object : NSObject(), WKNavigationDelegate {
            override fun webView(
                webView: WKWebView,
                didStartProvisionalNavigation: WKNavigation?
            ) {
                val url = webView.URL?.absoluteString ?: ""
                _currentUrl.value = url
                _loadingState.value = WebViewLoadingState.Loading(0)
                updateNavigationState(webView)
            }

            override fun webView(
                webView: WKWebView,
                didFinishNavigation: WKNavigation?
            ) {
                val url = webView.URL?.absoluteString ?: ""
                _loadingState.value = WebViewLoadingState.Loaded(url)
                updateNavigationState(webView)
            }

            override fun webView(
                webView: WKWebView,
                didFailNavigation: WKNavigation?,
                withError: NSError
            ) {
                val url = webView.URL?.absoluteString ?: ""
                _loadingState.value = WebViewLoadingState.Error(
                    errorCode = withError.code.toInt(),
                    description = withError.localizedDescription,
                    failingUrl = url
                )
            }

            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit
            ) {
                val url = decidePolicyForNavigationAction.request.URL?.absoluteString ?: ""

                // Check if URL is in allowed domains
                if (config.allowedDomains.isNotEmpty()) {
                    val isAllowed = config.allowedDomains.any { domain ->
                        url.contains(domain)
                    }
                    if (!isAllowed) {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                        return
                    }
                }

                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            }
        }

        // Configure WebView settings
        webView.allowsBackForwardNavigationGestures = true

        config.userAgentSuffix?.let { suffix ->
            webView.customUserAgent = "${webView.customUserAgent ?: ""} $suffix"
        }

        this.webView = webView
        return webView
    }

    private fun updateNavigationState(webView: WKWebView) {
        _canGoBack.value = webView.canGoBack
        _canGoForward.value = webView.canGoForward
    }

    override fun loadUrl(url: String) {
        NSURL.URLWithString(url)?.let { nsUrl ->
            val request = NSURLRequest.requestWithURL(nsUrl)
            webView?.loadRequest(request)
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
        webView?.evaluateJavaScript(script) { result, error ->
            callback?.invoke(result?.toString())
        }
    }

    override fun setJavaScriptMessageHandler(handler: (JavaScriptMessage) -> Unit) {
        messageHandler = handler
    }

    override fun clearCache() {
        // iOS WebView cache clearing would go here
        // WKWebsiteDataStore.defaultDataStore().removeDataOfTypes(...)
    }
}

actual fun getPlatformName(): String = "iOS"
