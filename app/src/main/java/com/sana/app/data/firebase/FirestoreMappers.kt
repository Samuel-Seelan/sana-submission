package com.sana.app.data.firebase

import com.sana.app.data.dto.PlanItemDto
import com.sana.app.data.dto.RecordingDto
import com.sana.app.data.dto.SharedPlaylistDto
import com.sana.app.data.dto.SharedPlaylistItemDto
import com.sana.app.data.dto.UserProfileDto
import com.sana.app.data.dto.WorkoutSessionDto
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.PlanItem
import com.sana.app.model.RecordingMetadata
import com.sana.app.model.SharedPlaylist
import com.sana.app.model.SharedPlaylistSummary
import com.sana.app.model.UserProfile
import com.sana.app.model.WorkoutSession

/*
 * FirestoreMappers.kt — DTO <-> domain conversions.
 * What: keeps Firestore field shapes out of the UI by translating each DTO into a clean domain
 *       model (and back). Plan/playlist items store only an exerciseId, joined to the bundled
 *       catalog here; unknown ids map to null and are dropped by the repository.
 * Who: Sana team (shared backend scaffold).
 * When: Goal 7 — Firebase integration.
 */

fun UserProfileDto.toDomain(): UserProfile = UserProfile(name = name, email = email)

fun PlanItemDto.toDomainOrNull(): PlanItem? {
    val exercise = ExerciseCatalog.find(exerciseId) ?: return null
    return PlanItem(exercise, targetReps, targetSets, targetDurationSec)
}

fun PlanItem.toDto(order: Int): PlanItemDto = PlanItemDto(
    exerciseId = exercise.id,
    targetReps = targetReps,
    targetSets = targetSets,
    targetDurationSec = targetDurationSec,
    order = order,
)

fun WorkoutSessionDto.toDomain(id: String): WorkoutSession = WorkoutSession(
    id = id,
    startedAtMillis = startedAt,
    endedAtMillis = endedAt,
    totalReps = totalReps,
    totalSets = totalSets,
    totalTimeMs = totalTimeMs,
    exerciseCount = exerciseCount,
)

fun WorkoutSession.toDto(): WorkoutSessionDto = WorkoutSessionDto(
    startedAt = startedAtMillis,
    endedAt = endedAtMillis,
    totalReps = totalReps,
    totalSets = totalSets,
    totalTimeMs = totalTimeMs,
    exerciseCount = exerciseCount,
)

fun RecordingDto.toDomain(id: String): RecordingMetadata = RecordingMetadata(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    sets = sets,
    reps = reps,
    durationMs = durationMs,
    videoStoragePath = videoStoragePath,
    createdAtMillis = createdAt,
)

fun RecordingMetadata.toDto(): RecordingDto = RecordingDto(
    sessionId = sessionId,
    exerciseId = exerciseId,
    sets = sets,
    reps = reps,
    durationMs = durationMs,
    videoStoragePath = videoStoragePath,
    createdAt = createdAtMillis,
)

fun SharedPlaylistItemDto.toDomainOrNull(): PlanItem? {
    val exercise = ExerciseCatalog.find(exerciseId) ?: return null
    return PlanItem(exercise, targetReps, targetSets, targetDurationSec)
}

fun PlanItem.toSharedItemDto(order: Int): SharedPlaylistItemDto = SharedPlaylistItemDto(
    exerciseId = exercise.id,
    targetReps = targetReps,
    targetSets = targetSets,
    targetDurationSec = targetDurationSec,
    order = order,
)

fun SharedPlaylistDto.toSummary(id: String): SharedPlaylistSummary = SharedPlaylistSummary(
    id = id,
    ownerUid = ownerUid,
    ownerName = ownerName,
    title = title,
    description = description,
    injuryFocus = injuryFocus,
    exerciseCount = itemCount,
    createdAtMillis = createdAt,
    uses = uses,
)

fun SharedPlaylistDto.toDomain(id: String, items: List<PlanItem>): SharedPlaylist = SharedPlaylist(
    id = id,
    ownerUid = ownerUid,
    ownerName = ownerName,
    title = title,
    description = description,
    injuryFocus = injuryFocus,
    items = items,
    createdAtMillis = createdAt,
    uses = uses,
)
