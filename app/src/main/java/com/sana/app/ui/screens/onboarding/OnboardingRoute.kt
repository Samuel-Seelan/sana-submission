package com.sana.app.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.model.ExerciseCatalog
import com.sana.app.viewmodel.OnboardingUiState
import com.sana.app.viewmodel.OnboardingViewModel

/*
 * OnboardingRoute.kt — connects OnboardingViewModel to the pure OnboardingScreen.
 * What: collects the submitting/error state and forwards sign-up / sign-in to the ViewModel.
 *       No navigation here: when auth succeeds, the app shell swaps onboarding for the app.
 * Who: Mimo.
 * When: Goal 7 — Firebase auth.
 */
@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel = remember { OnboardingViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = OnboardingUiState())

    OnboardingScreen(
        onSignUp = viewModel::signUp,
        onSignIn = viewModel::signIn,
        onFieldChanged = viewModel::clearError,
        isSubmitting = uiState.isSubmitting,
        errorMessage = uiState.error,
        injuryProfiles = ExerciseCatalog.injuryProfiles,
    )
}
