package com.projectduoplay.feature.webview

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSError
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
import platform.UIKit.UIApplication
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
    private var scriptMessageHandler: WKScriptMessageHandlerProtocol? = null
    private var navigationDelegate: WKNavigationDelegateProtocol? = null
    private val deferredLoads = DeferredUrlQueue()
    private val navigationPolicy = NavigationPolicy(
        config.allowedOrigins,
        config.allowAnyHttpsOrigin
    )
    var webView: WKWebView? = null
        private set

    fun createWebView(): WKWebView {
        val configuration = WKWebViewConfiguration()

        // Configure user content controller for JavaScript bridge
        val userContentController = WKUserContentController()

        if (config.enableJavaScript && config.enableJavaScriptBridge) {
            scriptMessageHandler = object : NSObject(), WKScriptMessageHandlerProtocol {
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
                scriptMessageHandler!!,
                config.javaScriptBridgeName
            )
        }

        configuration.userContentController = userContentController
        configuration.allowsInlineMediaPlayback = true
        configuration.websiteDataStore = WKWebsiteDataStore.defaultDataStore()
        config.userAgentSuffix?.let { suffix ->
            configuration.applicationNameForUserAgent = suffix
        }

        val webView = WKWebView(
            frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0),
            configuration = configuration
        )

        navigationDelegate = object : NSObject(), WKNavigationDelegateProtocol {
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
                val isMainFrame = decidePolicyForNavigationAction.targetFrame?.mainFrame ?: true

                when (navigationPolicy.decideForFrame(url, isMainFrame)) {
                    NavigationDecision.ALLOW_IN_WEB_VIEW -> {
                        if (decidePolicyForNavigationAction.targetFrame == null) {
                            webView.loadRequest(decidePolicyForNavigationAction.request)
                            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                        } else {
                            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                        }
                    }
                    NavigationDecision.OPEN_EXTERNALLY -> {
                        openExternal(url)
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                    NavigationDecision.BLOCK -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                }
            }
        }
        webView.navigationDelegate = navigationDelegate

        // Configure WebView settings
        webView.allowsBackForwardNavigationGestures = true
        webView.scrollView.scrollEnabled = true
        webView.scrollView.bounces = true
        webView.scrollView.delaysContentTouches = false

        this.webView = webView
        deferredLoads.attach { destination ->
            NSURL.URLWithString(destination)?.let { nsUrl ->
                webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
        }
        return webView
    }

    private fun updateNavigationState(webView: WKWebView) {
        _canGoBack.value = webView.canGoBack
        _canGoForward.value = webView.canGoForward
    }

    override fun loadUrl(url: String) {
        if (navigationPolicy.decide(url) != NavigationDecision.ALLOW_IN_WEB_VIEW) {
            _loadingState.value = WebViewLoadingState.Error(
                errorCode = ERROR_BLOCKED_NAVIGATION,
                description = "This link is not allowed inside the app.",
                failingUrl = url
            )
            return
        }
        _loadingState.value = WebViewLoadingState.Loading(0)
        deferredLoads.load(url, webView != null) { destination ->
            NSURL.URLWithString(destination)?.let { nsUrl ->
                webView?.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
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
        WKWebsiteDataStore.defaultDataStore().removeDataOfTypes(
            WKWebsiteDataStore.allWebsiteDataTypes(),
            NSDate(timeIntervalSinceReferenceDate = -978307200.0)
        ) {}
    }

    override fun dispose() {
        webView?.stopLoading()
        webView?.navigationDelegate = null
        webView?.configuration?.userContentController
            ?.removeScriptMessageHandlerForName(config.javaScriptBridgeName)
        webView = null
        deferredLoads.clear()
        navigationDelegate = null
        scriptMessageHandler = null
        messageHandler = null
    }

    private fun openExternal(url: String) {
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }

    private companion object {
        const val ERROR_BLOCKED_NAVIGATION = -1000
    }
}

actual fun getPlatformName(): String = "iOS"
