package com.meditrack.app.data

enum class ScheduleStatus { TAKEN, SKIPPED, MISSED, UPCOMING }

/** A single row on the Today screen. */
data class ScheduleItem(
    val medicineId: Long,
    val name: String,
    val dosageLabel: String,
    val note: String,
    val scheduledMillis: Long,
    val status: ScheduleStatus
)
