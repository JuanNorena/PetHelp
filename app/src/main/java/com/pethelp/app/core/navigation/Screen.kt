package com.pethelp.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * Define todas las rutas de navegación de la aplicación usando Kotlin Serialization.
 *
 * Cada objeto o clase de datos representa un destino en el grafo de navegación.
 * Los destinos que necesitan parámetros (como `PostDetail`) se modelan como
 * clases serializables para que Jetpack Navigation pueda pasar esos datos de
 * forma segura.
 *
 * Este enfoque mejora la legibilidad y el mantenimiento del código, ya que las
 * rutas están tipadas en lugar de usar cadenas arbitrarias.
 */
sealed class Screen {

    // ── Autenticación ─────────────────────────────────────────────────────────
    @Serializable data object Splash : Screen()
    @Serializable data object Login : Screen()
    @Serializable data object Register : Screen()
    @Serializable data object ForgotPassword : Screen()

    // ── Aplicación principal (usuario) ────────────────────────────────────────
    @Serializable data object Feed : Screen()

    /**
     * Pantalla de detalle de publicación.
     *
     * @param postId Identificador de la publicación que se debe mostrar.
     */
    @Serializable data class PostDetail(val postId: String) : Screen()

    /**
     * Pantalla para solicitar adopción.
     *
     * @param postId Identificador de la publicación relacionada.
     * @param petName Nombre de la mascota a la que se quiere adoptar.
     */
    @Serializable data class AdoptionRequest(val postId: String, val petName: String) : Screen()

    @Serializable data object AdoptionSuccess : Screen()
    @Serializable data object CreatePost : Screen()

    /**
     * Pantalla para seleccionar la ubicación de una publicación en creación.
     *
     * Todos los datos del post se envían como parámetros para que la pantalla
     * de ubicación los mantenga en el flujo de creación.
     */
    @Serializable data class LocationSelection(
        val title: String,
        val description: String,
        val category: String,
        val animalType: String,
        val breed: String,
        val size: String,
        val imageUris: List<String>,
        val street: String = "",
        val neighborhood: String = "",
        val city: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val locationName: String = ""
    ) : Screen()

    /**
     * Pantalla para mostrar el resumen de los datos ingresados antes de crear
     * la publicación. Se usa para validar y confirmar la información.
     */
    @Serializable data class PostDetails(
        val title: String,
        val description: String,
        val category: String,
        val animalType: String,
        val breed: String,
        val size: String,
        val imageUris: List<String>,
        val street: String,
        val neighborhood: String,
        val city: String,
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    ) : Screen()

    @Serializable data object MyPosts : Screen()
    @Serializable data class EditPost(val postId: String) : Screen()
    @Serializable data object Notifications : Screen()
    @Serializable data object AdoptionRequests : Screen()
    @Serializable data object Map : Screen()
    @Serializable data object Chat : Screen()
    @Serializable data class ChatThread(val threadId: String) : Screen()
    @Serializable data object Profile : Screen()
    @Serializable data object Favorites : Screen()
    @Serializable data object EditProfile : Screen()
    @Serializable data object Settings : Screen()
    @Serializable data object Language : Screen()
    @Serializable data object Security : Screen()
    @Serializable data object Privacy : Screen()
    @Serializable data object ProfileVisibility : Screen()
    @Serializable data object HelpCenter : Screen()
    @Serializable data object UserGuide : Screen()
    @Serializable data object Statistics : Screen()
    @Serializable data object Reputation : Screen()

    // ── Moderación ───────────────────────────────────────────────────────────
    @Serializable data object ModeratorPanel : Screen()

    /**
     * Pantalla de detalle para moderadores con el post específico a revisar.
     */
    @Serializable data class ModeratorDetail(val postId: String) : Screen()

    /**
     * Pantalla para revisar la publicación antes de aprobarla o rechazarla.
     *
     * Este destino transporta todos los campos necesarios para que el moderador
     * vea la información completa sin tener que solicitarla de nuevo.
     */
    @Serializable data class PostReview(
        val title: String,
        val description: String,
        val category: String,
        val animalType: String,
        val breed: String,
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
        val city: String,
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    ) : Screen()
}
