package com.sana.app.data.fake

import com.sana.app.repository.AuthRepository
import com.sana.app.repository.PlaylistRepository
import com.sana.app.repository.SanaRepository

/** Shared fake repository graph for ViewModel development before Firebase is wired in. */
object FakeRepositories {
    val authRepository: AuthRepository = FakeAuthRepository()
    val sanaRepository: SanaRepository = FakeSanaRepository()
    val playlistRepository: PlaylistRepository = FakePlaylistRepository(sanaRepository)
}
