package com.meditrack.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.meditrack.app.data.Medicine
import com.meditrack.app.util.TimeUtils
import java.time.LocalDate

/** Wraps AlarmManager. One repeating-by-rescheduling alarm per (medicine, time of day). */
class ReminderScheduler(private val context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    private fun requestCode(medicineId: Long, time: String): Int =
        (medicineId * 10_000 + TimeUtils.minuteOfDay(time)).toInt()

    /** Next trigger time for this medicine/time strictly after [afterMillis], or null if the course ended. */
    fun nextOccurrence(
        medicine: Medicine,
        time: String,
        afterMillis: Long = System.currentTimeMillis()
    ): Long? {
        var date = maxOf(LocalDate.now(), LocalDate.ofEpochDay(medicine.startDateEpochDay))
        var at = TimeUtils.toMillis(date, time)
        if (at <= afterMillis) {
            date = date.plusDays(1)
            at = TimeUtils.toMillis(date, time)
        }
        val end = medicine.endDateEpochDay
        if (end != null && date.toEpochDay() > end) return null
        return at
    }

    fun scheduleMedicine(medicine: Medicine) {
        if (!medicine.notifyEnabled) return
        medicine.times.forEach { scheduleNext(medicine, it) }
    }

    fun scheduleNext(medicine: Medicine, time: String) {
        if (!medicine.notifyEnabled) return
        val at = nextOccurrence(medicine, time) ?: return
        val intent = ReminderIntents.intent(
            context, ReminderIntents.ACTION_REMIND,
            medicineId = medicine.id, time = time, scheduledMillis = at
        )
        setAlarm(at, ReminderIntents.pending(context, intent, requestCode(medicine.id, time)))
    }

    fun cancelMedicine(medicine: Medicine) {
        medicine.times.forEach { time ->
            val intent = ReminderIntents.intent(
                context, ReminderIntents.ACTION_REMIND, medicineId = medicine.id, time = time
            )
            ReminderIntents.pendingIfExists(context, intent, requestCode(medicine.id, time))?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    // ---- missed-dose check and snooze (one per dose log) ----

    fun scheduleMissedCheck(logId: Long, at: Long) {
        val intent = ReminderIntents.intent(context, ReminderIntents.ACTION_MISSED_CHECK, logId = logId)
        setAlarm(at, ReminderIntents.pending(context, intent, logId.toInt()))
    }

    fun cancelMissedCheck(logId: Long) {
        val intent = ReminderIntents.intent(context, ReminderIntents.ACTION_MISSED_CHECK, logId = logId)
        ReminderIntents.pendingIfExists(context, intent, logId.toInt())?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun scheduleSnooze(logId: Long, at: Long) {
        val intent = ReminderIntents.intent(context, ReminderIntents.ACTION_SHOW, logId = logId)
        setAlarm(at, ReminderIntents.pending(context, intent, logId.toInt()))
    }

    private fun setAlarm(triggerAt: Long, pi: PendingIntent) {
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
