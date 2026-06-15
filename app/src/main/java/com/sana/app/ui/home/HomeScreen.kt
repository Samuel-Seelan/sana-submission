package com.sana.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.Milestone
import com.sana.app.model.PlanItem
import com.sana.app.model.SampleData
import com.sana.app.model.UserProfile
import com.sana.app.model.WeeklyStat
import com.sana.app.ui.components.ExerciseCard
import com.sana.app.ui.components.ProgressChart
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.components.targetLabel
import com.sana.app.ui.theme.SanaTheme

/*
 * HomeScreen.kt — the landing screen.
 * What: greeting, a "Today's Workout" carousel with Start / Edit actions, the weekly
 *       progress chart, and a link to the full plan overview.
 * Who: Isaac.
 * When: Goal 6 — UI skeleton.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartSession: () -> Unit,
    onEditPlaylist: () -> Unit,
    onOpenOverview: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenExercise: (String) -> Unit,
    user: UserProfile = SampleData.user,
    planItems: List<PlanItem> = SampleData.planItems,
    weeklyStats: List<WeeklyStat> = SampleData.weeklyStats,
    milestones: List<Milestone> = SampleData.milestones,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val firstName = user.name.trim().substringBefore(' ')
                    Text(
                        text = if (firstName.isEmpty()) "Welcome back" else "Hello, $firstName",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    IconButton(onClick = onOpenAccount) {
                        Icon(Icons.Filled.Person, contentDescription = "Account")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(
                title = "Today's Workout",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (planItems.isEmpty()) {
                EmptyPlanCard(onEditPlaylist = onEditPlaylist)
            } else {
                PlanCarousel(planItems = planItems, onOpenExercise = onOpenExercise)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onStartSession,
                    enabled = planItems.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = onEditPlaylist,
                    modifier = Modifier.height(52.dp),
                ) {
                    Text("Edit")
                }
            }

            SectionHeader(
                title = "Progress",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ProgressChart(
                stats = weeklyStats,
                milestones = milestones,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            TextButton(
                onClick = onOpenOverview,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Text("Full plan overview →")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Horizontally scrolling row of the day's planned exercises. */
@Composable
private fun PlanCarousel(
    planItems: List<PlanItem>,
    onOpenExercise: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(planItems, key = { it.exercise.id }) { planItem ->
            ExerciseCard(
                exercise = planItem.exercise,
                onClick = { onOpenExercise(planItem.exercise.id) },
                width = 240.dp,
                subtitle = targetLabel(
                    planItem.targetSets,
                    planItem.targetReps,
                    planItem.targetDurationSec,
                ),
            )
        }
    }
}

/** Shown when the plan has no exercises yet. */
@Composable
private fun EmptyPlanCard(onEditPlaylist: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Your plan is empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Add a few exercises to build today's workout. We'll suggest ones suited to your recovery.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onEditPlaylist) {
                Text("Edit playlist")
            }
        }
    }
}

@Preview(name = "Home", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    SanaTheme {
        HomeScreen(
            onStartSession = {},
            onEditPlaylist = {},
            onOpenOverview = {},
            onOpenAccount = {},
            onOpenExercise = {},
        )
    }
}

@Preview(name = "Home — empty plan", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 900)
@Composable
private fun HomeScreenEmptyPreview() {
    SanaTheme {
        HomeScreen(
            onStartSession = {},
            onEditPlaylist = {},
            onOpenOverview = {},
            onOpenAccount = {},
            onOpenExercise = {},
            planItems = emptyList(),
            weeklyStats = emptyList(),
        )
    }
}
