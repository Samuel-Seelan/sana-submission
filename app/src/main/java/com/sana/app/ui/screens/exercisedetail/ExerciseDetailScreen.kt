package com.sana.app.ui.screens.exercisedetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.Display
import com.sana.app.model.Exercise
import com.sana.app.model.ExerciseScore
import com.sana.app.model.Recording
import com.sana.app.model.SampleData
import com.sana.app.ui.components.EmptyState
import com.sana.app.ui.components.ExerciseDemoMedia
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.components.formatDuration
import com.sana.app.ui.theme.BlockedRed
import com.sana.app.ui.theme.SanaTheme
import com.sana.app.ui.theme.StarGold

/*
 * ExerciseDetailScreen.kt — a single catalog exercise.
 * What: looping demo video, metadata chips, a safety banner (recommended / not advised),
 *       step-by-step instructions, the user's past recording clips, and an "Add to plan" button
 *       that persists to Firestore.
 * Who: Sam.
 * When: Goal 7 — Firebase integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    exercise: Exercise? = SampleData.exercises.firstOrNull { it.id == exerciseId }
        ?: SampleData.quadSets,
    score: ExerciseScore? = ExerciseScore(blocked = false, recommended = true),
    recordings: List<Recording> = SampleData.recordings,
    inPlan: Boolean = false,
    onAddToPlan: () -> Unit = {},
    message: String? = null,
    onMessageShown: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise?.name ?: "Exercise",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (exercise == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState("Exercise not found.")
            }
        } else {
            ExerciseDetailContent(
                exercise = exercise,
                score = score,
                recordings = recordings,
                inPlan = inPlan,
                onAddToPlan = onAddToPlan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetailContent(
    exercise: Exercise,
    score: ExerciseScore?,
    recordings: List<Recording>,
    inPlan: Boolean,
    onAddToPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        ExerciseDemoMedia(
            exercise = exercise,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoChip(exercise.type.replaceFirstChar { it.uppercase() })
                InfoChip(difficultyLabel(exercise.difficulty))
                exercise.muscleGroups.forEach { group ->
                    InfoChip(Display.muscleGroup(group))
                }
            }

            when {
                score?.blocked == true -> RecommendationBanner(
                    icon = Icons.Filled.Block,
                    tint = BlockedRed,
                    message = "Not advised for your injuries — check with your physiotherapist",
                )
                score?.recommended == true -> RecommendationBanner(
                    icon = Icons.Filled.Star,
                    tint = StarGold,
                    message = "Recommended for your recovery",
                )
            }

            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionHeader("How to do it")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                exercise.instructions
                    .split('\n')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { step ->
                        Text(text = step, style = MaterialTheme.typography.bodyMedium)
                    }
            }
        }

        SectionHeader(
            title = "Your recordings",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        if (recordings.isEmpty()) {
            EmptyState(
                message = "Clips from your sessions will appear here.",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(recordings, key = { it.id }) { recording ->
                    RecordingCard(recording = recording)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onAddToPlan,
                enabled = !inPlan,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (inPlan) "In your plan" else "Add to plan")
            }
            Text(
                text = "Always follow your physiotherapist's guidance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
            )
        }
    }
}

/** "Difficulty ●●○" — filled dots up to the 1–3 difficulty level. */
private fun difficultyLabel(difficulty: Int): String {
    val level = difficulty.coerceIn(1, 3)
    return "Difficulty " + "●".repeat(level) + "○".repeat(3 - level)
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun RecommendationBanner(icon: ImageVector, tint: Color, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** One past recording: duration + sets/reps, with a play affordance once a clip exists. */
@Composable
private fun RecordingCard(recording: Recording) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(enabled = recording.hasClip) {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (recording.hasClip) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Play clip",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = formatDuration(recording.durationMs),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "${recording.sets} sets · ${recording.reps} reps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!recording.hasClip) {
                Text(
                    text = "Clip processing…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Exercise detail", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 1000)
@Composable
private fun ExerciseDetailScreenPreview() {
    SanaTheme {
        ExerciseDetailScreen(exerciseId = "quad_sets", onBack = {})
    }
}

@Preview(name = "Exercise detail — not advised", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 1000)
@Composable
private fun ExerciseDetailBlockedPreview() {
    SanaTheme {
        ExerciseDetailScreen(
            exerciseId = "deadlift",
            onBack = {},
            exercise = SampleData.deadlift,
            score = ExerciseScore(blocked = true, recommended = false),
            recordings = emptyList(),
        )
    }
}
