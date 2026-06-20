package com.sana.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.sana.app.model.AuthUser
import com.sana.app.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
 * FirebaseAuthRepository.kt — email/password auth backed by Firebase Authentication.
 * What: observes auth state as a Flow and implements sign up / sign in / sign out. Sign up also
 *       stores the display name on the Firebase user so the rest of the app can greet them by name.
 * Who: Mimo (owns login + auth flow).
 * When: Goal 7 — Firebase integration.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: error("Account could not be created.")
        val update = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName.trim())
            .build()
        user.updateProfile(update).await()
        AuthUser(uid = user.uid, email = user.email ?: email.trim(), displayName = displayName.trim())
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: error("Could not sign in.")
        user.toAuthUser()
    }

    override suspend fun signOut(): Result<Unit> = runCatching { auth.signOut() }

    override suspend fun changePassword(newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("You must be signed in.")
        user.updatePassword(newPassword).await()
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser =
    AuthUser(uid = uid, email = email.orEmpty(), displayName = displayName.orEmpty())
