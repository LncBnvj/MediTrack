package com.meditrack.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimes(times: List<String>): String = times.joinToString(",")

    @TypeConverter
    fun toTimes(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")
}
