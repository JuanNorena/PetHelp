package com.pethelp.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import com.pethelp.app.features.post.presentation.MyPostsScreen
import com.pethelp.app.features.post.presentation.PostDetailScreen
import com.pethelp.app.features.profile.presentation.EditProfileScreen
import com.pethelp.app.features.profile.presentation.ProfileScreen
import com.pethelp.app.features.reputation.presentation.ReputationScreen
import com.pethelp.app.features.stats.presentation.StatisticsScreen

/**
 * Grafo de navegación principal de PetHelp.
 *
 * Single-Activity Architecture: toda la navegación se gestiona aquí
 * mediante Jetpack Compose Navigation con Type Safety (Kotlin Serialization).
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
        startDestination = Screen.Splash
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable<Screen.Splash> {
            SplashScreen(navController = navController)
        }

        // ── Autenticación ─────────────────────────────────────────────────────
        composable<Screen.Login> {
            LoginScreen(navController = navController)
        }
        composable<Screen.Register> {
            RegisterScreen(navController = navController)
        }
        composable<Screen.ForgotPassword> {
            ForgotPasswordScreen(navController = navController)
        }

        // ── Feed principal ────────────────────────────────────────────────────
        composable<Screen.Feed> {
            FeedScreen(navController = navController)
        }

        // ── Detalle de publicación ────────────────────────────────────────────
        composable<Screen.PostDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PostDetail>()
            PostDetailScreen(postId = route.postId, navController = navController)
        }

        // ── Crear / Editar / Mis publicaciones ───────────────────────────────
        composable<Screen.CreatePost> {
            CreatePostScreen(navController = navController)
        }
        composable<Screen.MyPosts> {
            MyPostsScreen(navController = navController)
        }
        composable<Screen.EditPost> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.EditPost>()
            EditPostScreen(postId = route.postId, navController = navController)
        }

        // ── Notificaciones ────────────────────────────────────────────────────
        composable<Screen.Notifications> {
            NotificationsScreen(navController = navController)
        }

        // ── Perfil ────────────────────────────────────────────────────────────
        composable<Screen.Profile> {
            ProfileScreen(navController = navController)
        }
        composable<Screen.EditProfile> {
            EditProfileScreen(navController = navController)
        }

        // ── Estadísticas ──────────────────────────────────────────────────────
        composable<Screen.Statistics> {
            StatisticsScreen(navController = navController)
        }

        // ── Reputación ────────────────────────────────────────────────────────
        composable<Screen.Reputation> {
            ReputationScreen(navController = navController)
        }

        // ── Panel de moderación ───────────────────────────────────────────────
        composable<Screen.ModeratorPanel> {
            ModeratorPanelScreen(navController = navController)
        }
        composable<Screen.ModeratorDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ModeratorDetail>()
            ModeratorDetailScreen(postId = route.postId, navController = navController)
        }
    }
}
