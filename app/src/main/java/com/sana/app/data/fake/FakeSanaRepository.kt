package com.sana.app.data.fake

import com.sana.app.model.DayStats
import com.sana.app.model.Exercise
import com.sana.app.model.InjuryProfile
import com.sana.app.model.Milestone
import com.sana.app.model.OnboardingProfile
import com.sana.app.model.PlanItem
import com.sana.app.model.RecordingMetadata
import com.sana.app.model.SampleData
import com.sana.app.model.UserProfile
import com.sana.app.model.WeeklyStat
import com.sana.app.model.WorkoutSession
import com.sana.app.repository.SanaRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSanaRepository : SanaRepository {
    private val profileState = MutableStateFlow<UserProfile?>(SampleData.user)
    private val selectedInjuryIdsState = MutableStateFlow(SampleData.selectedInjuryIds)
    private val planState = MutableStateFlow(SampleData.planItems)
    private val sessionsState = MutableStateFlow(sampleSessions())
    private val recordingsState = MutableStateFlow(sampleRecordingMetadata())

    override fun observeUserProfile(): Flow<UserProfile?> = profileState

    override fun observeSelectedInjuryIds(): Flow<Set<String>> = selectedInjuryIdsState

    override fun observeInjuryProfiles(): Flow<List<InjuryProfile>> =
        MutableStateFlow(SampleData.injuryProfiles)

    override suspend fun saveOnboardingProfile(profile: OnboardingProfile): Result<Unit> {
        profileState.value = UserProfile(name = profile.name, email = profile.email)
        selectedInjuryIdsState.value = profile.selectedInjuryIds
        if (planState.value.isEmpty()) {
            planState.value = SampleData.planItems
        }
        return Result.success(Unit)
    }

    override fun observeExercises(): Flow<List<Exercise>> = MutableStateFlow(SampleData.exercises)

    override fun observeCurrentPlan(): Flow<List<PlanItem>> = planState

    override suspend fun saveCurrentPlan(items: List<PlanItem>): Result<Unit> {
        planState.value = items
        return Result.success(Unit)
    }

    override fun observeWeeklyStats(): Flow<List<WeeklyStat>> =
        MutableStateFlow(SampleData.weeklyStats)

    override fun observeMilestones(): Flow<List<Milestone>> =
        MutableStateFlow(SampleData.milestones)

    override fun observeSessions(): Flow<List<WorkoutSession>> = sessionsState

    override fun observeDayStats(epochDay: Long): Flow<DayStats?> =
        recordingsState.map { recordings ->
            val dayRecordings = recordings.filter { recording ->
                recording.createdAtMillis.toEpochDay() == epochDay
            }
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
        recordingsState.map { recordings ->
            recordings.filter { it.createdAtMillis.toEpochDay() == epochDay }
        }

    override fun observeExerciseHistory(exerciseId: String): Flow<List<RecordingMetadata>> =
        recordingsState.map { recordings ->
            recordings.filter { it.exerciseId == exerciseId }
        }

    override suspend fun saveCompletedSession(
        session: WorkoutSession,
        recordings: List<RecordingMetadata>,
    ): Result<Unit> {
        sessionsState.value = sessionsState.value + session
        recordingsState.value = recordingsState.value + recordings
        return Result.success(Unit)
    }

    private fun Long.toEpochDay(): Long =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

    private fun sampleSessions(): List<WorkoutSession> {
        val now = System.currentTimeMillis()
        return listOf(
            WorkoutSession(
                id = "fake-session-1",
                startedAtMillis = now - 18 * 60_000L,
                endedAtMillis = now,
                totalReps = 64,
                totalSets = 12,
                totalTimeMs = 18 * 60_000L,
                exerciseCount = SampleData.planItems.size,
            )
        )
    }

    private fun sampleRecordingMetadata(): List<RecordingMetadata> {
        val today = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return SampleData.recordings.map { recording ->
            RecordingMetadata(
                id = "fake-recording-${recording.id}",
                sessionId = "fake-session-1",
                exerciseId = recording.exercise.id,
                sets = recording.sets,
                reps = recording.reps,
                durationMs = recording.durationMs,
                videoStoragePath = if (recording.hasClip) {
                    "users/fake-user-1/sessions/fake-session-1/recordings/${recording.id}.mp4"
                } else {
                    null
                },
                createdAtMillis = today + recording.id * 60_000L,
            )
        }
    }
}
