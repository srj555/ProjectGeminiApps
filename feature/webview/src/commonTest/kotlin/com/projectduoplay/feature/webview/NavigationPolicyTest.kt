package com.projectduoplay.feature.webview

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationPolicyTest {
    private val policy = NavigationPolicy(listOf("https://example.com"))

    @Test
    fun allowsExactHttpsOrigin() {
        assertEquals(
            NavigationDecision.ALLOW_IN_WEB_VIEW,
            policy.decide("https://example.com/game?level=2")
        )
    }

    @Test
    fun normalizesHostCase() {
        assertEquals(
            NavigationDecision.ALLOW_IN_WEB_VIEW,
            policy.decide("https://EXAMPLE.com/game")
        )
    }

    @Test
    fun rejectsHostSuffixAttack() {
        assertEquals(
            NavigationDecision.OPEN_EXTERNALLY,
            policy.decide("https://example.com.attacker.invalid/")
        )
    }

    @Test
    fun rejectsUnlistedSubdomain() {
        assertEquals(
            NavigationDecision.OPEN_EXTERNALLY,
            policy.decide("https://login.example.com/")
        )
    }

    @Test
    fun rejectsCleartextFromEmbeddedWebView() {
        assertEquals(
            NavigationDecision.OPEN_EXTERNALLY,
            policy.decide("http://example.com/")
        )
    }

    @Test
    fun blocksScriptAndMalformedUrls() {
        assertEquals(NavigationDecision.BLOCK, policy.decide("javascript:alert(1)"))
        assertEquals(NavigationDecision.BLOCK, policy.decide("not a url"))
    }
}
