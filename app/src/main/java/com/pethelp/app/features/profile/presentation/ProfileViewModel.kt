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

    private fun observePreferredLanguage() {
        appLanguageManager.preferredLanguage.onEach { languageTag ->
            _preferredLanguage.value = languageTag
        }.launchIn(viewModelScope)
    }

    private fun observeDarkMode() {
        userPreferencesRepository.isDarkMode.onEach { darkModeEnabled ->
            _isDarkMode.value = darkModeEnabled
        }.launchIn(viewModelScope)
    }

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

    fun logout() {
        authRepository.logout()
    }

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

    fun setLanguage(languageTag: String) {
        changeAppLanguage(languageTag)
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    fun getSupportedLanguages(): List<String> {
        return AppLanguageManager.supportedLanguages.toList().sorted()
    }
}
