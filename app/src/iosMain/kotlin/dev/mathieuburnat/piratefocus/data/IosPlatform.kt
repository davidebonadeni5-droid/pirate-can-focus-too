package dev.mathieuburnat.piratefocus.data

import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/** Le coffre iPhone : NSUserDefaults, conservé lors des mises à jour de l'appli. */
class UserDefaultsStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun getString(key: String): String? = defaults.stringForKey(key)
    override fun putString(key: String, value: String) = defaults.setObject(value, forKey = key)
}

/** Notifications locales : l'iPhone sonne la fin de la traversée même appli fermée. */
class IosAlarms : Alarms {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun schedule(id: Int, atMillis: Long, title: String, message: String) {
        val seconds = (atMillis - ShipClock.now()) / 1000.0
        if (seconds <= 0) return
        // La première fois, l'iPhone demande l'autorisation ; ensuite la réponse est mémorisée.
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
            if (!granted) return@requestAuthorizationWithOptions
            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                setSound(UNNotificationSound.defaultSound)
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)
            center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(identifier(id), content, trigger), null)
        }
    }

    override fun cancelAll() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier(Alarms.VOYAGE_END), identifier(Alarms.BREAK_END)))
    }

    private fun identifier(id: Int) = "piratefocus.$id"
}
