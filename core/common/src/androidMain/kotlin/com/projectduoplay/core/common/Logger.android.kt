package com.projectduoplay.core.common

import android.util.Log as AndroidLog

actual class PlatformLogger : Logger {
    override fun d(tag: String, message: String) {
        AndroidLog.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        AndroidLog.i(tag, message)
    }

    override fun w(tag: String, message: String) {
        AndroidLog.w(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        AndroidLog.e(tag, message, throwable)
    }
}
