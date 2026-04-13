package com.pethelp.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.data.repository.UserPreferencesRepository
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import com.pethelp.app.features.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository, // Needed for logout
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("es")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        loadUserProfile()
        observeDarkMode()
        observeLanguage()
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            userPreferencesRepository.isDarkMode.collectLatest { enabled ->
                _isDarkMode.value = enabled
            }
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            userPreferencesRepository.language.collectLatest { lang ->
                _language.value = lang
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(languageCode)
        }
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
                    _uiState.value = ProfileUiState.Error(resource.message ?: "Error loading profile")
                    _snackbarMessage.emit(resource.message ?: "Error loading profile")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updateProfile(user: User) {
        profileRepository.updateProfile(user).onEach { resource ->
             when (resource) {
                is Resource.Loading -> { /* Keep current state, maybe show saving indicator */ }
                is Resource.Success -> {
                    _snackbarMessage.emit("Perfil actualizado correctamente")
                    // It will also trigger loadUserProfile because of the snapshot listener
                }
                is Resource.Error -> {
                    _snackbarMessage.emit(resource.message ?: "Error al actualizar perfil")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun uploadProfilePhoto(imageUri: String) {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return

        if (imageUri.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Selecciona una imagen válida.")
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
                        _snackbarMessage.emit("Foto de perfil actualizada correctamente.")
                    }
                }
                is Resource.Error -> {
                    val message = resource.message ?: "Error al subir la foto de perfil."
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

    fun updatePassword(newPassword: String) {
        authRepository.updatePassword(newPassword).onEach { resource ->
            when (resource) {
                is Resource.Loading -> { /* Show loading in UI if needed */ }
                is Resource.Success -> {
                    _snackbarMessage.emit("Contraseña actualizada correctamente")
                }
                is Resource.Error -> {
                    _snackbarMessage.emit(resource.message ?: "Error al actualizar contraseña")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun deleteAccount() {
         profileRepository.deleteAccount().onEach { resource ->
            when(resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _snackbarMessage.emit("Cuenta eliminada correctamente")
                }
                is Resource.Error -> {
                     _snackbarMessage.emit(resource.message ?: "Error al eliminar cuenta")
                }
            }
         }.launchIn(viewModelScope)
    }
}