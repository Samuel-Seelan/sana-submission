package com.sana.app.ui.screens.sharedplaylistdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.SharedPlaylistDetailUiState
import com.sana.app.viewmodel.SharedPlaylistDetailViewModel

/*
 * SharedPlaylistDetailRoute.kt — connects SharedPlaylistDetailViewModel to the detail screen.
 * What: loads the playlist by id and, once it is copied into the user's plan, fires onUsed so the
 *       shell can navigate back to Home.
 * Who: Sam.
 * When: Goal 7 — multi-user feature.
 */
@Composable
fun SharedPlaylistDetailRoute(
    playlistId: String,
    onBack: () -> Unit,
    onUsed: () -> Unit,
    onOpenExercise: (String) -> Unit,
    viewModel: SharedPlaylistDetailViewModel = remember { SharedPlaylistDetailViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = SharedPlaylistDetailUiState())
    val used by viewModel.used.collectAsState(initial = false)

    LaunchedEffect(playlistId) { viewModel.load(playlistId) }
    LaunchedEffect(used) { if (used) onUsed() }

    SharedPlaylistDetailScreen(
        onBack = onBack,
        playlist = uiState.playlist,
        isUsing = uiState.isUsing,
        onUsePlaylist = viewModel::usePlaylist,
        onOpenExercise = onOpenExercise,
        message = uiState.message,
        onMessageShown = viewModel::consumeMessage,
    )
}
