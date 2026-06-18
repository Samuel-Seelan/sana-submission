package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.data.fake.FakeRepositories
import com.sana.app.model.PlanItem
import com.sana.app.model.RecordingMetadata
import com.sana.app.model.WorkoutSession
import com.sana.app.repository.SanaRepository
import com.sana.app.ui.screens.session.SessionPhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionUiState(
    val isLoading: Boolean = true,
    val plan: List<PlanItem> = emptyList(),
    val phase: SessionPhase = SessionPhase.NOT_STARTED,
    val currentIndex: Int = 0,
    val repsThisSet: Int = 0,
    val setsCompleted: Int = 0,
    val totalReps: Int = 0,
    val totalSets: Int = 0,
    val totalTimeMs: Long = 0L,
    val saveError: String? = null,
)

private data class ExerciseResult(
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val durationMs: Long,
)

class SessionViewModel(
    private val sanaRepository: SanaRepository = FakeRepositories.sanaRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = mutableUiState.asStateFlow()

    private var sessionStartedAtMillis: Long = 0L
    private var exerciseStartedAtMillis: Long = 0L
    private var repsForExercise: Int = 0
    private val exerciseResults = mutableListOf<ExerciseResult>()
    private var hasSavedSession = false

    init {
        viewModelScope.launch {
            sanaRepository.observeCurrentPlan().collect { plan ->
                val currentIndex = mutableUiState.value.currentIndex.coerceAtMost(
                    (plan.size - 1).coerceAtLeast(0),
                )
                mutableUiState.value = mutableUiState.value.copy(
                    isLoading = false,
                    plan = plan,
                    currentIndex = currentIndex,
                )
            }
        }
    }

    fun startSession() {
        sessionStartedAtMillis = System.currentTimeMillis()
        mutableUiState.value = mutableUiState.value.copy(
            phase = SessionPhase.RESTING,
            saveError = null,
        )
    }

    fun startExercise() {
        exerciseStartedAtMillis = System.currentTimeMillis()
        repsForExercise = 0
        mutableUiState.value = mutableUiState.value.copy(
            phase = SessionPhase.EXERCISING,
            repsThisSet = 0,
            setsCompleted = 0,
            saveError = null,
        )
    }

    fun decrementReps() {
        val state = mutableUiState.value
        if (state.repsThisSet > 0) {
            mutableUiState.value = state.copy(repsThisSet = state.repsThisSet - 1)
        }
    }

    fun incrementReps() {
        val state = mutableUiState.value
        mutableUiState.value = state.copy(repsThisSet = state.repsThisSet + 1)
    }

    fun completeSet() {
        val state = mutableUiState.value
        repsForExercise += state.repsThisSet
        mutableUiState.value = state.copy(
            repsThisSet = 0,
            setsCompleted = state.setsCompleted + 1,
            totalReps = state.totalReps + state.repsThisSet,
            totalSets = state.totalSets + 1,
        )
    }

    fun endExercise() {
        val state = mutableUiState.value
        val plan = state.plan
        if (plan.isEmpty()) return

        val finalizedState = finalizeCurrentExercise(state)
        val isLast = finalizedState.currentIndex == plan.lastIndex

        mutableUiState.value = if (isLast) {
            finalizedState.copy(phase = SessionPhase.DONE)
        } else {
            finalizedState.copy(
                phase = SessionPhase.RESTING,
                currentIndex = finalizedState.currentIndex + 1,
                repsThisSet = 0,
                setsCompleted = 0,
            )
        }

        if (isLast) {
            saveCompletedSession()
        }
    }

    fun endEarly() {
        val state = mutableUiState.value
        val finalizedState = if (state.phase == SessionPhase.EXERCISING && state.plan.isNotEmpty()) {
            finalizeCurrentExercise(state)
        } else {
            state
        }
        mutableUiState.value = finalizedState.copy(phase = SessionPhase.DONE)
        saveCompletedSession()
    }

    private fun finalizeCurrentExercise(state: SessionUiState): SessionUiState {
        val item = state.plan.getOrNull(state.currentIndex) ?: return state
        val hasPartialSet = state.repsThisSet > 0
        val completedSets = state.setsCompleted + if (hasPartialSet) 1 else 0
        val completedReps = repsForExercise + state.repsThisSet
        val now = System.currentTimeMillis()
        val durationMs = (now - exerciseStartedAtMillis).coerceAtLeast(0L)

        if (completedSets > 0 || completedReps > 0 || item.targetDurationSec > 0) {
            exerciseResults += ExerciseResult(
                exerciseId = item.exercise.id,
                sets = completedSets,
                reps = completedReps,
                durationMs = durationMs,
            )
        }

        repsForExercise = 0

        return state.copy(
            repsThisSet = 0,
            setsCompleted = completedSets,
            totalReps = state.totalReps + state.repsThisSet,
            totalSets = state.totalSets + if (hasPartialSet) 1 else 0,
            totalTimeMs = (now - sessionStartedAtMillis).coerceAtLeast(0L),
        )
    }

    private fun saveCompletedSession() {
        if (hasSavedSession) return
        hasSavedSession = true

        val state = mutableUiState.value
        val endedAtMillis = System.currentTimeMillis()
        val startedAtMillis = if (sessionStartedAtMillis > 0L) {
            sessionStartedAtMillis
        } else {
            endedAtMillis
        }
        val sessionId = "session-$endedAtMillis"
        val totalTimeMs = (endedAtMillis - startedAtMillis).coerceAtLeast(state.totalTimeMs)
        val session = WorkoutSession(
            id = sessionId,
            startedAtMillis = startedAtMillis,
            endedAtMillis = endedAtMillis,
            totalReps = state.totalReps,
            totalSets = state.totalSets,
            totalTimeMs = totalTimeMs,
            exerciseCount = exerciseResults.size,
        )
        val recordings = exerciseResults.mapIndexed { index, result ->
            RecordingMetadata(
                id = "$sessionId-recording-$index",
                sessionId = sessionId,
                exerciseId = result.exerciseId,
                sets = result.sets,
                reps = result.reps,
                durationMs = result.durationMs,
                videoStoragePath = null,
                createdAtMillis = endedAtMillis,
            )
        }

        viewModelScope.launch {
            sanaRepository.saveCompletedSession(session, recordings)
                .onFailure { error ->
                    mutableUiState.value = mutableUiState.value.copy(
                        saveError = error.message ?: "Could not save session.",
                    )
                }
        }
    }
}
