package com.pethelp.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * Define todas las rutas de navegación de la aplicación usando Kotlin Serialization.
 *
 * Patrón recomendado: objetos sellados o clases de datos serializables para type-safety
 * con Navigation Compose 2.8+.
 */
sealed class Screen {

    // ── Autenticación ─────────────────────────────────────────────────────────
    @Serializable data object Splash : Screen()
    @Serializable data object Login : Screen()
    @Serializable data object Register : Screen()
    @Serializable data object ForgotPassword : Screen()

    // ── Aplicación principal (usuario) ────────────────────────────────────────
    @Serializable data object Feed : Screen()
    
    @Serializable data class PostDetail(val postId: String) : Screen()
    
    @Serializable data object CreatePost : Screen()

    @Serializable data object MyPosts : Screen()
    
    @Serializable 
    data class EditPost(val postId: String) : Screen()
    
    @Serializable data object Notifications : Screen()
    @Serializable data object Map : Screen()
    @Serializable data object Chat : Screen()
    @Serializable data object Profile : Screen()
    @Serializable data object EditProfile : Screen()
    @Serializable data object Statistics : Screen()
    @Serializable data object Reputation : Screen()

    // ── Moderación ────────────────────────────────────────────────────────────
    @Serializable data object ModeratorPanel : Screen()
    
    @Serializable 
    data class ModeratorDetail(val postId: String) : Screen()
}
