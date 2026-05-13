/**
 * ViewModel de la pantalla de notificaciones.
 *
 * Expone el estado de carga, lista de notificaciones y errores.
 * Coordina con [NotificationRepository] para observar notificaciones
 * en tiempo real y marcarlas como leídas.
 */
package com.pethelp.app.features.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.features.notifications.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * ViewModel para la pantalla de notificaciones.
 *
 * **Responsabilidad Principal:**
 * Observar las notificaciones del usuario autenticado desde Firestore mediante
 * [NotificationRepository] y exponer un estado reactivo ([NotificationsUiState])
 * que la pantalla de notificaciones puede observar para mostrar la lista,
 * indicadores de carga y mensajes de error.
 *
 * **Flujo de Datos:**
 * 1. Al crearse el ViewModel, inicia automáticamente la observación de notificaciones.
 * 2. El repositorio emite [Resource] con estados Loading, Success o Error.
 * 3. Cada emisión se mapea a un nuevo [NotificationsUiState] que la UI consume.
 *
 * @param notificationRepository Repositorio que encapsula el acceso a Firestore
 *                                 para la colección de notificaciones.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    /** Estado mutable interno; solo el ViewModel puede modificarlo. */
    private val _uiState = MutableStateFlow(NotificationsUiState())

    /** Estado público de solo lectura que la UI observa. */
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        // Inicia la observación automática al crear el ViewModel.
        observeNotifications()
    }

    /**
     * Suscripción reactiva al flujo de notificaciones del repositorio.
     *
     * Actualiza el estado de la UI según el tipo de recurso recibido:
     * - [Resource.Loading]: Activa el indicador de carga.
     * - [Resource.Success]: Reemplaza la lista de notificaciones y desactiva carga.
     * - [Resource.Error]: Desactiva carga y guarda el mensaje de error.
     */
    private fun observeNotifications() {
        notificationRepository.observeNotifications().onEach { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }
                is Resource.Success -> {
                    _uiState.value = NotificationsUiState(
                        isLoading = false,
                        notifications = resource.data ?: emptyList(),
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = resource.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Marca una notificación individual como leída.
     *
     * @param notificationId Identificador de la notificación a marcar.
     */
    fun markAsRead(notificationId: String) {
        notificationRepository.markAsRead(notificationId).launchIn(viewModelScope)
    }

    /**
     * Marca todas las notificaciones del usuario como leídas.
     *
     * Útil para el botón "Marcar todas como leídas" en la UI.
     */
    fun markAllAsRead() {
        notificationRepository.markAllAsRead().launchIn(viewModelScope)
    }
}
