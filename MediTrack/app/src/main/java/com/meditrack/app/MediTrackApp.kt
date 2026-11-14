package com.meditrack.app

import android.app.Application
import android.content.Context
import com.meditrack.app.data.AppDatabase
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.notification.NotificationHelper

class MediTrackApp : Application() {
    val database by lazy { AppDatabase.get(this) }
    val repository by lazy { MediTrackRepository(this, database) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}

/** Convenience accessor: context.meditrack.repository */
val Context.meditrack: MediTrackApp
    get() = applicationContext as MediTrackApp
