package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.AuthUser
import com.sana.app.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * AuthViewModel.kt — the single source of truth for "is someone signed in?".
 * What: maps the Firebase auth state into a three-way [AuthState] the app shell observes to decide
 *       between the loading splash, the onboarding screen, and the signed-in app.
 * Who: Mimo (owns login + auth flow).
 * When: Goal 7 — Firebase integration.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: AuthUser) : AuthState
}

class AuthViewModel(
    authRepository: AuthRepository = AppModule.authRepository,
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.currentUser
        .map { user -> if (user == null) AuthState.SignedOut else AuthState.SignedIn(user) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading,
        )
}
