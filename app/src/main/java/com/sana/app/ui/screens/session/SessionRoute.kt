package com.sana.app.ui.screens.session

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sana.app.ui.components.CameraSessionRecorder
import com.sana.app.viewmodel.SessionUiState
import com.sana.app.viewmodel.SessionViewModel

/**
 * What: connects the guided workout screen to session state, camera recording, and automatic rep
 * counting controls.
 * Who: Isaac.
 * When: Goal 7 — Firebase integration.
 */
@Composable
fun SessionRoute(
    onFinished: () -> Unit,
    viewModel: SessionViewModel = remember { SessionViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = SessionUiState())
    var showAutoCountPrompt by remember { mutableStateOf(true) }

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    if (showAutoCountPrompt) {
        AlertDialog(
            onDismissRequest = { showAutoCountPrompt = false },
            title = { Text("Enable automatic rep counting?") },
            text = {
                Text(
                    "ML Kit pose detection can count squat reps automatically when a squat exercise is active.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setAutomaticRepCountingEnabled(true)
                        showAutoCountPrompt = false
                    },
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setAutomaticRepCountingEnabled(false)
                        showAutoCountPrompt = false
                    },
                ) {
                    Text("Not now")
                }
            },
        )
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
        cameraContent = { modifier ->
            CameraSessionRecorder(
                shouldRecord = uiState.phase != SessionPhase.NOT_STARTED &&
                    uiState.phase != SessionPhase.DONE,
                automaticRepCountingEnabled = uiState.automaticRepCountingEnabled,
                squatRepCountingActive = uiState.automaticRepCountingEnabled &&
                    uiState.phase == SessionPhase.EXERCISING &&
                    uiState.isSquatExercise,
                modifier = modifier,
                onRecordingFilePrepared = viewModel::onRecordingFilePrepared,
                onRecordingFinalized = viewModel::onRecordingFinalized,
                onRecordingError = viewModel::onRecordingError,
                onAutomaticRepCount = viewModel::onAutomaticRepCount,
                onPoseStatusChanged = viewModel::onPoseStatusChanged,
            )
        },
    )
}
