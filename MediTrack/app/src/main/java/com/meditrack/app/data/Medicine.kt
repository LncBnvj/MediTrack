package com.meditrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,                 // e.g. "500"
    val unit: String,                   // e.g. "mg"
    val frequency: String,              // "Daily", "2+ daily", "3+ daily", "Custom"
    val times: List<String>,            // "HH:mm" (24h), stored via Converters
    val startDateEpochDay: Long,
    val endDateEpochDay: Long? = null,  // null = no end date
    val notes: String = "",             // e.g. "With breakfast"
    val notifyEnabled: Boolean = true,
    val stockLeft: Int? = null,         // optional remaining quantity
    val createdAt: Long = System.currentTimeMillis()
)

fun Medicine.dosageLabel(): String = "$dosage $unit".trim()

fun Medicine.occursOn(date: LocalDate): Boolean {
    val day = date.toEpochDay()
    return day >= startDateEpochDay && (endDateEpochDay == null || day <= endDateEpochDay)
}
