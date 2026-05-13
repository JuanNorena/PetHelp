/**
 * ViewModel principal de la feature de perfil.
 *
 * Orquesta la carga y edición de datos de usuario, preferencias de idioma,
 * modo oscuro y acciones sensibles como cambio de contraseña o eliminar cuenta.
 */
package com.pethelp.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.data.repository.UserPreferencesRepository
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.core.preferences.AppLanguageManager
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.notifications.FcmTokenSyncManager
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import com.pethelp.app.features.profile.domain.repository.ProfileRepository
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
 * ViewModel principal de la feature de perfil.
 *
 * Orquesta la carga/edicion de datos de usuario, preferencias de idioma,
 * modo oscuro y acciones sensibles como cambio de contrasena o eliminar cuenta.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val appLanguageManager: AppLanguageManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val fcmTokenSyncManager: FcmTokenSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    private val _preferredLanguage = MutableStateFlow(AppLanguageManager.DEFAULT_LANGUAGE)
    val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()
    val language: StateFlow<String> = preferredLanguage

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        loadUserProfile()
        observePreferredLanguage()
        observeDarkMode()
    }

    // ── Observadores de Preferencias ─────────────────────────────────────────
    /**
     * Escucha cambios en el idioma preferido almacenado en DataStore.
     *
     * Cada vez que el usuario cambia de idioma en configuración, este flujo
     * emite el nuevo tag y actualiza [_preferredLanguage] para que la UI
     * pueda mostrar el idioma actual sin recargar la pantalla.
     */
    private fun observePreferredLanguage() {
        appLanguageManager.preferredLanguage.onEach { languageTag ->
            _preferredLanguage.value = languageTag
        }.launchIn(viewModelScope)
    }

    /**
     * Escucha cambios en la preferencia de modo oscuro almacenada en DataStore.
     *
     * Mantiene sincronizado el estado [_isDarkMode] con la preferencia local
     * para que los composables del perfil reaccionen inmediatamente.
     */
    private fun observeDarkMode() {
        userPreferencesRepository.isDarkMode.onEach { darkModeEnabled ->
            _isDarkMode.value = darkModeEnabled
        }.launchIn(viewModelScope)
    }

    // ── Carga de Perfil ─────────────────────────────────────────────────────
    /**
     * Carga el perfil del usuario autenticado desde Firestore.
     *
     * **Flujo:**
     * 1. Emite [ProfileUiState.Loading] mientras llegan los datos.
     * 2. En éxito, emite [ProfileUiState.Success] con el objeto [User].
     * 3. En error, emite [ProfileUiState.Error] y un snackbar con el mensaje.
     *
     * Se usa un snapshot listener para mantener el perfil actualizado en tiempo real.
     */
    private fun loadUserProfile() {
        profileRepository.getCurrentUser().onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = ProfileUiState.Loading
                is Resource.Success -> {
                    resource.data?.let { user ->
                        _uiState.value = ProfileUiState.Success(user)
                    }
                }
                is Resource.Error -> {
                    val message = resource.message ?: UiText.DynamicString("Error loading profile")
                    _uiState.value = ProfileUiState.Error(message)
                    viewModelScope.launch {
                        _snackbarMessage.emit(message)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Actualización de Perfil ─────────────────────────────────────────────
    /**
     * Persiste los cambios del perfil del usuario en Firestore.
     *
     * @param user Objeto [User] con los campos actualizados.
     */
    fun updateProfile(user: User) {
        profileRepository.updateProfile(user).onEach { resource ->
             when (resource) {
                is Resource.Loading -> { /* Keep current state, maybe show saving indicator */ }
                is Resource.Success -> {
                    viewModelScope.launch {
                        _snackbarMessage.emit(UiText.StringResource(R.string.profile_updated_success))
                    }
                    // It will also trigger loadUserProfile because of the snapshot listener
                }
                is Resource.Error -> {
                    val errorText = resource.message ?: UiText.DynamicString("Error al actualizar perfil")
                    viewModelScope.launch {
                        _snackbarMessage.emit(errorText)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Actualiza las preferencias de notificación del usuario y sincroniza el token FCM.
     *
     * **Lógica de FCM:**
     * - Si se activa push, sincroniza el token actual para que Cloud Functions pueda enviar notificaciones.
     * - Si se desactiva, marca los tokens del usuario como inactivos en Firestore.
     *
     * @param pushEnabled true para recibir notificaciones push.
     * @param emailEnabled true para recibir alertas por correo.
     */
    fun updateNotificationPreferences(pushEnabled: Boolean, emailEnabled: Boolean) {
        profileRepository.updateNotificationPreferences(pushEnabled, emailEnabled).onEach { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        _uiState.value = currentState.copy(
                            user = currentState.user.copy(
                                pushNotificationsEnabled = pushEnabled,
                                emailAlertsEnabled = emailEnabled
                            )
                        )
                    }
                    // Gestionar token FCM segun la preferencia del usuario.
                    // Si se activa, se guarda/rehabilita el token actual; si se desactiva,
                    // se marcan como inactivos los tokens del usuario para que Cloud Functions no envíe push.
                    viewModelScope.launch {
                        try {
                            if (pushEnabled) {
                                fcmTokenSyncManager.syncPendingAndCurrentToken()
                                _snackbarMessage.emit(UiText.StringResource(R.string.settings_push_enabled))
                            } else {
                                fcmTokenSyncManager.disableTokensForCurrentUser()
                                _snackbarMessage.emit(UiText.StringResource(R.string.settings_push_disabled))
                            }
                        } catch (_: Exception) {
                            _snackbarMessage.emit(UiText.StringResource(R.string.settings_push_token_error))
                        }
                    }
                }
                is Resource.Error -> {
                    viewModelScope.launch {
                        _snackbarMessage.emit(
                            resource.message ?: UiText.StringResource(R.string.error_generic)
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Sube una nueva foto de perfil a Cloudinary y actualiza la URL en Firestore.
     *
     * **Pasos:**
     * 1. Valida que el URI no esté vacío.
     * 2. Marca el estado como subiendo (`isUploadingPhoto = true`).
     * 3. Delega la subida al [profileRepository].
     * 4. En éxito, actualiza la URL en el estado local y emite confirmación.
     * 5. En error, guarda el mensaje de error para mostrarlo en la UI.
     *
     * @param imageUri URI local de la imagen seleccionada por el usuario.
     */
    fun uploadProfilePhoto(imageUri: String) {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return

        if (imageUri.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit(UiText.StringResource(R.string.profile_invalid_image))
            }
            return
        }

        _uiState.value = currentState.copy(
            isUploadingPhoto = true,
            photoUploadError = null
        )

        profileRepository.updateProfilePhoto(imageUri).onEach { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    val latest = _uiState.value as? ProfileUiState.Success ?: currentState
                    val uploadedPhotoUrl = resource.data.orEmpty()
                    _uiState.value = latest.copy(
                        user = latest.user.copy(
                            photoUrl = uploadedPhotoUrl.ifBlank { latest.user.photoUrl }
                        ),
                        isUploadingPhoto = false,
                        photoUploadError = null
                    )
                    viewModelScope.launch {
                        _snackbarMessage.emit(UiText.StringResource(R.string.profile_photo_updated))
                    }
                }
                is Resource.Error -> {
                    val message = resource.message ?: UiText.StringResource(R.string.profile_photo_update_error)
                    val latest = _uiState.value as? ProfileUiState.Success ?: currentState
                    _uiState.value = latest.copy(
                        isUploadingPhoto = false,
                        photoUploadError = message
                    )
                    viewModelScope.launch {
                        _snackbarMessage.emit(message)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * Delega la operación a [AuthRepository]; la navegación posterior
     * a la pantalla de login la maneja la UI observando el estado de autenticación.
     */
    fun logout() {
        authRepository.logout()
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     *
     * **Validación:** Rechaza el envío si algún campo está en blanco.
     * **Nota:** Firebase requiere reautenticación reciente para operaciones sensibles;
     * el repository se encarga de eso internamente.
     *
     * @param currentPassword Contraseña actual del usuario.
     * @param newPassword Nueva contraseña deseada.
     */
    fun updatePassword(currentPassword: String, newPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit(UiText.StringResource(R.string.error_field_required))
            }
            return
        }

        profileRepository.changePassword(currentPassword, newPassword).onEach { resource ->
            when (resource) {
                is Resource.Loading -> { /* Show loading in UI if needed */ }
                is Resource.Success -> {
                    viewModelScope.launch {
                        _snackbarMessage.emit(UiText.StringResource(R.string.profile_password_updated))
                    }
                }
                is Resource.Error -> {
                    val errorText = resource.message ?: UiText.DynamicString("Error al actualizar contraseña")
                    viewModelScope.launch {
                        _snackbarMessage.emit(errorText)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Solicita la eliminación permanente de la cuenta del usuario.
     *
     * **Advertencia:** Esta acción es irreversible. Eliminara el documento
     * del usuario en Firestore y su cuenta de Firebase Authentication.
     *
     * @param onSuccess Callback opcional que la UI ejecuta tras éxito
     *                  (tipicamente navega a la pantalla de login).
     */
    fun deleteAccount(onSuccess: (() -> Unit)? = null) {
         profileRepository.deleteAccount().onEach { resource ->
            when(resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    viewModelScope.launch {
                        _snackbarMessage.emit(UiText.StringResource(R.string.profile_account_deleted))
                        onSuccess?.invoke()
                    }
                }
                is Resource.Error -> {
                     val errorText = resource.message ?: UiText.DynamicString("Error al eliminar cuenta")
                     viewModelScope.launch {
                         _snackbarMessage.emit(errorText)
                     }
                }
            }
         }.launchIn(viewModelScope)
    }

    /**
     * Cambia el idioma de la aplicación si el tag recibido está soportado.
     *
     * **Nota:** La UI se actualiza automaticamente porque Compose
     * recomposa con el nuevo contexto de localizacion.
     *
     * @param languageTag Tag BCP-47 del idioma (ej. "es", "en").
     */
    fun changeAppLanguage(languageTag: String) {
        viewModelScope.launch {
            if (!appLanguageManager.isSupportedLanguage(languageTag)) {
                _snackbarMessage.emit(UiText.StringResource(R.string.language_not_supported))
                return@launch
            }

            val normalizedLanguage = appLanguageManager.normalizeLanguage(languageTag)
            appLanguageManager.setPreferredLanguage(normalizedLanguage)

            // Note: The UI strings will update automatically because
            // the Activity's configuration changes or the composable recomposes
            // with the new locale context.
        }
    }

    /**
     * Alias de [changeAppLanguage] para compatibilidad con llamadas
     * desde la capa de presentacion.
     *
     * @param languageTag Tag BCP-47 del idioma.
     */
    fun setLanguage(languageTag: String) {
        changeAppLanguage(languageTag)
    }

    /**
     * Activa o desactiva el modo oscuro de la aplicación.
     *
     * Persiste la preferencia en DataStore para que sobreviva reinicios de la app.
     *
     * @param enabled true para modo oscuro, false para modo claro.
     */
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    /**
     * Retorna la lista de idiomas soportados por la aplicación, ordenados alfabeticamente.
     *
     * @return Lista de tags BCP-47 (ej. ["en", "es"]).
     */
    fun getSupportedLanguages(): List<String> {
        return AppLanguageManager.supportedLanguages.toList().sorted()
    }
}
