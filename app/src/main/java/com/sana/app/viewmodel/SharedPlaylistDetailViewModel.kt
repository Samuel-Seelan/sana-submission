@file:OptIn(ExperimentalCoroutinesApi::class)

package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.SharedPlaylist
import com.sana.app.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * SharedPlaylistDetailViewModel.kt — one shared playlist, with the option to use it.
 * What: streams a public playlist's full detail and copies it into the signed-in user's plan
 *       (replacing the current plan and bumping the shared use counter). Exposes a one-shot [used]
 *       signal the route turns into navigation back home.
 * Who: Sam (owns playlist + sharing).
 * When: Goal 7 — Firebase integration.
 */
data class SharedPlaylistDetailUiState(
    val isLoading: Boolean = true,
    val playlist: SharedPlaylist? = null,
    val isUsing: Boolean = false,
    val message: String? = null,
)

class SharedPlaylistDetailViewModel(
    private val playlistRepository: PlaylistRepository = AppModule.playlistRepository,
) : ViewModel() {

    private val playlistId = MutableStateFlow<String?>(null)
    private val isUsing = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val usedEvent = MutableStateFlow(false)

    /** One-shot signal: becomes true after the playlist is successfully copied into the user plan. */
    val used: StateFlow<Boolean> = usedEvent.asStateFlow()

    private val playlistFlow = playlistId.flatMapLatest { id ->
        if (id == null) flowOf(null) else playlistRepository.observeSharedPlaylist(id)
    }

    val uiState: StateFlow<SharedPlaylistDetailUiState> = combine(
        playlistId,
        playlistFlow,
        isUsing,
        message,
    ) { id, playlist, using, currentMessage ->
        SharedPlaylistDetailUiState(
            isLoading = id == null,
            playlist = playlist,
            isUsing = using,
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SharedPlaylistDetailUiState(),
    )

    fun load(id: String) {
        if (playlistId.value != id) playlistId.value = id
    }

    fun usePlaylist() {
        val id = playlistId.value ?: return
        viewModelScope.launch {
            isUsing.value = true
            playlistRepository.useSharedPlaylist(id)
                .onSuccess { usedEvent.value = true }
                .onFailure { message.value = it.message ?: "Could not use this playlist." }
            isUsing.value = false
        }
    }

    fun consumeMessage() {
        message.value = null
    }
}
