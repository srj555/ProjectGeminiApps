package com.projectduoplay.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlatformNetworkMonitor(
    private val context: Context
) : NetworkMonitor {
    private val _networkStatus = MutableStateFlow(NetworkStatus.UNKNOWN)
    override val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkStatus.value = NetworkStatus.AVAILABLE
        }

        override fun onLost(network: Network) {
            _networkStatus.value = NetworkStatus.UNAVAILABLE
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            _networkStatus.value = if (hasInternet) {
                NetworkStatus.AVAILABLE
            } else {
                NetworkStatus.UNAVAILABLE
            }
        }
    }

    override fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Set initial state
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _networkStatus.value = if (capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) == true
        ) {
            NetworkStatus.AVAILABLE
        } else {
            NetworkStatus.UNAVAILABLE
        }
    }

    override fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

// This will be provided by DI, so we don't implement the factory here
// The factory will be called from the shared module's DI setup
