package com.projectduoplay.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortalControllerTest {
    @Test
    fun savesAndRestoresDestinationWithoutClearingWebsiteData() {
        val store = FakeStore()
        val controller = PortalController(store)
        controller.updateInput("example.com")
        controller.previewInput()
        assertEquals("https://example.com", controller.state.value.pendingUrl)
        controller.saveAndOpen()
        assertEquals("https://example.com", store.value)
        assertFalse(controller.state.value.settingsVisible)

        val restored = PortalController(store)
        assertEquals("https://example.com", restored.state.value.savedUrl)
    }

    @Test
    fun resetClearsOnlySavedDestination() {
        val store = FakeStore("https://example.com")
        val controller = PortalController(store)
        controller.requestReset()
        assertTrue(controller.state.value.resetConfirmationVisible)
        controller.confirmReset()
        assertNull(store.value)
        assertNull(controller.state.value.savedUrl)
        assertTrue(controller.state.value.message!!.contains("Website data was kept"))
    }

    @Test
    fun invalidQrAndDeniedScannerKeepExistingDestination() {
        val store = FakeStore("https://example.com")
        val controller = PortalController(store)
        controller.previewQrPayload("javascript:alert(1)")
        assertNull(controller.state.value.pendingUrl)
        assertEquals("https://example.com", controller.state.value.savedUrl)
        controller.scannerUnavailable("Camera access was denied. Enter the URL manually.")
        assertTrue(controller.state.value.error!!.contains("denied"))
        assertEquals("https://example.com", controller.state.value.savedUrl)
    }

    private class FakeStore(var value: String? = null) : PortalDestinationStore {
        override fun load(): String? = value
        override fun save(url: String) { value = url }
        override fun clear() { value = null }
    }
}
