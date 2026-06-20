package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.InjuryProfile
import com.sana.app.model.OnboardingProfile
import com.sana.app.repository.AuthRepository
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * AccountViewModel.kt — profile management for the signed-in user.
 * What: streams the user's Firestore profile + selected injuries into one UI state and saves edits
 *       (name, email, injuries), changes the password, and signs out. Sign-out flips global auth
 *       state, so the app shell returns to onboarding on its own.
 * Who: Mimo (owns account + auth flow).
 * When: Goal 7 — Firebase integration.
 */
data class AccountUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val selectedInjuryIds: Set<String> = emptySet(),
    val injuryProfiles: List<InjuryProfile> = emptyList(),
    val message: String? = null,
)

class AccountViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository,
    private val sanaRepository: SanaRepository = AppModule.sanaRepository,
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AccountUiState> = combine(
        sanaRepository.observeUserProfile(),
        sanaRepository.observeSelectedInjuryIds(),
        sanaRepository.observeInjuryProfiles(),
        message,
    ) { profile, injuryIds, injuryProfiles, currentMessage ->
        AccountUiState(
            isLoading = false,
            name = profile?.name.orEmpty(),
            email = profile?.email.orEmpty(),
            selectedInjuryIds = injuryIds,
            injuryProfiles = injuryProfiles,
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountUiState(),
    )

    /** Persists name, email, and injuries together so neither button clobbers the other's fields. */
    fun save(name: String, email: String, injuryIds: Set<String>) {
        viewModelScope.launch {
            val profile = OnboardingProfile(
                name = name.trim(),
                email = email.trim(),
                selectedInjuryIds = injuryIds,
            )
            sanaRepository.saveOnboardingProfile(profile)
                .onSuccess { message.value = "Changes saved" }
                .onFailure { message.value = it.message ?: "Could not save changes." }
        }
    }

    fun changePassword(newPassword: String) {
        if (newPassword.length < 6) {
            message.value = "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            authRepository.changePassword(newPassword)
                .onSuccess { message.value = "Password updated" }
                .onFailure { message.value = it.message ?: "Could not update password." }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    fun consumeMessage() {
        message.value = null
    }
}
