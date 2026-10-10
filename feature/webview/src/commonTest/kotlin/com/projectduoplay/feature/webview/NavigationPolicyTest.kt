package com.projectduoplay.feature.webview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun crossOriginSubframesCannotLaunchExternalApps() {
        assertEquals(
            NavigationDecision.BLOCK,
            policy.decideForFrame("https://third-party.example/", isMainFrame = false)
        )
        assertEquals(
            NavigationDecision.OPEN_EXTERNALLY,
            policy.decideForFrame("https://third-party.example/", isMainFrame = true)
        )
        assertEquals(
            NavigationDecision.ALLOW_IN_WEB_VIEW,
            policy.decideForFrame("https://example.com/frame", isMainFrame = false)
        )
    }

    @Test
    fun defensiveRequestCheckBlocksOnlyDisallowedMainFrames() {
        assertTrue(
            policy.shouldBlockMainFrameRequest(
                "https://third-party.example/form-result",
                isMainFrame = true
            )
        )
        assertFalse(
            policy.shouldBlockMainFrameRequest(
                "https://example.com/form-result",
                isMainFrame = true
            )
        )
        assertFalse(
            policy.shouldBlockMainFrameRequest(
                "https://third-party.example/asset.js",
                isMainFrame = false
            )
        )
    }
}
