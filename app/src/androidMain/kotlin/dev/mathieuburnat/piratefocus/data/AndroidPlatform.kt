package dev.mathieuburnat.piratefocus.data

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dev.mathieuburnat.piratefocus.MainActivity
import dev.mathieuburnat.piratefocus.R

/** Le coffre Android : de simples SharedPreferences, conservées lors des mises à jour de l'appli. */
class PrefsStore(context: Context) : KeyValueStore {
    private val prefs = context.applicationContext.getSharedPreferences("savegame", Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
}

/** Programme les notifications de fin de traversée et d'escale avec AlarmManager. */
class AndroidAlarms(context: Context) : Alarms {
    private val context = context.applicationContext
    private val alarmManager = this.context.getSystemService(AlarmManager::class.java)

    override fun schedule(id: Int, atMillis: Long, title: String, message: String) {
        val intent = pendingIntent(id) {
            putExtra(AlarmReceiver.EXTRA_ID, id)
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, message)
        }
        // À la seconde près si le téléphone l'autorise, sinon à peu près (Android regroupe les réveils).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        }
    }

    override fun cancelAll() {
        listOf(Alarms.VOYAGE_END, Alarms.BREAK_END).forEach { alarmManager.cancel(pendingIntent(it) {}) }
    }

    private fun pendingIntent(id: Int, extras: Intent.() -> Unit): PendingIntent = PendingIntent.getBroadcast(
        context,
        id,
        Intent(context, AlarmReceiver::class.java).apply(extras),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

/** Sonne la cloche du navire : affiche la notification programmée par [AndroidAlarms]. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Fin de traversée", NotificationManager.IMPORTANCE_HIGH),
        )
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_skull)
            .setContentTitle(intent.getStringExtra(EXTRA_TITLE))
            .setContentText(intent.getStringExtra(EXTRA_MESSAGE))
            .setStyle(android.app.Notification.BigTextStyle().bigText(intent.getStringExtra(EXTRA_MESSAGE)))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_BASE + intent.getIntExtra(EXTRA_ID, 0), notification)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        private const val CHANNEL_ID = "voyages"

        /** Le gardien utilise déjà la notification n°1. */
        private const val NOTIFICATION_BASE = 100
    }
}
