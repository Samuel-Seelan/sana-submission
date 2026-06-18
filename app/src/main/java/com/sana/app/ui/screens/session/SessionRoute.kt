package com.sana.app.ui.screens.session

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.SessionUiState
import com.sana.app.viewmodel.SessionViewModel

@Composable
fun SessionRoute(
    onFinished: () -> Unit,
    viewModel: SessionViewModel = remember { SessionViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = SessionUiState())

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    SessionScreen(
        state = uiState,
        onFinished = onFinished,
        onStartSession = viewModel::startSession,
        onStartExercise = viewModel::startExercise,
        onDecrementReps = viewModel::decrementReps,
        onIncrementReps = viewModel::incrementReps,
        onCompleteSet = viewModel::completeSet,
        onEndExercise = viewModel::endExercise,
        onEndEarly = viewModel::endEarly,
    )
}
