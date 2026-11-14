package com.meditrack.app.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meditrack.app.meditrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms are cleared on reboot, so rebuild them from the database. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = context.meditrack.repository
                repo.sweepOverdue()
                repo.rescheduleAll()
            } finally {
                result.finish()
            }
        }
    }
}
