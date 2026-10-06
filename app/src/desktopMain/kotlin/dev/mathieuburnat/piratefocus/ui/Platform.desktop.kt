package dev.mathieuburnat.piratefocus.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
actual fun KeepScreenOn(enabled: Boolean) = Unit

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

@Composable
actual fun rememberGuardReady(): Boolean = true

@Composable
actual fun GuardSettings() {
    Text("Le gardien du navire ne monte la garde que sur Android.", style = MaterialTheme.typography.bodySmall)
}
