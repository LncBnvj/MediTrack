package com.meditrack.app.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.meditrack.app.MainActivity
import com.meditrack.app.R
import com.meditrack.app.data.DoseLog
import com.meditrack.app.scheduler.ReminderIntents
import com.meditrack.app.util.TimeUtils

object NotificationHelper {
    const val CHANNEL_ID = "dose_reminders"

    fun createChannels(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Dose reminders", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to take your medicine"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun action(context: Context, action: String, log: DoseLog): PendingIntent =
        ReminderIntents.pending(
            context,
            ReminderIntents.intent(context, action, logId = log.id),
            log.id.toInt()
        )

    @SuppressLint("MissingPermission")
    fun showReminder(context: Context, log: DoseLog, note: String) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return

        val body = buildString {
            append(log.dosageText)
            if (note.isNotBlank()) append(" · ").append(note)
            append(" · Scheduled for ").append(TimeUtils.format12h(log.scheduledTime))
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Time to take ${log.medicineName}")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppIntent(context))
            .addAction(0, "TAKEN", action(context, ReminderIntents.ACTION_TAKEN, log))
            .addAction(0, "SNOOZE 10 MIN", action(context, ReminderIntents.ACTION_SNOOZE, log))
            .addAction(0, "SKIP", action(context, ReminderIntents.ACTION_SKIP, log))
            .build()
        nm.notify(log.id.toInt(), notification)
    }

    /** Replaces the reminder notification (same id) with a missed-dose notice. */
    @SuppressLint("MissingPermission")
    fun showMissed(context: Context, log: DoseLog) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Missed dose: ${log.medicineName}")
            .setContentText(
                "You did not log your ${TimeUtils.format12h(log.scheduledTime)} dose. Tap to update your history."
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        nm.notify(log.id.toInt(), notification)
    }

    fun cancel(context: Context, logId: Long) {
        NotificationManagerCompat.from(context).cancel(logId.toInt())
    }
}
