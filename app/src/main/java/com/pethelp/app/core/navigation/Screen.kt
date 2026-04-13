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
    @Serializable data class LocationSelection(
        val title: String,
        val description: String,
        val category: String,
        val animalType: String,
        val size: String,
        val imageUris: List<String>
    ) : Screen()

    @Serializable data class PostDetails(
        val title: String,
        val description: String,
        val category: String,
        val animalType: String,
        val size: String,
        val imageUris: List<String>,
        val street: String,
        val neighborhood: String,
        val city: String
    ) : Screen()

    @Serializable data object MyPosts : Screen()
    
    @Serializable 
    data class EditPost(val postId: String) : Screen()
    
    @Serializable data object Notifications : Screen()
    @Serializable data object Map : Screen()
    @Serializable data object Chat : Screen()
    @Serializable data object Profile : Screen()
    @Serializable data object EditProfile : Screen()
    @Serializable data object Settings : Screen()
    @Serializable data object Security : Screen()
    @Serializable data object Privacy : Screen()
    @Serializable data object ProfileVisibility : Screen()
    @Serializable data object HelpCenter : Screen()
    @Serializable data object UserGuide : Screen()
    @Serializable data object Statistics : Screen()
    @Serializable data object Reputation : Screen()

    // ── Moderación ────────────────────────────────────────────────────────────
    @Serializable data object ModeratorPanel : Screen()
    
    @Serializable 
    data class ModeratorDetail(val postId: String) : Screen()
    @Serializable data class PostReview(
        val title: String,
        val description: String,
        val category: String,
        val animalType: String,
        val age: String,
        val gender: String,
        val size: String,
        val vaccinated: Boolean,
        val dewormed: Boolean,
        val sterilized: Boolean,
        val behavior: List<String>,
        val imageUris: List<String>,
        val street: String,
        val neighborhood: String,
        val city: String
    ) : Screen()
}
