package com.pethelp.app.features.profile.presentation

import com.pethelp.app.core.domain.model.User

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
