package com.meditrack.app.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.Medicine
import com.meditrack.app.meditrack
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MedicineForm(
    val id: Long = 0,
    val name: String = "",
    val dosage: String = "",
    val unit: String = "mg",
    val frequency: String = "Daily",
    val times: List<String> = listOf("08:00"),
    val startDate: LocalDate = LocalDate.now(),
    val endDateEpochDay: Long? = null,   // preserved when editing (no UI yet)
    val notes: String = "",
    val notify: Boolean = true,
    val stock: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class AddMedicineViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.meditrack.repository
    private var loadedId: Long? = null
    private var original: Medicine? = null

    var form by mutableStateOf(MedicineForm())
        private set

    val isEditing: Boolean get() = form.id != 0L
    val canSave: Boolean
        get() = form.name.isNotBlank() && form.dosage.isNotBlank() && form.times.isNotEmpty()

    fun update(block: (MedicineForm) -> MedicineForm) {
        form = block(form)
    }

    fun load(id: Long) {
        if (loadedId == id) return
        loadedId = id
        if (id == 0L) return
        viewModelScope.launch {
            repo.getMedicine(id)?.let { m ->
                original = m
                form = MedicineForm(
                    id = m.id,
                    name = m.name,
                    dosage = m.dosage,
                    unit = m.unit,
                    frequency = m.frequency,
                    times = m.times,
                    startDate = LocalDate.ofEpochDay(m.startDateEpochDay),
                    endDateEpochDay = m.endDateEpochDay,
                    notes = m.notes,
                    notify = m.notifyEnabled,
                    stock = m.stockLeft?.toString() ?: "",
                    createdAt = m.createdAt
                )
            }
        }
    }

    fun setFrequency(freq: String) = update {
        val times = when (freq) {
            "Daily" -> listOf(it.times.firstOrNull() ?: "08:00")
            "2+ daily" -> if (it.times.size >= 2) it.times else listOf("08:00", "20:00")
            "3+ daily" -> if (it.times.size >= 3) it.times else listOf("08:00", "14:00", "20:00")
            else -> it.times
        }
        it.copy(frequency = freq, times = times)
    }

    fun addTime(hhmm: String) = update {
        if (hhmm in it.times) it else it.copy(times = (it.times + hhmm).sorted(), frequency = "Custom")
    }

    fun removeTime(hhmm: String) = update {
        if (it.times.size <= 1) it else it.copy(times = it.times - hhmm, frequency = "Custom")
    }

    fun save(onDone: () -> Unit) {
        if (!canSave) return
        val f = form
        val medicine = Medicine(
            id = f.id,
            name = f.name.trim(),
            dosage = f.dosage.trim(),
            unit = f.unit,
            frequency = f.frequency,
            times = f.times.sorted(),
            startDateEpochDay = f.startDate.toEpochDay(),
            endDateEpochDay = f.endDateEpochDay,
            notes = f.notes.trim(),
            notifyEnabled = f.notify,
            stockLeft = f.stock.trim().toIntOrNull(),
            createdAt = f.createdAt
        )
        viewModelScope.launch {
            repo.saveMedicine(medicine)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val m = original ?: return
        viewModelScope.launch {
            repo.deleteMedicine(m)
            onDone()
        }
    }
}
