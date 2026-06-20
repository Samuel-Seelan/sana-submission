package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.CatalogGroup
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.ExerciseType
import com.sana.app.model.PlanItem
import com.sana.app.model.Recommendations
import com.sana.app.model.ScoredExercise
import com.sana.app.repository.PlaylistRepository
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * EditPlaylistViewModel.kt — builds and persists the user's curated plan.
 * What: owns the working plan (add / remove exercises), scores the catalog against the user's
 *       injuries (recommended / not advised), saves the plan to Firestore, and publishes it as a
 *       public shared playlist. The screen is a pure reflection of this state.
 * Who: Sam (owns Edit playlist + sharing).
 * When: Goal 7 — Firebase integration.
 */
data class EditPlaylistUiState(
    val isLoading: Boolean = true,
    val planItems: List<PlanItem> = emptyList(),
    val catalogGroups: List<CatalogGroup> = emptyList(),
    val muscleGroupKeys: List<String> = ExerciseCatalog.muscleGroupKeys,
    val isSaving: Boolean = false,
    val message: String? = null,
)

class EditPlaylistViewModel(
    private val sanaRepository: SanaRepository = AppModule.sanaRepository,
    private val playlistRepository: PlaylistRepository = AppModule.playlistRepository,
) : ViewModel() {

    // null until the saved plan loads once; then the user's in-progress edits drive it.
    private val workingPlan = MutableStateFlow<List<PlanItem>?>(null)
    private val selectedInjuryIds = MutableStateFlow<Set<String>>(emptySet())
    private val isSaving = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            sanaRepository.observeCurrentPlan().collect { plan ->
                if (workingPlan.value == null) workingPlan.value = plan
            }
        }
        viewModelScope.launch {
            sanaRepository.observeSelectedInjuryIds().collect { selectedInjuryIds.value = it }
        }
    }

    val uiState: StateFlow<EditPlaylistUiState> = combine(
        workingPlan,
        selectedInjuryIds,
        isSaving,
        message,
    ) { plan, injuries, saving, currentMessage ->
        EditPlaylistUiState(
            isLoading = plan == null,
            planItems = plan.orEmpty(),
            catalogGroups = buildCatalogGroups(injuries),
            isSaving = saving,
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditPlaylistUiState(),
    )

    fun addExercise(exerciseId: String) {
        val current = workingPlan.value ?: return
        if (current.any { it.exercise.id == exerciseId }) return
        val exercise = ExerciseCatalog.find(exerciseId) ?: return
        workingPlan.value = current + PlanItem(
            exercise = exercise,
            targetReps = exercise.defaultReps,
            targetSets = exercise.defaultSets,
            targetDurationSec = exercise.defaultDurationSec,
        )
    }

    fun removeExercise(exerciseId: String) {
        val current = workingPlan.value ?: return
        workingPlan.value = current.filterNot { it.exercise.id == exerciseId }
    }

    fun save() {
        val plan = workingPlan.value ?: return
        viewModelScope.launch {
            isSaving.value = true
            sanaRepository.saveCurrentPlan(plan)
                .onSuccess { message.value = "Plan saved" }
                .onFailure { message.value = it.message ?: "Could not save your plan." }
            isSaving.value = false
        }
    }

    fun publish(title: String, description: String) {
        if (title.isBlank()) {
            message.value = "Give your playlist a title."
            return
        }
        val plan = workingPlan.value.orEmpty()
        if (plan.isEmpty()) {
            message.value = "Add exercises before publishing."
            return
        }
        viewModelScope.launch {
            isSaving.value = true
            // Persist the working plan first so the published copy matches what the user sees.
            val saved = sanaRepository.saveCurrentPlan(plan)
            if (saved.isFailure) {
                message.value = saved.exceptionOrNull()?.message ?: "Could not save your plan."
                isSaving.value = false
                return@launch
            }
            playlistRepository.publishCurrentPlan(title, description, selectedInjuryIds.value.toList())
                .onSuccess { message.value = "Playlist published" }
                .onFailure { message.value = it.message ?: "Could not publish your playlist." }
            isSaving.value = false
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    private fun buildCatalogGroups(injuryIds: Set<String>): List<CatalogGroup> {
        val scored = ExerciseCatalog.exercises.map { exercise ->
            ScoredExercise(exercise, Recommendations.scoreFor(exercise.id, injuryIds))
        }
        return listOf(
            CatalogGroup(
                key = "recommended",
                title = "Recommended",
                exercises = scored.filter { it.score.recommended },
            ),
            CatalogGroup(
                key = "strength",
                title = "Strength",
                exercises = scored.filter { it.exercise.type == ExerciseType.STRENGTH },
            ),
            CatalogGroup(
                key = "mobility",
                title = "Mobility & balance",
                exercises = scored.filter {
                    it.exercise.type == ExerciseType.MOBILITY || it.exercise.type == ExerciseType.BALANCE
                },
            ),
        ).filter { it.exercises.isNotEmpty() }
    }
}
