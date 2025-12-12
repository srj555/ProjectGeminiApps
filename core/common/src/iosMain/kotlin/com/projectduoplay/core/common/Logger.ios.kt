package com.projectduoplay.core.common

import platform.Foundation.NSLog

actual class PlatformLogger : Logger {
    override fun d(tag: String, message: String) {
        NSLog("[$tag] DEBUG: $message")
    }

    override fun i(tag: String, message: String) {
        NSLog("[$tag] INFO: $message")
    }

    override fun w(tag: String, message: String) {
        NSLog("[$tag] WARNING: $message")
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        val throwableMessage = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        NSLog("[$tag] ERROR: $message$throwableMessage")
    }
}
