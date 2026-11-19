package com.meditrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditrack.app.data.ScheduleItem
import com.meditrack.app.data.ScheduleStatus
import com.meditrack.app.ui.components.AppButton
import com.meditrack.app.ui.components.AppCard
import com.meditrack.app.ui.components.IconBubble
import com.meditrack.app.ui.components.MascotBubble
import com.meditrack.app.ui.components.Pill
import com.meditrack.app.ui.theme.Danger
import com.meditrack.app.ui.theme.DangerLight
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Outline
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryOn
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
    val next = items.getOrNull(nextIndex)
    val hasMissed = items.any { it.status == ScheduleStatus.MISSED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TodayHeader(vm.userName) }
        item { ProgressCard(taken, items.size, next) }
        item { MascotBubble(bearMessage(vm.userName, taken, items.size, next, hasMissed)) }
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today's schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                TextButton(onClick = onSeeAll) {
                    Text("See all", color = Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (items.isEmpty()) {
            item {
                Text(
                    "No doses scheduled for today. Add a medicine to get started.",
                    color = Muted, fontSize = 15.sp
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

/** What Bruno says, based on today's progress. */
private fun bearMessage(
    name: String,
    taken: Int,
    total: Int,
    next: ScheduleItem?,
    hasMissed: Boolean
): String {
    val who = if (name.isBlank()) "" else ", $name"
    return when {
        total == 0 -> "No doses today$who. Enjoy your rest!"
        taken == total -> "All done today$who! Bear-y proud of you!"
        hasMissed -> "Did you take your missed dose$who? Tap \"I took it late\"."
        taken == 0 && next != null -> "Let's start$who! First up: ${next.name} at ${TimeUtils.format12h(next.scheduledMillis)}."
        next != null -> "Bear with me$who. ${next.name} at ${TimeUtils.format12h(next.scheduledMillis)}!"
        else -> "Nothing left to take today$who. Nice work!"
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
    Column(Modifier.fillMaxWidth()) {
        Text(if (name.isBlank()) greeting else "$greeting, $name", color = Muted, fontSize = 15.sp)
        Text(
            "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink
        )
    }
}

@Composable
private fun ProgressCard(taken: Int, total: Int, next: ScheduleItem?) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Primary)
            .padding(20.dp)
    ) {
        Text("Today's progress", color = PrimaryOn.copy(alpha = 0.85f), fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            if (total == 0) "No doses today" else "$taken of $total doses taken",
            color = PrimaryOn, fontSize = 24.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        // One segment per dose
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (i < taken) PrimaryOn else PrimaryOn.copy(alpha = 0.25f))
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            next?.let {
                "Next: ${it.name} ${it.dosageLabel} at ${TimeUtils.format12h(it.scheduledMillis)}"
            } ?: "Nothing left to take today",
            color = PrimaryOn, fontSize = 15.sp
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
            when (item.status) {
                ScheduleStatus.TAKEN -> IconBubble(Icons.Filled.Check, background = SuccessLight, tint = Success)
                ScheduleStatus.MISSED -> IconBubble(Icons.Filled.Medication, background = DangerLight, tint = Danger)
                else -> IconBubble(Icons.Filled.Medication)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text(
                    if (item.note.isBlank()) item.dosageLabel else "${item.dosageLabel} · ${item.note}",
                    fontSize = 14.sp, color = Muted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    TimeUtils.format12h(item.scheduledMillis),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink
                )
                Spacer(Modifier.height(4.dp))
                StatusPill(item, isNext)
            }
        }
        if (item.status == ScheduleStatus.MISSED) {
            Spacer(Modifier.height(12.dp))
            // Taking a dose late: marks the missed dose as taken.
            AppButton("I took it late", onTaken, Modifier.fillMaxWidth(), filled = false)
        }
        if (isNext) {
            Spacer(Modifier.height(14.dp))
            AppButton("Mark as taken", onTaken, Modifier.fillMaxWidth())
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onSkip) {
                    Text(
                        "Skip this dose",
                        color = Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                }
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
