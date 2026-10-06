package dev.mathieuburnat.piratefocus.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Petit coffre clé/valeur : SharedPreferences sur Android, NSUserDefaults sur iPhone. */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

/** Les réveils de fin de traversée et de fin d'escale, même appli fermée. */
interface Alarms {
    fun schedule(id: Int, atMillis: Long, title: String, message: String)
    fun cancelAll()

    companion object {
        const val VOYAGE_END = 1
        const val BREAK_END = 2

        val None = object : Alarms {
            override fun schedule(id: Int, atMillis: Long, title: String, message: String) = Unit
            override fun cancelAll() = Unit
        }
    }
}

/** Le temps qui passe, en heure locale du téléphone. */
object ShipClock {
    fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private fun local(millis: Long) = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())

    /** Numéro du jour local (jours depuis le 1er janvier 1970). */
    fun dayOf(millis: Long): Long = local(millis).date.toEpochDays().toLong()

    /** « 06/10 14:32 » */
    fun shortDateTime(millis: Long): String {
        val t = local(millis)
        fun two(n: Int) = n.toString().padStart(2, '0')
        return "${two(t.dayOfMonth)}/${two(t.monthNumber)} ${two(t.hour)}:${two(t.minute)}"
    }
}
