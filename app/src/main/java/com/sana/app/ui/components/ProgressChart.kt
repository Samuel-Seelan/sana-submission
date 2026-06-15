package com.sana.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sana.app.model.Milestone
import com.sana.app.model.SampleData
import com.sana.app.model.WeeklyStat
import com.sana.app.ui.theme.SanaPrimary
import com.sana.app.ui.theme.SanaSecondary
import com.sana.app.ui.theme.SanaTheme
import com.sana.app.ui.theme.StarGold
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
 * ProgressChart.kt — the weekly progress visual on Home.
 * What: bars for weekly reps (primary), a line for weekly active minutes (secondary), and
 *       gold milestone dots on the top axis. Drawn on a Compose Canvas.
 * Who: Sana team (shared component).
 * When: Goal 6 — UI skeleton.
 *
 * For the skeleton, milestone markers are spread evenly across the weeks for illustration
 * (the real app buckets each milestone into its achievement week).
 */
@Composable
fun ProgressChart(
    stats: List<WeeklyStat>,
    milestones: List<Milestone>,
    modifier: Modifier = Modifier,
) {
    if (stats.isEmpty()) {
        EmptyState("Complete your first session to see your progress here.", modifier)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val milestoneStyle = TextStyle(fontSize = 9.sp, color = StarGold)
    val weekFormatter = DateTimeFormatter.ofPattern("MMM d")

    // For illustration: place the available milestones on evenly spaced weeks.
    val milestoneWeekIndices: Map<Int, Milestone> = if (milestones.isEmpty()) {
        emptyMap()
    } else {
        val step = (stats.size - 1).coerceAtLeast(1) / milestones.size.coerceAtLeast(1)
        milestones.mapIndexed { i, m -> (i * step + step).coerceAtMost(stats.size - 1) to m }.toMap()
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val leftPad = 8.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 22.dp.toPx()
            val bottomPad = 18.dp.toPx()
            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad

            val maxReps = stats.maxOf { it.totalReps }.coerceAtLeast(1)
            val maxMinutes = stats.maxOf { it.totalTimeMs / 60_000f }.coerceAtLeast(1f)
            val slotW = chartW / stats.size
            val barW = (slotW * 0.55f).coerceAtMost(28.dp.toPx())

            // Reps bars.
            stats.forEachIndexed { i, stat ->
                val barH = chartH * (stat.totalReps.toFloat() / maxReps)
                val x = leftPad + slotW * i + (slotW - barW) / 2
                drawRoundRect(
                    color = SanaPrimary.copy(alpha = 0.85f),
                    topLeft = Offset(x, topPad + chartH - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }

            // Active-minutes line.
            if (stats.size > 1) {
                val path = Path()
                stats.forEachIndexed { i, stat ->
                    val x = leftPad + slotW * i + slotW / 2
                    val y = topPad + chartH * (1f - (stat.totalTimeMs / 60_000f) / maxMinutes)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = SanaSecondary, style = Stroke(width = 2.dp.toPx()))
            }
            stats.forEachIndexed { i, stat ->
                val x = leftPad + slotW * i + slotW / 2
                val y = topPad + chartH * (1f - (stat.totalTimeMs / 60_000f) / maxMinutes)
                drawCircle(color = SanaSecondary, radius = 3.dp.toPx(), center = Offset(x, y))
            }

            // Milestone markers.
            milestoneWeekIndices.forEach { (i, milestone) ->
                val x = leftPad + slotW * i + slotW / 2
                drawCircle(color = StarGold, radius = 4.dp.toPx(), center = Offset(x, 8.dp.toPx()))
                val label = textMeasurer.measure(milestone.label, milestoneStyle)
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        (x - label.size.width / 2).coerceIn(0f, size.width - label.size.width),
                        12.dp.toPx(),
                    ),
                )
            }

            // Week labels: first, middle, last.
            val labelIndices = listOf(0, stats.size / 2, stats.size - 1).distinct()
            labelIndices.forEach { i ->
                val date = LocalDate.ofEpochDay(stats[i].weekStartEpochDay)
                val label = textMeasurer.measure(date.format(weekFormatter), labelStyle)
                val x = (leftPad + slotW * i + slotW / 2 - label.size.width / 2)
                    .coerceIn(0f, size.width - label.size.width)
                drawText(label, topLeft = Offset(x, size.height - label.size.height))
            }
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(SanaPrimary); Text(" Reps", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(12.dp))
            LegendDot(SanaSecondary); Text(" Active minutes", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(12.dp))
            LegendDot(StarGold); Text(" Milestone", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Spacer(
        Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1420)
@Composable
private fun ProgressChartPreview() {
    SanaTheme {
        ProgressChart(
            stats = SampleData.weeklyStats,
            milestones = SampleData.milestones,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
