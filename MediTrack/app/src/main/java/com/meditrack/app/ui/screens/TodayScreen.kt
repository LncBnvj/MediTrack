package com.meditrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditrack.app.data.ScheduleItem
import com.meditrack.app.data.ScheduleStatus
import com.meditrack.app.ui.components.AppButton
import com.meditrack.app.ui.components.AppCard
import com.meditrack.app.ui.components.IconBubble
import com.meditrack.app.ui.components.Pill
import com.meditrack.app.ui.theme.Danger
import com.meditrack.app.ui.theme.DangerLight
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Outline
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.Success
import com.meditrack.app.ui.theme.SuccessLight
import com.meditrack.app.ui.theme.Warning
import com.meditrack.app.ui.theme.WarningLight
import com.meditrack.app.ui.viewmodel.TodayViewModel
import com.meditrack.app.util.TimeUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(onSeeAll: () -> Unit, vm: TodayViewModel = viewModel()) {
    val items by vm.schedule.collectAsStateWithLifecycle()
    val taken = items.count { it.status == ScheduleStatus.TAKEN }
    val nextIndex = items.indexOfFirst { it.status == ScheduleStatus.UPCOMING }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TodayHeader(vm.userName) }
        item { ProgressCard(taken, items.size, items.getOrNull(nextIndex)) }
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today's schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                TextButton(onClick = onSeeAll) { Text("See all", color = Primary) }
            }
        }
        if (items.isEmpty()) {
            item {
                Text(
                    "No doses scheduled for today. Add a medicine to get started.",
                    color = Muted, fontSize = 13.sp
                )
            }
        }
        itemsIndexed(items, key = { _, it -> "${it.medicineId}-${it.scheduledMillis}" }) { index, item ->
            ScheduleRow(
                item = item,
                isNext = index == nextIndex,
                onTaken = { vm.markTaken(item) },
                onSkip = { vm.skip(item) }
            )
        }
    }
}

@Composable
private fun TodayHeader(name: String) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(if (name.isBlank()) greeting else "$greeting, $name", color = Muted, fontSize = 13.sp)
            Text(
                "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink
            )
        }
        IconBubble(Icons.Filled.Notifications, size = 40.dp)
    }
}

@Composable
private fun ProgressCard(taken: Int, total: Int, next: ScheduleItem?) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Primary)
            .padding(18.dp)
    ) {
        Text("Today's progress", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            if (total == 0) "No doses today" else "$taken of $total doses taken",
            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else taken / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            next?.let {
                "Next: ${it.name} ${it.dosageLabel} at ${TimeUtils.format12h(it.scheduledMillis)}"
            } ?: "Nothing left to take today",
            color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp
        )
    }
}

@Composable
private fun ScheduleRow(
    item: ScheduleItem,
    isNext: Boolean,
    onTaken: () -> Unit,
    onSkip: () -> Unit
) {
    AppCard(highlighted = isNext) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.status == ScheduleStatus.TAKEN) {
                IconBubble(Icons.Filled.Check, background = SuccessLight, tint = Success)
            } else {
                IconBubble(Icons.Filled.Medication)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text(
                    if (item.note.isBlank()) item.dosageLabel else "${item.dosageLabel} · ${item.note}",
                    fontSize = 12.sp, color = Muted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    TimeUtils.format12h(item.scheduledMillis),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
                )
                Spacer(Modifier.height(4.dp))
                StatusPill(item, isNext)
            }
        }
        if (isNext) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppButton("Mark as taken", onTaken, Modifier.weight(1f))
                AppButton("Skip", onSkip, Modifier.weight(1f), filled = false, contentColor = Muted)
            }
        }
    }
}

@Composable
private fun StatusPill(item: ScheduleItem, isNext: Boolean) {
    when (item.status) {
        ScheduleStatus.TAKEN -> Pill("Taken", SuccessLight, Success)
        ScheduleStatus.SKIPPED -> Pill("Skipped", Outline, Muted)
        ScheduleStatus.MISSED -> Pill("Missed", DangerLight, Danger)
        ScheduleStatus.UPCOMING -> {
            if (isNext) {
                val mins = (item.scheduledMillis - System.currentTimeMillis()) / 60_000
                val label = when {
                    mins <= 0 -> "Due now"
                    mins < 60 -> "In $mins min"
                    else -> "In ${mins / 60} h"
                }
                Pill(label, WarningLight, Warning)
            } else {
                Pill("Upcoming", Outline, Muted)
            }
        }
    }
}
