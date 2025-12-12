package com.projectduoplay.core.common

/**
 * Logging interface for platform-specific implementations
 */
interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Default logger implementation
 */
expect class PlatformLogger() : Logger

/**
 * Global logger instance
 */
object Log {
    private var logger: Logger = PlatformLogger()

    fun setLogger(customLogger: Logger) {
        logger = customLogger
    }

    fun d(tag: String, message: String) = logger.d(tag, message)
    fun i(tag: String, message: String) = logger.i(tag, message)
    fun w(tag: String, message: String) = logger.w(tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        logger.e(tag, message, throwable)
}
