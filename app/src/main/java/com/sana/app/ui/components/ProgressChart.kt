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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
 * ProgressChart.kt — the weekly progress visual on Home.
 * What: bars for weekly reps and a line for weekly active minutes.
 * Who: Sana team.
 * When: Goal 6 — UI skeleton.
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
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.White)
    val weekFormatter = DateTimeFormatter.ofPattern("MMM d")

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

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = SanaSecondary,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            // Week labels: first, middle, last.
            val labelIndices = listOf(0, stats.size / 2, stats.size - 1).distinct()

            labelIndices.forEach { i ->
                val date = LocalDate.ofEpochDay(stats[i].weekStartEpochDay)
                val label = textMeasurer.measure(date.format(weekFormatter), labelStyle)
                val x = (leftPad + slotW * i + slotW / 2 - label.size.width / 2)
                    .coerceIn(0f, size.width - label.size.width)

                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(x, size.height - label.size.height),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(SanaPrimary)

            Text(
                text = " Reps",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )

            Spacer(Modifier.width(12.dp))

            LegendDot(SanaSecondary)

            Text(
                text = " Active minutes",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Spacer(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape),
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