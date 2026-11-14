package com.meditrack.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.ScheduleItem
import com.meditrack.app.meditrack
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.meditrack.repository

    val userName: String get() = repo.prefs.userName

    val schedule: StateFlow<List<ScheduleItem>> = repo.todaySchedule()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repo.sweepOverdue() }
    }

    fun markTaken(item: ScheduleItem) {
        viewModelScope.launch { repo.markTaken(item.medicineId, item.scheduledMillis) }
    }

    fun skip(item: ScheduleItem) {
        viewModelScope.launch { repo.markSkipped(item.medicineId, item.scheduledMillis) }
    }
}
