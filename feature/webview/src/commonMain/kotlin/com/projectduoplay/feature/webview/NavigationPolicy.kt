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
class NavigationPolicy(allowedOrigins: List<String>) {
    private val allowedOrigins = allowedOrigins.mapNotNull(::parseHttpsOrigin).toSet()

    fun decide(rawUrl: String): NavigationDecision {
        val scheme = rawUrl.substringBefore(':', missingDelimiterValue = "").lowercase()
        if (scheme in EXTERNAL_SCHEMES && scheme != "https") {
            return NavigationDecision.OPEN_EXTERNALLY
        }

        val origin = parseHttpsOrigin(rawUrl)
            ?: return if (scheme == "http") {
                NavigationDecision.OPEN_EXTERNALLY
            } else {
                NavigationDecision.BLOCK
            }

        return if (origin in allowedOrigins) {
            NavigationDecision.ALLOW_IN_WEB_VIEW
        } else {
            NavigationDecision.OPEN_EXTERNALLY
        }
    }

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
