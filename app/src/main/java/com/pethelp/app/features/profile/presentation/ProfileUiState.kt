package com.pethelp.app.features.profile.presentation

import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.User

/**
 * Estados de interfaz para la seccion de perfil.
 */
sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(
        val user: User,
        val isUploadingPhoto: Boolean = false,
        val photoUploadError: UiText? = null
    ) : ProfileUiState()
    data class Error(val uiText: UiText) : ProfileUiState()
}
