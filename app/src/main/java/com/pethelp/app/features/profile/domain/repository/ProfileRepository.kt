package com.pethelp.app.features.profile.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de operaciones de perfil de usuario.
 *
 * Todas las acciones exponen `Flow<Resource<T>>` para soportar estados
 * de carga, exito y error consumidos por la capa de presentacion.
 */
interface ProfileRepository {
    fun getCurrentUser(): Flow<Resource<User>>
    fun updateProfile(user: User): Flow<Resource<User>>
    fun updateProfilePhoto(imageUri: String): Flow<Resource<String>>
    fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>>
    fun deleteAccount(): Flow<Resource<Unit>>
}
