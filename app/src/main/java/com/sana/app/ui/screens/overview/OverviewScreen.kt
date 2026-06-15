package com.sana.app.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.DayCell
import com.sana.app.model.DayStatus
import com.sana.app.model.SampleData
import com.sana.app.model.WeekRow
import com.sana.app.ui.components.EmptyState
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.theme.DoneGreen
import com.sana.app.ui.theme.MissedRed
import com.sana.app.ui.theme.SanaTheme
import com.sana.app.ui.theme.UpcomingGrey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
 * OverviewScreen.kt — the full plan overview.
 * What: a Mon..Sun strip for the current week plus one row per recovery week (most recent
 *       first). Every day cell / mini-dot opens that day's detail screen.
 * Who: Max.
 * When: Goal 6 — UI skeleton.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onBack: () -> Unit,
    onOpenDay: (Long) -> Unit,
    thisWeek: List<DayCell> = SampleData.thisWeek,
    weeks: List<WeekRow> = SampleData.weeks,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan overview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "this_week_header") { SectionHeader("This week") }
            item(key = "this_week_strip") {
                Column {
                    ThisWeekStrip(cells = thisWeek, onOpenDay = onOpenDay)
                    StatusLegend(modifier = Modifier.padding(top = 10.dp, start = 4.dp))
                }
            }
            item(key = "week_list_header") {
                SectionHeader("Week list", modifier = Modifier.padding(top = 8.dp))
            }
            if (weeks.isEmpty()) {
                item(key = "week_list_empty") {
                    EmptyState(
                        message = "Your recovery weeks will appear here.",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(weeks, key = { it.weekStartEpochDay }) { week ->
                    WeekRowCard(week = week, onOpenDay = onOpenDay)
                }
            }
        }
    }
}

/** 7 large day cells, Monday..Sunday of the current week. */
@Composable
private fun ThisWeekStrip(cells: List<DayCell>, onOpenDay: (Long) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            cells.forEach { cell ->
                DayCellView(
                    cell = cell,
                    onClick = { onOpenDay(cell.epochDay) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .semantics { contentDescription = cell.description() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = cell.dayLetter,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        // Outer halo highlights today; inner circle carries the status ring/fill.
        Box(
            modifier = Modifier
                .size(42.dp)
                .then(
                    if (cell.isToday) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            val statusColor = cell.status.color()
            val filled = cell.status == DayStatus.DONE
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (filled) statusColor else Color.Transparent)
                    .border(1.5.dp, statusColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cell.dayNumber.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    // DoneGreen == primary, so onPrimary keeps the number readable on the fill.
                    color = if (filled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

/** Week label + session count on the left, 7 clickable status dots on the right. */
@Composable
private fun WeekRowCard(week: WeekRow, onOpenDay: (Long) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = week.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (week.sessionCount == 1) "1 session" else "${week.sessionCount} sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                week.days.forEach { day ->
                    MiniDayDot(day = day, onClick = { onOpenDay(day.epochDay) })
                }
            }
        }
    }
}

@Composable
private fun MiniDayDot(day: DayCell, onClick: () -> Unit) {
    // 24dp touch target around a 12dp dot.
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = day.description() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(day.status.color())
                .then(
                    if (day.isToday) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    } else {
                        Modifier
                    }
                )
        )
    }
}

@Composable
private fun StatusLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LegendEntry(DoneGreen, "Done")
        LegendEntry(MissedRed, "Missed")
        LegendEntry(UpcomingGrey, "Upcoming")
    }
}

@Composable
private fun LegendEntry(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun DayStatus.color(): Color = when (this) {
    DayStatus.DONE -> DoneGreen
    DayStatus.MISSED -> MissedRed
    DayStatus.UPCOMING -> UpcomingGrey
}

private val DAY_DESCRIPTION_FORMAT = DateTimeFormatter.ofPattern("MMM d")

/** Accessibility label, e.g. "Jun 11, done". */
private fun DayCell.description(): String {
    val date = LocalDate.ofEpochDay(epochDay).format(DAY_DESCRIPTION_FORMAT)
    val statusLabel = when (status) {
        DayStatus.DONE -> "done"
        DayStatus.MISSED -> "missed"
        DayStatus.UPCOMING -> "upcoming"
    }
    return "$date, $statusLabel"
}

@Preview(name = "Overview", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 900)
@Composable
private fun OverviewScreenPreview() {
    SanaTheme {
        OverviewScreen(onBack = {}, onOpenDay = {})
    }
}
