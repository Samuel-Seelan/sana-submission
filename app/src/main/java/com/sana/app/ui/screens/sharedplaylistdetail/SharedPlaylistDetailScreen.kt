package com.sana.app.ui.screens.sharedplaylistdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.PlanItem
import com.sana.app.model.SampleData
import com.sana.app.model.SharedPlaylist
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.theme.SanaTheme

/*
 * SharedPlaylistDetailScreen.kt — one shared playlist, with the option to use it.
 * What: shows the playlist's owner, description, injury focus, and full exercise list, plus a
 *       "Use this playlist" action (confirmed by a dialog) that copies it into the user's own plan.
 * Who: Sam.
 * When: Goal 7 — multi-user feature.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SharedPlaylistDetailScreen(
    onBack: () -> Unit,
    playlist: SharedPlaylist? = SampleData.sharedPlaylistDetail,
    isUsing: Boolean = false,
    onUsePlaylist: () -> Unit = {},
    onOpenExercise: (String) -> Unit = {},
    message: String? = null,
    onMessageShown: () -> Unit = {},
) {
    val injuryNames = remember { ExerciseCatalog.injuryProfiles.associate { it.id to it.name } }
    var showConfirm by rememberSaveable { mutableStateOf(false) }

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
                        text = playlist?.title ?: "Playlist",
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
    ) { padding ->
        if (playlist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "by ${playlist.ownerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (playlist.description.isNotBlank()) {
                    Text(text = playlist.description, style = MaterialTheme.typography.bodyMedium)
                }
                if (playlist.injuryFocus.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        playlist.injuryFocus.forEach { injuryId ->
                            InjuryChip(text = injuryNames[injuryId] ?: injuryId)
                        }
                    }
                }

                SectionHeader("Exercises")
                playlist.items.forEach { item ->
                    ExerciseRow(item = item, onClick = { onOpenExercise(item.exercise.id) })
                }

                Text(
                    text = if (playlist.uses == 1) "Used by 1 person" else "Used by ${playlist.uses} people",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = { showConfirm = true },
                    enabled = !isUsing && playlist.items.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isUsing) "Using…" else "Use this playlist")
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Use this playlist?") },
            text = { Text("This replaces your current plan with these exercises.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onUsePlaylist()
                    },
                ) {
                    Text("Use playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ExerciseRow(item: PlanItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = item.exercise.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = item.targetLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InjuryChip(text: String) {
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

@Preview(name = "Shared playlist detail", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 800)
@Composable
private fun SharedPlaylistDetailScreenPreview() {
    SanaTheme {
        SharedPlaylistDetailScreen(onBack = {})
    }
}
