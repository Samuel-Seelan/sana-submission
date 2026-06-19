@file:OptIn(ExperimentalCoroutinesApi::class)

package com.sana.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sana.app.data.dto.PlanItemDto
import com.sana.app.data.dto.RecordingDto
import com.sana.app.data.dto.UserProfileDto
import com.sana.app.data.dto.WorkoutSessionDto
import com.sana.app.model.DayStats
import com.sana.app.model.Exercise
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.InjuryProfile
import com.sana.app.model.Milestone
import com.sana.app.model.OnboardingProfile
import com.sana.app.model.PlanItem
import com.sana.app.model.Recommendations
import com.sana.app.model.RecordingMetadata
import com.sana.app.model.UserProfile
import com.sana.app.model.WeeklyStat
import com.sana.app.model.WorkoutSession
import com.sana.app.repository.SanaRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/*
 * FirebaseSanaRepository.kt — Cloud Firestore implementation of the app's data contract.
 * What: stores and observes the signed-in user's profile, curated plan, sessions, and recording
 *       metadata in Firestore. The exercise/injury catalog is bundled reference data. Weekly stats
 *       and milestones are derived from saved sessions. All reads are scoped to the current uid and
 *       re-bind automatically when the user signs in or out.
 * Who: Sana team (shared backend; session/progress methods support Isaac and Max).
 * When: Goal 7 — Firebase integration.
 */
class FirebaseSanaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : SanaRepository {

    private val refs = FirebaseRefs(db)

    // ---- Profile ----

    override fun observeUserProfile(): Flow<UserProfile?> =
        auth.uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(null)
            } else {
                refs.userProfile(uid).snapshotFlow()
                    .map { it?.toObject(UserProfileDto::class.java)?.toDomain() }
                    .catch { emit(null) }
            }
        }

    override fun observeSelectedInjuryIds(): Flow<Set<String>> =
        auth.uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptySet())
            } else {
                refs.userProfile(uid).snapshotFlow()
                    .map { it?.toObject(UserProfileDto::class.java)?.selectedInjuryIds?.toSet() ?: emptySet() }
                    .catch { emit(emptySet()) }
            }
        }

    override fun observeInjuryProfiles(): Flow<List<InjuryProfile>> =
        flowOf(ExerciseCatalog.injuryProfiles)

    override suspend fun saveOnboardingProfile(profile: OnboardingProfile): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("You must be signed in."))
        return runCatching {
            val now = System.currentTimeMillis()
            refs.userProfile(uid).set(
                UserProfileDto(
                    name = profile.name.trim(),
                    email = profile.email.trim(),
                    selectedInjuryIds = profile.selectedInjuryIds.toList(),
                    createdAt = now,
                    updatedAt = now,
                ),
            ).await()

            // Seed the curated starter plan only if the user doesn't have one yet.
            val existingPlan = refs.userPlan(uid).get().await()
            if (existingPlan.isEmpty) {
                val initial = Recommendations.initialPlan(profile.selectedInjuryIds)
                val batch = db.batch()
                initial.forEachIndexed { index, item ->
                    batch.set(refs.userPlan(uid).document(item.exercise.id), item.toDto(index))
                }
                batch.commit().await()
            }
            Unit
        }
    }

    // ---- Catalog (bundled reference data) ----

    override fun observeExercises(): Flow<List<Exercise>> = flowOf(ExerciseCatalog.exercises)

    // ---- Plan ----

    override fun observeCurrentPlan(): Flow<List<PlanItem>> =
        auth.uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                refs.userPlan(uid).orderBy("order").snapshotFlow()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull {
                            it.toObject(PlanItemDto::class.java)?.toDomainOrNull()
                        }
                    }
                    .catch { emit(emptyList()) }
            }
        }

    override suspend fun saveCurrentPlan(items: List<PlanItem>): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("You must be signed in."))
        return runCatching {
            val collection = refs.userPlan(uid)
            val existing = collection.get().await()
            val batch = db.batch()
            existing.documents.forEach { batch.delete(it.reference) }
            items.forEachIndexed { index, item ->
                batch.set(collection.document(item.exercise.id), item.toDto(index))
            }
            batch.commit().await()
            Unit
        }
    }

    // ---- Sessions, progress, milestones ----

    override fun observeWeeklyStats(): Flow<List<WeeklyStat>> =
        observeSessions().map { sessions -> computeWeeklyStats(sessions) }

    override fun observeMilestones(): Flow<List<Milestone>> =
        observeSessions().map { sessions -> computeMilestones(sessions) }

    override fun observeSessions(): Flow<List<WorkoutSession>> =
        auth.uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                refs.userSessions(uid).orderBy("startedAt").snapshotFlow()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { document ->
                            document.toObject(WorkoutSessionDto::class.java)?.toDomain(document.id)
                        }
                    }
                    .catch { emit(emptyList()) }
            }
        }

    override fun observeDayStats(epochDay: Long): Flow<DayStats?> =
        observeRecordingsForDay(epochDay).map { dayRecordings ->
            if (dayRecordings.isEmpty()) {
                null
            } else {
                DayStats(
                    totalReps = dayRecordings.sumOf { it.reps },
                    totalSets = dayRecordings.sumOf { it.sets },
                    totalTimeMs = dayRecordings.sumOf { it.durationMs },
                    exercisesDone = dayRecordings.map { it.exerciseId }.distinct().size,
                )
            }
        }

    override fun observeRecordingsForDay(epochDay: Long): Flow<List<RecordingMetadata>> =
        observeAllRecordings().map { recordings ->
            recordings.filter { epochDayOf(it.createdAtMillis) == epochDay }
        }

    override fun observeExerciseHistory(exerciseId: String): Flow<List<RecordingMetadata>> =
        observeAllRecordings().map { recordings ->
            recordings.filter { it.exerciseId == exerciseId }
        }

    override suspend fun saveCompletedSession(
        session: WorkoutSession,
        recordings: List<RecordingMetadata>,
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("You must be signed in."))
        return runCatching {
            val batch = db.batch()
            batch.set(refs.userSessions(uid).document(session.id), session.toDto())
            recordings.forEach { recording ->
                batch.set(refs.userRecordings(uid).document(recording.id), recording.toDto())
            }
            batch.commit().await()
            Unit
        }
    }

    // ---- Internal helpers ----

    private fun observeAllRecordings(): Flow<List<RecordingMetadata>> =
        auth.uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                refs.userRecordings(uid).snapshotFlow()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { document ->
                            document.toObject(RecordingDto::class.java)?.toDomain(document.id)
                        }
                    }
                    .catch { emit(emptyList()) }
            }
        }

    private fun computeWeeklyStats(sessions: List<WorkoutSession>): List<WeeklyStat> {
        if (sessions.isEmpty()) return emptyList()
        val zone = ZoneId.systemDefault()
        return sessions
            .groupBy { session ->
                Instant.ofEpochMilli(session.startedAtMillis)
                    .atZone(zone)
                    .toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .toEpochDay()
            }
            .map { (weekStart, weekSessions) ->
                WeeklyStat(
                    weekStartEpochDay = weekStart,
                    totalReps = weekSessions.sumOf { it.totalReps },
                    totalTimeMs = weekSessions.sumOf { it.totalTimeMs },
                )
            }
            .sortedBy { it.weekStartEpochDay }
    }

    private fun computeMilestones(sessions: List<WorkoutSession>): List<Milestone> {
        if (sessions.isEmpty()) return emptyList()
        val totalReps = sessions.sumOf { it.totalReps }
        return buildList {
            add(Milestone(1L, "First session"))
            if (totalReps >= 100) add(Milestone(2L, "100 reps"))
            if (sessions.size >= 10) add(Milestone(3L, "10 sessions"))
        }
    }

    private fun epochDayOf(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
}
