package at.rudeboy.ferratafit.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import at.rudeboy.ferratafit.MainActivity
import at.rudeboy.ferratafit.R
import at.rudeboy.ferratafit.data.AppState
import at.rudeboy.ferratafit.data.Journey
import at.rudeboy.ferratafit.data.StageKind
import at.rudeboy.ferratafit.data.Store
import java.util.Calendar

/**
 * Tägliche Erinnerung an die offene Etappe.
 *
 * Ohne Anstupser lebt ein Etappensystem nicht: Wer nicht in die App schaut, weiß nicht,
 * dass heute etwas ansteht — und die Serie reißt.
 *
 * Zur Technik: Es kommt [AlarmManager] zum Einsatz, nicht WorkManager. Der zöge Room und
 * SQLite in eine App, die bewusst ohne Datenbank auskommt, und garantiert trotzdem keine
 * Uhrzeit — periodische Arbeit hat 15 Minuten Mindestabstand und kann im Doze um Stunden
 * verrutschen.
 *
 * Gesetzt wird mit `setAndAllowWhileIdle`: Exakte Alarme verlangen ab Android 12 eine
 * eigene Freigabe, die Android 14 nur noch auf Nachfrage erteilt. Für eine Tageserinnerung
 * wäre das unverhältnismäßig — die paar Minuten Ungenauigkeit fallen nicht auf.
 */
object Reminders {

    const val CHANNEL_ID = "ferratafit.daily"
    private const val REQUEST_CODE = 4711

    /** Legt den Benachrichtigungskanal an. Mehrfaches Aufrufen ist unschädlich. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Tägliche Erinnerung",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Erinnert an die offene Etappe."
                setShowBadge(true)
            }
        )
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /**
     * Plant die nächste Erinnerung auf die angegebene Uhrzeit.
     * Liegt sie heute schon in der Vergangenheit, wird auf morgen gelegt.
     */
    fun schedule(context: Context, hour: Int, minute: Int) {
        ensureChannel(context)
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return

        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        runCatching {
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                pendingIntent(context)
            )
        }
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching { alarm.cancel(pendingIntent(context)) }
    }

    /** Setzt die Planung anhand des gespeicherten Profils neu auf. */
    fun rescheduleFrom(context: Context, state: AppState) {
        if (state.profile.reminderEnabled) {
            schedule(context, state.profile.reminderHour, state.profile.reminderMinute)
        } else {
            cancel(context)
        }
    }

    /**
     * Der Text der Erinnerung — nennt die konkrete Etappe, damit man weiß, worum es geht.
     * Reines Lesen: Der Zustand wird hier nie verändert.
     */
    fun buildText(state: AppState): Pair<String, String> {
        val stage = Journey.current(state.progress)
        val index = Journey.currentIndex(state.progress) + 1
        val body = when (stage.kind) {
            StageKind.STRENGTH -> "Etappe $index wartet: ${stage.title}. Rund ${
                Journey.estimateMinutes(stage).coerceAtLeast(35)
            } Minuten am Gerät."
            StageKind.MOBILITY -> "Etappe $index: ${stage.title} — ein paar Minuten Dehnen, mehr nicht."
            StageKind.ENDURANCE -> "Etappe $index: Rausgehen. Eine halbe Stunde reicht."
            StageKind.RECOVERY -> "Etappe $index: Runterkommen, langes Dehnen. Der Körper baut jetzt auf."
            // Kommt hier nie vor: Begehungen stehen außerhalb des Zyklus und werden nie
            // als offene Etappe angeboten. Die App erinnert nicht an Bergtouren.
            StageKind.FERRATA -> "Etappe $index steht an."
        }
        return "${stage.icon} ${stage.title}" to body
    }
}

/**
 * Zeigt die Erinnerung an und plant gleich die nächste.
 *
 * Der Alarm wird jeden Tag neu gesetzt, weil `setAndAllowWhileIdle` nur einmal auslöst —
 * ein wiederholender Alarm wäre im Doze deutlich unzuverlässiger.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = runCatching { Store(context).state.value }.getOrNull() ?: return

        // Nächsten Tag einplanen, bevor irgendetwas schiefgehen kann
        if (state.profile.reminderEnabled) {
            Reminders.schedule(context, state.profile.reminderHour, state.profile.reminderMinute)
        } else {
            return
        }

        // Wer heute schon trainiert hat, braucht keinen Anstupser
        if (state.profile.reminderSkipIfDone && trainedToday(state)) return

        Reminders.ensureChannel(context)
        val (title, body) = Reminders.buildText(state)

        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()

        // Ohne Freigabe wirft das eine Ausnahme statt still zu scheitern
        runCatching { NotificationManagerCompat.from(context).notify(1, notification) }
    }

    private fun trainedToday(state: AppState): Boolean {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return state.progress.any { it.at >= startOfDay }
    }
}

/**
 * Stellt die Erinnerung nach einem Neustart wieder her — und nach einem Update der App.
 *
 * Beides löscht gesetzte Alarme. Gerade das Update trifft diese App, weil sie sich selbst
 * aktualisiert: Ohne diesen Empfänger wäre die Erinnerung nach jedem Update stillschweigend weg.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                val state = runCatching { Store(context).state.value }.getOrNull() ?: return
                Reminders.rescheduleFrom(context, state)
            }
        }
    }
}
