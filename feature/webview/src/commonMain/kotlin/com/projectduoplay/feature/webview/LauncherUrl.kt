package com.projectduoplay.feature.webview

import io.ktor.http.Url

sealed class LauncherUrlResult {
    data class Valid(val url: String) : LauncherUrlResult()
    data class Invalid(val message: String) : LauncherUrlResult()
}

/** Validates user and QR input without ever evaluating it as code. */
fun normalizeLauncherUrl(input: String): LauncherUrlResult {
    val raw = input.trim()
    if (raw.isEmpty()) return LauncherUrlResult.Invalid("Enter a website address.")
    if (raw.any { it.isWhitespace() || it.code < 0x20 }) {
        return LauncherUrlResult.Invalid("The address contains invalid whitespace.")
    }

    val scheme = SCHEME.find(raw)?.groupValues?.get(1)?.lowercase()
    if (scheme != null && scheme != "https") {
        return LauncherUrlResult.Invalid("Portal opens HTTPS websites only.")
    }
    val candidate = if (scheme == null) "https://$raw" else raw
    val authority = candidate.removePrefix("https://")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
    if (authority.isBlank() || '@' in authority) {
        return LauncherUrlResult.Invalid("Use a website address without a username or password.")
    }

    return runCatching {
        val url = Url(candidate)
        if (!url.protocol.name.equals("https", ignoreCase = true) || url.host.isBlank()) {
            LauncherUrlResult.Invalid("Enter a valid HTTPS website address.")
        } else {
            LauncherUrlResult.Valid(url.toString())
        }
    }.getOrElse {
        LauncherUrlResult.Invalid("Enter a valid HTTPS website address.")
    }
}

private val SCHEME = Regex("^([A-Za-z][A-Za-z0-9+.-]*):")
