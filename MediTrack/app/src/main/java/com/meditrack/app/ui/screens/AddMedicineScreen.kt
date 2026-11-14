package com.meditrack.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditrack.app.ui.components.AppButton
import com.meditrack.app.ui.components.AppChip
import com.meditrack.app.ui.theme.Danger
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Outline
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.viewmodel.AddMedicineViewModel
import com.meditrack.app.util.TimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val units = listOf("mg", "mcg", "g", "ml", "IU", "tablet(s)", "capsule(s)")
private val frequencies = listOf("Daily", "2+ daily", "3+ daily", "Custom")

@Composable
fun AddMedicineScreen(
    medicineId: Long,
    onClose: () -> Unit,
    vm: AddMedicineViewModel = viewModel()
) {
    val context = LocalContext.current
    val form = vm.form
    LaunchedEffect(medicineId) { vm.load(medicineId) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
            }
            Text(
                if (vm.isEditing) "Edit Medicine" else "Add Medicine",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink
            )
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Field("Medicine name") {
                AppTextField(form.name, { v -> vm.update { it.copy(name = v) } }, "e.g. Metformin")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    Field("Dosage") {
                        AppTextField(
                            form.dosage,
                            { v -> vm.update { it.copy(dosage = v) } },
                            "500",
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    Field("Unit") {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            FieldBox(form.unit, Icons.Filled.ArrowDropDown) { expanded = true }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            vm.update { it.copy(unit = u) }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Field("Frequency") {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    frequencies.forEach { f ->
                        AppChip(f, onClick = { vm.setFrequency(f) }, selected = form.frequency == f)
                    }
                }
            }

            Field("Reminder times") {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    form.times.forEach { t ->
                        // Tap a time to remove it (at least one time is always kept)
                        AppChip(
                            TimeUtils.format12h(t),
                            onClick = { vm.removeTime(t) },
                            leadingIcon = Icons.Filled.Schedule,
                            trailingIcon = if (form.times.size > 1) Icons.Filled.Close else null
                        )
                    }
                    AppChip(
                        "Add time",
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> vm.addTime(TimeUtils.hhmm(h, m)) },
                                8, 0, false
                            ).show()
                        },
                        selected = false,
                        leadingIcon = Icons.Filled.Add
                    )
                }
            }

            Field("Start date") {
                FieldBox(
                    form.startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    Icons.Filled.CalendarToday
                ) {
                    val d = form.startDate
                    DatePickerDialog(
                        context,
                        { _, y, month, day -> vm.update { it.copy(startDate = LocalDate.of(y, month + 1, day)) } },
                        d.year, d.monthValue - 1, d.dayOfMonth
                    ).show()
                }
            }

            Field("Notes (optional)") {
                AppTextField(form.notes, { v -> vm.update { it.copy(notes = v) } }, "e.g. Take after meals")
            }

            Field("Quantity left (optional)") {
                AppTextField(
                    form.stock,
                    { v -> vm.update { it.copy(stock = v.filter { c -> c.isDigit() }) } },
                    "e.g. 30",
                    keyboardType = KeyboardType.Number
                )
            }

            // Notify switch row
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Outline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Notifications, null, tint = Primary)
                Text(
                    "Notify me at each reminder time",
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    fontSize = 14.sp, color = Ink
                )
                Switch(
                    checked = form.notify,
                    onCheckedChange = { v -> vm.update { it.copy(notify = v) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                )
            }

            if (vm.isEditing) {
                AppButton(
                    "Delete medicine",
                    onClick = { vm.delete(onClose) },
                    modifier = Modifier.fillMaxWidth(),
                    filled = false,
                    contentColor = Danger
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        AppButton(
            "Save medicine",
            onClick = { vm.save(onClose) },
            enabled = vm.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, color = Muted, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Muted) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            unfocusedBorderColor = Outline,
            focusedBorderColor = Primary
        )
    )
}

/** Read-only clickable box that looks like a text field (unit dropdown, date). */
@Composable
private fun FieldBox(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .border(1.dp, Outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, modifier = Modifier.weight(1f), fontSize = 15.sp, color = Ink)
        Icon(icon, null, tint = Muted)
    }
}
