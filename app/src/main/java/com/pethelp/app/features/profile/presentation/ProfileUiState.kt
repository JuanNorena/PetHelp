package com.pethelp.app.features.profile.presentation

import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.User

/**
 * Estados de interfaz para la sección de perfil.
 *
 * Representa los estados posibles de carga, éxito y error
 * al obtener o actualizar los datos del usuario autenticado.
 *
 * - [Loading]: Estado inicial mientras se carga el perfil desde Firestore.
 * - [Success]: Perfil cargado correctamente, con indicador opcional de subida de foto.
 * - [Error]: Fallo al cargar o guardar el perfil, con mensaje localizado.
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
