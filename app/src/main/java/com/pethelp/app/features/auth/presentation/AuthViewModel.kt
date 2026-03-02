package com.pethelp.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Estado principal de la UI ────────────────────────────────────────────
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Estado específico para "enlace de reset enviado" ─────────────────────
    private val _resetEmailSent = MutableStateFlow(false)
    val resetEmailSent: StateFlow<Boolean> = _resetEmailSent.asStateFlow()

    init {
        // Verificar si ya hay una sesión activa al crear el ViewModel
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
            // Hay sesión: obtener el perfil completo
            authRepository.getCurrentUser().onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { /* No emitir Loading aquí para evitar flash */ }
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _uiState.value = AuthUiState.Authenticated(user)
                        }
                    }
                    is Resource.Error -> {
                        // Sesión inválida o error de Firestore: considerar como no autenticado
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
     * Emite: Loading → Authenticated | Error
     */
    fun login(email: String, password: String) {
        authRepository.login(email, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    resource.data?.let { user ->
                        _uiState.value = AuthUiState.Authenticated(user)
                    }
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(
                        resource.message ?: "Error al iniciar sesión."
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Registrar nueva cuenta ───────────────────────────────────────────────
    /**
     * Crea una cuenta nueva y persiste el perfil en Firestore.
     * Emite: Loading → Authenticated | Error
     */
    fun register(name: String, email: String, password: String) {
        authRepository.register(name, email, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    resource.data?.let { user ->
                        _uiState.value = AuthUiState.Authenticated(user)
                    }
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(
                        resource.message ?: "Error al crear la cuenta."
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Enviar correo de restablecimiento ────────────────────────────────────
    /**
     * Envía un enlace de reset de contraseña al correo indicado.
     * Emite: Loading → Success (actualiza resetEmailSent) | Error
     */
    fun sendPasswordReset(email: String) {
        _resetEmailSent.value = false
        authRepository.sendPasswordResetEmail(email).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
                is Resource.Success -> {
                    _uiState.value = AuthUiState.Idle
                    _resetEmailSent.value = true
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(
                        resource.message ?: "Error al enviar el enlace."
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Cerrar sesión ────────────────────────────────────────────────────────
    /**
     * Cierra la sesión actual de Firebase Auth.
     * Restaura el estado a Unauthenticated.
     */
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Unauthenticated
    }

    // ── Limpiar error ────────────────────────────────────────────────────────
    /**
     * Permite que la UI vuelva a Idle después de mostrar un error
     * (por ejemplo, al cambiar de campo de texto).
     */
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
