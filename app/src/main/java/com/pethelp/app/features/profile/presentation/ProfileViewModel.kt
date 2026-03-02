package com.pethelp.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository // Needed for logout
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadUserProfile()
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

    fun logout() {
        authRepository.logout()
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