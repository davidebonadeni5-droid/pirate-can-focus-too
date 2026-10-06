package dev.mathieuburnat.piratefocus.focus

enum class Phase {
    /** Au port, prêt à lever l'ancre. */
    IDLE,

    /** En pleine traversée : on se concentre. */
    FOCUS,

    /** Escale au port : pause méritée. */
    BREAK,

    /** Le navire a coulé (session abandonnée). */
    SUNK,
}

data class FocusState(
    val phase: Phase = Phase.IDLE,
    val focusMinutes: Int = 30,
    val breakMinutes: Int = 5,
    val remainingSeconds: Int = 30 * 60,
    val totalSeconds: Int = 30 * 60,
    /**
     * Heure de fin de la phase en cours (ms depuis 1970). Le décompte est recalculé à partir d'elle :
     * il ne dérive pas et survit à l'appli mise en veille ou fermée par le téléphone.
     */
    val endsAtMillis: Long = 0,
    /** Doublons dans le coffre (ce qui reste après les achats). */
    val doubloons: Int = 0,
    val voyages: Int = 0,
) {
    /** Avancement de la phase en cours, de 0f à 1f. */
    val progress: Float
        get() = if (totalSeconds == 0) 0f else 1f - remainingSeconds.toFloat() / totalSeconds

    val isRunning: Boolean
        get() = phase == Phase.FOCUS || phase == Phase.BREAK
}

/** Logique pure du minuteur, sans dépendance Android. L'heure est toujours passée en paramètre. */
object FocusTimer {

    const val MIN_MINUTES = 1
    const val MAX_MINUTES = 180

    fun selectDuration(state: FocusState, minutes: Int): FocusState {
        if (state.phase != Phase.IDLE) return state
        val safeMinutes = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        return state.copy(focusMinutes = safeMinutes, remainingSeconds = safeMinutes * 60, totalSeconds = safeMinutes * 60)
    }

    fun setSail(state: FocusState, now: Long): FocusState {
        val seconds = state.focusMinutes * 60
        return state.copy(phase = Phase.FOCUS, remainingSeconds = seconds, totalSeconds = seconds, endsAtMillis = now + seconds * 1000L)
    }

    /**
     * Met le minuteur à l'heure [now]. Si l'appli a dormi longtemps, on rattrape tout d'un coup :
     * la traversée finie rapporte ses doublons, et l'escale peut être finie elle aussi.
     */
    fun tick(state: FocusState, now: Long): FocusState {
        var current = state
        while (current.isRunning && now >= current.endsAtMillis) {
            current = when (current.phase) {
                Phase.FOCUS -> {
                    val breakSeconds = current.breakMinutes * 60
                    current.copy(
                        phase = Phase.BREAK,
                        totalSeconds = breakSeconds,
                        endsAtMillis = current.endsAtMillis + breakSeconds * 1000L,
                        doubloons = current.doubloons + rewardFor(current.focusMinutes),
                        voyages = current.voyages + 1,
                    )
                }
                else -> backToPort(current)
            }
        }
        if (!current.isRunning) return current
        val remaining = ((current.endsAtMillis - now + 999) / 1000).toInt()
        return current.copy(remainingSeconds = remaining.coerceAtMost(current.totalSeconds))
    }

    fun abandonShip(state: FocusState): FocusState =
        if (state.phase == Phase.FOCUS) state.copy(phase = Phase.SUNK, remainingSeconds = 0, endsAtMillis = 0) else state

    fun backToPort(state: FocusState): FocusState {
        val seconds = state.focusMinutes * 60
        return state.copy(phase = Phase.IDLE, remainingSeconds = seconds, totalSeconds = seconds, endsAtMillis = 0)
    }

    /** Un doublon par minute de concentration, plus un bonus pour les longues traversées. */
    fun rewardFor(minutes: Int): Int = minutes + if (minutes >= 50) 10 else 0

    fun format(seconds: Int): String = "${pad(seconds / 60)}:${pad(seconds % 60)}"

    /** Complète avec des zéros à gauche (pas de String.format en Kotlin multiplateforme). */
    fun pad(value: Int, length: Int = 2): String = value.toString().padStart(length, '0')
}
