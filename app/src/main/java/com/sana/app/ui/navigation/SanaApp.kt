package com.sana.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.auth.AuthState
import com.sana.app.ui.screens.onboarding.OnboardingRoute
import com.sana.app.viewmodel.AuthState
import com.sana.app.viewmodel.AuthViewModel

/*
 * SanaApp.kt — the app shell that gates everything on auth state.
 * What: observes the signed-in/out state and shows a loading splash, the onboarding screen, or the
 *       full navigation graph. Signing in or out anywhere in the app flips this automatically.
 * Who: Sana team (shared infrastructure).
 * When: Goal 7 — Firebase auth.
 */
@Composable
fun SanaApp(
    authViewModel: AuthViewModel = remember { AuthViewModel() },
) {
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        AuthState.SignedOut -> OnboardingRoute()
        is AuthState.SignedIn -> SanaNavGraph()
    }
}
