package com.projectduoplay.feature.webview

/** Keeps the latest approved load until the platform WebView has attached. */
internal class DeferredUrlQueue {
    private var pendingUrl: String? = null

    fun load(url: String, isAttached: Boolean, loader: (String) -> Unit) {
        if (isAttached) {
            pendingUrl = null
            loader(url)
        } else {
            pendingUrl = url
        }
    }

    fun attach(loader: (String) -> Unit) {
        val destination = pendingUrl ?: return
        pendingUrl = null
        loader(destination)
    }

    fun clear() {
        pendingUrl = null
    }
}
