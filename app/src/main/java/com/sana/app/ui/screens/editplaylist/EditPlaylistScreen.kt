package com.sana.app.ui.screens.editplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.CatalogGroup
import com.sana.app.model.Display
import com.sana.app.model.Exercise
import com.sana.app.model.SampleData
import com.sana.app.model.ScoredExercise
import com.sana.app.ui.components.EmptyState
import com.sana.app.ui.components.ExerciseBadge
import com.sana.app.ui.components.ExerciseCard
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.theme.SanaTheme

/*
 * EditPlaylistScreen.kt — build the day's workout.
 * What: the working plan as a removable card row, then the full scored catalog with search,
 *       a "My injuries only" filter, and single-select muscle-group filters. Add / remove
 *       toggle the plan; Save commits (back discards).
 * Who: Sam.
 * When: Goal 6 — UI skeleton.
 */

/** Matches the card badge scrim so overlaid icon buttons read against any thumbnail. */
private val OverlayScrim = Color(0xCC0E1420)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaylistScreen(
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    initialPlan: List<Exercise> = SampleData.planExercises,
    catalogGroups: List<CatalogGroup> = SampleData.catalogGroups,
    muscleGroupKeys: List<String> = SampleData.muscleGroupKeys,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var myInjuriesOnly by rememberSaveable { mutableStateOf(false) }
    var muscleFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var planIds by remember { mutableStateOf(initialPlan.map { it.id }.toSet()) }

    val planExercises = initialPlan.filter { it.id in planIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "plan_header") {
                SectionHeader("Your plan", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item(key = "plan_row") {
                if (planExercises.isEmpty()) {
                    EmptyState(
                        "Your plan is empty — add exercises from the catalog below.",
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(planExercises, key = { it.id }) { exercise ->
                            PlanCard(
                                exercise = exercise,
                                onClick = { onOpenExercise(exercise.id) },
                                onRemove = { planIds = planIds - exercise.id },
                            )
                        }
                    }
                }
            }
            item(key = "search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search exercises") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                )
            }
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = myInjuriesOnly,
                        onClick = { myInjuriesOnly = !myInjuriesOnly },
                        label = { Text("My injuries only") },
                    )
                    muscleGroupKeys.forEach { key ->
                        FilterChip(
                            selected = muscleFilter == key,
                            onClick = { muscleFilter = if (muscleFilter == key) null else key },
                            label = { Text(Display.muscleGroup(key)) },
                        )
                    }
                }
            }
            catalogGroups.forEach { group ->
                item(key = "group_header_${group.key}") {
                    SectionHeader(group.title, modifier = Modifier.padding(horizontal = 16.dp))
                }
                item(key = "group_row_${group.key}") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(group.exercises, key = { it.exercise.id }) { scored ->
                            CatalogCard(
                                scored = scored,
                                added = scored.exercise.id in planIds,
                                onClick = { onOpenExercise(scored.exercise.id) },
                                onAdd = { planIds = planIds + scored.exercise.id },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Working-plan card with a remove (X) overlay at the top-start corner. */
@Composable
private fun PlanCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    ExerciseCard(
        exercise = exercise,
        onClick = onClick,
        overlay = {
            // ExerciseCard invokes the overlay inside its Box, which places it top-start.
            OverlayIconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove from plan",
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )
}

/** Catalog card with score badge and an add (+) / already-added (check) overlay at bottom-end. */
@Composable
private fun CatalogCard(
    scored: ScoredExercise,
    added: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
) {
    val badge = when {
        scored.score.blocked -> ExerciseBadge.NOT_ADVISED
        scored.score.recommended -> ExerciseBadge.RECOMMENDED
        else -> ExerciseBadge.NONE
    }
    Box {
        ExerciseCard(
            exercise = scored.exercise,
            onClick = onClick,
            badge = badge,
        )
        OverlayIconButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            if (added) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Already in plan",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add to plan",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OverlayIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(OverlayScrim),
    ) {
        content()
    }
}

@Preview(name = "Edit playlist", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 1000)
@Composable
private fun EditPlaylistScreenPreview() {
    SanaTheme {
        EditPlaylistScreen(onBack = {}, onOpenExercise = {})
    }
}
// Sam Seelan - EditPlaylist and ExerciseDetail screens
