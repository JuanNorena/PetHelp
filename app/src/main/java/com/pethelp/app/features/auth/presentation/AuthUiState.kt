package com.pethelp.app.features.auth.presentation

import com.pethelp.app.core.domain.model.User
import com.pethelp.app.core.common.UiText

/**
 * Representa la jerarquía de estados posibles para la interfaz de usuario (UI) en el módulo de Autenticación.
 *
 * **Responsabilidad Principal:**
 * Actuar como una "fuente de verdad" única que describe en qué situación se encuentra el flujo de
 * autenticación en un momento dado. Esto permite que las pantallas reaccionen de forma reactiva y
 * predecible a los cambios de estado.
 *
 * **Propósito y Arquitectura:**
 * - **Seguridad de Tipos:** Al ser una `sealed class` (clase sellada), el compilador conoce todos los
 *   subtipos posibles. Esto obliga al desarrollador a manejar cada caso en una expresión `when`,
 *   evitando estados no contemplados.
 * - **Patrón State:** Sigue las recomendaciones de Jetpack Compose para el manejo de estados en la UI.
 *
 * **Ejemplo de Manejo en un Composable:**
 * ```kotlin
 * val uiState by viewModel.uiState.collectAsState()
 *
 * when (uiState) {
 *     is AuthUiState.Loading -> CircularProgressIndicator()
 *     is AuthUiState.Authenticated -> Text("Bienvenido, ${uiState.user.name}")
 *     is AuthUiState.Error -> ShowError(uiState.uiText.asString())
 *     else -> // Manejar otros estados
 * }
 * ```
 *
 * **Notas para Junior Developers:**
 * - Una `sealed class` es como un "enum vitaminado": permite que cada estado contenga datos
 *   diferentes (como un objeto `User` o un `UiText`).
 * - Usa `data object` para estados que no llevan datos adicionales y `data class` para los que sí.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see User Modelo de datos del usuario autenticado.
 * @see UiText Clase de utilidad para mensajes localizados.
 */
sealed class AuthUiState {

    /**
     * Estado inicial o de reposo.
     * Se utiliza cuando la aplicación arranca o cuando no hay ninguna operación activa.
     */
    data object Idle : AuthUiState()

    /**
     * Estado de carga activa.
     * Indica que una operación asíncrona (login, registro o recuperación) está en progreso.
     * La UI debería mostrar un indicador visual (spinner, barra de progreso).
     */
    data object Loading : AuthUiState()

    /**
     * Estado de éxito con sesión activa.
     * Se alcanza cuando el usuario se ha logueado o registrado correctamente.
     *
     * @property user El objeto [User] que contiene la información del perfil del usuario logueado.
     */
    data class Authenticated(val user: User) : AuthUiState()

    /**
     * Estado de sesión inexistente.
     * Indica que el usuario no ha iniciado sesión o que la sesión ha expirado.
     * La UI debería mostrar la pantalla de Login o Bienvenida.
     */
    data object Unauthenticated : AuthUiState()

    /**
     * Estado de error en la operación.
     * Se emite cuando ocurre un fallo de red, credenciales incorrectas o errores de servidor.
     *
     * @property uiText Un objeto [UiText] que contiene el mensaje de error listo para ser traducido y mostrado.
     */
    data class Error(val uiText: UiText) : AuthUiState()
}
