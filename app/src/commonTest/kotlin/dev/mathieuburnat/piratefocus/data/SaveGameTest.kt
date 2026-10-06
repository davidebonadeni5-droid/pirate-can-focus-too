package dev.mathieuburnat.piratefocus.data

import dev.mathieuburnat.piratefocus.focus.FocusState
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.journal.Entry
import dev.mathieuburnat.piratefocus.journal.JournalState
import dev.mathieuburnat.piratefocus.shop.Inventory
import dev.mathieuburnat.piratefocus.shop.Item
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveGameTest {

    @Test
    fun `une partie neuve`() {
        val save = SaveGame(InMemoryStore())

        assertEquals(FocusState(), save.loadTimer())
        assertEquals(Inventory(), save.loadInventory())
        assertEquals(emptyList(), save.loadHistory())
    }

    @Test
    fun `la traversée en cours et les doublons survivent à un redémarrage`() {
        val store = InMemoryStore()
        val timer = FocusState(
            phase = Phase.FOCUS,
            focusMinutes = 45,
            remainingSeconds = 45 * 60,
            totalSeconds = 45 * 60,
            endsAtMillis = 123_456_789,
            doubloons = 77,
            voyages = 4,
        )
        SaveGame(store).saveTimer(timer)

        assertEquals(timer, SaveGame(store).loadTimer())
    }

    @Test
    fun `la boutique est sauvegardée`() {
        val store = InMemoryStore()
        val inventory = Inventory(owned = setOf(Item.PERROQUET, Item.GALION), equipped = setOf(Item.GALION))
        SaveGame(store).saveInventory(inventory)

        assertEquals(inventory, SaveGame(store).loadInventory())
    }

    @Test
    fun `le journal tourne la page chaque jour`() {
        val store = InMemoryStore()
        val journal = JournalState().add(Entry.BIERE).add(Entry.GRIMPE)
        SaveGame(store).saveJournal(today = 100, journal = journal)

        assertEquals(journal, SaveGame(store).loadJournal(today = 100))
        assertEquals(JournalState(), SaveGame(store).loadJournal(today = 101))
    }
}
