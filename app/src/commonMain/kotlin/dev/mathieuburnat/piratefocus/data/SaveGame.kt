package dev.mathieuburnat.piratefocus.data

import dev.mathieuburnat.piratefocus.focus.FocusState
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.journal.Entry
import dev.mathieuburnat.piratefocus.journal.JournalState
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

    /** Le journal secret est celui du jour : une nouvelle page chaque matin. */
    fun loadJournal(today: Long): JournalState {
        if (store.getString("journal.day")?.toLongOrNull() != today) return JournalState()
        val counts = Entry.entries.associateWith { int("journal.${it.name}") ?: 0 }.filterValues { it > 0 }
        return JournalState(counts)
    }

    fun saveJournal(today: Long, journal: JournalState) {
        store.putString("journal.day", today.toString())
        Entry.entries.forEach { store.putString("journal.${it.name}", journal.count(it).toString()) }
    }

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
