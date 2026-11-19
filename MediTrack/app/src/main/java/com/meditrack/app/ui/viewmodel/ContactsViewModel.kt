package com.meditrack.app.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.EmergencyContact
import com.meditrack.app.meditrack
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.meditrack.repository

    val contacts: StateFlow<List<EmergencyContact>> = repo.contacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var smsOnMissed by mutableStateOf(repo.prefs.smsOnMissed)
        private set


    var userName by mutableStateOf(repo.prefs.userName)
        private set

    fun updateSmsOnMissed(enabled: Boolean) {
        smsOnMissed = enabled
        repo.prefs.smsOnMissed = enabled
    }

    fun updateUserName(value: String) {
        userName = value
        repo.prefs.userName = value.trim()
    }

    fun save(contact: EmergencyContact) {
        viewModelScope.launch { repo.saveContact(contact) }
    }

    fun delete(contact: EmergencyContact) {
        viewModelScope.launch { repo.deleteContact(contact) }
    }

    fun sendEmergency(onResult: (Int) -> Unit) {
        viewModelScope.launch { onResult(repo.sendEmergencySms()) }
    }
}