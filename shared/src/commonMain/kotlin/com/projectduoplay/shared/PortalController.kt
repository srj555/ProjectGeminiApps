package com.projectduoplay.shared

import com.projectduoplay.feature.webview.LauncherUrlResult
import com.projectduoplay.feature.webview.normalizeLauncherUrl
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PortalDestinationStore {
    fun load(): String?
    fun save(url: String)
    fun clear()
}

class SettingsPortalDestinationStore(
    private val settings: Settings = Settings()
) : PortalDestinationStore {
    override fun load(): String? = settings.getStringOrNull(KEY)
    override fun save(url: String) = settings.putString(KEY, url)
    override fun clear() = settings.remove(KEY)

    private companion object {
        const val KEY = "portal.saved_https_destination"
    }
}

data class PortalState(
    val savedUrl: String? = null,
    val input: String = "",
    val error: String? = null,
    val settingsVisible: Boolean = savedUrl == null,
    val resetConfirmationVisible: Boolean = false,
    val clearDataConfirmationVisible: Boolean = false,
    val message: String? = null,
)

class PortalController(private val store: PortalDestinationStore) {
    private val restored = store.load()?.let(::normalizeLauncherUrl)
    private val initialUrl = (restored as? LauncherUrlResult.Valid)?.url
    private val _state = MutableStateFlow(
        PortalState(
            savedUrl = initialUrl,
            input = initialUrl.orEmpty(),
            error = if (restored is LauncherUrlResult.Invalid) {
                "The saved destination is invalid. Choose a new HTTPS address."
            } else null,
        )
    )
    val state: StateFlow<PortalState> = _state.asStateFlow()

    fun updateInput(value: String) = update { copy(input = value, error = null) }

    fun openInput() = saveAndOpen(_state.value.input)

    fun openQrPayload(value: String) = saveAndOpen(value)

    private fun saveAndOpen(value: String) {
        when (val result = normalizeLauncherUrl(value)) {
            is LauncherUrlResult.Valid -> {
                store.save(result.url)
                update {
                    copy(
                        savedUrl = result.url,
                        input = result.url,
                        error = null,
                        settingsVisible = false,
                        message = null,
                    )
                }
            }
            is LauncherUrlResult.Invalid -> update {
                copy(error = result.message, message = null)
            }
        }
    }

    fun showSettings() = update {
        copy(settingsVisible = true, input = savedUrl.orEmpty(), error = null)
    }

    fun closeSettings() {
        if (_state.value.savedUrl != null) update { copy(settingsVisible = false) }
    }

    fun requestReset() = update { copy(resetConfirmationVisible = true) }
    fun cancelReset() = update { copy(resetConfirmationVisible = false) }

    fun confirmReset() {
        store.clear()
        update {
            PortalState(message = "Saved destination cleared. Website data was kept.")
        }
    }

    fun requestClearWebsiteData() = update { copy(clearDataConfirmationVisible = true) }
    fun cancelClearWebsiteData() = update { copy(clearDataConfirmationVisible = false) }
    fun confirmWebsiteDataCleared() = update {
        copy(
            clearDataConfirmationVisible = false,
            message = "Cookies and website storage cleared.",
        )
    }

    fun scannerUnavailable(message: String) = update {
        copy(error = message)
    }

    private inline fun update(transform: PortalState.() -> PortalState) {
        _state.value = _state.value.transform()
    }
}
