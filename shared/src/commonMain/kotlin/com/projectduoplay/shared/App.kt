package com.projectduoplay.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectduoplay.feature.webview.WebViewScreen
import com.projectduoplay.feature.webview.WebViewViewModel
import com.projectduoplay.core.common.AppConfigProvider
import com.projectduoplay.core.common.AppMode
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// Simple black and white color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    error = Color(0xFFFF4444),
    onError = Color.Black
)

@Composable
fun App(
    deepLinkUrl: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    MaterialTheme(
        colorScheme = DarkColorScheme
    ) {
        val appConfigProvider: AppConfigProvider = koinInject()
        val appConfig = remember(appConfigProvider) { appConfigProvider.getConfig() }
        val viewModel: WebViewViewModel = koinViewModel()

        if (appConfig.mode == AppMode.LAUNCHER) {
            PortalApp(viewModel = viewModel)
            return@MaterialTheme
        }
        var splashCompleted by remember { mutableStateOf(false) }

        LaunchedEffect(deepLinkUrl) {
            deepLinkUrl?.let {
                viewModel.handleDeepLink(it)
                onDeepLinkConsumed()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // Keep the WebView mounted while the branded splash is visible.
                WebViewScreen(viewModel = viewModel)

                if (!splashCompleted) {
                    SplashScreen(
                        appName = appConfig.appName,
                        appSubtitle = appConfig.appSubtitle,
                        onSplashComplete = {
                            splashCompleted = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PortalApp(viewModel: WebViewViewModel) {
    val controller = remember { PortalController(SettingsPortalDestinationStore()) }
    val state by controller.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        state.savedUrl?.let { destination ->
            WebViewScreen(
                viewModel = viewModel,
                initialUrl = destination,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (state.settingsVisible) {
            PortalSetupScreen(
                state = state,
                onInputChange = controller::updateInput,
                onPreview = controller::previewInput,
                onSave = controller::saveAndOpen,
                onClose = controller::closeSettings,
                onReset = controller::requestReset,
                onClearData = controller::requestClearWebsiteData,
                onReload = viewModel::reload,
            )
        } else {
            OutlinedButton(
                onClick = controller::showSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Text("Settings")
            }
        }

        if (state.resetConfirmationVisible) {
            AlertDialog(
                onDismissRequest = controller::cancelReset,
                title = { Text("Clear saved destination?") },
                text = { Text("Portal will return to setup. Cookies and website storage will be kept.") },
                confirmButton = {
                    TextButton(onClick = controller::confirmReset) { Text("Clear destination") }
                },
                dismissButton = {
                    TextButton(onClick = controller::cancelReset) { Text("Cancel") }
                },
            )
        }

        if (state.clearDataConfirmationVisible) {
            AlertDialog(
                onDismissRequest = controller::cancelClearWebsiteData,
                title = { Text("Clear website data?") },
                text = { Text("This removes cookies, browser cache, and website storage. Your saved destination remains.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearWebsiteData()
                        controller.confirmWebsiteDataCleared()
                    }) { Text("Clear website data") }
                },
                dismissButton = {
                    TextButton(onClick = controller::cancelClearWebsiteData) { Text("Cancel") }
                },
            )
        }
    }
}

@Composable
private fun PortalSetupScreen(
    state: PortalState,
    onInputChange: (String) -> Unit,
    onPreview: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onClearData: () -> Unit,
    onReload: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF070A12),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("PORTAL", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                "Your web, one tap away",
                color = Color(0xFF9FA8C5),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
            )
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChange,
                label = { Text("HTTPS website") },
                placeholder = { Text("example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
            }
            state.message?.let {
                Text(it, color = Color(0xFF9FA8C5), modifier = Modifier.padding(top = 10.dp))
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onPreview, modifier = Modifier.fillMaxWidth()) {
                Text("Review destination")
            }
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Scan QR — pending")
            }
            Text(
                "QR scanning is not enabled in this build. Enter the URL manually.",
                color = Color(0xFF78819D),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )

            state.pendingUrl?.let { pending ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    color = Color(0xFF151A2A),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Confirm destination", fontWeight = FontWeight.Bold)
                        Text(pending, color = Color(0xFFB8C4EE), modifier = Modifier.padding(vertical = 10.dp))
                        Text("Portal will save and open this HTTPS address. Scanned or typed text is never executed as code.", fontSize = 12.sp)
                        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Text("Save & open")
                        }
                    }
                }
            }

            if (state.savedUrl != null) {
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Back to site") }
                    TextButton(onClick = onReload, modifier = Modifier.weight(1f)) { Text("Reload") }
                }
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Reset URL") }
                    TextButton(onClick = onClearData, modifier = Modifier.weight(1f)) { Text("Clear website data") }
                }
            }
        }
    }
}
