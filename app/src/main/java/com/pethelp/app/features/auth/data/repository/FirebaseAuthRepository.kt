package com.pethelp.app.features.auth.data.repository

import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Resource
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
 * Implementación concreta del repositorio de autenticación
 * usando Firebase Auth + Firestore.
 *
 * Firebase Auth gestiona las credenciales (email/password).
 * Firestore almacena el perfil completo del usuario (colección "users").
 *
 * Cada operación emite un Flow<Resource<T>>:
 *   Loading → (operación async) → Success | Error
 *
 * Validaciones locales se ejecutan ANTES de cualquier llamada a Firebase
 * para evitar consumos innecesarios de red y cuota.
 *
 * Errores manejados:
 *   - FirebaseAuthInvalidCredentialsException → credenciales incorrectas
 *   - FirebaseAuthInvalidUserException → usuario no encontrado
 *   - FirebaseAuthUserCollisionException → correo ya registrado
 *   - FirebaseAuthWeakPasswordException → contraseña débil
 *   - FirebaseNetworkException → sin conexión a internet
 *   - FirebaseTooManyRequestsException → límite de intentos excedido
 *   - CONFIGURATION_NOT_FOUND → reCAPTCHA no configurado (ver PetHelpApplication)
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_NAME_LENGTH = 100
        private const val MAX_EMAIL_LENGTH = 254
    }

    // ── Login ────────────────────────────────────────────────────────────────
    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        // — Validaciones locales —
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            emit(Resource.Error("Ingresa tu correo electrónico."))
            return@flow
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emit(Resource.Error("El formato del correo electrónico no es válido."))
            return@flow
        }
        if (password.isBlank()) {
            emit(Resource.Error("Ingresa tu contraseña."))
            return@flow
        }

        try {
            // 1. Autenticar con Firebase Auth
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("No se pudo obtener el usuario autenticado.")

            // 2. Obtener perfil del usuario desde Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                    ?: createDefaultUser(firebaseUser.uid, firebaseUser.email ?: trimmedEmail)
            } else {
                // Si no hay documento, crear uno por defecto
                val defaultUser = createDefaultUser(
                    firebaseUser.uid,
                    firebaseUser.email ?: trimmedEmail
                )
                saveUserToFirestore(defaultUser)
                defaultUser
            }

            emit(Resource.Success(user))

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error("Correo o contraseña incorrectos. Verifica tus datos."))
        } catch (e: FirebaseAuthInvalidUserException) {
            emit(Resource.Error("No existe una cuenta registrada con este correo."))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo."))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error("Demasiados intentos fallidos. Espera unos minutos e intenta de nuevo."))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, "Error inesperado al iniciar sesión.")))
        }
    }

    // ── Register ─────────────────────────────────────────────────────────────
    override fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        // — Validaciones locales —
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()

        if (trimmedName.isBlank()) {
            emit(Resource.Error("Ingresa tu nombre completo."))
            return@flow
        }
        if (trimmedName.length > MAX_NAME_LENGTH) {
            emit(Resource.Error("El nombre no puede superar los $MAX_NAME_LENGTH caracteres."))
            return@flow
        }
        if (trimmedEmail.isBlank()) {
            emit(Resource.Error("Ingresa tu correo electrónico."))
            return@flow
        }
        if (trimmedEmail.length > MAX_EMAIL_LENGTH) {
            emit(Resource.Error("El correo electrónico es demasiado largo."))
            return@flow
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emit(Resource.Error("El formato del correo electrónico no es válido."))
            return@flow
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            emit(Resource.Error("La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres."))
            return@flow
        }

        try {
            // 1. Crear cuenta en Firebase Auth
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("No se pudo crear la cuenta.")

            // 1.5 Enviar correo de verificación
            firebaseUser.sendEmailVerification().await()

            // 2. Construir el perfil del usuario
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

            // 3. Persistir en Firestore
            saveUserToFirestore(user)

            emit(Resource.Success(user))

        } catch (e: FirebaseAuthWeakPasswordException) {
            emit(Resource.Error("La contraseña es demasiado débil. Usa al menos 6 caracteres con letras y números."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error("El correo electrónico no tiene un formato válido."))
        } catch (e: FirebaseAuthUserCollisionException) {
            emit(Resource.Error("Ya existe una cuenta registrada con este correo electrónico."))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo."))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error("Demasiados intentos. Espera unos minutos e intenta de nuevo."))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, "Error inesperado al crear la cuenta.")))
        }
    }

    // ── Password Reset ───────────────────────────────────────────────────────
    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        // — Validaciones locales —
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            emit(Resource.Error("Ingresa tu correo electrónico."))
            return@flow
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emit(Resource.Error("El formato del correo electrónico no es válido."))
            return@flow
        }

        try {
            firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
            emit(Resource.Success(Unit))
        } catch (e: FirebaseAuthInvalidUserException) {
            // Por seguridad, no revelar si el correo existe o no.
            // Se muestra éxito igualmente para evitar enumeración de usuarios.
            emit(Resource.Success(Unit))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo."))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error("Demasiados intentos. Espera unos minutos e intenta de nuevo."))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, "Error al enviar el enlace de recuperación.")))
        }
    }

    // ── Session Check ────────────────────────────────────────────────────────
    override fun isUserAuthenticated(): Boolean =
        firebaseAuth.currentUser != null

    // ── Current User ─────────────────────────────────────────────────────────
    override fun getCurrentUser(): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            trySend(Resource.Error("No hay sesión activa."))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        Resource.Error(
                            error.localizedMessage ?: "Error al obtener el perfil del usuario."
                        )
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                        ?.copy(id = firebaseUser.uid)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    } else {
                        trySend(Resource.Error("Error al leer los datos del perfil."))
                    }
                } else {
                    // El documento no existe — crear uno por defecto
                    val defaultUser = createDefaultUser(
                        firebaseUser.uid,
                        firebaseUser.email ?: ""
                    )
                    trySend(Resource.Success(defaultUser))
                }
            }

        awaitClose { listener.remove() }
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    override fun logout() {
        firebaseAuth.signOut()
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    /**
     * Mapea excepciones genéricas a mensajes amigables.
     * Cubre el caso de CONFIGURATION_NOT_FOUND (reCAPTCHA no configurado)
     * y errores de red no tipados.
     */
    private fun mapGenericError(e: Exception, fallback: String): String = when {
        e.message?.contains("CONFIGURATION_NOT_FOUND") == true ->
            "Error de configuración del servicio de autenticación. Contacta al soporte técnico."
        e.message?.contains("NETWORK", ignoreCase = true) == true ->
            "Error de red. Verifica tu conexión a internet."
        e.message?.contains("internal error", ignoreCase = true) == true ->
            "Error interno del servidor. Intenta de nuevo en unos momentos."
        else -> e.localizedMessage ?: fallback
    }

    private fun createDefaultUser(uid: String, email: String): User = User(
        id = uid,
        name = "",
        email = email,
        role = UserRole.USER,
        points = 0,
        level = UserLevel.FRIEND,
        createdAt = System.currentTimeMillis()
    )

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
    }
}
