package com.meditrack.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DoseStatus { PENDING, TAKEN, MISSED, SKIPPED }

/** One scheduled dose and what happened to it. Powers the History screen. */
@Entity(
    tableName = "dose_logs",
    indices = [Index(value = ["medicineId", "scheduledTime"], unique = true)]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val dosageText: String,
    val scheduledTime: Long,            // epoch millis
    val status: DoseStatus = DoseStatus.PENDING,
    val actionTime: Long? = null        // when the user responded
)
