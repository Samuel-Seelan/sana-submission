package com.sana.app.ui.screens.daydetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sana.app.model.DayStats
import com.sana.app.model.Recording
import com.sana.app.model.SampleData
import com.sana.app.ui.components.EmptyState
import com.sana.app.ui.components.ExerciseCard
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.components.StatChip
import com.sana.app.ui.components.VideoPlaceholder
import com.sana.app.ui.components.formatDuration
import com.sana.app.ui.theme.SanaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
 * DayDetailScreen.kt — a read-only view of one calendar day.
 * What: a carousel of the exercises recorded that day (tap to replay the clip or jump to the
 *       exercise) plus the day's stat chips (reps, sets, active time, exercises).
 * Who: Max.
 * When: Goal 6 — UI skeleton.
 *
 * The real app replays each clip with a Media3 player; the skeleton swaps in a placeholder
 * surface so the replay dialog still demonstrates the flow without any video files.
 */
private val DayTitleFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    epochDay: Long,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    recordings: List<Recording> = SampleData.recordings,
    dayStats: DayStats = SampleData.dayStats,
) {
    val title = remember(epochDay) {
        val day = if (epochDay > 0) LocalDate.ofEpochDay(epochDay) else LocalDate.now()
        day.format(DayTitleFormatter)
    }
    var replayTarget by remember { mutableStateOf<Recording?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState("No session recorded this day.")
            }
        } else {
            DayDetailContent(
                recordings = recordings,
                dayStats = dayStats,
                contentPadding = padding,
                onRecordingClick = { recording ->
                    if (recording.hasClip) {
                        replayTarget = recording
                    } else {
                        onOpenExercise(recording.exercise.id)
                    }
                },
            )
        }
    }

    replayTarget?.let { target ->
        ReplayDialog(
            recording = target,
            onDismiss = { replayTarget = null },
            onOpenExercise = {
                replayTarget = null
                onOpenExercise(target.exercise.id)
            },
        )
    }
}

@Composable
private fun DayDetailContent(
    recordings: List<Recording>,
    dayStats: DayStats,
    contentPadding: PaddingValues,
    onRecordingClick: (Recording) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp))
        SectionHeader("Exercises", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))
        RecordingCarousel(recordings = recordings, onRecordingClick = onRecordingClick)

        Spacer(Modifier.height(24.dp))
        SectionHeader("Stats", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))
        DayStatsRow(stats = dayStats)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RecordingCarousel(
    recordings: List<Recording>,
    onRecordingClick: (Recording) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(recordings, key = { it.id }) { recording ->
            ExerciseCard(
                exercise = recording.exercise,
                onClick = { onRecordingClick(recording) },
                subtitle = "${recording.sets} sets · ${recording.reps} reps",
                overlay = if (recording.hasClip) {
                    {
                        // Centered over the thumbnail area (same 16:10 box as ExerciseThumb).
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 10f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.PlayCircle,
                                contentDescription = "Play recording",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun DayStatsRow(stats: DayStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatChip(label = "Total reps", value = stats.totalReps.toString())
        StatChip(label = "Total sets", value = stats.totalSets.toString())
        StatChip(label = "Active time", value = formatDuration(stats.totalTimeMs))
        StatChip(label = "Exercises", value = stats.exercisesDone.toString())
    }
}

@Composable
private fun ReplayDialog(
    recording: Recording,
    onDismiss: () -> Unit,
    onOpenExercise: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = recording.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                VideoPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    label = "Recorded clip",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onOpenExercise) {
                        Text("Exercise details")
                    }
                }
            }
        }
    }
}

@Preview(name = "Day detail", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 800)
@Composable
private fun DayDetailScreenPreview() {
    SanaTheme {
        DayDetailScreen(epochDay = LocalDate.now().toEpochDay(), onBack = {}, onOpenExercise = {})
    }
}
