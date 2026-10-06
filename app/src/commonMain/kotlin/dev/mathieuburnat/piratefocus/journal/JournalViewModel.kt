package dev.mathieuburnat.piratefocus.journal

import androidx.lifecycle.ViewModel
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.data.ShipClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class JournalUiState(
    val journal: JournalState = JournalState(),
    val quote: String = JournalQuotes.verdict(Verdict.PAGE_BLANCHE),
    /** Petite réaction à afficher en toast. */
    val toast: String? = null,
    /** Grande intervention à afficher en pop-up. */
    val popup: String? = null,
)

/** Les compteurs sont sauvegardés pour la journée : une nouvelle page s'ouvre chaque matin. */
class JournalViewModel(
    private val saveGame: SaveGame,
    private val today: () -> Long = { ShipClock.dayOf(ShipClock.now()) },
) : ViewModel() {

    private var day = today()

    private val _uiState = MutableStateFlow(
        saveGame.loadJournal(day).let { JournalUiState(journal = it, quote = JournalQuotes.verdict(it.verdict)) },
    )
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun add(entry: Entry) {
        _uiState.update { state ->
            val journal = freshPage(state).add(entry)
            val popup = if (entry.side == Side.BOISSON) JournalQuotes.intervention(journal.total(Side.BOISSON)) else null
            state.copy(
                journal = journal,
                quote = quoteFor(state, journal),
                toast = if (popup == null) JournalQuotes.reaction(entry) else null,
                popup = popup,
            )
        }
        save()
    }

    fun remove(entry: Entry) {
        _uiState.update { state ->
            val journal = freshPage(state).remove(entry)
            state.copy(journal = journal, quote = quoteFor(state, journal), toast = "Rayé du journal. Personne n'a rien vu.")
        }
        save()
    }

    fun newQuote() = _uiState.update {
        it.copy(quote = JournalQuotes.verdict(it.journal.verdict, current = it.quote))
    }

    fun consumeToast() = _uiState.update { it.copy(toast = null) }

    fun dismissPopup() = _uiState.update { it.copy(popup = null) }

    /** Si minuit est passé appli ouverte, on tourne la page avant d'écrire. */
    private fun freshPage(state: JournalUiState): JournalState {
        if (today() == day) return state.journal
        day = today()
        return JournalState()
    }

    private fun save() = saveGame.saveJournal(day, _uiState.value.journal)

    /** Le capitaine change d'avis seulement quand le verdict change. */
    private fun quoteFor(state: JournalUiState, journal: JournalState): String =
        if (journal.verdict != state.journal.verdict) JournalQuotes.verdict(journal.verdict) else state.quote
}
