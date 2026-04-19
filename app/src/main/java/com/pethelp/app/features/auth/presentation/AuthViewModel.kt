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
 * ViewModel compartido para las pantallas de autenticación:
 * Splash, Login, Register y ForgotPassword.
 *
 * Patrón MVVM + Clean Architecture:
 *   UI (Composable) → ViewModel → Repository → Firebase
 *
 * Los estados se exponen como StateFlow para que la UI los
 * observe de forma reactiva con collectAsStateWithLifecycle().
 *
 * Los mensajes de error de una sola vez se emiten a través de
 * SharedFlow (snackbarMessage) para evitar que se re-muestren
 * al re-componer la pantalla (patrón de eventos one-shot).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Estado principal de la UI ────────────────────────────────────────────
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Evento one-shot para Snackbar ────────────────────────────────────────
    // SharedFlow con replay=0 garantiza que el mensaje se muestre solo una vez
    private val _snackbarMessage = MutableSharedFlow<UiText>()
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    // ── Estado específico para "enlace de reset enviado" ─────────────────────
    private val _resetEmailSent = MutableStateFlow(false)
    val resetEmailSent: StateFlow<Boolean> = _resetEmailSent.asStateFlow()

    init {
        checkAuthState()
    }

    // ── Verificar sesión activa ──────────────────────────────────────────────
    /**
     * Se ejecuta automáticamente al iniciar.
     * Si el usuario ya tiene sesión, emite Authenticated.
     * Si no, emite Unauthenticated para mostrar Splash/Login.
     */
    private fun checkAuthState() {
        if (authRepository.isUserAuthenticated()) {
            authRepository.getCurrentUser().onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { /* No emitir Loading aquí para evitar flash */ }
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _uiState.value = AuthUiState.Authenticated(user)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = AuthUiState.Unauthenticated
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    // ── Iniciar sesión ───────────────────────────────────────────────────────
    /**
     * Autentica al usuario con email y contraseña.
     *
     * Validaciones locales previas a la llamada al repositorio:
     *   1. Email no vacío
     *   2. Email con formato válido (Patterns.EMAIL_ADDRESS)
     *   3. Contraseña no vacía
     *
     * Flujo: Loading → Authenticated | Error (con Snackbar)
     */
    fun login(email: String, password: String) {
        // — Validaciones locales (doble capa: UI + ViewModel) —
        val trimmedEmail = email.trim()
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

        authRepository.login(trimmedEmail, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    resource.data?.let { user ->
                        _uiState.value = AuthUiState.Authenticated(user)
                    }
                }
                is Resource.Error -> {
                    val errorUiText = resource.uiText ?: UiText.StringResource(R.string.error_login_failed)
                    _uiState.value = AuthUiState.Error(errorUiText)
                    _snackbarMessage.emit(errorUiText)
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Registrar nueva cuenta ───────────────────────────────────────────────
    /**
     * Crea una cuenta nueva y persiste el perfil en Firestore.
     *
     * Validaciones locales previas:
     *   1. Nombre no vacío y ≤ 100 caracteres
     *   2. Email no vacío, formato válido, ≤ 254 caracteres
     *   3. Contraseña ≥ 6 caracteres
     *
     * Flujo: Loading → Authenticated | Error (con Snackbar)
     */
    fun register(name: String, email: String, password: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

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

        authRepository.register(trimmedName, trimmedEmail, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    resource.data?.let { user ->
                        _uiState.value = AuthUiState.Authenticated(user)
                    }
                }
                is Resource.Error -> {
                    val errorUiText = resource.uiText ?: UiText.StringResource(R.string.error_register_failed)
                    _uiState.value = AuthUiState.Error(errorUiText)
                    _snackbarMessage.emit(errorUiText)
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Enviar correo de restablecimiento ────────────────────────────────────
    /**
     * Envía un enlace de reset de contraseña al correo indicado.
     *
     * Validaciones locales:
     *   1. Email no vacío
     *   2. Email con formato válido
     *
     * Flujo: Loading → Idle + resetEmailSent=true | Error (con Snackbar)
     */
    fun sendPasswordReset(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            emitError(UiText.StringResource(R.string.error_email_required))
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emitError(UiText.StringResource(R.string.error_email_invalid))
            return
        }

        _resetEmailSent.value = false
        authRepository.sendPasswordResetEmail(trimmedEmail).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    _uiState.value = AuthUiState.Idle
                    _resetEmailSent.value = true
                }
                is Resource.Error -> {
                    val errorUiText = resource.uiText ?: UiText.StringResource(R.string.error_reset_failed)
                    _uiState.value = AuthUiState.Error(errorUiText)
                    _snackbarMessage.emit(errorUiText)
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Cerrar sesión ────────────────────────────────────────────────────────
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Unauthenticated
    }

    // ── Limpiar error ────────────────────────────────────────────────────────
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    // ── Helper: emitir error one-shot ────────────────────────────────────────
    private fun emitError(uiText: UiText) {
        _uiState.value = AuthUiState.Error(uiText)
        viewModelScope.launch {
            _snackbarMessage.emit(uiText)
        }
    }
}
