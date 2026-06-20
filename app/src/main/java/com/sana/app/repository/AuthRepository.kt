package com.sana.app.repository

import com.sana.app.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUser>

    suspend fun signIn(email: String, password: String): Result<AuthUser>

    suspend fun signOut(): Result<Unit>

    suspend fun changePassword(newPassword: String): Result<Unit>
}
