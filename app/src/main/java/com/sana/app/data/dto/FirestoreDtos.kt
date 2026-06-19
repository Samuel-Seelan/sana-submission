package com.sana.app.data.dto

/*
 * FirestoreDtos.kt — Firestore document shapes.
 * What: plain data classes that mirror the Firestore documents exactly, kept separate from the
 *       domain models so serialization concerns never leak into the UI. Every field has a default
 *       so Firestore's toObject() can use the generated no-arg constructor.
 * Who: Sana team (shared backend scaffold).
 * When: Goal 7 — Firebase integration.
 */

/** users/{uid}/profile/private */
data class UserProfileDto(
    val name: String = "",
    val email: String = "",
    val selectedInjuryIds: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/** users/{uid}/plan/{planItemId} — stores the exerciseId, joined to the catalog when read. */
data class PlanItemDto(
    val exerciseId: String = "",
    val targetReps: Int = 0,
    val targetSets: Int = 0,
    val targetDurationSec: Int = 0,
    val order: Int = 0,
)

/** users/{uid}/sessions/{sessionId} — the session id is the document id. */
data class WorkoutSessionDto(
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val totalReps: Int = 0,
    val totalSets: Int = 0,
    val totalTimeMs: Long = 0L,
    val exerciseCount: Int = 0,
)

/** users/{uid}/recordings/{recordingId} — flat per-user collection for easy day/exercise queries. */
data class RecordingDto(
    val sessionId: String = "",
    val exerciseId: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val durationMs: Long = 0L,
    val videoStoragePath: String? = null,
    val createdAt: Long = 0L,
)

/** sharedPlaylists/{playlistId} — public playlists other users can browse and use. */
data class SharedPlaylistDto(
    val ownerUid: String = "",
    val ownerName: String = "",
    val title: String = "",
    val description: String = "",
    val injuryFocus: List<String> = emptyList(),
    val itemCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val uses: Int = 0,
    // Named "published" rather than "isPublic": Firestore's Kotlin mapper drops the `is` prefix on
    // Boolean getters, which would store/query the field under the wrong key.
    val published: Boolean = true,
)

/** sharedPlaylists/{playlistId}/items/{itemId} */
data class SharedPlaylistItemDto(
    val exerciseId: String = "",
    val targetReps: Int = 0,
    val targetSets: Int = 0,
    val targetDurationSec: Int = 0,
    val order: Int = 0,
)
