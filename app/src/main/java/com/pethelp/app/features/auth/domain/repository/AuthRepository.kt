/**
 * Contrato del repositorio de autenticación.
 *
 * Define operaciones de login, registro, recuperación de contraseña,
 * cierre de sesión y observación del estado de autenticación del usuario.
 *
 * Todas las operaciones exponen `Flow<Resource<T>>` para permitir
 * que la UI reaccione a estados de carga, éxito y error.
 */
package com.pethelp.app.features.auth.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz concreta del repositorio de autenticación.
 *
 * Capa de dominio: define las operaciones disponibles sin depender
 * de la implementación concreta (Firebase, Room, etc.).
 *
 * Cada método retorna un Flow<Resource<T>> para permitir que la UI
 * observe estados Loading → Success/Error de forma reactiva.
 */
interface AuthRepository {

    /**
     * Inicia sesión con correo electrónico y contraseña.
     * @return Flow que emite Loading → Success(User) o Error.
     */
    fun login(email: String, password: String): Flow<Resource<User>>

    /**
     * Registra un nuevo usuario con nombre, correo y contraseña.
     * Además de crear la cuenta en Firebase Auth, persiste el perfil
     * del usuario en Firestore (colección "users").
     * @return Flow que emite Loading → Success(User) o Error.
     */
    fun register(name: String, email: String, password: String): Flow<Resource<User>>

    /**
     * Envía un correo de restablecimiento de contraseña.
     * @return Flow que emite Loading → Success(Unit) o Error.
     */
    fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>>

    /**
     * Verifica si hay una sesión activa.
     * @return true si el usuario ya está autenticado.
     */
    fun isUserAuthenticated(): Boolean

    /**
     * Obtiene el usuario actualmente autenticado desde Firestore.
     * @return Flow que emite Loading → Success(User) o Error.
     */
    fun getCurrentUser(): Flow<Resource<User>>

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout()

    /**
     * Actualiza la contraseña del usuario autenticado.
     * Requiere que el usuario se haya autenticado recientemente.
     */
    fun updatePassword(newPassword: String): Flow<Resource<Unit>>
}
