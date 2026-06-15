package com.sana.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sana.app.ui.screens.account.AccountScreen
import com.sana.app.ui.screens.daydetail.DayDetailScreen
import com.sana.app.ui.screens.editplaylist.EditPlaylistScreen
import com.sana.app.ui.screens.exercisedetail.ExerciseDetailScreen
import com.sana.app.ui.screens.home.HomeScreen
import com.sana.app.ui.screens.onboarding.OnboardingScreen
import com.sana.app.ui.screens.overview.OverviewScreen
import com.sana.app.ui.screens.session.SessionScreen

/*
 * SanaNavGraph.kt — the app's navigation skeleton.
 * What: defines every route and wires the navigation callbacks between the nine screens.
 *       Screens are pure UI (no view models yet), so navigation is the only wiring here.
 * Who: Sana team (shared infrastructure).
 * When: Goal 6 — UI skeleton.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val EDIT_PLAYLIST = "edit_playlist"
    const val SESSION = "session"
    const val OVERVIEW = "overview"
    const val DAY_DETAIL = "day/{epochDay}"
    const val EXERCISE_DETAIL = "exercise/{exerciseId}"

    fun dayDetail(epochDay: Long) = "day/$epochDay"
    fun exerciseDetail(exerciseId: String) = "exercise/$exerciseId"
}

@Composable
fun SanaNavGraph() {
    val navController = rememberNavController()

    // The skeleton boots straight to Home so the main flow is easy to demo. Onboarding is
    // still reachable (sign out from Account) and previewable on its own.
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onStartSession = { navController.navigate(Routes.SESSION) },
                onEditPlaylist = { navController.navigate(Routes.EDIT_PLAYLIST) },
                onOpenOverview = { navController.navigate(Routes.OVERVIEW) },
                onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
            )
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.EDIT_PLAYLIST) {
            EditPlaylistScreen(
                onBack = { navController.popBackStack() },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
            )
        }
        composable(Routes.SESSION) {
            SessionScreen(
                onFinished = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.OVERVIEW) {
            OverviewScreen(
                onBack = { navController.popBackStack() },
                onOpenDay = { epochDay -> navController.navigate(Routes.dayDetail(epochDay)) },
            )
        }
        composable(
            route = Routes.DAY_DETAIL,
            arguments = listOf(navArgument("epochDay") { type = NavType.LongType }),
        ) { backStackEntry ->
            DayDetailScreen(
                epochDay = backStackEntry.arguments?.getLong("epochDay") ?: 0L,
                onBack = { navController.popBackStack() },
                onOpenExercise = { id -> navController.navigate(Routes.exerciseDetail(id)) },
            )
        }
        composable(
            route = Routes.EXERCISE_DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType }),
        ) { backStackEntry ->
            ExerciseDetailScreen(
                exerciseId = backStackEntry.arguments?.getString("exerciseId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
