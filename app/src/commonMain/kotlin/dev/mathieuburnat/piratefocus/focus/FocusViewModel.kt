package dev.mathieuburnat.piratefocus.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mathieuburnat.piratefocus.data.Alarms
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.data.ShipClock
import dev.mathieuburnat.piratefocus.logbook.Logbook
import dev.mathieuburnat.piratefocus.logbook.Voyage
import dev.mathieuburnat.piratefocus.shop.Inventory
import dev.mathieuburnat.piratefocus.shop.Item
import dev.mathieuburnat.piratefocus.shop.Purchase
import dev.mathieuburnat.piratefocus.shop.Shop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FocusUiState(
    val timer: FocusState = FocusState(),
    val quote: String = PirateQuotes.randomFor(Phase.IDLE),
    val history: List<Voyage> = emptyList(),
    val inventory: Inventory = Inventory(),
)

/**
 * L'état du navire : minuteur, doublons, carnet de bord et boutique.
 * Tout est sauvegardé, et la traversée en cours reprend même si le téléphone a fermé l'appli.
 */
class FocusViewModel(
    private val saveGame: SaveGame,
    private val alarms: Alarms,
    private val now: () -> Long = ShipClock::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FocusUiState(timer = saveGame.loadTimer(), history = saveGame.loadHistory(), inventory = saveGame.loadInventory()),
    )
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var ticker: Job? = null

    init {
        // Rattrape le temps passé appli fermée, puis reprend le décompte s'il reste de la mer.
        transition { FocusTimer.tick(it, now()) }
        if (_uiState.value.timer.isRunning) startTicking()
    }

    fun selectDuration(minutes: Int) = transition { FocusTimer.selectDuration(it, minutes) }

    fun setSail() {
        transition { FocusTimer.setSail(it, now()) }
        val timer = _uiState.value.timer
        alarms.schedule(Alarms.VOYAGE_END, timer.endsAtMillis, PirateQuotes.VOYAGE_DONE_TITLE, PirateQuotes.voyageDone(FocusTimer.rewardFor(timer.focusMinutes)))
        alarms.schedule(Alarms.BREAK_END, timer.endsAtMillis + timer.breakMinutes * 60_000L, PirateQuotes.BREAK_DONE_TITLE, PirateQuotes.BREAK_DONE)
        startTicking()
    }

    fun abandonShip() {
        ticker?.cancel()
        alarms.cancelAll()
        val timer = _uiState.value.timer
        if (timer.phase == Phase.FOCUS) {
            val sailed = ((timer.totalSeconds - timer.remainingSeconds) / 60).coerceAtLeast(0)
            record(Voyage(endedAtMillis = now(), minutes = sailed, completed = false))
        }
        transition(FocusTimer::abandonShip)
    }

    fun backToPort() {
        ticker?.cancel()
        alarms.cancelAll()
        transition(FocusTimer::backToPort)
    }

    /** Le capitaine change de réplique (au port, en escale, ou quand on tape sur la bulle). */
    fun newQuote() {
        _uiState.update { it.copy(quote = PirateQuotes.randomFor(it.timer.phase, current = it.quote)) }
    }

    /** Achète un objet. Renvoie la réplique du marchand. */
    fun buy(item: Item): String {
        val state = _uiState.value
        return when (val purchase = Shop.buy(state.timer.doubloons, state.inventory, item)) {
            is Purchase.Done -> {
                _uiState.update { it.copy(timer = it.timer.copy(doubloons = purchase.doubloons), inventory = purchase.inventory) }
                saveGame.saveTimer(_uiState.value.timer)
                saveGame.saveInventory(purchase.inventory)
                PirateQuotes.bought(item)
            }
            Purchase.TooPoor -> PirateQuotes.tooPoor(item.price - state.timer.doubloons)
            Purchase.AlreadyOwned -> PirateQuotes.ALREADY_OWNED
        }
    }

    fun toggleItem(item: Item) {
        _uiState.update { it.copy(inventory = Shop.toggle(it.inventory, item)) }
        saveGame.saveInventory(_uiState.value.inventory)
    }

    /**
     * Pas de décompte seconde par seconde : on relit l'horloge à chaque tour,
     * donc pas de dérive, et un retour de veille remet tout à l'heure aussitôt.
     */
    private fun startTicking() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                delay(250)
                transition { FocusTimer.tick(it, now()) }
                if (!_uiState.value.timer.isRunning) break
            }
        }
    }

    private fun record(voyage: Voyage) {
        _uiState.update { it.copy(history = Logbook.add(it.history, voyage)) }
        saveGame.saveHistory(_uiState.value.history)
    }

    /** Applique une transition, change de réplique quand la phase change, et sauvegarde ce qui compte. */
    private fun transition(change: (FocusState) -> FocusState) {
        val before = _uiState.value.timer
        _uiState.update { current ->
            val next = change(current.timer)
            val quote = if (next.phase != current.timer.phase) PirateQuotes.randomFor(next.phase) else current.quote
            current.copy(timer = next, quote = quote)
        }
        val after = _uiState.value.timer
        if (after.voyages > before.voyages) {
            // La traversée s'est finie à l'heure prévue, même si l'appli dormait à ce moment-là.
            record(Voyage(endedAtMillis = before.endsAtMillis, minutes = before.focusMinutes, completed = true))
        }
        // Le décompte seul ne vaut pas une sauvegarde : il se recalcule à partir de l'heure de fin.
        if (after.copy(remainingSeconds = 0) != before.copy(remainingSeconds = 0)) saveGame.saveTimer(after)
    }
}
