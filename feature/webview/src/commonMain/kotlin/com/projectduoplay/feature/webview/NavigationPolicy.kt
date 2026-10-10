package com.projectduoplay.feature.webview

import io.ktor.http.Url

enum class NavigationDecision {
    ALLOW_IN_WEB_VIEW,
    OPEN_EXTERNALLY,
    BLOCK
}

/**
 * Applies one origin policy to initial loads, redirects, deep links and bridge navigation.
 * Only exact HTTPS origins are eligible for loading inside the app.
 */
class NavigationPolicy(
    allowedOrigins: List<String>,
    private val allowAnyHttpsOrigin: Boolean = false
) {
    private val allowedOrigins = allowedOrigins.mapNotNull(::parseHttpsOrigin).toSet()

    fun decide(rawUrl: String): NavigationDecision {
        val scheme = rawUrl.substringBefore(':', missingDelimiterValue = "").lowercase()
        if (scheme == "http") {
            return if (allowAnyHttpsOrigin) NavigationDecision.BLOCK else NavigationDecision.OPEN_EXTERNALLY
        }
        if (scheme in EXTERNAL_SCHEMES && scheme != "https") {
            return NavigationDecision.OPEN_EXTERNALLY
        }

        val origin = parseHttpsOrigin(rawUrl)
            ?: return NavigationDecision.BLOCK

        return if (allowAnyHttpsOrigin || origin in allowedOrigins) {
            NavigationDecision.ALLOW_IN_WEB_VIEW
        } else {
            NavigationDecision.OPEN_EXTERNALLY
        }
    }

    /**
     * Subframes may load only allowlisted origins and must never open another app or browser.
     * A missing target frame is treated as a main-frame/new-window navigation by callers.
     */
    fun decideForFrame(rawUrl: String, isMainFrame: Boolean): NavigationDecision {
        val decision = decide(rawUrl)
        return if (!isMainFrame && decision == NavigationDecision.OPEN_EXTERNALLY) {
            NavigationDecision.BLOCK
        } else {
            decision
        }
    }

    /**
     * Defensive main-frame request check for platform callbacks that also observe POST loads.
     * This intentionally does not claim to be a subresource firewall.
     */
    fun shouldBlockMainFrameRequest(rawUrl: String, isMainFrame: Boolean): Boolean =
        isMainFrame && decide(rawUrl) != NavigationDecision.ALLOW_IN_WEB_VIEW

    private fun parseHttpsOrigin(rawUrl: String): WebOrigin? {
        if (!rawUrl.startsWith("https://", ignoreCase = true)) return null

        return runCatching {
            val url = Url(rawUrl)
            if (!url.protocol.name.equals("https", ignoreCase = true) || url.host.isBlank()) {
                return null
            }
            WebOrigin(
                scheme = "https",
                host = url.host.lowercase().trimEnd('.'),
                port = url.port
            )
        }.getOrNull()
    }

    private data class WebOrigin(
        val scheme: String,
        val host: String,
        val port: Int
    )

    private companion object {
        val EXTERNAL_SCHEMES = setOf("http", "https", "mailto", "tel")
    }
}
