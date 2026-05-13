/**
 * Contrato del repositorio de perfil de usuario.
 *
 * Define operaciones de lectura, actualización, cambio de foto,
 * preferencias de notificación y operaciones sensibles de cuenta,
 * todas retornando `Flow<Resource<T>>`.
 */
package com.pethelp.app.features.profile.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz concreta del repositorio de perfil.
 *
 * Todas las acciones exponen `Flow<Resource<T>>` para soportar estados
 * de carga, exito y error consumidos por la capa de presentacion.
 */
interface ProfileRepository {
    fun getCurrentUser(): Flow<Resource<User>>
    fun updateProfile(user: User): Flow<Resource<User>>
    fun updateNotificationPreferences(pushEnabled: Boolean, emailEnabled: Boolean): Flow<Resource<Unit>>
    fun updateProfilePhoto(imageUri: String): Flow<Resource<String>>
    fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>>
    fun deleteAccount(): Flow<Resource<Unit>>
}
