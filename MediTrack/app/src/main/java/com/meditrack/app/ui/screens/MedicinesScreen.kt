package com.meditrack.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditrack.app.data.Medicine
import com.meditrack.app.data.dosageLabel
import com.meditrack.app.ui.components.AppCard
import com.meditrack.app.ui.components.AppChip
import com.meditrack.app.ui.components.IconBubble
import com.meditrack.app.ui.components.Pill
import com.meditrack.app.ui.components.ScreenTitle
import com.meditrack.app.ui.theme.CardSurface
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Outline
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryOn
import com.meditrack.app.ui.theme.Warning
import com.meditrack.app.ui.theme.WarningLight
import com.meditrack.app.ui.viewmodel.MedicinesViewModel
import com.meditrack.app.util.Config
import com.meditrack.app.util.TimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MedicinesScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    vm: MedicinesViewModel = viewModel()
) {
    val medicines by vm.medicines.collectAsStateWithLifecycle()
    val total by vm.totalCount.collectAsStateWithLifecycle()
    val query by vm.query.collectAsState()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenTitle(
                    "My Medicines",
                    "$total active medicine${if (total == 1) "" else "s"}"
                )
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.query.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search medicine") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Muted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        unfocusedBorderColor = Outline,
                        focusedBorderColor = Primary
                    )
                )
            }
            if (medicines.isEmpty()) {
                item {
                    Text(
                        if (total == 0) "No medicines yet. Tap + to add your first one."
                        else "No medicine matches your search.",
                        color = Muted, fontSize = 13.sp
                    )
                }
            }
            items(medicines, key = { it.id }) { medicine ->
                MedicineCard(medicine, onEdit = { onEdit(medicine.id) })
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            containerColor = Primary,
            contentColor = PrimaryOn,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "Add medicine") }
    }
}

@Composable
private fun MedicineCard(medicine: Medicine, onEdit: () -> Unit) {
    val until = medicine.endDateEpochDay?.let {
        " until ${LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("MMM d"))}"
    } ?: ""

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Filled.Medication)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(medicine.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text(
                    "${medicine.dosageLabel()} · ${medicine.frequency}$until",
                    fontSize = 12.sp, color = Muted
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit ${medicine.name}", tint = Muted)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                medicine.times.forEach { t ->
                    AppChip(TimeUtils.format12h(t), onClick = onEdit, leadingIcon = Icons.Filled.Schedule)
                }
            }
            medicine.stockLeft?.let { left ->
                if (left <= Config.LOW_STOCK_THRESHOLD) {
                    Pill("$left left · refill soon", WarningLight, Warning)
                } else {
                    Pill("$left left", Outline, Muted)
                }
            }
        }
    }
}
