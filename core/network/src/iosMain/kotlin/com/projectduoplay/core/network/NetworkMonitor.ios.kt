package com.projectduoplay.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.darwin.dispatch_get_main_queue

class PlatformNetworkMonitor : NetworkMonitor {
    private val _networkStatus = MutableStateFlow(NetworkStatus.UNKNOWN)
    override val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val monitor = nw_path_monitor_create()

    override fun startMonitoring() {
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = if (path != null) {
                val pathStatus = platform.Network.nw_path_get_status(path)
                if (pathStatus == nw_path_status_satisfied) {
                    NetworkStatus.AVAILABLE
                } else {
                    NetworkStatus.UNAVAILABLE
                }
            } else {
                NetworkStatus.UNAVAILABLE
            }
            _networkStatus.value = status
        }

        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }

    override fun stopMonitoring() {
        nw_path_monitor_cancel(monitor)
    }
}

actual fun createNetworkMonitor(): NetworkMonitor = PlatformNetworkMonitor()
