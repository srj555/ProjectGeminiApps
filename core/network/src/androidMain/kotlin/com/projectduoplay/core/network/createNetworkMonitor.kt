package com.projectduoplay.core.network

/**
 * Stub for Android - actual implementation provided by DI
 * This is here just to satisfy the expect/actual requirement
 */
actual fun createNetworkMonitor(): NetworkMonitor {
    throw UnsupportedOperationException("Use DI to get NetworkMonitor on Android")
}
