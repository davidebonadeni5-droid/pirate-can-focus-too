package dev.mathieuburnat.piratefocus

import androidx.compose.ui.window.ComposeUIViewController
import dev.mathieuburnat.piratefocus.data.IosAlarms
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.data.UserDefaultsStore
import dev.mathieuburnat.piratefocus.ui.PirateApp
import platform.UIKit.UIViewController

/** Point d'entrée de l'app iPhone, appelé depuis iosApp/iosApp/ContentView.swift. */
fun MainViewController(): UIViewController {
    val saveGame = SaveGame(UserDefaultsStore())
    val alarms = IosAlarms()
    return ComposeUIViewController { PirateApp(saveGame, alarms) }
}
