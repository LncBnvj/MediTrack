package com.meditrack.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val formatter12h: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    /** "HH:mm" on a given date -> epoch millis. */
    fun toMillis(date: LocalDate, hhmm: String): Long =
        date.atTime(LocalTime.parse(hhmm)).atZone(zone).toInstant().toEpochMilli()

    fun startOfDay(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDay(date: LocalDate): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun minuteOfDay(hhmm: String): Int {
        val t = LocalTime.parse(hhmm)
        return t.hour * 60 + t.minute
    }

    fun hhmm(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour, minute)

    fun format12h(millis: Long): String =
        formatter12h.format(Instant.ofEpochMilli(millis).atZone(zone))

    fun format12h(hhmm: String): String =
        formatter12h.format(LocalTime.parse(hhmm))
}
