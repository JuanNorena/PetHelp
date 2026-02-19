package com.pethelp.app.core.navigation

/**
 * Define todas las rutas de navegación de la aplicación.
 *
 * Patrón recomendado: objetos sellados para type-safety.
 * Los parámetros de ruta se declaran con llaves: {param}.
 */
sealed class Screen(val route: String) {

    // ── Autenticación ─────────────────────────────────────────────────────────
    data object Splash       : Screen("splash")
    data object Login        : Screen("login")
    data object Register     : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    // ── Aplicación principal (usuario) ────────────────────────────────────────
    data object Feed         : Screen("feed")
    data object PostDetail   : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    data object CreatePost   : Screen("create_post")
    data object EditPost     : Screen("edit_post/{postId}") {
        fun createRoute(postId: String) = "edit_post/$postId"
    }
    data object Notifications : Screen("notifications")
    data object Profile      : Screen("profile")
    data object EditProfile  : Screen("edit_profile")
    data object Statistics   : Screen("statistics")
    data object Reputation   : Screen("reputation")

    // ── Moderación ────────────────────────────────────────────────────────────
    data object ModeratorPanel   : Screen("moderator_panel")
    data object ModeratorDetail  : Screen("moderator_detail/{postId}") {
        fun createRoute(postId: String) = "moderator_detail/$postId"
    }
}
