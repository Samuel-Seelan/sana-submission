package com.sana.app.ui.screens.sharedplaylists

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.SharedPlaylistsUiState
import com.sana.app.viewmodel.SharedPlaylistsViewModel

/*
 * SharedPlaylistsRoute.kt — connects SharedPlaylistsViewModel to the pure browse screen.
 * Who: Sam.
 * When: Goal 7 — multi-user feature.
 */
@Composable
fun SharedPlaylistsRoute(
    onBack: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    viewModel: SharedPlaylistsViewModel = remember { SharedPlaylistsViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = SharedPlaylistsUiState())

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    SharedPlaylistsScreen(
        onBack = onBack,
        onOpenPlaylist = onOpenPlaylist,
        playlists = uiState.playlists,
    )
}
