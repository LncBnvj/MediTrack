package com.meditrack.app.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/** Builds the intents/PendingIntents that wake ReminderReceiver. */
object ReminderIntents {
    const val ACTION_REMIND = "com.meditrack.app.action.REMIND"               // scheduled dose time
    const val ACTION_SHOW = "com.meditrack.app.action.SHOW"                   // re-show after snooze
    const val ACTION_TAKEN = "com.meditrack.app.action.TAKEN"
    const val ACTION_SKIP = "com.meditrack.app.action.SKIP"
    const val ACTION_SNOOZE = "com.meditrack.app.action.SNOOZE"
    const val ACTION_MISSED_CHECK = "com.meditrack.app.action.MISSED_CHECK"

    const val EXTRA_MEDICINE_ID = "medicine_id"
    const val EXTRA_TIME = "time"
    const val EXTRA_SCHEDULED = "scheduled_millis"
    const val EXTRA_LOG_ID = "log_id"

    fun intent(
        context: Context,
        action: String,
        medicineId: Long = 0,
        time: String = "",
        scheduledMillis: Long = 0,
        logId: Long = 0
    ): Intent = Intent(context, ReminderReceiver::class.java).apply {
        this.action = action
        putExtra(EXTRA_MEDICINE_ID, medicineId)
        putExtra(EXTRA_TIME, time)
        putExtra(EXTRA_SCHEDULED, scheduledMillis)
        putExtra(EXTRA_LOG_ID, logId)
    }

    fun pending(context: Context, intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun pendingIfExists(context: Context, intent: Intent, requestCode: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
}
