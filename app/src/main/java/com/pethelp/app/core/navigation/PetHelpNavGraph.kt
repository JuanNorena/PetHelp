package com.pethelp.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pethelp.app.features.auth.presentation.ForgotPasswordScreen
import com.pethelp.app.features.auth.presentation.LoginScreen
import com.pethelp.app.features.auth.presentation.RegisterScreen
import com.pethelp.app.features.auth.presentation.SplashScreen
import com.pethelp.app.features.feed.presentation.FeedScreen
import com.pethelp.app.features.moderation.presentation.ModeratorDetailScreen
import com.pethelp.app.features.moderation.presentation.ModeratorPanelScreen
import com.pethelp.app.features.notifications.presentation.NotificationsScreen
import com.pethelp.app.features.post.presentation.CreatePostScreen
import com.pethelp.app.features.post.presentation.EditPostScreen
import com.pethelp.app.features.post.presentation.PostDetailScreen
import com.pethelp.app.features.profile.presentation.EditProfileScreen
import com.pethelp.app.features.profile.presentation.ProfileScreen
import com.pethelp.app.features.reputation.presentation.ReputationScreen
import com.pethelp.app.features.stats.presentation.StatisticsScreen

/**
 * Grafo de navegación principal de PetHelp.
 *
 * Single-Activity Architecture: toda la navegación se gestiona aquí
 * mediante Jetpack Compose Navigation.
 *
 * Flujo:
 *   Splash → (autenticado) → Feed
 *          → (no autenticado) → Login → Register / ForgotPassword
 */
@Composable
fun PetHelpNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // ── Autenticación ─────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // ── Feed principal ────────────────────────────────────────────────────
        composable(Screen.Feed.route) {
            FeedScreen(navController = navController)
        }

        // ── Detalle de publicación ────────────────────────────────────────────
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(postId = postId, navController = navController)
        }

        // ── Crear / Editar publicación ────────────────────────────────────────
        composable(Screen.CreatePost.route) {
            CreatePostScreen(navController = navController)
        }
        composable(
            route = Screen.EditPost.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            EditPostScreen(postId = postId, navController = navController)
        }

        // ── Notificaciones ────────────────────────────────────────────────────
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        // ── Perfil ────────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        // ── Estadísticas ──────────────────────────────────────────────────────
        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }

        // ── Reputación ────────────────────────────────────────────────────────
        composable(Screen.Reputation.route) {
            ReputationScreen(navController = navController)
        }

        // ── Panel de moderación ───────────────────────────────────────────────
        composable(Screen.ModeratorPanel.route) {
            ModeratorPanelScreen(navController = navController)
        }
        composable(
            route = Screen.ModeratorDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            ModeratorDetailScreen(postId = postId, navController = navController)
        }
    }
}
