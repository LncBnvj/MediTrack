package com.meditrack.app.data

import android.content.Context
import androidx.core.content.edit

/** Small key-value settings (not medical data). */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("meditrack_prefs", Context.MODE_PRIVATE)

    /** Shown in the Today greeting. Leave blank to show no name. */
    var userName: String
        get() = sp.getString("user_name", "") ?: ""
        set(value) = sp.edit { putString("user_name", value) }

    /** "Send SMS after 2 missed doses" switch on the Contacts screen. */
    var smsOnMissed: Boolean
        get() = sp.getBoolean("sms_on_missed", false)
        set(value) = sp.edit { putBoolean("sms_on_missed", value) }
}
