package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.OnboardingProfile
import com.sana.app.repository.AuthRepository
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * OnboardingViewModel.kt — account creation and sign-in.
 * What: validates the form, creates the Firebase account (then saves the profile + curated starter
 *       plan to Firestore) or signs an existing user in. Surfaces a single loading/error state; the
 *       app shell handles navigation once auth state flips to signed-in.
 * Who: Mimo (owns login + auth flow).
 * When: Goal 7 — Firebase integration.
 */
data class OnboardingUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

class OnboardingViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository,
    private val sanaRepository: SanaRepository = AppModule.sanaRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = mutableUiState.asStateFlow()

    fun signUp(name: String, email: String, password: String, injuryIds: Set<String>) {
        val validationError = validate(name, email, password)
        if (validationError != null) {
            mutableUiState.value = OnboardingUiState(error = validationError)
            return
        }
        viewModelScope.launch {
            mutableUiState.value = OnboardingUiState(isSubmitting = true)
            authRepository.signUp(email, password, name)
                .onSuccess {
                    val profile = OnboardingProfile(
                        name = name.trim(),
                        email = email.trim(),
                        selectedInjuryIds = injuryIds,
                    )
                    sanaRepository.saveOnboardingProfile(profile)
                        .onFailure { error ->
                            mutableUiState.value =
                                OnboardingUiState(error = error.message ?: "Could not save your profile.")
                        }
                        .onSuccess { mutableUiState.value = OnboardingUiState() }
                }
                .onFailure { error ->
                    mutableUiState.value =
                        OnboardingUiState(error = error.message ?: "Could not create your account.")
                }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            mutableUiState.value = OnboardingUiState(error = "Enter your email and password.")
            return
        }
        viewModelScope.launch {
            mutableUiState.value = OnboardingUiState(isSubmitting = true)
            authRepository.signIn(email, password)
                .onSuccess { mutableUiState.value = OnboardingUiState() }
                .onFailure { error ->
                    mutableUiState.value =
                        OnboardingUiState(error = error.message ?: "Could not sign in.")
                }
        }
    }

    fun clearError() {
        if (mutableUiState.value.error != null) {
            mutableUiState.value = mutableUiState.value.copy(error = null)
        }
    }

    private fun validate(name: String, email: String, password: String): String? = when {
        name.isBlank() -> "Enter your name."
        email.isBlank() -> "Enter your email."
        password.length < 6 -> "Password must be at least 6 characters."
        else -> null
    }
}
