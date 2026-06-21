package com.sana.app.repository

import com.sana.app.model.SharedPlaylist
import com.sana.app.model.SharedPlaylistSummary
import kotlinx.coroutines.flow.Flow

/**
 * What: defines how shared playlists are observed, published, and copied into a user's plan.
 * Who: Isaac.
 * When: Goal 7 — Firebase integration.
 */
interface PlaylistRepository {
    fun observeSharedPlaylists(): Flow<List<SharedPlaylistSummary>>

    fun observeSharedPlaylist(playlistId: String): Flow<SharedPlaylist?>

    suspend fun publishCurrentPlan(
        title: String,
        description: String,
        injuryFocus: List<String>,
    ): Result<String>

    suspend fun useSharedPlaylist(playlistId: String): Result<Unit>
}
