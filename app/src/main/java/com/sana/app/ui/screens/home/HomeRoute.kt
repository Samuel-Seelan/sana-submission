package com.sana.app.ui.screens.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.viewmodel.HomeUiState
import com.sana.app.viewmodel.HomeViewModel

@Composable
fun HomeRoute(
    onStartSession: () -> Unit,
    onEditPlaylist: () -> Unit,
    onOpenOverview: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenExercise: (String) -> Unit,
    viewModel: HomeViewModel = remember { HomeViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = HomeUiState())

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    val user = uiState.user
    if (user == null) {
        Text("No profile found")
        return
    }

    HomeScreen(
        onStartSession = onStartSession,
        onEditPlaylist = onEditPlaylist,
        onOpenOverview = onOpenOverview,
        onOpenAccount = onOpenAccount,
        onOpenExercise = onOpenExercise,
        user = user,
        planItems = uiState.planItems,
        weeklyStats = uiState.weeklyStats,
        milestones = uiState.milestones,
    )
}
