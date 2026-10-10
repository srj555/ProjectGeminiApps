package com.projectduoplay.feature.webview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LauncherUrlTest {
    @Test
    fun normalizesBareHostToHttps() {
        val result = assertIs<LauncherUrlResult.Valid>(normalizeLauncherUrl("example.com/path"))
        assertEquals("https://example.com/path", result.url)
    }

    @Test
    fun acceptsHttpsAndRejectsExecutableOrInsecureSchemes() {
        assertIs<LauncherUrlResult.Valid>(normalizeLauncherUrl("https://example.com"))
        for (value in listOf(
            "http://example.com",
            "javascript:alert(1)",
            "file:///tmp/page.html",
            "data:text/html,test",
            "intent://example.com",
        )) {
            assertIs<LauncherUrlResult.Invalid>(normalizeLauncherUrl(value), value)
        }
    }

    @Test
    fun rejectsCredentialsAndNonUrlQrPayloads() {
        assertIs<LauncherUrlResult.Invalid>(normalizeLauncherUrl("https://user:pass@example.com"))
        assertIs<LauncherUrlResult.Invalid>(normalizeLauncherUrl("hello world"))
    }

    @Test
    fun launcherPolicyKeepsArbitraryHttpsInsideAndBlocksCleartext() {
        val policy = NavigationPolicy(emptyList(), allowAnyHttpsOrigin = true)
        assertEquals(
            NavigationDecision.ALLOW_IN_WEB_VIEW,
            policy.decide("https://another.example/account")
        )
        assertEquals(NavigationDecision.BLOCK, policy.decide("http://another.example"))
        assertEquals(NavigationDecision.BLOCK, policy.decide("javascript:alert(1)"))
    }
}
