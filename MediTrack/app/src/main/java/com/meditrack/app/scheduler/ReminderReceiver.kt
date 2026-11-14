package com.meditrack.app.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meditrack.app.meditrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Receives alarms and notification button taps (Taken / Snooze / Skip). */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(context, intent)
            } finally {
                result.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        val repo = context.meditrack.repository
        val medicineId = intent.getLongExtra(ReminderIntents.EXTRA_MEDICINE_ID, 0)
        val time = intent.getStringExtra(ReminderIntents.EXTRA_TIME) ?: ""
        val scheduled = intent.getLongExtra(ReminderIntents.EXTRA_SCHEDULED, 0)
        val logId = intent.getLongExtra(ReminderIntents.EXTRA_LOG_ID, 0)

        when (intent.action) {
            ReminderIntents.ACTION_REMIND -> repo.onReminderFired(medicineId, time, scheduled)
            ReminderIntents.ACTION_SHOW -> repo.showExisting(logId)
            ReminderIntents.ACTION_TAKEN -> repo.markTakenByLog(logId)
            ReminderIntents.ACTION_SKIP -> repo.markSkippedByLog(logId)
            ReminderIntents.ACTION_SNOOZE -> repo.snooze(logId)
            ReminderIntents.ACTION_MISSED_CHECK -> repo.onMissedCheck(logId)
        }
    }
}
