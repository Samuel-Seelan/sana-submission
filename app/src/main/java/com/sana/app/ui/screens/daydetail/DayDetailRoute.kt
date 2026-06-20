package com.sana.app.ui.screens.daydetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.model.DayStats
import com.sana.app.viewmodel.DayDetailUiState
import com.sana.app.viewmodel.DayDetailViewModel

/*
 * DayDetailRoute.kt — connects DayDetailViewModel to the pure DayDetailScreen.
 * What: loads the selected day's recordings and totals from Firestore.
 * Who: Max.
 * When: Goal 7 — Firebase integration.
 */
@Composable
fun DayDetailRoute(
    epochDay: Long,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    viewModel: DayDetailViewModel = remember { DayDetailViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = DayDetailUiState())

    LaunchedEffect(epochDay) { viewModel.load(epochDay) }

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    DayDetailScreen(
        epochDay = epochDay,
        onBack = onBack,
        onOpenExercise = onOpenExercise,
        recordings = uiState.recordings,
        dayStats = uiState.dayStats ?: DayStats(totalReps = 0, totalSets = 0, totalTimeMs = 0L, exercisesDone = 0),
    )
}
