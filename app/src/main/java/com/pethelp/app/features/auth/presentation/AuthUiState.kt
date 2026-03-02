package com.pethelp.app.features.auth.presentation

import com.pethelp.app.core.domain.model.User

/**
 * Representa los posibles estados de la UI de autenticación.
 *
 * Se usa como state en el AuthViewModel y se observa desde
 * las pantallas Splash, Login, Register y ForgotPassword.
 */
sealed class AuthUiState {

    /** Estado inicial: aún no se ha determinado el estado de sesión. */
    data object Idle : AuthUiState()

    /** Operación en curso (login, registro, envío de correo). */
    data object Loading : AuthUiState()

    /** Usuario autenticado exitosamente. */
    data class Authenticated(val user: User) : AuthUiState()

    /** No hay sesión activa (usuario no autenticado). */
    data object Unauthenticated : AuthUiState()

    /** Error con mensaje descriptivo para mostrar en la UI. */
    data class Error(val message: String) : AuthUiState()
}
