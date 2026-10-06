package dev.mathieuburnat.piratefocus.logbook

import kotlin.test.Test
import kotlin.test.assertEquals

class LogbookTest {

    /** Dans les tests, un « jour » dure 1000 ms : plus simple à lire. */
    private val dayOf: (Long) -> Long = { it / 1000 }

    private fun done(day: Long, minutes: Int = 25) = Voyage(endedAtMillis = day * 1000 + 500, minutes = minutes, completed = true)
    private fun sunk(day: Long) = Voyage(endedAtMillis = day * 1000 + 500, minutes = 3, completed = false)

    @Test
    fun `les statistiques du carnet`() {
        val history = listOf(done(1, 30), sunk(2), done(10, 20), done(10, 10))
        val stats = Logbook.stats(history, today = 10, dayOf)

        assertEquals(3, stats.voyages)
        assertEquals(1, stats.sunk)
        assertEquals(60, stats.minutes)
        assertEquals(30, stats.todayMinutes)
    }

    @Test
    fun `la série de jours`() {
        val history = listOf(done(1), done(2), done(3), done(7), done(8))

        assertEquals(2, Logbook.stats(history, today = 8, dayOf).currentStreak)
        // Pas encore navigué aujourd'hui : la série d'hier tient toujours.
        assertEquals(2, Logbook.stats(history, today = 9, dayOf).currentStreak)
        // Un jour sans navire, et la série retombe à zéro.
        assertEquals(0, Logbook.stats(history, today = 10, dayOf).currentStreak)
        assertEquals(3, Logbook.stats(history, today = 10, dayOf).bestStreak)
    }

    @Test
    fun `un naufrage ne compte pas dans la série`() {
        assertEquals(0, Logbook.stats(listOf(sunk(5)), today = 5, dayOf).currentStreak)
    }

    @Test
    fun `les sept derniers jours`() {
        val days = Logbook.lastDays(listOf(done(4, 10), done(10, 20), done(10, 5), sunk(10)), today = 10, dayOf)

        assertEquals((4L..10L).toList(), days.map { it.first })
        assertEquals(listOf(10, 0, 0, 0, 0, 0, 25), days.map { it.second })
    }

    @Test
    fun `les jours de la semaine`() {
        assertEquals("jeu", Logbook.weekday(0)) // 1er janvier 1970
        assertEquals("lun", Logbook.weekday(4))
    }

    @Test
    fun `le carnet se sauvegarde et se relit`() {
        val history = listOf(done(1, 30), sunk(2))
        assertEquals(history, Logbook.decode(Logbook.encode(history)))
        assertEquals(emptyList(), Logbook.decode(null))
        assertEquals(emptyList(), Logbook.decode("n'importe quoi"))
    }

    @Test
    fun `le carnet garde les dernières traversées`() {
        var history = emptyList<Voyage>()
        repeat(Logbook.MAX_ENTRIES + 3) { history = Logbook.add(history, done(it.toLong())) }

        assertEquals(Logbook.MAX_ENTRIES, history.size)
        assertEquals(done(3), history.first())
    }
}
