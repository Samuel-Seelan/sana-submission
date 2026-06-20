package com.sana.app.ui.screens.account

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sana.app.model.UserProfile
import com.sana.app.viewmodel.AccountUiState
import com.sana.app.viewmodel.AccountViewModel

/*
 * AccountRoute.kt — connects AccountViewModel to the pure AccountScreen.
 * What: streams the user's profile/injuries from Firestore and forwards save / change-password /
 *       sign-out actions. Sign-out flips global auth state, so the shell returns to onboarding.
 * Who: Mimo.
 * When: Goal 7 — Firebase integration.
 */
@Composable
fun AccountRoute(
    onBack: () -> Unit,
    viewModel: AccountViewModel = remember { AccountViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState(initial = AccountUiState())

    if (uiState.isLoading) {
        Text("Loading")
        return
    }

    AccountScreen(
        onBack = onBack,
        onSignOut = viewModel::signOut,
        user = UserProfile(name = uiState.name, email = uiState.email),
        injuryProfiles = uiState.injuryProfiles,
        initialSelectedInjuryIds = uiState.selectedInjuryIds,
        onSave = viewModel::save,
        onChangePassword = viewModel::changePassword,
        message = uiState.message,
        onMessageShown = viewModel::consumeMessage,
    )
}
