package dev.mathieuburnat.piratefocus

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.mathieuburnat.piratefocus.data.Alarms
import dev.mathieuburnat.piratefocus.data.JavaPrefsStore
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.ui.PirateApp

/** La version ordinateur, pour tester l'appli sans téléphone : ./gradlew :app:run */
fun main() = application {
    val saveGame = SaveGame(JavaPrefsStore())
    Window(
        onCloseRequest = ::exitApplication,
        title = "Pirate Focus",
        state = rememberWindowState(width = 420.dp, height = 860.dp),
    ) {
        PirateApp(saveGame, Alarms.None)
    }
}
