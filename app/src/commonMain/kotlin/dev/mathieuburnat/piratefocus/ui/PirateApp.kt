package dev.mathieuburnat.piratefocus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mathieuburnat.piratefocus.data.Alarms
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.focus.FocusViewModel
import dev.mathieuburnat.piratefocus.journal.JournalViewModel
import dev.mathieuburnat.piratefocus.ui.theme.PirateFocusTheme

private enum class Screen { MENU, FOCUS, LOGBOOK, SHOP, JOURNAL, SETTINGS }

/** Racine de l'app, partagée par Android et iPhone : le menu de pirate et la navigation entre les écrans. */
@Composable
fun PirateApp(
    saveGame: SaveGame,
    alarms: Alarms,
    viewModel: FocusViewModel = viewModel { FocusViewModel(saveGame, alarms) },
    journalViewModel: JournalViewModel = viewModel { JournalViewModel(saveGame) },
) {
    var screen by rememberSaveable { mutableStateOf(Screen.MENU) }
    // Le journal secret se déverrouille à chaque lancement (7 coups sur la porte).
    var journalUnlocked by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val phase = state.timer.phase
    val guardReady = rememberGuardReady()
    val toaster = remember { Toaster() }

    PlatformBackHandler(enabled = screen != Screen.MENU) { screen = Screen.MENU }

    PirateFocusTheme {
        CompositionLocalProvider(LocalToaster provides toaster) {
            Box(Modifier.fillMaxSize()) {
                when (screen) {
                    Screen.MENU -> MenuScreen(
                        phase = phase,
                        doubloons = state.timer.doubloons,
                        inventory = state.inventory,
                        journalUnlocked = journalUnlocked,
                        onFocus = { screen = Screen.FOCUS },
                        onLogbook = { screen = Screen.LOGBOOK },
                        onShop = { screen = Screen.SHOP },
                        onUnlockJournal = { journalUnlocked = true },
                        onJournal = { screen = Screen.JOURNAL },
                        onSettings = { screen = Screen.SETTINGS },
                    )
                    Screen.FOCUS -> FocusRoute(viewModel, guardReady = guardReady, onMenu = { screen = Screen.MENU })
                    Screen.LOGBOOK -> LogbookScreen(history = state.history, onBack = { screen = Screen.MENU })
                    Screen.SHOP -> ShopScreen(
                        doubloons = state.timer.doubloons,
                        inventory = state.inventory,
                        onBuy = { toaster.show(viewModel.buy(it)) },
                        onToggle = viewModel::toggleItem,
                        onBack = { screen = Screen.MENU },
                    )
                    Screen.JOURNAL -> JournalScreen(journalViewModel, onBack = { screen = Screen.MENU })
                    Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.MENU })
                }
                ToastHost(toaster, Modifier.align(Alignment.BottomCenter).safeDrawingPadding())
            }
        }
    }
}
