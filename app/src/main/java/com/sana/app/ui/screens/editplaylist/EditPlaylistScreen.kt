package com.sana.app.ui.screens.editplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sana.app.model.CatalogGroup
import com.sana.app.model.Display
import com.sana.app.model.Exercise
import com.sana.app.model.PlanItem
import com.sana.app.model.SampleData
import com.sana.app.model.ScoredExercise
import com.sana.app.ui.components.EmptyState
import com.sana.app.ui.components.ExerciseBadge
import com.sana.app.ui.components.ExerciseCard
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.theme.SanaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/*
 * EditPlaylistScreen.kt — build the day's workout.
 * What: the working plan as a removable card row, then the full scored catalog with working search,
 *       a "My injuries only" filter, and muscle-group filters. Add / remove edit the plan in the
 *       ViewModel; Save persists to Firestore; Share publishes the plan for other users.
 * Who: Sam.
 * When: Goal 7 — Firebase integration.
 */

/** Matches the card badge scrim so overlaid icon buttons read against any thumbnail. */
private val OverlayScrim = Color(0xCC0E1420)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaylistScreen(
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onBrowseShared: () -> Unit = {},
    planItems: List<PlanItem> = SampleData.planItems,
    catalogGroups: List<CatalogGroup> = SampleData.catalogGroups,
    muscleGroupKeys: List<String> = SampleData.muscleGroupKeys,
    isSaving: Boolean = false,
    message: String? = null,
    onMessageShown: () -> Unit = {},
    onAddExercise: (String) -> Unit = {},
    onRemoveExercise: (String) -> Unit = {},
    onMoveExercise: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onSave: () -> Unit = {},
    onPublish: (title: String, description: String) -> Unit = { _, _ -> },
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var myInjuriesOnly by rememberSaveable { mutableStateOf(false) }
    var muscleFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showPublishDialog by rememberSaveable { mutableStateOf(false) }

    val planIds = planItems.map { it.exercise.id }.toSet()
    val planExercises = planItems.map { it.exercise }
    val planScrollState = rememberScrollState()
    var planViewportWidthPx by remember { mutableIntStateOf(0) }
    var draggedExerciseId by remember { mutableStateOf<String?>(null) }
    var draggedOffsetX by remember { mutableFloatStateOf(0f) }
    var draggedCenterX by remember { mutableFloatStateOf(0f) }
    val currentPlanExercises by rememberUpdatedState(planExercises)
    val currentOnMoveExercise by rememberUpdatedState(onMoveExercise)

    // A card is 160dp wide with 12dp between cards. The list itself remains unchanged while the
    // user drags; this slot size is used to choose the final position when they release the card.
    val planItemSlotPx = with(androidx.compose.ui.platform.LocalDensity.current) { 172.dp.toPx() }
    val planCardHalfWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { 80.dp.toPx() }
    val planContentPaddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { 16.dp.toPx() }
    val autoScrollEdgePx = with(androidx.compose.ui.platform.LocalDensity.current) { 72.dp.toPx() }
    val maxAutoScrollPerFramePx = with(androidx.compose.ui.platform.LocalDensity.current) { 18.dp.toPx() }

    LaunchedEffect(draggedExerciseId) {
        while (isActive && draggedExerciseId != null) {
            val leftEdge = autoScrollEdgePx
            val rightEdge = planViewportWidthPx - autoScrollEdgePx
            val scrollAmount = when {
                draggedCenterX < leftEdge -> {
                    val intensity = ((leftEdge - draggedCenterX) / autoScrollEdgePx).coerceIn(0f, 1f)
                    -maxAutoScrollPerFramePx * intensity
                }
                draggedCenterX > rightEdge -> {
                    val intensity = ((draggedCenterX - rightEdge) / autoScrollEdgePx).coerceIn(0f, 1f)
                    maxAutoScrollPerFramePx * intensity
                }
                else -> 0f
            }

            if (scrollAmount != 0f) {
                val consumed = planScrollState.scrollBy(scrollAmount)
                // Scrolling moves the row beneath the pointer. Counteract that movement on the
                // selected card so it remains visually attached to the user's finger.
                draggedOffsetX += consumed
            }
            delay(16L)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    fun applyFilters(exercises: List<ScoredExercise>): List<ScoredExercise> = exercises.filter { scored ->
        val matchesQuery = searchQuery.isBlank() ||
            scored.exercise.name.contains(searchQuery.trim(), ignoreCase = true)
        val matchesMuscle = muscleFilter == null || muscleFilter in scored.exercise.muscleGroups
        val matchesInjury = !myInjuriesOnly || scored.score.recommended
        matchesQuery && matchesMuscle && matchesInjury
    }

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
                    TextButton(onClick = onSave, enabled = !isSaving) {
                        Text(if (isSaving) "Saving…" else "Save")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { planViewportWidthPx = it.width },
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(
                                    state = planScrollState,
                                    enabled = draggedExerciseId == null,
                                )
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            planExercises.forEach { exercise ->
                                key(exercise.id) {
                                    val isDragging = draggedExerciseId == exercise.id
                                    PlanCard(
                                        exercise = exercise,
                                        onClick = { onOpenExercise(exercise.id) },
                                        onRemove = { onRemoveExercise(exercise.id) },
                                        modifier = Modifier
                                            .zIndex(if (isDragging) 1f else 0f)
                                            .graphicsLayer {
                                                translationX = if (isDragging) draggedOffsetX else 0f
                                                scaleX = if (isDragging) 1.03f else 1f
                                                scaleY = if (isDragging) 1.03f else 1f
                                            }
                                            .pointerInput(exercise.id) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        draggedExerciseId = exercise.id
                                                        draggedOffsetX = 0f
                                                        val currentIndex = currentPlanExercises
                                                            .indexOfFirst { it.id == exercise.id }
                                                        draggedCenterX = planContentPaddingPx +
                                                            currentIndex * planItemSlotPx +
                                                            planCardHalfWidthPx -
                                                            planScrollState.value
                                                    },
                                                    onDragCancel = {
                                                        draggedExerciseId = null
                                                        draggedOffsetX = 0f
                                                        draggedCenterX = 0f
                                                    },
                                                    onDragEnd = {
                                                        val currentIndex = currentPlanExercises
                                                            .indexOfFirst { it.id == exercise.id }
                                                        if (currentIndex != -1) {
                                                            val targetIndex = (currentIndex +
                                                                (draggedOffsetX / planItemSlotPx).roundToInt())
                                                                .coerceIn(currentPlanExercises.indices)
                                                            if (targetIndex != currentIndex) {
                                                                currentOnMoveExercise(currentIndex, targetIndex)
                                                            }
                                                        }
                                                        draggedExerciseId = null
                                                        draggedOffsetX = 0f
                                                        draggedCenterX = 0f
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        draggedOffsetX += dragAmount.x
                                                        draggedCenterX += dragAmount.x
                                                    }
                                                )
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item(key = "share_actions") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { showPublishDialog = true },
                        enabled = planExercises.isNotEmpty() && !isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Share plan")
                    }
                    OutlinedButton(
                        onClick = onBrowseShared,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Browse shared")
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
                val filtered = applyFilters(group.exercises)
                if (filtered.isNotEmpty()) {
                    item(key = "group_header_${group.key}") {
                        SectionHeader(group.title, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    item(key = "group_row_${group.key}") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filtered, key = { it.exercise.id }) { scored ->
                                CatalogCard(
                                    scored = scored,
                                    added = scored.exercise.id in planIds,
                                    onClick = { onOpenExercise(scored.exercise.id) },
                                    onAdd = { onAddExercise(scored.exercise.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPublishDialog) {
        PublishDialog(
            onDismiss = { showPublishDialog = false },
            onPublish = { title, description ->
                showPublishDialog = false
                onPublish(title, description)
            },
        )
    }
}

/** Title + description prompt shown before publishing the current plan as a shared playlist. */
@Composable
private fun PublishDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, description: String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share your plan") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Publish your current plan so other Sana users can browse and use it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPublish(title, description) },
                enabled = title.isNotBlank(),
            ) {
                Text("Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** Working-plan card with a remove (X) overlay at the top-start corner. */
@Composable
private fun PlanCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExerciseCard(
        exercise = exercise,
        onClick = onClick,
        modifier = modifier,
        overlay = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OverlayIconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove from plan",
                        modifier = Modifier.size(18.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(OverlayScrim),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.DragHandle,
                        contentDescription = "Press and hold, then drag to reorder",
                        modifier = Modifier.size(18.dp),
                    )
                }
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
