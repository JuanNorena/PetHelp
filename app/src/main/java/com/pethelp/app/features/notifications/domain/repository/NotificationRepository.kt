package com.pethelp.app.features.notifications.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.PetNotification
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de notificaciones.
 *
 * Define operaciones para observar notificaciones en tiempo real
 * y marcarlas como leídas (individual o masivamente).
 *
 * Todas las operaciones retornan `Flow<Resource<T>>` para permitir
 * que la UI reaccione a estados de carga, éxito y error.
 */
interface NotificationRepository {
    /** Observa las notificaciones del usuario autenticado en tiempo real. */
    fun observeNotifications(): Flow<Resource<List<PetNotification>>>

    /** Marca una notificación específica como leída. */
    fun markAsRead(notificationId: String): Flow<Resource<Unit>>

    /** Marca todas las notificaciones del usuario como leídas. */
    fun markAllAsRead(): Flow<Resource<Unit>>
}
