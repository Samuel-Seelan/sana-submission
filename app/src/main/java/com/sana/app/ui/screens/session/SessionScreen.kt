package com.sana.app.ui.screens.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.PlanItem
import com.sana.app.model.SampleData
import com.sana.app.ui.components.CameraPlaceholder
import com.sana.app.ui.components.StatChip
import com.sana.app.ui.components.VideoPlaceholder
import com.sana.app.ui.components.formatDuration
import com.sana.app.ui.components.targetLabel
import com.sana.app.ui.theme.SanaBackground
import com.sana.app.ui.theme.SanaSurface
import com.sana.app.ui.theme.SanaTheme
import com.sana.app.ui.theme.StarGold

/*
 * SessionScreen.kt — the guided workout session.
 * What: a demo video on top, the live camera mirror below, and a per-exercise rep counter.
 *       The session walks NOT_STARTED -> RESTING -> EXERCISING (repeat) -> DONE.
 * Who: Isaac
 * When: Goal 6 — UI skeleton.
 *
 * The real app records one continuous video (CameraX) and plays demos with Media3. For the
 * skeleton those are replaced with placeholder surfaces, and the phase is plain local state.
 */

/** Where we are in the session walk-through. */
enum class SessionPhase { NOT_STARTED, RESTING, EXERCISING, DONE }

@Composable
fun SessionScreen(
    onFinished: () -> Unit,
    plan: List<PlanItem> = SampleData.planItems,
    initialPhase: SessionPhase = SessionPhase.NOT_STARTED,
) {
    var phase by remember { mutableStateOf(initialPhase) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var repsThisSet by remember { mutableIntStateOf(0) }
    var setsCompleted by remember { mutableIntStateOf(0) }

    if (plan.isEmpty()) {
        MessageWithHomeButton(
            message = "Your plan is empty. Add exercises before starting a session.",
            onFinished = onFinished,
        )
        return
    }

    when (phase) {
        SessionPhase.DONE -> SessionDoneContent(plan = plan, onFinished = onFinished)
        else -> ActiveSession(
            plan = plan,
            currentIndex = currentIndex,
            phase = phase,
            repsThisSet = repsThisSet,
            setsCompleted = setsCompleted,
            onExit = onFinished,
            onStartSession = { phase = SessionPhase.RESTING },
            onStartExercise = {
                repsThisSet = 0
                setsCompleted = 0
                phase = SessionPhase.EXERCISING
            },
            onDecrementReps = { if (repsThisSet > 0) repsThisSet-- },
            onIncrementReps = { repsThisSet++ },
            onCompleteSet = { setsCompleted++; repsThisSet = 0 },
            onEndExercise = {
                if (currentIndex == plan.lastIndex) {
                    phase = SessionPhase.DONE
                } else {
                    currentIndex++
                    phase = SessionPhase.RESTING
                }
            },
            onEndEarly = { phase = SessionPhase.DONE },
        )
    }
}

@Composable
private fun MessageWithHomeButton(message: String, onFinished: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
        Button(onClick = onFinished) {
            Text("Back to Home")
        }
    }
}

@Composable
private fun ActiveSession(
    plan: List<PlanItem>,
    currentIndex: Int,
    phase: SessionPhase,
    repsThisSet: Int,
    setsCompleted: Int,
    onExit: () -> Unit,
    onStartSession: () -> Unit,
    onStartExercise: () -> Unit,
    onDecrementReps: () -> Unit,
    onIncrementReps: () -> Unit,
    onCompleteSet: () -> Unit,
    onEndExercise: () -> Unit,
    onEndEarly: () -> Unit,
) {
    val currentItem = plan[currentIndex]
    val exercise = currentItem.exercise
    val isLast = currentIndex == plan.lastIndex

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top half: looping demo of the current exercise (placeholder).
            VideoPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "${exercise.name} — demo",
            )
            // Bottom half: live camera mirror (placeholder).
            CameraPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        ) {
            SessionHeader(
                plan = plan,
                currentIndex = currentIndex,
                onExit = onExit,
            )
            if (phase == SessionPhase.EXERCISING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    RepCounterCard(
                        setsCompleted = setsCompleted,
                        targetSets = currentItem.targetSets,
                        reps = repsThisSet,
                        onDecrement = onDecrementReps,
                        onIncrement = onIncrementReps,
                        onCompleteSet = onCompleteSet,
                    )
                }
            }
        }

        BottomActionArea(
            phase = phase,
            isLast = isLast,
            timedDurationSec = currentItem.targetDurationSec,
            nextName = exercise.name,
            onStartSession = onStartSession,
            onStartExercise = onStartExercise,
            onEndExercise = onEndExercise,
            onEndEarly = onEndEarly,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun SessionHeader(
    plan: List<PlanItem>,
    currentIndex: Int,
    onExit: () -> Unit,
) {
    val item = plan[currentIndex]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SanaBackground.copy(alpha = 0.85f))
            .statusBarsPadding()
            .padding(bottom = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Close session")
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
            ) {
                Text(
                    text = "Exercise ${currentIndex + 1} of ${plan.size} — ${item.exercise.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Target: " + targetLabel(
                        sets = item.targetSets,
                        reps = item.targetReps,
                        durationSec = item.targetDurationSec,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LinearProgressIndicator(
            progress = { currentIndex.toFloat() / plan.size },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun RepCounterCard(
    setsCompleted: Int,
    targetSets: Int,
    reps: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onCompleteSet: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = SanaSurface.copy(alpha = 0.92f))) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Set ${setsCompleted + 1}/$targetSets",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease reps")
                }
                Text(
                    text = "$reps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase reps")
                }
            }
            Text(
                text = "reps this set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onCompleteSet) {
                Text("Complete set")
            }
        }
    }
}

@Composable
private fun BottomActionArea(
    phase: SessionPhase,
    isLast: Boolean,
    timedDurationSec: Int,
    nextName: String,
    onStartSession: () -> Unit,
    onStartExercise: () -> Unit,
    onEndExercise: () -> Unit,
    onEndEarly: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SanaBackground.copy(alpha = 0.85f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (phase) {
            SessionPhase.NOT_STARTED -> {
                Text(
                    text = "Your camera records one continuous video for form review — " +
                        "it's clipped per exercise automatically afterwards.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = onStartSession, modifier = Modifier.fillMaxWidth()) {
                    Text("Start session")
                }
            }

            SessionPhase.RESTING -> {
                Text(
                    text = "Next: $nextName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = onStartExercise, modifier = Modifier.fillMaxWidth()) {
                    Text("Start exercise")
                }
                TextButton(onClick = onEndEarly) {
                    Text("End session early")
                }
            }

            SessionPhase.EXERCISING -> {
                if (timedDurationSec > 0) {
                    Text(
                        text = "Target ${timedDurationSec}s hold",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Button(onClick = onEndExercise, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isLast) "End exercise & finish" else "End exercise & next")
                }
                TextButton(onClick = onEndEarly) {
                    Text("End session early")
                }
            }

            SessionPhase.DONE -> Unit // Handled by SessionDoneContent.
        }
    }
}

/** Post-session recap with stat chips and any new milestones. */
@Composable
private fun SessionDoneContent(plan: List<PlanItem>, onFinished: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Session complete",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Great work — recovery is built one session at a time.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatChip(label = "Exercises", value = "${plan.size}")
            StatChip(label = "Total reps", value = "64")
            StatChip(label = "Duration", value = formatDuration(18 * 60_000L))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "New milestones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        SampleData.milestones.take(1).forEach { milestone ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(text = milestone.label, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home")
        }
    }
}

@Preview(name = "Session — start", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 820)
@Composable
private fun SessionStartPreview() {
    SanaTheme { SessionScreen(onFinished = {}, initialPhase = SessionPhase.NOT_STARTED) }
}

@Preview(name = "Session — exercising", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 820)
@Composable
private fun SessionExercisingPreview() {
    SanaTheme { SessionScreen(onFinished = {}, initialPhase = SessionPhase.EXERCISING) }
}

@Preview(name = "Session — done", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 820)
@Composable
private fun SessionDonePreview() {
    SanaTheme { SessionScreen(onFinished = {}, initialPhase = SessionPhase.DONE) }
}
