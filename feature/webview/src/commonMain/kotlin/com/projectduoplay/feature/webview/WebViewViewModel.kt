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
    private lateinit var javaScriptBridge: JavaScriptBridge

    val webViewConfig = WebViewConfig(
        initialUrl = appConfig.baseUrl,
        allowedDomains = appConfig.allowedDomains,
        enableJavaScript = true,
        enableDomStorage = true,
        userAgentSuffix = appConfig.customUserAgent,
        javaScriptBridgeName = "NativeBridge"
    )

    init {
        observeNetworkStatus()
    }

    fun initializeWebView(controller: WebViewController) {
        webViewController = controller

        if (appConfig.enableJavaScriptBridge) {
            javaScriptBridge = JavaScriptBridge(controller)
        }

        observeWebViewState()
        loadInitialUrl()
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
                }
            }
        }
    }

    private fun observeWebViewState() {
        viewModelScope.launch {
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

    private fun loadInitialUrl() {
        webViewController.loadUrl(appConfig.baseUrl)
    }

    fun reload() {
        webViewController.reload()
    }

    fun goBack() {
        if (webViewController.canGoBack.value) {
            webViewController.goBack()
        }
    }

    fun handleDeepLink(url: String) {
        Analytics.logDeepLink(url)
        webViewController.loadUrl(url)
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}

sealed class WebViewUiState {
    data object Idle : WebViewUiState()
    data object Loading : WebViewUiState()
    data class Success(val url: String) : WebViewUiState()
    data class Error(val message: String, val isNetworkError: Boolean) : WebViewUiState()
}
