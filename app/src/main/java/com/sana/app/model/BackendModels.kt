package com.sana.app.model

/** Minimal authenticated user shape shared by auth-aware ViewModels. */
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
)

/** Profile fields collected during onboarding before they are saved. */
data class OnboardingProfile(
    val name: String,
    val email: String,
    val selectedInjuryIds: Set<String>,
)

/** Persisted workout session summary. */
data class WorkoutSession(
    val id: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val totalReps: Int,
    val totalSets: Int,
    val totalTimeMs: Long,
    val exerciseCount: Int,
)

/** Firebase-friendly recording metadata; video bytes live in Storage. */
data class RecordingMetadata(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val durationMs: Long,
    val videoStoragePath: String?,
    val createdAtMillis: Long,
)

/** Compact row model for the shared playlist list page. */
data class SharedPlaylistSummary(
    val id: String,
    val ownerUid: String,
    val ownerName: String,
    val title: String,
    val description: String,
    val injuryFocus: List<String>,
    val exerciseCount: Int,
    val createdAtMillis: Long,
    val uses: Int,
)

/** Full shared playlist detail with exercise targets resolved into PlanItems. */
data class SharedPlaylist(
    val id: String,
    val ownerUid: String,
    val ownerName: String,
    val title: String,
    val description: String,
    val injuryFocus: List<String>,
    val items: List<PlanItem>,
    val createdAtMillis: Long,
    val uses: Int,
)
