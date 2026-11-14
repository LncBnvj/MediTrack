package com.meditrack.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.DoseLog
import com.meditrack.app.data.DoseStatus
import com.meditrack.app.meditrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter { ALL, TAKEN, MISSED }

data class HistoryStats(val taken: Int = 0, val missed: Int = 0, val adherence: Int = 0)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.meditrack.repository

    val filter = MutableStateFlow(HistoryFilter.ALL)

    private val logs: StateFlow<List<DoseLog>> = repo.history(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<HistoryStats> = logs.map { list ->
        val taken = list.count { it.status == DoseStatus.TAKEN }
        val missed = list.count { it.status == DoseStatus.MISSED }
        val skipped = list.count { it.status == DoseStatus.SKIPPED }
        val total = taken + missed + skipped
        HistoryStats(taken, missed, if (total == 0) 0 else Math.round(taken * 100f / total))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryStats())

    val visible: StateFlow<List<DoseLog>> = combine(logs, filter) { list, f ->
        when (f) {
            HistoryFilter.ALL -> list
            HistoryFilter.TAKEN -> list.filter { it.status == DoseStatus.TAKEN }
            HistoryFilter.MISSED -> list.filter { it.status == DoseStatus.MISSED }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Lets the user correct a missed dose they actually took late. */
    fun markTakenLate(logId: Long) {
        viewModelScope.launch { repo.markTakenByLog(logId) }
    }
}
