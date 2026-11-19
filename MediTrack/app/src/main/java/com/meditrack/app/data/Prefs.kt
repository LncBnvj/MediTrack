package com.meditrack.app.data

import android.content.Context
import androidx.core.content.edit


class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("meditrack_prefs", Context.MODE_PRIVATE)


    var userName: String
        get() = sp.getString("user_name", "") ?: ""
        set(value) = sp.edit { putString("user_name", value) }


    var smsOnMissed: Boolean
        get() = sp.getBoolean("sms_on_missed", false)
        set(value) = sp.edit { putBoolean("sms_on_missed", value) }
}