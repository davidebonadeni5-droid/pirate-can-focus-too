package dev.mathieuburnat.piratefocus.focus

import kotlin.test.Test
import kotlin.test.assertEquals

class FocusTimerTest {

    private val minute = 60_000L

    @Test
    fun `finir une traversée rapporte des doublons et passe en escale`() {
        var state = FocusTimer.setSail(FocusTimer.selectDuration(FocusState(), 15), now = 0)
        state = FocusTimer.tick(state, now = 15 * minute)

        assertEquals(Phase.BREAK, state.phase)
        assertEquals(15, state.doubloons)
        assertEquals(1, state.voyages)
        assertEquals(5 * 60, state.remainingSeconds)
    }

    @Test
    fun `le décompte se recalcule sur l'horloge`() {
        val state = FocusTimer.setSail(FocusTimer.selectDuration(FocusState(), 10), now = 0)

        assertEquals(10 * 60, FocusTimer.tick(state, now = 0).remainingSeconds)
        assertEquals(10 * 60 - 1, FocusTimer.tick(state, now = 1_000).remainingSeconds)
        // Une demi-seconde entamée compte encore comme une seconde à attendre.
        assertEquals(10 * 60 - 1, FocusTimer.tick(state, now = 1_500).remainingSeconds)
        assertEquals(1, FocusTimer.tick(state, now = 10 * minute - 1).remainingSeconds)
    }

    @Test
    fun `l'appli fermée pendant toute la traversée et l'escale rattrape tout`() {
        val state = FocusTimer.setSail(FocusTimer.selectDuration(FocusState(), 30), now = 0)
        val after = FocusTimer.tick(state, now = 3 * 60 * minute)

        assertEquals(Phase.IDLE, after.phase)
        assertEquals(30, after.doubloons)
        assertEquals(1, after.voyages)
        assertEquals(30 * 60, after.remainingSeconds)
    }

    @Test
    fun `la fin de l'escale ramène au port`() {
        val state = FocusState(phase = Phase.BREAK, remainingSeconds = 1, totalSeconds = 300, endsAtMillis = 1_000)
        val after = FocusTimer.tick(state, now = 1_000)

        assertEquals(Phase.IDLE, after.phase)
        assertEquals(30 * 60, after.remainingSeconds)
    }

    @Test
    fun `une durée sur mesure est bornée`() {
        assertEquals(45, FocusTimer.selectDuration(FocusState(), 45).focusMinutes)
        assertEquals(FocusTimer.MAX_MINUTES, FocusTimer.selectDuration(FocusState(), 999).focusMinutes)
        assertEquals(FocusTimer.MIN_MINUTES, FocusTimer.selectDuration(FocusState(), 0).focusMinutes)
    }

    @Test
    fun `abandonner coule le navire sans récompense`() {
        val state = FocusTimer.abandonShip(FocusTimer.setSail(FocusState(), now = 0))

        assertEquals(Phase.SUNK, state.phase)
        assertEquals(0, state.doubloons)
        // Même si on attend, un navire coulé ne rapporte rien.
        assertEquals(state, FocusTimer.tick(state, now = 99 * minute))
    }

    @Test
    fun `les longues traversées ont un bonus`() {
        assertEquals(60, FocusTimer.rewardFor(50))
        assertEquals(30, FocusTimer.rewardFor(30))
    }

    @Test
    fun `format en minutes et secondes`() {
        assertEquals("12:34", FocusTimer.format(754))
        assertEquals("00:05", FocusTimer.format(5))
        assertEquals("0042", FocusTimer.pad(42, 4))
    }
}
