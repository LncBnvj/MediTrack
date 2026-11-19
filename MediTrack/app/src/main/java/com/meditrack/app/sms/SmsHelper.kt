package com.meditrack.app.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.meditrack.app.data.EmergencyContact

object SmsHelper {
    private const val TAG = "SmsHelper"

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION")
    private fun smsManager(context: Context): SmsManager =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }


    fun send(context: Context, contacts: List<EmergencyContact>, message: String): Int =
        send(context, contacts) { message }

    fun send(
        context: Context,
        contacts: List<EmergencyContact>,
        messageFor: (EmergencyContact) -> String
    ): Int {
        if (!hasPermission(context) || contacts.isEmpty()) return 0
        val sms = smsManager(context)
        var sent = 0
        contacts.forEach { contact ->
            try {
                val parts = sms.divideMessage(messageFor(contact))
                sms.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                sent++
            } catch (e: Exception) {
                Log.e(TAG, "SMS to ${contact.name} failed", e)
            }
        }
        return sent
    }
}