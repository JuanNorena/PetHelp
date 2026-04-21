package com.pethelp.app.features.auth.presentation

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import com.pethelp.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel compartido que centraliza la lógica de negocio para todo el flujo de autenticación.
 *
 * **Responsabilidad Principal:**
 * Actuar como el "cerebro" reactivo entre las pantallas de la interfaz de usuario (Splash, Login, Register,
 * ForgotPassword) y la fuente de datos ([AuthRepository]). Gestiona el ciclo de vida del estado de la sesión,
 * valida las entradas del usuario antes de enviarlas al servidor y traduce las respuestas crudas de Firebase
 * en estados visuales claros ([AuthUiState]).
 *
 * **Arquitectura (MVVM + Clean Architecture):**
 * - **UI (Composables):** Observan de forma pasiva el flujo de datos y reaccionan a los cambios.
 * - **ViewModel:** Sobrevive a rotaciones de pantalla y otros cambios de configuración, manteniendo la integridad del flujo.
 * - **Repository:** Se encarga de la comunicación externa (Firebase Auth y Firestore).
 *
 * **Lógica de Flujos de Datos Asíncronos (Kotlin Flow):**
 * 1. **StateFlow ([uiState]):** Un "estado caliente" que siempre tiene un valor. Ideal para representar el estado actual de la pantalla (Cargando, Autenticado, etc.).
 * 2. **SharedFlow ([snackbarMessage]):** Un "flujo de eventos" que no retiene el último valor. Perfecto para notificaciones que deben mostrarse una sola vez (como errores temporales).
 * 3. **StateFlow ([resetEmailSent]):** Un estado booleano específico para controlar la visibilidad de diálogos o pantallas de éxito tras recuperar contraseña.
 *
 * **Conceptos Clave para Desarrolladores Junior:**
 * - **Encapsulamiento:** Usamos `MutableStateFlow` privado (`_uiState`) y `StateFlow` público (`uiState`). Esto impide que la UI modifique el estado directamente, obligándola a llamar a funciones del ViewModel (Principio de Única Fuente de Verdad).
 * - **Gestión de Corrutinas:** Todo el trabajo asíncrono ocurre dentro del [viewModelScope], lo que garantiza que si el usuario sale de la pantalla de autenticación, cualquier petición en curso se cancele automáticamente, evitando fugas de memoria o cuelgues.
 * - **Validación de Capas:** Validar el formato del email localmente antes de llamar a la red ahorra ancho de banda y mejora la experiencia del usuario (feedback instantáneo).
 *
 * @property authRepository Instancia inyectada por Hilt para gestionar la persistencia y autenticación de usuarios.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see AuthUiState Para entender la máquina de estados de la UI.
 * @see AuthRepository Para ver la implementación de los servicios de datos.
 * @see Resource Para el manejo estandarizado de respuestas asíncronas.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Estado principal de la UI (State Machine) ────────────────────────────
    /**
     * Fuente de verdad mutable interna. Solo accesible dentro de esta clase.
     * Se inicializa en [AuthUiState.Idle] (Inactivo) esperando acciones del usuario.
     */
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    /**
     * Flujo de estado de solo lectura expuesto a la capa de presentación.
     * Al usar [asStateFlow], nos aseguramos de que nadie fuera del ViewModel pueda emitir nuevos estados.
     */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Eventos de un solo uso (One-shot events) ─────────────────────────────
    /**
     * Canal interno para mensajes temporales (Snackbars).
     * `replay = 0` asegura que los mensajes antiguos no se vuelvan a mostrar al rotar la pantalla.
     */
    private val _snackbarMessage = MutableSharedFlow<UiText>()

    /**
     * Flujo público para que la UI "escuche" notificaciones.
     * Se observa comúnmente mediante `LaunchedEffect` en Compose.
     */
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    // ── Estado de recuperación de contraseña ─────────────────────────────────
    /** Estado interno para rastrear si el correo de reset fue aceptado por Firebase. */
    private val _resetEmailSent = MutableStateFlow(false)

    /** Flujo público para disparar navegaciones o diálogos de éxito en la UI. */
    val resetEmailSent: StateFlow<Boolean> = _resetEmailSent.asStateFlow()

    /**
     * Bloque de inicialización. Se ejecuta apenas se crea el ViewModel.
     * Su misión principal es recuperar la sesión del usuario si existe una previa persistida.
     *
     * **PASO 1:** Invocar [checkAuthState] para sincronizar el estado local con Firebase.
     */
    init {
        checkAuthState()
    }

    // ── Gestión de Sesión ────────────────────────────────────────────────────
    /**
     * Verifica de forma asíncrona si el usuario ya inició sesión previamente.
     *
     * **Lógica Paso a Paso:**
     * 1. **Consulta Rápida:** Pregunta al SDK de Firebase si hay un `UID` activo en caché.
     * 2. **Sincronización:** Si hay sesión, solicita al repositorio el perfil completo desde Firestore.
     * 3. **Actualización Reactiva:** Escucha el flujo de datos ([Resource]) y actualiza [_uiState] según el resultado.
     * 4. **Manejo de Errores:** Si no hay sesión o hay un error de red, marca al usuario como [AuthUiState.Unauthenticated].
     *
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     * @see AuthRepository.isUserAuthenticated
     * @see AuthRepository.getCurrentUser
     */
    private fun checkAuthState() {
        // PASO 1: Verificación de token local en Firebase.
        if (authRepository.isUserAuthenticated()) {
            // PASO 2: Recuperación del perfil extendido del usuario.
            authRepository.getCurrentUser().onEach { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Opcional: Podríamos mostrar un splash screen extendido aquí.
                    }
                    is Resource.Success -> {
                        // PASO 3: Sesión válida y perfil recuperado con éxito.
                        resource.data?.let { user ->
                            _uiState.value = AuthUiState.Authenticated(user)
                        }
                    }
                    is Resource.Error -> {
                        // PASO 4: Fallo al recuperar perfil (posible error de red o permisos).
                        _uiState.value = AuthUiState.Unauthenticated
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            // PASO 5: No existe sesión activa en el dispositivo.
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    // ── Lógica de Login ──────────────────────────────────────────────────────
    /**
     * Orquesta el proceso de inicio de sesión de un usuario.
     *
     * **Validaciones Locales (Frontend):**
     * - No permite campos vacíos para evitar peticiones inútiles a la nube.
     * - Valida la estructura sintáctica del correo usando patrones de Android.
     *
     * **Flujo de Ejecución:**
     * 1. Limpia espacios en blanco del email.
     * 2. Ejecuta validaciones de formato.
     * 3. Si todo es correcto, delega la autenticación al [AuthRepository].
     *
     * @param email Dirección de correo electrónico proporcionada.
     * @param password Contraseña secreta.
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     * @see Patterns.EMAIL_ADDRESS Para la expresión regular de validación de correo.
     */
    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()

        // PASO 1: Validaciones de seguridad básicas.
        if (trimmedEmail.isBlank()) {
            emitError(UiText.StringResource(R.string.error_email_required))
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emitError(UiText.StringResource(R.string.error_email_invalid))
            return
        }
        if (password.isBlank()) {
            emitError(UiText.StringResource(R.string.error_password_required))
            return
        }

        // PASO 2: Ejecución de la petición asíncrona al repositorio.
        authRepository.login(trimmedEmail, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Bloqueamos la UI mostrando un indicador de carga.
                    _uiState.value = AuthUiState.Loading
                }
                is Resource.Success -> {
                    // Login exitoso: Guardamos el usuario en el estado global.
                    resource.data?.let { user ->
                        _uiState.value = AuthUiState.Authenticated(user)
                    }
                }
                is Resource.Error -> {
                    // Fallo en la autenticación (ej. credenciales incorrectas).
                    val errorUiText = resource.uiText ?: UiText.StringResource(R.string.error_login_failed)
                    _uiState.value = AuthUiState.Error(errorUiText)
                    _snackbarMessage.emit(errorUiText)
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Lógica de Registro ───────────────────────────────────────────────────
    /**
     * Gestiona la creación de una nueva cuenta de usuario en el sistema.
     *
     * **Reglas de Negocio:**
     * - El nombre debe ser descriptivo y no exceder los 100 caracteres.
     * - La contraseña debe cumplir con el mínimo de seguridad de Firebase (6 caracteres).
     *
     * @param name Nombre completo o alias del nuevo usuario.
     * @param email Correo electrónico institucional o personal.
     * @param password Contraseña de acceso (mínimo 6 caracteres).
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     * @see AuthRepository.register Para ver cómo se crea el usuario y el perfil en Firestore.
     */
    fun register(name: String, email: String, password: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        // PASO 1: Validaciones de integridad de datos.
        if (trimmedName.isBlank()) {
            emitError(UiText.StringResource(R.string.error_name_required))
            return
        }
        if (trimmedName.length > 100) {
            emitError(UiText.StringResource(R.string.error_name_too_long, 100))
            return
        }
        if (trimmedEmail.isBlank()) {
            emitError(UiText.StringResource(R.string.error_email_required))
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emitError(UiText.StringResource(R.string.error_email_invalid))
            return
        }
        if (password.length < 6) {
            emitError(UiText.StringResource(R.string.error_password_too_short, 6))
            return
        }

        // PASO 2: Llamada al repositorio para registro dual (Auth + Firestore).
        authRepository.register(trimmedName, trimmedEmail, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    // Registro exitoso: El usuario ya está logueado automáticamente.
                    resource.data?.let { user ->
                        _uiState.value = AuthUiState.Authenticated(user)
                    }
                }
                is Resource.Error -> {
                    // Manejo de errores (ej. el correo ya está en uso).
                    val errorUiText = resource.uiText ?: UiText.StringResource(R.string.error_register_failed)
                    _uiState.value = AuthUiState.Error(errorUiText)
                    _snackbarMessage.emit(errorUiText)
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Recuperación de Contraseña ───────────────────────────────────────────
    /**
     * Inicia el flujo de recuperación de contraseña enviando un enlace al correo.
     *
     * **Nota para Junior Developers:**
     * Es importante resetear el estado de [_resetEmailSent] antes de cada intento para
     * evitar falsos positivos si el usuario intenta enviar el correo varias veces.
     *
     * @param email Correo electrónico de la cuenta a recuperar.
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     * @see AuthRepository.sendPasswordResetEmail
     */
    fun sendPasswordReset(email: String) {
        val trimmedEmail = email.trim()

        // PASO 1: Validación de formato.
        if (trimmedEmail.isBlank()) {
            emitError(UiText.StringResource(R.string.error_email_required))
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emitError(UiText.StringResource(R.string.error_email_invalid))
            return
        }

        // PASO 2: Reinicio de banderas de éxito previas.
        _resetEmailSent.value = false

        // PASO 3: Petición asíncrona a Firebase.
        authRepository.sendPasswordResetEmail(trimmedEmail).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    // Éxito: Volvemos a estado Idle y marcamos el envío como completado.
                    _uiState.value = AuthUiState.Idle
                    _resetEmailSent.value = true
                }
                is Resource.Error -> {
                    // Error: Notificamos al usuario por qué falló el envío.
                    val errorUiText = resource.uiText ?: UiText.StringResource(R.string.error_reset_failed)
                    _uiState.value = AuthUiState.Error(errorUiText)
                    _snackbarMessage.emit(errorUiText)
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Cierre de Sesión ─────────────────────────────────────────────────────
    /**
     * Cierra la sesión activa del usuario y limpia los datos en memoria.
     *
     * **Impacto en la UI:**
     * Al cambiar el estado a [AuthUiState.Unauthenticated], los navegadores de la app
     * deberían redirigir automáticamente al usuario a la pantalla de Login.
     *
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     */
    fun logout() {
        // PASO 1: Notificar al repositorio para que elimine tokens locales.
        authRepository.logout()
        // PASO 2: Actualizar el estado para disparar la navegación de salida.
        _uiState.value = AuthUiState.Unauthenticated
    }

    // ── Utilidades de Estado ─────────────────────────────────────────────────
    /**
     * Limpia cualquier mensaje de error presente en el estado de la UI.
     * Se debe llamar cuando el usuario interactúa con un botón de "Cerrar" o "Reintentar".
     *
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     */
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    /**
     * Helper privado para emitir errores de forma consistente en dos canales:
     * 1. **Estado Persistente:** Para que la UI pueda mostrar alertas o bordes rojos.
     * 2. **Evento One-shot:** Para mostrar un mensaje emergente (Snackbar).
     *
     * @param uiText El mensaje de error que será mostrado al usuario.
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     */
    private fun emitError(uiText: UiText) {
        // PASO 1: Actualizar el estado de error para la UI.
        _uiState.value = AuthUiState.Error(uiText)

        // PASO 2: Lanzar el evento de Snackbar en una corrutina (SharedFlow requiere suspensión).
        viewModelScope.launch {
            _snackbarMessage.emit(uiText)
        }
    }
}
