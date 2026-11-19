package com.meditrack.app.data

import android.content.Context
import com.meditrack.app.notification.NotificationHelper
import com.meditrack.app.scheduler.ReminderScheduler
import com.meditrack.app.sms.SmsHelper
import com.meditrack.app.util.Config
import com.meditrack.app.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Single source of truth: Room + alarms + notifications + SMS. */
class MediTrackRepository(private val context: Context, db: AppDatabase) {
    private val medicineDao = db.medicineDao()
    private val logDao = db.doseLogDao()
    private val contactDao = db.contactDao()
    private val scheduler = ReminderScheduler(context)
    val prefs = Prefs(context)


    fun medicines(): Flow<List<Medicine>> = medicineDao.observeAll()

    suspend fun getMedicine(id: Long): Medicine? = medicineDao.getById(id)

    suspend fun saveMedicine(medicine: Medicine) {
        if (medicine.id != 0L) {
            medicineDao.getById(medicine.id)?.let { scheduler.cancelMedicine(it) }
            medicineDao.update(medicine)
            scheduler.scheduleMedicine(medicine)
        } else {
            val id = medicineDao.insert(medicine)
            scheduler.scheduleMedicine(medicine.copy(id = id))
        }
    }

    suspend fun deleteMedicine(medicine: Medicine) {
        scheduler.cancelMedicine(medicine)
        logDao.deletePendingFor(medicine.id)
        medicineDao.delete(medicine)
    }


    private fun minuteTicker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(60_000)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun todaySchedule(): Flow<List<ScheduleItem>> = minuteTicker().flatMapLatest {
        val today = LocalDate.now()
        val now = System.currentTimeMillis()
        combine(
            medicineDao.observeAll(),
            logDao.observeBetween(TimeUtils.startOfDay(today), TimeUtils.endOfDay(today))
        ) { meds, logs -> buildSchedule(meds, logs, today, now) }
    }

    private fun buildSchedule(
        meds: List<Medicine>,
        logs: List<DoseLog>,
        today: LocalDate,
        now: Long
    ): List<ScheduleItem> =
        meds.filter { it.occursOn(today) }.flatMap { m ->
            m.times.mapNotNull { t ->
                val at = TimeUtils.toMillis(today, t)
                val log = logs.firstOrNull { it.medicineId == m.id && it.scheduledTime == at }
                // Don't show doses that were already in the past when the medicine was added.
                if (log == null && at < m.createdAt) return@mapNotNull null
                val status = when {
                    log?.status == DoseStatus.TAKEN -> ScheduleStatus.TAKEN
                    log?.status == DoseStatus.SKIPPED -> ScheduleStatus.SKIPPED
                    log?.status == DoseStatus.MISSED -> ScheduleStatus.MISSED
                    at < now - Config.MISSED_AFTER_MS -> ScheduleStatus.MISSED
                    else -> ScheduleStatus.UPCOMING
                }
                ScheduleItem(m.id, m.name, m.dosageLabel(), m.notes, at, status)
            }
        }.sortedBy { it.scheduledMillis }


    fun history(days: Int = 7): Flow<List<DoseLog>> {
        val from = TimeUtils.startOfDay(LocalDate.now().minusDays((days - 1).toLong()))
        return logDao.observeSince(from)
    }

    /** Pending doses older than the missed window become MISSED (e.g. phone was off). */
    suspend fun sweepOverdue() {
        val cutoff = System.currentTimeMillis() - Config.MISSED_AFTER_MS
        logDao.getOverduePending(cutoff).forEach {
            logDao.setStatus(it.id, DoseStatus.MISSED, null)
            NotificationHelper.cancel(context, it.id)
        }
    }


    private suspend fun ensureLog(m: Medicine, scheduled: Long): DoseLog {
        logDao.find(m.id, scheduled)?.let { return it }
        logDao.insertIgnore(
            DoseLog(
                medicineId = m.id,
                medicineName = m.name,
                dosageText = m.dosageLabel(),
                scheduledTime = scheduled
            )
        )
        return logDao.find(m.id, scheduled)!!
    }

    private suspend fun applyStatus(log: DoseLog, status: DoseStatus) {
        if (log.status == status) return
        logDao.setStatus(log.id, status, System.currentTimeMillis())
        if (status == DoseStatus.TAKEN) medicineDao.decrementStock(log.medicineId)
        NotificationHelper.cancel(context, log.id)
        scheduler.cancelMissedCheck(log.id)
    }

    /** From the Today screen. */
    suspend fun markTaken(medicineId: Long, scheduled: Long) {
        val m = medicineDao.getById(medicineId) ?: return
        applyStatus(ensureLog(m, scheduled), DoseStatus.TAKEN)
    }

    suspend fun markSkipped(medicineId: Long, scheduled: Long) {
        val m = medicineDao.getById(medicineId) ?: return
        applyStatus(ensureLog(m, scheduled), DoseStatus.SKIPPED)
    }

    /** From notification buttons / History screen. */
    suspend fun markTakenByLog(logId: Long) {
        logDao.getById(logId)?.let { applyStatus(it, DoseStatus.TAKEN) }
    }

    suspend fun markSkippedByLog(logId: Long) {
        logDao.getById(logId)?.let { applyStatus(it, DoseStatus.SKIPPED) }
    }

    suspend fun snooze(logId: Long) {
        NotificationHelper.cancel(context, logId)
        scheduler.scheduleSnooze(logId, System.currentTimeMillis() + Config.SNOOZE_MS)
    }


    suspend fun onReminderFired(medicineId: Long, time: String, scheduled: Long) {
        val m = medicineDao.getById(medicineId) ?: return
        scheduler.scheduleNext(m, time)
        val log = ensureLog(m, scheduled)
        if (log.status != DoseStatus.PENDING) return
        NotificationHelper.showReminder(context, log, m.notes)
        scheduler.scheduleMissedCheck(log.id, scheduled + Config.MISSED_AFTER_MS)
    }

    suspend fun showExisting(logId: Long) {
        val log = logDao.getById(logId) ?: return
        if (log.status != DoseStatus.PENDING) return
        val note = medicineDao.getById(log.medicineId)?.notes ?: ""
        NotificationHelper.showReminder(context, log, note)
    }

    suspend fun onMissedCheck(logId: Long) {
        val log = logDao.getById(logId) ?: return
        if (log.status != DoseStatus.PENDING) return
        logDao.setStatus(logId, DoseStatus.MISSED, null)
        NotificationHelper.showMissed(context, log)
        maybeSendMissedAlert()
    }

    private fun contactLabel(contact: EmergencyContact): String =
        "${contact.name.trim()} (${contact.phone.trim()})"

    /** Sends an SMS exactly once per streak, when 2 doses in a row have been missed. */
    private suspend fun maybeSendMissedAlert() {
        if (!prefs.smsOnMissed) return
        val recent = logDao.lastN(6).filter { it.status != DoseStatus.PENDING }
        val streak = recent.takeWhile { it.status == DoseStatus.MISSED }.size
        if (streak != 2) return
        val who = prefs.userName.ifBlank { "The MediTrack user" }
        SmsHelper.send(context, contactDao.getAll()) { contact ->
            "MediTrack alert to ${contactLabel(contact)}: $who missed 2 scheduled medicine doses in a row. Please check on them."
        }
    }

    /** After reboot / app update / exact-alarm permission change. */
    suspend fun rescheduleAll() {
        medicineDao.getAll().forEach { scheduler.scheduleMedicine(it) }
        val now = System.currentTimeMillis()
        logDao.getPending().forEach {
            scheduler.scheduleMissedCheck(
                it.id, maxOf(now + 60_000, it.scheduledTime + Config.MISSED_AFTER_MS)
            )
        }
    }


    fun contacts(): Flow<List<EmergencyContact>> = contactDao.observeAll()

    suspend fun saveContact(contact: EmergencyContact) {
        if (contact.isPrimary) contactDao.clearPrimary()
        contactDao.upsert(contact)
    }

    suspend fun deleteContact(contact: EmergencyContact) = contactDao.delete(contact)

    /** Manual "send emergency SMS now". Returns number of contacts messaged. */
    suspend fun sendEmergencySms(): Int = withContext(Dispatchers.IO) {
        val who = prefs.userName.ifBlank { "A MediTrack user" }
        SmsHelper.send(context, contactDao.getAll()) { contact ->
            "MediTrack emergency SMS to ${contactLabel(contact)}: $who needs help. Please contact them as soon as possible."
        }
    }
}