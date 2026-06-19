package com.sana.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sana.app.ui.screens.account.AccountRoute
import com.sana.app.ui.screens.daydetail.DayDetailRoute
import com.sana.app.ui.screens.editplaylist.EditPlaylistRoute
import com.sana.app.ui.screens.exercisedetail.ExerciseDetailRoute
import com.sana.app.ui.screens.home.HomeRoute
import com.sana.app.ui.screens.overview.OverviewRoute
import com.sana.app.ui.screens.session.SessionRoute
import com.sana.app.ui.screens.sharedplaylistdetail.SharedPlaylistDetailRoute
import com.sana.app.ui.screens.sharedplaylists.SharedPlaylistsRoute

/*
 * SanaNavGraph.kt — the signed-in app's navigation graph.
 * What: defines every in-app route and wires navigation between the screens via their Route
 *       wrappers (each Route owns its ViewModel). Onboarding lives outside this graph — the app
 *       shell (SanaApp) shows it when signed out — so this graph starts at Home.
 * Who: Sana team (shared infrastructure).
 * When: Goal 7 — Firebase integration.
 */
object Routes {
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val EDIT_PLAYLIST = "edit_playlist"
    const val SESSION = "session"
    const val OVERVIEW = "overview"
    const val SHARED_PLAYLISTS = "shared_playlists"
    const val DAY_DETAIL = "day/{epochDay}"
    const val EXERCISE_DETAIL = "exercise/{exerciseId}"
    const val SHARED_PLAYLIST_DETAIL = "shared_playlist/{playlistId}"

    fun dayDetail(epochDay: Long) = "day/$epochDay"
    fun exerciseDetail(exerciseId: String) = "exercise/$exerciseId"
    fun sharedPlaylistDetail(playlistId: String) = "shared_playlist/$playlistId"
}

@Composable
fun SanaNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeRoute(
                onStartSession = { navController.navigate(Routes.SESSION) },
                onEditPlaylist = { navController.navigate(Routes.EDIT_PLAYLIST) },
                onOpenOverview = { navController.navigate(Routes.OVERVIEW) },
                onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
            )
        }
        composable(Routes.ACCOUNT) {
            AccountRoute(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.EDIT_PLAYLIST) {
            EditPlaylistRoute(
                onBack = { navController.popBackStack() },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
                onBrowseShared = { navController.navigate(Routes.SHARED_PLAYLISTS) },
            )
        }
        composable(Routes.SESSION) {
            SessionRoute(
                onFinished = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.OVERVIEW) {
            OverviewRoute(
                onBack = { navController.popBackStack() },
                onOpenDay = { epochDay -> navController.navigate(Routes.dayDetail(epochDay)) },
            )
        }
        composable(Routes.SHARED_PLAYLISTS) {
            SharedPlaylistsRoute(
                onBack = { navController.popBackStack() },
                onOpenPlaylist = { id -> navController.navigate(Routes.sharedPlaylistDetail(id)) },
            )
        }
        composable(
            route = Routes.DAY_DETAIL,
            arguments = listOf(navArgument("epochDay") { type = NavType.LongType }),
        ) { backStackEntry ->
            DayDetailRoute(
                epochDay = backStackEntry.arguments?.getLong("epochDay") ?: 0L,
                onBack = { navController.popBackStack() },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
            )
        }
        composable(
            route = Routes.EXERCISE_DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType }),
        ) { backStackEntry ->
            ExerciseDetailRoute(
                exerciseId = backStackEntry.arguments?.getString("exerciseId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.SHARED_PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
        ) { backStackEntry ->
            SharedPlaylistDetailRoute(
                playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty(),
                onBack = { navController.popBackStack() },
                onUsed = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
            )
        }
    }
}
