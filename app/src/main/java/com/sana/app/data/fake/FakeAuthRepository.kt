package com.sana.app.data.fake

import com.sana.app.model.AuthUser
import com.sana.app.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initialUser: AuthUser? = AuthUser(
        uid = "fake-user-1",
        email = "alex.rivera@example.com",
        displayName = "Alex Rivera",
    ),
) : AuthRepository {
    private val userState = MutableStateFlow(initialUser)

    override val currentUser: Flow<AuthUser?> = userState

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUser> {
        val user = AuthUser(
            uid = "fake-${email.trim().lowercase().hashCode()}",
            email = email.trim(),
            displayName = displayName.trim(),
        )
        userState.value = user
        return Result.success(user)
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        val user = AuthUser(
            uid = "fake-${email.trim().lowercase().hashCode()}",
            email = email.trim(),
            displayName = email.substringBefore('@').replace('.', ' ').replaceFirstChar {
                it.uppercase()
            },
        )
        userState.value = user
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        userState.value = null
        return Result.success(Unit)
    }
}
