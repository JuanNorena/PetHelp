package com.pethelp.app.features.notifications.presentation

import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.PetNotification

/**
 * Estado inmutable de la pantalla de notificaciones.
 *
 * @param isLoading Indica si se están cargando las notificaciones por primera vez.
 * @param notifications Lista de notificaciones del usuario ordenadas por fecha descendente.
 * @param errorMessage Mensaje de error localizado si ocurre un fallo de carga.
 */
data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<PetNotification> = emptyList(),
    val errorMessage: UiText? = null
)
