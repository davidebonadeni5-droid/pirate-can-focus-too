package dev.mathieuburnat.piratefocus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.mathieuburnat.piratefocus.data.Alarms
import dev.mathieuburnat.piratefocus.data.JavaPrefsStore
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.ui.PirateApp
import dev.mathieuburnat.piratefocus.ui.theme.PirateFocusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** La version ordinateur (Windows, Mac, Linux). Pour tester sans téléphone : ./gradlew :app:run */
fun main() = application {
    val saveGame = SaveGame(JavaPrefsStore())
    var updateMessage by remember { mutableStateOf<String?>(null) }

    // À chaque lancement, le capitaine regarde à la longue-vue si une nouvelle version est arrivée.
    LaunchedEffect(Unit) {
        val update = withContext(Dispatchers.IO) { DesktopUpdater.findUpdate() } ?: return@LaunchedEffect
        updateMessage = "Nouvelle version en vue ! Le capitaine hisse les voiles... (mise à jour 1.0.${update.build})"
        val installed = withContext(Dispatchers.IO) { runCatching { DesktopUpdater.install(update) }.isSuccess }
        if (installed) exitApplication() else updateMessage = null
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Pirate Focus",
        state = rememberWindowState(width = 420.dp, height = 860.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            PirateApp(saveGame, Alarms.None)
            updateMessage?.let { message ->
                PirateFocusTheme {
                    Text(
                        message,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.surface, RectangleShape)
                            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RectangleShape)
                            .padding(12.dp),
                    )
                }
            }
        }
    }
}
