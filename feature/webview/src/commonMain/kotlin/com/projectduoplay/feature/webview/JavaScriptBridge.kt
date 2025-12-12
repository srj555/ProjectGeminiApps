package com.projectduoplay.feature.webview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * JavaScript bridge for web-to-native communication
 */
class JavaScriptBridge(
    private val controller: WebViewController,
    private val config: JavaScriptBridgeConfig = JavaScriptBridgeConfig()
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val json = Json { ignoreUnknownKeys = true }

    init {
        setupBridge()
    }

    private fun setupBridge() {
        controller.setJavaScriptMessageHandler { message ->
            handleMessage(message)
        }
    }

    private fun handleMessage(message: JavaScriptMessage) {
        scope.launch {
            try {
                val command = json.decodeFromString<BridgeCommand>(message.data)
                if (command.action in config.enabledCommands) {
                    processCommand(command)
                }
            } catch (e: Exception) {
                sendResponse(
                    BridgeResponse(
                        success = false,
                        error = "Invalid command format: ${e.message}"
                    )
                )
            }
        }
    }

    private fun processCommand(command: BridgeCommand) {
        when (command.action) {
            "navigateTo" -> {
                val url = command.data["url"] as? String
                url?.let { controller.loadUrl(it) }
                sendResponse(BridgeResponse(success = true, data = mapOf("navigated" to "true")))
            }
            "reload" -> {
                controller.reload()
                sendResponse(BridgeResponse(success = true))
            }
            "goBack" -> {
                controller.goBack()
                sendResponse(BridgeResponse(success = true))
            }
            "share" -> {
                // Platform-specific sharing implementation
                sendResponse(BridgeResponse(success = true))
            }
            "getDeviceInfo" -> {
                val deviceInfo = getDeviceInfo()
                sendResponse(BridgeResponse(success = true, data = deviceInfo))
            }
            else -> {
                sendResponse(
                    BridgeResponse(
                        success = false,
                        error = "Unknown command: ${command.action}"
                    )
                )
            }
        }
    }

    private fun sendResponse(response: BridgeResponse) {
        val responseJson = json.encodeToString(response)
        val script = """
            if (window.${config.bridgeName}Callback) {
                window.${config.bridgeName}Callback($responseJson);
            }
        """.trimIndent()
        controller.evaluateJavaScript(script)
    }

    /**
     * Call JavaScript function from native
     */
    fun callJavaScriptFunction(functionName: String, vararg args: Any) {
        val argsString = args.joinToString(",") { arg ->
            when (arg) {
                is String -> "\"$arg\""
                else -> arg.toString()
            }
        }
        controller.evaluateJavaScript("$functionName($argsString)")
    }

    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "platform" to getPlatformName(),
            "version" to "1.0.0"
        )
    }
}

@Serializable
data class BridgeCommand(
    val action: String,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class BridgeResponse(
    val success: Boolean,
    val data: Map<String, String>? = null,
    val error: String? = null
)

expect fun getPlatformName(): String
