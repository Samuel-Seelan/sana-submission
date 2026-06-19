package com.sana.app.ui.screens.sharedplaylists

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.SampleData
import com.sana.app.model.SharedPlaylistSummary
import com.sana.app.ui.components.EmptyState
import com.sana.app.ui.theme.SanaTheme

/*
 * SharedPlaylistsScreen.kt — browse playlists published by other users.
 * What: a real-time list of public shared playlists with an injury-focus filter. Each card shows the
 *       title, owner, description, injury focus, exercise count, and use count; tapping opens detail.
 * Who: Sam.
 * When: Goal 7 — multi-user feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPlaylistsScreen(
    onBack: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    playlists: List<SharedPlaylistSummary> = SampleData.sharedPlaylistSummaries,
) {
    val injuryNames = remember { ExerciseCatalog.injuryProfiles.associate { it.id to it.name } }
    var injuryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val availableInjuries = remember(playlists) { playlists.flatMap { it.injuryFocus }.distinct() }
    val filtered = if (injuryFilter == null) {
        playlists
    } else {
        playlists.filter { injuryFilter in it.injuryFocus }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared playlists") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (availableInjuries.isNotEmpty()) {
                item(key = "filters") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = injuryFilter == null,
                            onClick = { injuryFilter = null },
                            label = { Text("All") },
                        )
                        availableInjuries.forEach { injuryId ->
                            FilterChip(
                                selected = injuryFilter == injuryId,
                                onClick = {
                                    injuryFilter = if (injuryFilter == injuryId) null else injuryId
                                },
                                label = { Text(injuryNames[injuryId] ?: injuryId) },
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        message = "No shared playlists yet. Publish yours from Edit playlist.",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(filtered, key = { it.id }) { playlist ->
                    SharedPlaylistCard(
                        playlist = playlist,
                        injuryNames = injuryNames,
                        onClick = { onOpenPlaylist(playlist.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SharedPlaylistCard(
    playlist: SharedPlaylistSummary,
    injuryNames: Map<String, String>,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "by ${playlist.ownerName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (playlist.description.isNotBlank()) {
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (playlist.injuryFocus.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    playlist.injuryFocus.forEach { injuryId ->
                        InjuryChip(text = injuryNames[injuryId] ?: injuryId)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = exerciseCountLabel(playlist.exerciseCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = useCountLabel(playlist.uses),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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

private fun exerciseCountLabel(count: Int): String =
    if (count == 1) "1 exercise" else "$count exercises"

private fun useCountLabel(uses: Int): String =
    if (uses == 1) "1 use" else "$uses uses"

@Preview(name = "Shared playlists", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 800)
@Composable
private fun SharedPlaylistsScreenPreview() {
    SanaTheme {
        SharedPlaylistsScreen(onBack = {}, onOpenPlaylist = {})
    }
}
