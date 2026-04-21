package com.pethelp.app.features.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.core.domain.model.UserLevel
import com.pethelp.app.core.domain.model.UserRole
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [AuthRepository] utilizando los servicios de Firebase (Auth y Firestore).
 *
 * **Responsabilidad Principal:**
 * Esta clase es el motor de seguridad de la aplicación. Gestiona el ciclo de vida de la sesión del usuario,
 * desde el registro y login hasta la recuperación de contraseña y cierre de sesión. Además, se encarga
 * de sincronizar la información de autenticación con el perfil del usuario almacenado en base de datos.
 *
 * **Propósito y Arquitectura:**
 * - **Seguridad:** Centraliza el manejo de excepciones específicas de Firebase (contraseñas débiles, usuarios duplicados, etc.).
 * - **Reactividad:** Utiliza [Flow] para emitir estados de carga ([Resource.Loading]), éxito ([Resource.Success]) o error ([Resource.Error]).
 * - **Persistencia:** Al registrar o loguear, asegura que el perfil del usuario exista en la colección `users` de Firestore.
 *
 * **Lógica de Funcionamiento (Paso a Paso):**
 * 1. **Autenticación:** Intenta la operación con [FirebaseAuth].
 * 2. **Sincronización:** Si tiene éxito, consulta o crea el documento del usuario en [FirebaseFirestore].
 * 3. **Manejo de Errores:** Captura excepciones de red o de lógica de negocio y las traduce a mensajes amigables ([UiText]).
 * 4. **Limpieza:** Permite el cierre de sesión eliminando el estado persistente de Firebase.
 *
 * **Notas para Junior Developers:**
 * - Se usa `.await()` de la librería `kotlinx-coroutines-play-services` para convertir las tareas de Firebase (Tasks)
 *   en funciones de suspensión de Kotlin, evitando el uso de callbacks anidados.
 * - `callbackFlow` se emplea en [getCurrentUser] para escuchar cambios en tiempo real en el perfil del usuario.
 * - Siempre se recomienda usar `trim().lowercase()` en los correos electrónicos para evitar errores tipográficos comunes.
 *
 * @property firebaseAuth Instancia de Firebase para gestionar el correo y la contraseña.
 * @property firestore Instancia de la base de datos NoSQL para almacenar perfiles de usuario.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see AuthRepository Interfaz que define las operaciones de esta clase.
 * @see User Modelo de datos del usuario.
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        /** Nombre de la colección en Firestore donde se guardan los perfiles. */
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Inicia sesión de un usuario existente mediante correo y contraseña.
     *
     * **Proceso:**
     * 1. Emite estado de carga.
     * 2. Autentica en Firebase.
     * 3. Recupera el perfil desde Firestore.
     * 4. Si el perfil no existe, crea uno por defecto.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña de la cuenta.
     * @return Un [Flow] que emite el estado del recurso ([Resource]) con el objeto [User].
     * @throws Exception Si ocurre un error inesperado no capturado específicamente.
     */
    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        // PASO 1: Notificamos a la UI que estamos cargando.
        emit(Resource.Loading())

        val trimmedEmail = email.trim().lowercase()

        try {
            // PASO 2: Intentamos la autenticación con Firebase.
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("No user found")

            // PASO 3: Buscamos los datos adicionales del usuario en nuestra base de datos (Firestore).
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                // Si el documento existe, lo convertimos a nuestro modelo User.
                userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                    ?: createDefaultUser(firebaseUser.uid, firebaseUser.email ?: trimmedEmail)
            } else {
                // Si no existe (ej. error en registro previo), creamos uno base.
                val defaultUser = createDefaultUser(
                    firebaseUser.uid,
                    firebaseUser.email ?: trimmedEmail
                )
                saveUserToFirestore(defaultUser)
                defaultUser
            }

            // PASO 4: Emitimos éxito con el objeto de usuario completo.
            emit(Resource.Success(user))

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_login_failed)))
        } catch (e: FirebaseAuthInvalidUserException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_user_not_found)))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_no_internet)))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_too_many_requests)))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, R.string.error_login_failed)))
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * **Proceso:**
     * 1. Crea la cuenta en Firebase Auth.
     * 2. Envía un correo de verificación.
     * 3. Crea el perfil inicial del usuario con valores por defecto ([UserRole.USER], [UserLevel.FRIEND]).
     * 4. Guarda el perfil en Firestore.
     *
     * @param name Nombre completo del usuario.
     * @param email Correo electrónico.
     * @param password Contraseña elegida.
     * @return Un [Flow] con el estado de la operación y el nuevo usuario.
     */
    override fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()

        try {
            // PASO 1: Creamos el usuario en Firebase.
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("User creation failed")

            // PASO 2: Enviamos correo de verificación para mayor seguridad.
            firebaseUser.sendEmailVerification().await()

            // PASO 3: Construimos nuestro modelo de dominio User.
            val user = User(
                id = firebaseUser.uid,
                name = trimmedName,
                email = trimmedEmail,
                photoUrl = "",
                role = UserRole.USER,
                points = 0,
                level = UserLevel.FRIEND,
                badges = emptyList(),
                notificationRadiusKm = 10.0,
                createdAt = System.currentTimeMillis()
            )

            // PASO 4: Persistimos los datos en Firestore.
            saveUserToFirestore(user)
            emit(Resource.Success(user))

        } catch (e: FirebaseAuthWeakPasswordException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_weak_password)))
        } catch (e: FirebaseAuthUserCollisionException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_user_collision)))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_no_internet)))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_too_many_requests)))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, R.string.error_register_failed)))
        }
    }

    /**
     * Envía un correo electrónico para restablecer la contraseña.
     *
     * @param email Correo del usuario.
     * @return [Flow] que indica éxito o error en el envío.
     */
    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val trimmedEmail = email.trim().lowercase()

        try {
            firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
            emit(Resource.Success(Unit))
        } catch (e: FirebaseAuthInvalidUserException) {
            // Por seguridad, si el usuario no existe, emitimos éxito para no dar pistas.
            emit(Resource.Success(Unit))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_no_internet)))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_too_many_requests)))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, R.string.error_reset_failed)))
        }
    }

    /**
     * Comprueba si existe una sesión activa actualmente.
     *
     * @return `true` si hay un usuario logueado, `false` en caso contrario.
     */
    override fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    /**
     * Obtiene el perfil del usuario actual y escucha cambios en tiempo real.
     *
     * **Nota Técnica:**
     * Utiliza `callbackFlow` para envolver el `snapshotListener` de Firestore,
     * lo que permite que la UI se actualice automáticamente si el usuario gana puntos,
     * sube de nivel, etc.
     *
     * @return [Flow] reactivo con los datos del usuario.
     */
    override fun getCurrentUser(): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            trySend(Resource.Error(UiText.StringResource(R.string.error_auth_no_session)))
            close()
            return@callbackFlow
        }

        // Suscripción a cambios en el documento de Firestore.
        val listener = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.StringResource(R.string.error_auth_profile_load)))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    } else {
                        trySend(Resource.Error(UiText.StringResource(R.string.error_auth_profile_read)))
                    }
                } else {
                    val defaultUser = createDefaultUser(firebaseUser.uid, firebaseUser.email ?: "")
                    trySend(Resource.Success(defaultUser))
                }
            }

        // PASO CRÍTICO: Eliminamos el listener cuando el Flow se cierra para evitar fugas de memoria.
        awaitClose { listener.remove() }
    }

    /**
     * Cierra la sesión del usuario actual en el dispositivo.
     */
    override fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Permite cambiar la contraseña del usuario autenticado.
     *
     * @param newPassword Nueva contraseña (debe cumplir requisitos de seguridad).
     * @return [Flow] que indica el resultado de la actualización.
     */
    override fun updatePassword(newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                emit(Resource.Error(UiText.StringResource(R.string.error_auth_no_session)))
                return@flow
            }
            user.updatePassword(newPassword).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, R.string.error_generic)))
        }
    }

    /**
     * Traduce excepciones técnicas de Firebase a mensajes legibles por el usuario.
     *
     * @param e La excepción capturada.
     * @param fallbackRes ID del recurso de string por defecto si no se reconoce el error.
     * @return Un objeto [UiText] con el mensaje traducido.
     */
    private fun mapGenericError(e: Exception, fallbackRes: Int): UiText {
        return when {
            e.message?.contains("CONFIGURATION_NOT_FOUND") == true ->
                UiText.StringResource(R.string.error_auth_config_missing)
            e.message?.contains("NETWORK", ignoreCase = true) == true ->
                UiText.StringResource(R.string.error_no_internet)
            e.message?.contains("internal error", ignoreCase = true) == true ->
                UiText.StringResource(R.string.error_auth_server)
            else -> e.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(fallbackRes)
        }
    }

    /**
     * Crea un perfil de usuario base cuando no se encuentra uno en Firestore.
     */
    private fun createDefaultUser(uid: String, email: String): User = User(
        id = uid,
        name = "",
        email = email,
        role = UserRole.USER,
        points = 0,
        level = UserLevel.FRIEND,
        createdAt = System.currentTimeMillis()
    )

    /**
     * Guarda de forma asíncrona un objeto de usuario en la base de datos.
     */
    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
    }
}
