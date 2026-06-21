package com.sana.app.ui.screens.editplaylist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.EditPlaylistUiState
import com.sana.app.viewmodel.EditPlaylistViewModel

/*
 * EditPlaylistRoute.kt — connects EditPlaylistViewModel to the pure EditPlaylistScreen.
 * What: streams the working plan and scored catalog, and forwards add / remove / save / publish.
 * Who: Sam.
 * When: Goal 7 — Firebase integration.
 */
@Composable
fun EditPlaylistRoute(
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onBrowseShared: () -> Unit,
    viewModel: EditPlaylistViewModel = remember { EditPlaylistViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = EditPlaylistUiState())

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    EditPlaylistScreen(
        onBack = onBack,
        onOpenExercise = onOpenExercise,
        onBrowseShared = onBrowseShared,
        planItems = uiState.planItems,
        catalogGroups = uiState.catalogGroups,
        muscleGroupKeys = uiState.muscleGroupKeys,
        isSaving = uiState.isSaving,
        message = uiState.message,
        onMessageShown = viewModel::consumeMessage,
        onAddExercise = viewModel::addExercise,
        onRemoveExercise = viewModel::removeExercise,
        onMoveExercise = viewModel::moveExercise,
        onSave = viewModel::save,
        onPublish = viewModel::publish,
    )
}
