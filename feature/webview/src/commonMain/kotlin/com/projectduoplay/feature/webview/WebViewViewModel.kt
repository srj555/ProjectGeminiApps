package com.projectduoplay.feature.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectduoplay.core.analytics.Analytics
import com.projectduoplay.core.common.AppConfig
import com.projectduoplay.core.network.NetworkMonitor
import com.projectduoplay.core.network.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WebViewViewModel(
    private val appConfig: AppConfig,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebViewUiState>(WebViewUiState.Loading)
    val uiState: StateFlow<WebViewUiState> = _uiState.asStateFlow()

    private val _networkStatus = MutableStateFlow(NetworkStatus.UNKNOWN)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private lateinit var webViewController: WebViewController
    private var javaScriptBridge: JavaScriptBridge? = null
    private var webViewStateJob: Job? = null
    private var pendingDeepLink: String? = null
    private var networkMonitoringStarted = false

    val webViewConfig = WebViewConfig(
        initialUrl = appConfig.baseUrl,
        allowedOrigins = appConfig.allowedOrigins,
        enableJavaScript = true,
        enableJavaScriptBridge = appConfig.enableJavaScriptBridge,
        enableDomStorage = true,
        userAgentSuffix = appConfig.customUserAgent,
        javaScriptBridgeName = "NativeBridge"
    )

    init {
        networkMonitoringStarted = runCatching {
            networkMonitor.startMonitoring()
            true
        }.getOrDefault(false)
        observeNetworkStatus()
    }

    fun initializeWebView(controller: WebViewController) {
        if (::webViewController.isInitialized && webViewController === controller) return

        webViewController = controller

        if (appConfig.enableJavaScriptBridge) {
            javaScriptBridge = JavaScriptBridge(controller)
        } else {
            javaScriptBridge = null
        }

        observeWebViewState()
        controller.loadUrl(pendingDeepLink ?: appConfig.baseUrl)
        pendingDeepLink = null
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.networkStatus.collect { status ->
                _networkStatus.value = status
                if (status == NetworkStatus.UNAVAILABLE) {
                    _uiState.value = WebViewUiState.Error(
                        "No internet connection",
                        isNetworkError = true
                    )
                } else if (
                    status == NetworkStatus.AVAILABLE &&
                    (_uiState.value as? WebViewUiState.Error)?.isNetworkError == true
                ) {
                    _uiState.value = WebViewUiState.Loading
                    if (::webViewController.isInitialized) {
                        webViewController.reload()
                    }
                }
            }
        }
    }

    private fun observeWebViewState() {
        webViewStateJob?.cancel()
        webViewStateJob = viewModelScope.launch {
            webViewController.loadingState.collect { state ->
                when (state) {
                    is WebViewLoadingState.Idle -> {
                        _uiState.value = WebViewUiState.Idle
                    }
                    is WebViewLoadingState.Loading -> {
                        _uiState.value = WebViewUiState.Loading
                    }
                    is WebViewLoadingState.Loaded -> {
                        _uiState.value = WebViewUiState.Success(state.url)
                        Analytics.logWebViewLoad(state.url)
                    }
                    is WebViewLoadingState.Error -> {
                        _uiState.value = WebViewUiState.Error(
                            state.description,
                            isNetworkError = false
                        )
                        Analytics.logWebViewError(state.failingUrl, state.errorCode)
                    }
                }
            }
        }
    }

    fun reload() {
        if (::webViewController.isInitialized) {
            webViewController.reload()
        }
    }

    fun goBack() {
        if (::webViewController.isInitialized && webViewController.canGoBack.value) {
            webViewController.goBack()
        }
    }

    fun handleDeepLink(url: String) {
        Analytics.logDeepLink(url)
        if (::webViewController.isInitialized) {
            webViewController.loadUrl(url)
        } else {
            pendingDeepLink = url
        }
    }

    override fun onCleared() {
        super.onCleared()
        webViewStateJob?.cancel()
        if (::webViewController.isInitialized) {
            webViewController.dispose()
        }
        if (networkMonitoringStarted) {
            runCatching { networkMonitor.stopMonitoring() }
        }
    }
}

sealed class WebViewUiState {
    data object Idle : WebViewUiState()
    data object Loading : WebViewUiState()
    data class Success(val url: String) : WebViewUiState()
    data class Error(val message: String, val isNetworkError: Boolean) : WebViewUiState()
}
