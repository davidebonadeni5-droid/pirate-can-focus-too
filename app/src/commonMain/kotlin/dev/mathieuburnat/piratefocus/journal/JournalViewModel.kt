package dev.mathieuburnat.piratefocus.journal

import androidx.lifecycle.ViewModel
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.data.ShipClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class JournalUiState(
    val journal: Journal = Journal(),
    /** Aujourd'hui (numéro de jour local). */
    val today: Long = 0,
    /** La page affichée : aujourd'hui, ou un jour passé qu'on relit (et qu'on peut corriger). */
    val day: Long = 0,
    val quote: String = JournalQuotes.verdict(Verdict.PAGE_BLANCHE),
    /** Journal caché derrière les 7 coups sur la porte (réglable dans les paramètres). */
    val secret: Boolean = true,
    /** Petite réaction à afficher en toast. */
    val toast: String? = null,
    /** Grande intervention à afficher en pop-up. */
    val popup: String? = null,
) {
    val isToday: Boolean get() = day == today
}

/** Le journal sport contre boissons : une page par jour, sauvegardée. */
class JournalViewModel(
    private val saveGame: SaveGame,
    private val today: () -> Long = { ShipClock.dayOf(ShipClock.now()) },
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        today().let { day ->
            val journal = saveGame.loadJournal()
            JournalUiState(
                journal = journal,
                today = day,
                day = day,
                quote = JournalQuotes.verdict(journal.verdict(day)),
                secret = saveGame.loadJournalSecret(),
            )
        },
    )
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    /** +1 ou -1 sur une ligne de la page affichée. */
    fun change(category: Category, delta: Int) = setCount(category, _uiState.value.let { it.journal.count(it.day, category.id) } + delta)

    /** Fixe directement le nombre (« j'ai bu 6 bières », tapé au clavier). */
    fun setCount(category: Category, value: Int) {
        refreshToday()
        _uiState.update { state ->
            val before = state.journal.count(state.day, category.id)
            val drinksBefore = state.journal.total(state.day, Side.BOISSON)
            val journal = state.journal.set(state.day, category.id, value)
            val after = journal.count(state.day, category.id)
            if (after == before) return@update state

            val popup = if (category.side == Side.BOISSON) {
                JournalQuotes.intervention(drinksBefore, journal.total(state.day, Side.BOISSON))
            } else {
                null
            }
            val toast = when {
                popup != null -> null
                after > before -> JournalQuotes.reaction(category)
                else -> "Rayé du journal. Personne n'a rien vu."
            }
            state.copy(journal = journal, quote = quoteFor(state, journal), toast = toast, popup = popup)
        }
        save()
    }

    fun addCategory(label: String, side: Side) {
        val state = _uiState.value
        val journal = state.journal.addCategory(label, side)
        if (journal == state.journal) {
            _uiState.update { it.copy(toast = "Cette ligne existe déjà, ou elle n'a pas de nom, moussaillon.") }
            return
        }
        _uiState.update { it.copy(journal = journal, toast = "Nouvelle ligne au journal : ${journal.custom.last().label} !") }
        save()
    }

    fun removeCategory(category: Category) {
        _uiState.update { state ->
            val journal = state.journal.removeCategory(category.id)
            state.copy(journal = journal, quote = quoteFor(state, journal), toast = "« ${category.label} » passe par-dessus bord.")
        }
        save()
    }

    /** Feuilleter le journal : un jour plus tôt (-1) ou plus tard (+1), jamais dans le futur. */
    fun turnPage(delta: Int) {
        refreshToday()
        _uiState.update { state ->
            val day = (state.day + delta).coerceAtMost(state.today)
            state.copy(day = day, quote = JournalQuotes.verdict(state.journal.verdict(day)))
        }
    }

    fun openDay(day: Long) = turnPage((day - _uiState.value.day).toInt())

    fun setSecret(secret: Boolean) {
        _uiState.update { it.copy(secret = secret) }
        saveGame.saveJournalSecret(secret)
    }

    fun newQuote() = _uiState.update {
        it.copy(quote = JournalQuotes.verdict(it.journal.verdict(it.day), current = it.quote))
    }

    fun consumeToast() = _uiState.update { it.copy(toast = null) }

    fun dismissPopup() = _uiState.update { it.copy(popup = null) }

    /** Si minuit est passé appli ouverte, « aujourd'hui » avance (et la page affichée aussi, si c'était celle du jour). */
    private fun refreshToday() {
        val now = today()
        _uiState.update { state ->
            if (now == state.today) state else state.copy(today = now, day = if (state.isToday) now else state.day)
        }
    }

    private fun save() = saveGame.saveJournal(_uiState.value.journal)

    /** Le capitaine change d'avis seulement quand le verdict change. */
    private fun quoteFor(state: JournalUiState, journal: Journal): String =
        if (journal.verdict(state.day) != state.journal.verdict(state.day)) JournalQuotes.verdict(journal.verdict(state.day)) else state.quote
}
