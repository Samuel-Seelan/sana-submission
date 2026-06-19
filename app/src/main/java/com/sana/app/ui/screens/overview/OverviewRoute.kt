package com.sana.app.ui.screens.overview

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.OverviewUiState
import com.sana.app.viewmodel.OverviewViewModel

/*
 * OverviewRoute.kt — connects OverviewViewModel to the pure OverviewScreen.
 * What: streams the derived this-week strip and weekly rows from saved sessions.
 * Who: Max.
 * When: Goal 7 — Firebase integration.
 */
@Composable
fun OverviewRoute(
    onBack: () -> Unit,
    onOpenDay: (Long) -> Unit,
    viewModel: OverviewViewModel = remember { OverviewViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = OverviewUiState())

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    OverviewScreen(
        onBack = onBack,
        onOpenDay = onOpenDay,
        thisWeek = uiState.thisWeek,
        weeks = uiState.weeks,
    )
}
