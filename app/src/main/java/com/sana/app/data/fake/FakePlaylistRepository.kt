package com.sana.app.data.fake

import com.sana.app.model.PlanItem
import com.sana.app.model.SampleData
import com.sana.app.model.SharedPlaylist
import com.sana.app.model.SharedPlaylistSummary
import com.sana.app.repository.PlaylistRepository
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FakePlaylistRepository(
    private val sanaRepository: SanaRepository,
) : PlaylistRepository {
    private val playlistsState = MutableStateFlow(sampleSharedPlaylists())

    override fun observeSharedPlaylists(): Flow<List<SharedPlaylistSummary>> =
        playlistsState.map { playlists ->
            playlists.map { playlist ->
                SharedPlaylistSummary(
                    id = playlist.id,
                    ownerUid = playlist.ownerUid,
                    ownerName = playlist.ownerName,
                    title = playlist.title,
                    description = playlist.description,
                    injuryFocus = playlist.injuryFocus,
                    exerciseCount = playlist.items.size,
                    createdAtMillis = playlist.createdAtMillis,
                    uses = playlist.uses,
                )
            }
        }

    override fun observeSharedPlaylist(playlistId: String): Flow<SharedPlaylist?> =
        playlistsState.map { playlists -> playlists.firstOrNull { it.id == playlistId } }

    override suspend fun publishCurrentPlan(
        title: String,
        description: String,
        injuryFocus: List<String>,
    ): Result<String> {
        val currentPlan = sanaRepository.observeCurrentPlan().first()
        val profile = sanaRepository.observeUserProfile().first()
        val playlistId = "fake-shared-${System.currentTimeMillis()}"
        val playlist = SharedPlaylist(
            id = playlistId,
            ownerUid = "fake-user-1",
            ownerName = profile?.name.orEmpty().ifBlank { "Sana user" },
            title = title.trim(),
            description = description.trim(),
            injuryFocus = injuryFocus,
            items = currentPlan,
            createdAtMillis = System.currentTimeMillis(),
            uses = 0,
        )
        playlistsState.value = playlistsState.value + playlist
        return Result.success(playlistId)
    }

    override suspend fun useSharedPlaylist(playlistId: String): Result<Unit> {
        val playlist = playlistsState.value.firstOrNull { it.id == playlistId }
            ?: return Result.failure(IllegalArgumentException("Shared playlist not found."))
        sanaRepository.saveCurrentPlan(playlist.items)
        playlistsState.value = playlistsState.value.map {
            if (it.id == playlistId) it.copy(uses = it.uses + 1) else it
        }
        return Result.success(Unit)
    }

    private fun sampleSharedPlaylists(): List<SharedPlaylist> {
        val baseItems: List<PlanItem> = SampleData.planItems
        return listOf(
            SharedPlaylist(
                id = "knee-starter",
                ownerUid = "coach-maya",
                ownerName = "Maya Chen",
                title = "Knee recovery starter",
                description = "Gentle quad activation, knee mobility, and supported holds.",
                injuryFocus = listOf("acl_tear", "meniscus_tear"),
                items = baseItems.take(4),
                createdAtMillis = System.currentTimeMillis() - 3 * 86_400_000L,
                uses = 18,
            ),
            SharedPlaylist(
                id = "back-core-reset",
                ownerUid = "coach-ravi",
                ownerName = "Ravi Patel",
                title = "Back and core reset",
                description = "Low-impact stability work for early lower-back recovery.",
                injuryFocus = listOf("lower_back_strain"),
                items = listOf(
                    SampleData.planItems.last(),
                    PlanItem(
                        exercise = SampleData.birdDog,
                        targetReps = 8,
                        targetSets = 3,
                        targetDurationSec = 0,
                    ),
                    PlanItem(
                        exercise = SampleData.gluteBridge,
                        targetReps = 12,
                        targetSets = 3,
                        targetDurationSec = 0,
                    ),
                ),
                createdAtMillis = System.currentTimeMillis() - 6 * 86_400_000L,
                uses = 11,
            ),
        )
    }
}
