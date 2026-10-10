package com.projectduoplay.feature.webview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeferredUrlQueueTest {
    @Test
    fun loadBeforeAttachmentIsDeliveredWhenWebViewAttaches() {
        val loaded = mutableListOf<String>()
        val queue = DeferredUrlQueue()

        queue.load("https://example.com", isAttached = false, loaded::add)
        assertTrue(loaded.isEmpty())

        queue.attach(loaded::add)
        assertEquals(listOf("https://example.com"), loaded)
    }

    @Test
    fun latestPreAttachmentDestinationWinsAndIsDeliveredOnce() {
        val loaded = mutableListOf<String>()
        val queue = DeferredUrlQueue()

        queue.load("https://old.example", isAttached = false, loaded::add)
        queue.load("https://new.example", isAttached = false, loaded::add)
        queue.attach(loaded::add)
        queue.attach(loaded::add)

        assertEquals(listOf("https://new.example"), loaded)
    }
}
