package dev.mathieuburnat.piratefocus.journal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JournalTest {

    private val day = 100L

    private fun journal(vararg entries: Category) = entries.fold(Journal()) { journal, entry -> journal.add(day, entry.id) }

    @Test
    fun `les verdicts du capitaine`() {
        assertEquals(Verdict.PAGE_BLANCHE, journal().verdict(day))
        assertEquals(Verdict.ATHLETE, journal(Categories.GRIMPE).verdict(day))
        assertEquals(Verdict.PILIER_DE_TAVERNE, journal(Categories.BIERE).verdict(day))
        assertEquals(Verdict.EPONGE, journal(Categories.BIERE, Categories.VIN, Categories.COCKTAIL).verdict(day))
        assertEquals(Verdict.EQUILIBRE, journal(Categories.ABDOS, Categories.VIN).verdict(day))
        assertEquals(Verdict.SPORTIF, journal(Categories.ABDOS, Categories.SALLE, Categories.VIN).verdict(day))
        assertEquals(Verdict.PILIER_DE_TAVERNE, journal(Categories.ABDOS, Categories.VIN, Categories.BIERE).verdict(day))
    }

    @Test
    fun `les totaux par camp`() {
        val state = journal(Categories.BIERE, Categories.BIERE, Categories.SALLE)
        assertEquals(2, state.total(day, Side.BOISSON))
        assertEquals(1, state.total(day, Side.SPORT))
        assertEquals(2, state.count(day, Categories.BIERE.id))
    }

    @Test
    fun `écrire directement le nombre, borné entre 0 et 99`() {
        val state = Journal().set(day, Categories.BIERE.id, 6)
        assertEquals(6, state.count(day, Categories.BIERE.id))
        assertEquals(Categories.MAX_COUNT, state.set(day, Categories.BIERE.id, 500).count(day, Categories.BIERE.id))
        assertEquals(0, state.add(day, Categories.BIERE.id, -10).count(day, Categories.BIERE.id))
    }

    @Test
    fun `chaque jour a sa page`() {
        val state = Journal().set(day, Categories.VIN.id, 2).set(day - 1, Categories.SALLE.id, 1)
        assertEquals(0, state.count(day, Categories.SALLE.id))
        assertEquals(1, state.count(day - 1, Categories.SALLE.id))
        assertEquals(listOf(day, day - 1), state.history())
        // Une page remise à zéro disparaît de l'historique.
        assertEquals(listOf(day - 1), state.set(day, Categories.VIN.id, 0).history())
    }

    @Test
    fun `les lignes perso`() {
        val state = Journal().addCategory("  Course  ", Side.SPORT).addCategory("Shot", Side.BOISSON)
        val course = state.categories(Side.SPORT).last()
        assertEquals("Course", course.label)
        assertTrue(course.custom)

        // Pas de doublon, pas de ligne sans nom.
        assertEquals(state, state.addCategory("course", Side.SPORT))
        assertEquals(state, state.addCategory("   ", Side.BOISSON))

        val counted = state.add(day, course.id, 3)
        assertEquals(3, counted.total(day, Side.SPORT))

        // Jeter une ligne perso efface ses compteurs ; les lignes fournies restent.
        val removed = counted.removeCategory(course.id)
        assertEquals(0, removed.total(day, Side.SPORT))
        assertEquals(removed, removed.removeCategory(Categories.BIERE.id))
    }

    @Test
    fun `le journal se sauvegarde et se relit`() {
        val state = Journal().addCategory("Shot; tequila", Side.BOISSON).let { journal ->
            journal.set(day, journal.custom.first().id, 4).set(day - 3, Categories.GRIMPE.id, 2)
        }
        val reloaded = Journal(
            Journal.decodeCategories(Journal.encodeCategories(state.custom)),
            Journal.decodePages(Journal.encodePages(state.pages)),
        )
        assertEquals(state.pages, reloaded.pages)
        assertEquals("Shot, tequila", reloaded.custom.first().label)
    }

    @Test
    fun `le capitaine intervient tous les cinq verres`() {
        assertNull(JournalQuotes.intervention(3, 4))
        assertNotNull(JournalQuotes.intervention(4, 5))
        assertNotNull(JournalQuotes.intervention(9, 10))
        // Même en notant plusieurs verres d'un coup.
        assertNotNull(JournalQuotes.intervention(2, 7))
        // Et pas quand on en retire.
        assertNull(JournalQuotes.intervention(6, 5))
    }

    @Test
    fun `le capitaine réagit aussi aux lignes perso`() {
        val shot = Journal().addCategory("Shot", Side.BOISSON).custom.first()
        assertTrue(JournalQuotes.reaction(shot).isNotEmpty())
        assertTrue(JournalQuotes.reaction(Categories.SALLE).isNotEmpty())
    }

    @Test
    fun `on frappe sept fois à la porte du journal`() {
        assertNull(JournalQuotes.knock(6))
        assertNotNull(JournalQuotes.knock(1))
    }
}
