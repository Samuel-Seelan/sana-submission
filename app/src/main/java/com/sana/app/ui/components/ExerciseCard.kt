package com.sana.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sana.app.model.Exercise
import com.sana.app.model.SampleData
import com.sana.app.ui.theme.BlockedRed
import com.sana.app.ui.theme.SanaTheme
import com.sana.app.ui.theme.StarGold
import com.sana.app.ui.theme.ThumbGradients
import kotlin.math.absoluteValue

/*
 * ExerciseCard.kt — the carousel/grid card shown for every exercise.
 * What: a gradient "thumbnail" (stable per exercise), name, subtitle, and an optional
 *       recommended / not-advised badge. Reused by Home, Edit playlist, and Day detail.
 * Who: Sana team (shared component).
 * When: Goal 6 — UI skeleton.
 */

enum class ExerciseBadge { NONE, RECOMMENDED, NOT_ADVISED }

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 160.dp,
    badge: ExerciseBadge = ExerciseBadge.NONE,
    subtitle: String? = null,
    overlay: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .width(width)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box {
            Column {
                ExerciseThumb(
                    exercise = exercise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f),
                )
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle ?: exercise.targetLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            when (badge) {
                ExerciseBadge.RECOMMENDED -> BadgeIcon(
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Recommended", tint = StarGold) },
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                ExerciseBadge.NOT_ADVISED -> BadgeIcon(
                    icon = { Icon(Icons.Filled.Block, contentDescription = "Not advised", tint = BlockedRed) },
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                ExerciseBadge.NONE -> Unit
            }
            overlay?.invoke()
        }
    }
}

@Composable
private fun BadgeIcon(icon: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(6.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xCC0E1420))
            .padding(4.dp)
    ) {
        icon()
    }
}

/** Stable gradient placeholder thumbnail with the exercise's initial. */
@Composable
fun ExerciseThumb(exercise: Exercise, modifier: Modifier = Modifier) {
    val (top, bottom) = ThumbGradients[exercise.id.hashCode().absoluteValue % ThumbGradients.size]
    Box(
        modifier = modifier.background(Brush.verticalGradient(listOf(top, bottom))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = exercise.name.first().uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Bold,
        )
    }
}

/** "3 × 12 reps" or "3 × 30s" depending on whether the targets are rep- or time-based. */
fun targetLabel(sets: Int, reps: Int, durationSec: Int): String =
    if (durationSec > 0) "$sets × ${durationSec}s" else "$sets × $reps reps"

@Preview(showBackground = true, backgroundColor = 0xFF0E1420)
@Composable
private fun ExerciseCardPreview() {
    SanaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExerciseCard(
                exercise = SampleData.quadSets,
                onClick = {},
                badge = ExerciseBadge.RECOMMENDED,
            )
            ExerciseCard(
                exercise = SampleData.deadlift,
                onClick = {},
                badge = ExerciseBadge.NOT_ADVISED,
            )
        }
    }
}
