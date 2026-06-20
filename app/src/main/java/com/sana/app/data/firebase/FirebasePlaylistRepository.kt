package com.sana.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sana.app.data.dto.SharedPlaylistDto
import com.sana.app.data.dto.SharedPlaylistItemDto
import com.sana.app.model.SharedPlaylist
import com.sana.app.model.SharedPlaylistSummary
import com.sana.app.repository.PlaylistRepository
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/*
 * FirebasePlaylistRepository.kt — the multi-user shared-playlist feature, backed by Firestore.
 * What: lets a user publish their current plan as a public playlist, browse playlists published by
 *       other users in real time, and copy one into their own plan (bumping a shared use counter).
 *       This is Sana's meaningful multi-user interaction.
 * Who: Sam (owns playlist + sharing).
 * When: Goal 7 — Firebase integration.
 */
class FirebasePlaylistRepository(
    private val sanaRepository: SanaRepository,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : PlaylistRepository {

    private val refs = FirebaseRefs(db)

    override fun observeSharedPlaylists(): Flow<List<SharedPlaylistSummary>> =
        refs.sharedPlaylists.whereEqualTo("published", true).snapshotFlow()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(SharedPlaylistDto::class.java)?.toSummary(it.id) }
                    .sortedByDescending { it.createdAtMillis }
            }
            .catch { emit(emptyList()) }

    override fun observeSharedPlaylist(playlistId: String): Flow<SharedPlaylist?> {
        val playlistFlow = refs.sharedPlaylists.document(playlistId).snapshotFlow()
            .map { it?.toObject(SharedPlaylistDto::class.java) }
        val itemsFlow = refs.sharedPlaylistItems(playlistId).orderBy("order").snapshotFlow()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(SharedPlaylistItemDto::class.java)?.toDomainOrNull()
                }
            }
        return combine(playlistFlow, itemsFlow) { dto, items ->
            dto?.toDomain(playlistId, items)
        }.catch { emit(null) }
    }

    override suspend fun publishCurrentPlan(
        title: String,
        description: String,
        injuryFocus: List<String>,
    ): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("You must be signed in."))
        return runCatching {
            val plan = sanaRepository.observeCurrentPlan().first()
            if (plan.isEmpty()) {
                throw IllegalStateException("Add exercises to your plan before publishing.")
            }
            val profile = sanaRepository.observeUserProfile().first()
            val now = System.currentTimeMillis()
            val playlistRef = refs.sharedPlaylists.document()
            val dto = SharedPlaylistDto(
                ownerUid = uid,
                ownerName = profile?.name?.takeIf { it.isNotBlank() } ?: "Sana user",
                title = title.trim(),
                description = description.trim(),
                injuryFocus = injuryFocus,
                itemCount = plan.size,
                createdAt = now,
                updatedAt = now,
                uses = 0,
                published = true,
            )
            // Create the playlist document first so the items' security rule can resolve its owner.
            playlistRef.set(dto).await()
            val batch = db.batch()
            plan.forEachIndexed { index, item ->
                batch.set(
                    playlistRef.collection("items").document(item.exercise.id),
                    item.toSharedItemDto(index),
                )
            }
            batch.commit().await()
            playlistRef.id
        }
    }

    override suspend fun useSharedPlaylist(playlistId: String): Result<Unit> {
        if (auth.currentUser == null) {
            return Result.failure(IllegalStateException("You must be signed in."))
        }
        return runCatching {
            val itemsSnapshot = refs.sharedPlaylistItems(playlistId).orderBy("order").get().await()
            val items = itemsSnapshot.documents.mapNotNull {
                it.toObject(SharedPlaylistItemDto::class.java)?.toDomainOrNull()
            }
            if (items.isEmpty()) {
                throw IllegalStateException("This playlist has no exercises.")
            }
            sanaRepository.saveCurrentPlan(items).getOrThrow()
            // Best-effort: bump the public use counter, but never fail the copy if it's blocked.
            runCatching {
                refs.sharedPlaylists.document(playlistId)
                    .update("uses", FieldValue.increment(1))
                    .await()
            }
            Unit
        }
    }
}
