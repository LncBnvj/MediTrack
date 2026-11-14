package com.meditrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditrack.app.data.DoseLog
import com.meditrack.app.data.DoseStatus
import com.meditrack.app.ui.components.AppCard
import com.meditrack.app.ui.components.AppChip
import com.meditrack.app.ui.components.IconBubble
import com.meditrack.app.ui.components.Pill
import com.meditrack.app.ui.components.ScreenTitle
import com.meditrack.app.ui.theme.Danger
import com.meditrack.app.ui.theme.DangerLight
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Outline
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryLight
import com.meditrack.app.ui.theme.Success
import com.meditrack.app.ui.theme.SuccessLight
import com.meditrack.app.ui.viewmodel.HistoryFilter
import com.meditrack.app.ui.viewmodel.HistoryViewModel
import com.meditrack.app.util.TimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(vm: HistoryViewModel = viewModel()) {
    val logs by vm.visible.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsState()
    var confirmLogId by remember { mutableStateOf<Long?>(null) }

    val grouped = logs.groupBy { TimeUtils.toLocalDate(it.scheduledTime) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle("History", "Last 7 days") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppChip("All", { vm.filter.value = HistoryFilter.ALL }, selected = filter == HistoryFilter.ALL)
                AppChip("Taken", { vm.filter.value = HistoryFilter.TAKEN }, selected = filter == HistoryFilter.TAKEN)
                AppChip("Missed", { vm.filter.value = HistoryFilter.MISSED }, selected = filter == HistoryFilter.MISSED)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${stats.taken}", "Taken", SuccessLight, Success, Modifier.weight(1f))
                StatTile("${stats.missed}", "Missed", DangerLight, Danger, Modifier.weight(1f))
                StatTile("${stats.adherence}%", "Adherence", PrimaryLight, Primary, Modifier.weight(1f))
            }
        }
        if (logs.isEmpty()) {
            item { Text("No records yet.", color = Muted, fontSize = 13.sp) }
        }
        grouped.forEach { (date, dayLogs) ->
            item(key = "header-$date") { DayHeader(date) }
            items(dayLogs, key = { it.id }) { log ->
                HistoryRow(log, onClick = {
                    if (log.status == DoseStatus.MISSED) confirmLogId = log.id
                })
            }
        }
    }

    confirmLogId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmLogId = null },
            title = { Text("Mark as taken?") },
            text = { Text("Did you actually take this dose late? This will update your history.") },
            confirmButton = {
                TextButton(onClick = { vm.markTakenLate(id); confirmLogId = null }) { Text("Yes, mark taken") }
            },
            dismissButton = { TextButton(onClick = { confirmLogId = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, bg: Color, fg: Color, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = fg)
        Text(label, fontSize = 12.sp, color = fg)
    }
}

@Composable
private fun DayHeader(date: LocalDate) {
    val today = LocalDate.now()
    val dateText = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())).uppercase()
    val label = when (date) {
        today -> "TODAY · $dateText"
        today.minusDays(1) -> "YESTERDAY · $dateText"
        else -> dateText
    }
    Text(
        label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = Muted,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun HistoryRow(log: DoseLog, onClick: () -> Unit) {
    val scheduled = TimeUtils.format12h(log.scheduledTime)
    val (icon, bg, fg) = when (log.status) {
        DoseStatus.TAKEN -> Triple(Icons.Filled.Check, SuccessLight, Success)
        DoseStatus.MISSED -> Triple(Icons.Filled.Close, DangerLight, Danger)
        else -> Triple(Icons.Filled.SkipNext, Outline, Muted)
    }
    val detail = when (log.status) {
        DoseStatus.TAKEN -> "$scheduled · taken at ${log.actionTime?.let { TimeUtils.format12h(it) } ?: scheduled}"
        DoseStatus.MISSED -> "$scheduled · no response"
        DoseStatus.SKIPPED -> "$scheduled · skipped"
        DoseStatus.PENDING -> scheduled
    }
    AppCard(onClick = if (log.status == DoseStatus.MISSED) onClick else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon, background = bg, tint = fg)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    "${log.medicineName} ${log.dosageText}",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink
                )
                Text(detail, fontSize = 12.sp, color = Muted)
            }
            when (log.status) {
                DoseStatus.TAKEN -> Pill("Taken", SuccessLight, Success)
                DoseStatus.MISSED -> Pill("Missed", DangerLight, Danger)
                DoseStatus.SKIPPED -> Pill("Skipped", Outline, Muted)
                DoseStatus.PENDING -> {}
            }
        }
    }
}
