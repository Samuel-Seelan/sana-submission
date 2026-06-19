package com.sana.app.ui.screens.exercisedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.ExerciseDetailUiState
import com.sana.app.viewmodel.ExerciseDetailViewModel

/*
 * ExerciseDetailRoute.kt — connects ExerciseDetailViewModel to the pure ExerciseDetailScreen.
 * What: loads the exercise by id, streams its score + history, and forwards "Add to plan".
 * Who: Sam.
 * When: Goal 7 — Firebase integration.
 */
@Composable
fun ExerciseDetailRoute(
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = remember { ExerciseDetailViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = ExerciseDetailUiState())

    LaunchedEffect(exerciseId) { viewModel.load(exerciseId) }

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    ExerciseDetailScreen(
        exerciseId = exerciseId,
        onBack = onBack,
        exercise = uiState.exercise,
        score = uiState.score,
        recordings = uiState.recordings,
        inPlan = uiState.inPlan,
        onAddToPlan = viewModel::addToPlan,
        message = uiState.message,
        onMessageShown = viewModel::consumeMessage,
    )
}
