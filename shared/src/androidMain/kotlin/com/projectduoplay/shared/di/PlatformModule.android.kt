package com.projectduoplay.shared.di

import com.projectduoplay.core.network.NetworkMonitor
import com.projectduoplay.core.network.PlatformNetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NetworkMonitor> {
        PlatformNetworkMonitor(context = get())
    }
}
