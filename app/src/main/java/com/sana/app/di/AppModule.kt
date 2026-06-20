package com.sana.app.di

import com.sana.app.data.firebase.FirebaseAuthRepository
import com.sana.app.data.firebase.FirebasePlaylistRepository
import com.sana.app.data.firebase.FirebaseSanaRepository
import com.sana.app.repository.AuthRepository
import com.sana.app.repository.PlaylistRepository
import com.sana.app.repository.SanaRepository

/*
 * AppModule.kt — the app's tiny service locator.
 * What: provides the single shared repository instances the ViewModels depend on. ViewModels default
 *       their repository parameter to these, so a no-arg ViewModel constructor still gets the real
 *       Firebase-backed implementation. Swap these to fakes here if you ever want an offline build.
 * Who: Sana team (shared infrastructure).
 * When: Goal 7 — Firebase integration.
 */
object AppModule {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val sanaRepository: SanaRepository by lazy { FirebaseSanaRepository() }
    val playlistRepository: PlaylistRepository by lazy { FirebasePlaylistRepository(sanaRepository) }
}
