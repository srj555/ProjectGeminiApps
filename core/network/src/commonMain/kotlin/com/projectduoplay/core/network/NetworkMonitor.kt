package com.projectduoplay.core.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Network connectivity status
 */
enum class NetworkStatus {
    AVAILABLE,
    UNAVAILABLE,
    UNKNOWN
}

/**
 * Network monitor interface
 */
interface NetworkMonitor {
    val networkStatus: StateFlow<NetworkStatus>
    fun startMonitoring()
    fun stopMonitoring()
}

/**
 * Factory function for creating platform-specific network monitor
 */
expect fun createNetworkMonitor(): NetworkMonitor
