package com.sana.app.repository

import com.sana.app.model.DayStats
import com.sana.app.model.Exercise
import com.sana.app.model.InjuryProfile
import com.sana.app.model.Milestone
import com.sana.app.model.OnboardingProfile
import com.sana.app.model.PlanItem
import com.sana.app.model.RecordingMetadata
import com.sana.app.model.UserProfile
import com.sana.app.model.WeeklyStat
import com.sana.app.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface SanaRepository {
    fun observeUserProfile(): Flow<UserProfile?>

    fun observeSelectedInjuryIds(): Flow<Set<String>>

    fun observeInjuryProfiles(): Flow<List<InjuryProfile>>

    suspend fun saveOnboardingProfile(profile: OnboardingProfile): Result<Unit>

    fun observeExercises(): Flow<List<Exercise>>

    fun observeCurrentPlan(): Flow<List<PlanItem>>

    suspend fun saveCurrentPlan(items: List<PlanItem>): Result<Unit>

    fun observeWeeklyStats(): Flow<List<WeeklyStat>>

    fun observeMilestones(): Flow<List<Milestone>>

    fun observeSessions(): Flow<List<WorkoutSession>>

    fun observeDayStats(epochDay: Long): Flow<DayStats?>

    fun observeRecordingsForDay(epochDay: Long): Flow<List<RecordingMetadata>>

    fun observeExerciseHistory(exerciseId: String): Flow<List<RecordingMetadata>>

    suspend fun saveCompletedSession(
        session: WorkoutSession,
        recordings: List<RecordingMetadata>,
    ): Result<Unit>
}
