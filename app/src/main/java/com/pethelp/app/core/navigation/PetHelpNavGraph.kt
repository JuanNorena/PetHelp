/**
 * Grafo de navegación principal de PetHelp.
 *
 * Define todas las rutas y destinos de la aplicación usando [NavHost]
 * de Jetpack Compose Navigation. Cada pantalla se registra con su Composable
 * correspondiente y los argumentos tipados se extraen mediante [toRoute].
 *
 * Usa [Screen] como contrato tipado de rutas para evitar cadenas mágicas
 * y obtener autocompletado en tiempo de compilación.
 */
package com.pethelp.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pethelp.app.features.ai.presentation.AIQuizScreen
import com.pethelp.app.features.ai.presentation.AIResultsScreen
import com.pethelp.app.features.auth.presentation.ForgotPasswordScreen
import com.pethelp.app.features.auth.presentation.LoginScreen
import com.pethelp.app.features.auth.presentation.RegisterScreen
import com.pethelp.app.features.auth.presentation.SplashScreen
import com.pethelp.app.features.chat.presentation.ChatScreen
import com.pethelp.app.features.chat.presentation.ChatThreadScreen
import com.pethelp.app.features.feed.presentation.FeedScreen
import com.pethelp.app.features.map.presentation.MapScreen
import com.pethelp.app.features.moderation.presentation.ModeratorDetailScreen
import com.pethelp.app.features.moderation.presentation.ModeratorPanelScreen
import com.pethelp.app.features.notifications.presentation.NotificationsScreen
import com.pethelp.app.features.post.presentation.AdoptionRequestScreen
import com.pethelp.app.features.post.presentation.AdoptionRequestsScreen
import com.pethelp.app.features.post.presentation.AdoptionSuccessScreen
import com.pethelp.app.features.post.presentation.CreatePostScreen
import com.pethelp.app.features.post.presentation.EditPostScreen
import com.pethelp.app.features.post.presentation.FavoritesScreen
import com.pethelp.app.features.post.presentation.LocationSelectionScreen
import com.pethelp.app.features.post.presentation.MyPostsScreen
import com.pethelp.app.features.post.presentation.PostDetailScreen
import com.pethelp.app.features.post.presentation.PostDetailsScreen
import com.pethelp.app.features.post.presentation.PostReviewScreen
import com.pethelp.app.features.profile.presentation.EditProfileScreen
import com.pethelp.app.features.profile.presentation.HelpCenterScreen
import com.pethelp.app.features.profile.presentation.LanguageScreen
import com.pethelp.app.features.profile.presentation.PrivacyScreen
import com.pethelp.app.features.profile.presentation.ProfileScreen
import com.pethelp.app.features.profile.presentation.ProfileVisibilityScreen
import com.pethelp.app.features.profile.presentation.SecurityScreen
import com.pethelp.app.features.profile.presentation.SettingsScreen
import com.pethelp.app.features.profile.presentation.UserGuideScreen
import com.pethelp.app.features.reputation.presentation.ReputationScreen
import com.pethelp.app.features.stats.presentation.StatisticsScreen

/**
 * Grafo de navegación principal de PetHelp.
 *
 * Esta función define todas las rutas de navegación de la aplicación usando
 * Jetpack Compose Navigation.
 *
 * En una arquitectura de actividad única, todas las pantallas se representan como
 * destinos en este grafo. El `NavHostController` se usa para mover al usuario
 * entre esas pantallas.
 */
@Composable
fun PetHelpNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {

        // ── Pantalla inicial / Splash ────────────────────────────────────────
        composable<Screen.Splash> {
            SplashScreen(navController = navController)
        }

        // ── Rutas de autenticación ──────────────────────────────────────────
        composable<Screen.Login> {
            LoginScreen(navController = navController)
        }
        composable<Screen.Register> {
            RegisterScreen(navController = navController)
        }
        composable<Screen.ForgotPassword> {
            ForgotPasswordScreen(navController = navController)
        }

        // ── Feed principal ──────────────────────────────────────────────────
        composable<Screen.Feed> {
            FeedScreen(navController = navController)
        }

        // ── Detalle de publicación ──────────────────────────────────────────
        composable<Screen.PostDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PostDetail>()
            PostDetailScreen(postId = route.postId, navController = navController)
        }

        // ── Solicitudes de adopción ─────────────────────────────────────────
        composable<Screen.AdoptionRequest> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AdoptionRequest>()
            AdoptionRequestScreen(
                postId = route.postId,
                petName = route.petName,
                navController = navController
            )
        }
        composable<Screen.AdoptionRequests> {
            AdoptionRequestsScreen(navController = navController)
        }
        composable<Screen.AdoptionSuccess> {
            AdoptionSuccessScreen(navController = navController)
        }

        // ── Crear / editar publicaciones ──────────────────────────────────────
        composable<Screen.CreatePost> {
            CreatePostScreen(navController = navController)
        }
        composable<Screen.LocationSelection> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.LocationSelection>()
            LocationSelectionScreen(navController = navController, postData = route)
        }
        composable<Screen.PostDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PostDetails>()
            PostDetailsScreen(navController = navController, postData = route)
        }
        composable<Screen.PostReview> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PostReview>()
            PostReviewScreen(navController = navController, postData = route)
        }
        composable<Screen.MyPosts> {
            MyPostsScreen(navController = navController)
        }
        composable<Screen.EditPost> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.EditPost>()
            EditPostScreen(postId = route.postId, navController = navController)
        }

        // ── Notificaciones ──────────────────────────────────────────────────
        composable<Screen.Notifications> {
            NotificationsScreen(navController = navController)
        }

        // ── Mensajes / Chat ─────────────────────────────────────────────────
        composable<Screen.Chat> {
            ChatScreen(navController = navController)
        }
        composable<Screen.ChatThread> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ChatThread>()
            ChatThreadScreen(navController = navController, threadId = route.threadId)
        }

        // ── Inteligencia Artificial ─────────────────────────────────────────
        composable<Screen.AIQuiz> {
            AIQuizScreen(navController = navController)
        }
        composable<Screen.AIResults> {
            AIResultsScreen(
                recommendations = "",
                navController = navController,
                matchedPosts = emptyList(),
                isLoadingPosts = false,
                onRestart = { navController.popBackStack(Screen.AIQuiz, false) }
            )
        }

        // ── Mapa ────────────────────────────────────────────────────────────
        composable<Screen.Map> {
            MapScreen(navController = navController)
        }

        // ── Perfil y ajustes ──────────────────────────────────────────────────
        composable<Screen.Profile> {
            ProfileScreen(navController = navController)
        }
        composable<Screen.Favorites> {
            FavoritesScreen(navController = navController)
        }
        composable<Screen.EditProfile> {
            EditProfileScreen(navController = navController)
        }
        composable<Screen.Settings> {
            SettingsScreen(navController = navController)
        }
        composable<Screen.Language> {
            LanguageScreen(navController = navController)
        }
        composable<Screen.Security> {
            SecurityScreen(navController = navController)
        }
        composable<Screen.Privacy> {
            PrivacyScreen(navController = navController)
        }
        composable<Screen.ProfileVisibility> {
            ProfileVisibilityScreen(navController = navController)
        }
        composable<Screen.HelpCenter> {
            HelpCenterScreen(navController = navController)
        }
        composable<Screen.UserGuide> {
            UserGuideScreen(navController = navController)
        }

        // ── Estadísticas y reputación ────────────────────────────────────────
        composable<Screen.Statistics> {
            StatisticsScreen(navController = navController)
        }
        composable<Screen.Reputation> {
            ReputationScreen(navController = navController)
        }

        // ── Moderación ──────────────────────────────────────────────────────
        composable<Screen.ModeratorPanel> {
            ModeratorPanelScreen(navController = navController)
        }
        composable<Screen.ModeratorDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ModeratorDetail>()
            ModeratorDetailScreen(postId = route.postId, navController = navController)
        }
    }
}
