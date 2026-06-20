@file:OptIn(ExperimentalCoroutinesApi::class)

package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.Exercise
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.ExerciseScore
import com.sana.app.model.PlanItem
import com.sana.app.model.Recommendations
import com.sana.app.model.Recording
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * ExerciseDetailViewModel.kt — a single catalog exercise plus the user's history with it.
 * What: resolves the exercise from the catalog, scores it against the user's injuries
 *       (recommended / not advised), streams the user's past recordings of it, and adds it to the
 *       plan. Call load(exerciseId) once from the route.
 * Who: Sam.
 * When: Goal 7 — Firebase integration.
 */
data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exercise: Exercise? = null,
    val score: ExerciseScore? = null,
    val recordings: List<Recording> = emptyList(),
    val inPlan: Boolean = false,
    val message: String? = null,
)

class ExerciseDetailViewModel(
    private val sanaRepository: SanaRepository = AppModule.sanaRepository,
) : ViewModel() {

    private val exerciseId = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val historyFlow = exerciseId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else sanaRepository.observeExerciseHistory(id)
    }

    val uiState: StateFlow<ExerciseDetailUiState> = combine(
        exerciseId,
        sanaRepository.observeSelectedInjuryIds(),
        historyFlow,
        sanaRepository.observeCurrentPlan(),
        message,
    ) { id, injuries, history, plan, currentMessage ->
        val exercise = id?.let { ExerciseCatalog.find(it) }
        ExerciseDetailUiState(
            isLoading = id == null,
            exercise = exercise,
            score = exercise?.let { Recommendations.scoreFor(it.id, injuries) },
            recordings = if (exercise == null) {
                emptyList()
            } else {
                history.mapIndexed { index, metadata ->
                    Recording(
                        id = index.toLong(),
                        exercise = exercise,
                        sets = metadata.sets,
                        reps = metadata.reps,
                        durationMs = metadata.durationMs,
                        hasClip = metadata.videoStoragePath != null,
                    )
                }
            },
            inPlan = exercise != null && plan.any { it.exercise.id == exercise.id },
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseDetailUiState(),
    )

    fun load(id: String) {
        if (exerciseId.value != id) exerciseId.value = id
    }

    fun addToPlan() {
        val id = exerciseId.value ?: return
        val exercise = ExerciseCatalog.find(id) ?: return
        viewModelScope.launch {
            val plan = sanaRepository.observeCurrentPlan().first()
            if (plan.any { it.exercise.id == id }) {
                message.value = "Already in your plan"
                return@launch
            }
            val updated = plan + PlanItem(
                exercise = exercise,
                targetReps = exercise.defaultReps,
                targetSets = exercise.defaultSets,
                targetDurationSec = exercise.defaultDurationSec,
            )
            sanaRepository.saveCurrentPlan(updated)
                .onSuccess { message.value = "Added to your plan" }
                .onFailure { message.value = it.message ?: "Could not add to plan." }
        }
    }

    fun consumeMessage() {
        message.value = null
    }
}
