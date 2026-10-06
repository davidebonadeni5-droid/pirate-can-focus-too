package dev.mathieuburnat.piratefocus.logbook

/** Une traversée terminée (ou coulée), notée dans le carnet de bord. */
data class Voyage(
    /** Fin de la traversée, en ms depuis 1970. */
    val endedAtMillis: Long,
    val minutes: Int,
    /** false : le navire a coulé en route. */
    val completed: Boolean,
)

data class LogbookStats(
    val voyages: Int = 0,
    val sunk: Int = 0,
    val minutes: Int = 0,
    val todayMinutes: Int = 0,
    /** Jours d'affilée avec au moins une traversée réussie (aujourd'hui compris s'il y en a une). */
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
)

/**
 * Logique pure du carnet de bord. Les jours sont des numéros de jour local
 * (jours depuis le 1er janvier 1970), fournis par [dayOf] pour rester testable.
 */
object Logbook {

    /** On garde les dernières traversées seulement, le coffre n'est pas infini. */
    const val MAX_ENTRIES = 500

    fun add(history: List<Voyage>, voyage: Voyage): List<Voyage> = (history + voyage).takeLast(MAX_ENTRIES)

    fun stats(history: List<Voyage>, today: Long, dayOf: (Long) -> Long): LogbookStats {
        val completed = history.filter { it.completed }
        val days = completed.map { dayOf(it.endedAtMillis) }.toSet()

        var best = 0
        var run = 0
        var previous: Long? = null
        for (day in days.sorted()) {
            run = if (previous != null && day == previous + 1) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }

        // La série tient toujours si on n'a pas encore navigué aujourd'hui : elle part d'hier.
        var current = 0
        var day = if (today in days) today else today - 1
        while (day in days) {
            current++
            day--
        }

        return LogbookStats(
            voyages = completed.size,
            sunk = history.size - completed.size,
            minutes = completed.sumOf { it.minutes },
            todayMinutes = completed.filter { dayOf(it.endedAtMillis) == today }.sumOf { it.minutes },
            currentStreak = current,
            bestStreak = best,
        )
    }

    /** Minutes de traversées réussies pour chacun des [count] derniers jours (le plus ancien d'abord). */
    fun lastDays(history: List<Voyage>, today: Long, dayOf: (Long) -> Long, count: Int = 7): List<Pair<Long, Int>> {
        val perDay = history.filter { it.completed }.groupBy { dayOf(it.endedAtMillis) }
        return (today - count + 1..today).map { day -> day to (perDay[day]?.sumOf { it.minutes } ?: 0) }
    }

    /** Jour de la semaine en abrégé (le 1er janvier 1970 était un jeudi). */
    fun weekday(day: Long): String = listOf("lun", "mar", "mer", "jeu", "ven", "sam", "dim")[((day % 7 + 7 + 3) % 7).toInt()]

    fun encode(history: List<Voyage>): String =
        history.joinToString("\n") { "${it.endedAtMillis};${it.minutes};${if (it.completed) 1 else 0}" }

    fun decode(text: String?): List<Voyage> = text.orEmpty().lineSequence().mapNotNull { line ->
        val parts = line.split(';')
        if (parts.size != 3) return@mapNotNull null
        Voyage(
            endedAtMillis = parts[0].toLongOrNull() ?: return@mapNotNull null,
            minutes = parts[1].toIntOrNull() ?: return@mapNotNull null,
            completed = parts[2] == "1",
        )
    }.toList()
}
