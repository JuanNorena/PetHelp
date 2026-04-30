package com.pethelp.app.features.notifications.presentation

import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.PetNotification

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<PetNotification> = emptyList(),
    val errorMessage: UiText? = null
)
