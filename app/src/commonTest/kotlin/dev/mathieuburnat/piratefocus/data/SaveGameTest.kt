package dev.mathieuburnat.piratefocus.data

import dev.mathieuburnat.piratefocus.focus.FocusState
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.journal.Categories
import dev.mathieuburnat.piratefocus.journal.Journal
import dev.mathieuburnat.piratefocus.journal.Side
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
    fun `le journal garde toutes ses pages et ses lignes perso`() {
        val store = InMemoryStore()
        val journal = Journal().addCategory("Course", Side.SPORT).let {
            it.set(100, Categories.BIERE.id, 3).set(99, it.custom.first().id, 1)
        }
        SaveGame(store).saveJournal(journal)

        assertEquals(journal, SaveGame(store).loadJournal())
    }

    @Test
    fun `la page du jour de l'ancienne version est récupérée`() {
        val store = InMemoryStore()
        store.putString("journal.day", "100")
        store.putString("journal.BIERE", "2")
        store.putString("journal.GRIMPE", "1")

        val journal = SaveGame(store).loadJournal()
        assertEquals(2, journal.count(100, Categories.BIERE.id))
        assertEquals(1, journal.count(100, Categories.GRIMPE.id))
    }

    @Test
    fun `le journal est secret par défaut, au choix de chacun`() {
        val store = InMemoryStore()
        assertEquals(true, SaveGame(store).loadJournalSecret())
        SaveGame(store).saveJournalSecret(false)
        assertEquals(false, SaveGame(store).loadJournalSecret())
    }
}
