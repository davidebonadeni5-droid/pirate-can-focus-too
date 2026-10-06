package dev.mathieuburnat.piratefocus.data

import dev.mathieuburnat.piratefocus.focus.FocusState
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.journal.Categories
import dev.mathieuburnat.piratefocus.journal.Journal
import dev.mathieuburnat.piratefocus.journal.JournalPage
import dev.mathieuburnat.piratefocus.logbook.Logbook
import dev.mathieuburnat.piratefocus.logbook.Voyage
import dev.mathieuburnat.piratefocus.shop.Inventory
import dev.mathieuburnat.piratefocus.shop.Shop

/** La sauvegarde de la partie : doublons, traversée en cours, carnet de bord, boutique, journal. */
class SaveGame(private val store: KeyValueStore) {

    fun loadTimer(): FocusState {
        val default = FocusState()
        val focusMinutes = int("timer.focusMinutes") ?: default.focusMinutes
        return FocusState(
            phase = store.getString("timer.phase")?.let { name -> Phase.entries.firstOrNull { it.name == name } } ?: Phase.IDLE,
            focusMinutes = focusMinutes,
            remainingSeconds = int("timer.totalSeconds") ?: (focusMinutes * 60),
            totalSeconds = int("timer.totalSeconds") ?: (focusMinutes * 60),
            endsAtMillis = store.getString("timer.endsAt")?.toLongOrNull() ?: 0,
            doubloons = int("timer.doubloons") ?: 0,
            voyages = int("timer.voyages") ?: 0,
        )
    }

    fun saveTimer(state: FocusState) {
        store.putString("timer.phase", state.phase.name)
        store.putString("timer.focusMinutes", state.focusMinutes.toString())
        store.putString("timer.totalSeconds", state.totalSeconds.toString())
        store.putString("timer.endsAt", state.endsAtMillis.toString())
        store.putString("timer.doubloons", state.doubloons.toString())
        store.putString("timer.voyages", state.voyages.toString())
    }

    fun loadHistory(): List<Voyage> = Logbook.decode(store.getString("logbook"))

    fun saveHistory(history: List<Voyage>) = store.putString("logbook", Logbook.encode(history))

    fun loadInventory(): Inventory {
        val owned = Shop.decode(store.getString("shop.owned"))
        return Inventory(owned = owned, equipped = Shop.decode(store.getString("shop.equipped")) intersect owned)
    }

    fun saveInventory(inventory: Inventory) {
        store.putString("shop.owned", Shop.encode(inventory.owned))
        store.putString("shop.equipped", Shop.encode(inventory.equipped))
    }

    /** Le journal : les lignes perso et une page par jour. */
    fun loadJournal(): Journal {
        val custom = Journal.decodeCategories(store.getString("journal.categories"))
        val stored = store.getString("journal.pages")
        if (stored != null) return Journal(custom, Journal.decodePages(stored))
        return Journal(custom, legacyPage())
    }

    fun saveJournal(journal: Journal) {
        store.putString("journal.categories", Journal.encodeCategories(journal.custom))
        store.putString("journal.pages", Journal.encodePages(journal.pages))
    }

    /** L'ancienne version ne gardait que la page du jour : on la récupère pour ne rien perdre. */
    private fun legacyPage(): Map<Long, JournalPage> {
        val day = store.getString("journal.day")?.toLongOrNull() ?: return emptyMap()
        val page = Categories.builtIn.fold(JournalPage()) { page, category ->
            page.set(category.id, int("journal.${category.id}") ?: 0)
        }
        return if (page.isEmpty) emptyMap() else mapOf(day to page)
    }

    /** Chacun choisit : journal caché derrière 7 coups sur la porte, ou visible dans le menu. */
    fun loadJournalSecret(): Boolean = store.getString("journal.secret") != "false"

    fun saveJournalSecret(secret: Boolean) = store.putString("journal.secret", secret.toString())

    private fun int(key: String): Int? = store.getString(key)?.toIntOrNull()
}

/** Un coffre en mémoire, pour les tests et les aperçus. */
class InMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) {
        values[key] = value
    }
}
