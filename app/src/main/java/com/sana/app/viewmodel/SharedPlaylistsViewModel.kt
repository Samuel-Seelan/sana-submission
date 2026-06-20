package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.SharedPlaylistSummary
import com.sana.app.repository.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * SharedPlaylistsViewModel.kt — the browse list for the multi-user sharing feature.
 * What: streams every public shared playlist (published by any user) in real time.
 * Who: Sam (owns playlist + sharing).
 * When: Goal 7 — Firebase integration.
 */
data class SharedPlaylistsUiState(
    val isLoading: Boolean = true,
    val playlists: List<SharedPlaylistSummary> = emptyList(),
)

class SharedPlaylistsViewModel(
    playlistRepository: PlaylistRepository = AppModule.playlistRepository,
) : ViewModel() {
    val uiState: StateFlow<SharedPlaylistsUiState> = playlistRepository.observeSharedPlaylists()
        .map { SharedPlaylistsUiState(isLoading = false, playlists = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SharedPlaylistsUiState(),
        )
}
