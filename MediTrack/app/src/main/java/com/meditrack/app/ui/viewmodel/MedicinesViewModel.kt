package com.meditrack.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.Medicine
import com.meditrack.app.meditrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MedicinesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.meditrack.repository

    val query = MutableStateFlow("")

    val medicines: StateFlow<List<Medicine>> = combine(repo.medicines(), query) { list, q ->
        if (q.isBlank()) list else list.filter { it.name.contains(q.trim(), ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Total count, ignoring the search filter. */
    val totalCount: StateFlow<Int> = combine(repo.medicines(), query) { list, _ -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
