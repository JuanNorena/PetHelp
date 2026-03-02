package com.pethelp.app.features.auth.data.repository

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
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // ── Login ────────────────────────────────────────────────────────────────
    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            // 1. Autenticar con Firebase Auth
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("No se pudo obtener el usuario autenticado.")

            // 2. Obtener perfil del usuario desde Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                    ?: createDefaultUser(firebaseUser.uid, firebaseUser.email ?: email)
            } else {
                // Si no hay documento, crear uno por defecto
                val defaultUser = createDefaultUser(firebaseUser.uid, firebaseUser.email ?: email)
                saveUserToFirestore(defaultUser)
                defaultUser
            }

            emit(Resource.Success(user))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error("Correo o contraseña incorrectos."))
        } catch (e: FirebaseAuthInvalidUserException) {
            emit(Resource.Error("No existe una cuenta con este correo."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al iniciar sesión."))
        }
    }

    // ── Register ─────────────────────────────────────────────────────────────
    override fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            // 1. Crear cuenta en Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("No se pudo crear la cuenta.")

            // 2. Construir el perfil del usuario
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
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
            emit(Resource.Error("La contraseña es demasiado débil. Usa al menos 6 caracteres."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error("El correo electrónico no es válido."))
        } catch (e: FirebaseAuthUserCollisionException) {
            emit(Resource.Error("Ya existe una cuenta con este correo electrónico."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al crear la cuenta."))
        }
    }

    // ── Password Reset ───────────────────────────────────────────────────────
    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(Unit))
        } catch (e: FirebaseAuthInvalidUserException) {
            emit(Resource.Error("No existe una cuenta con este correo."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al enviar el enlace de recuperación."))
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
                    trySend(Resource.Error(error.localizedMessage ?: "Error al obtener perfil."))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                        ?.copy(id = firebaseUser.uid)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    } else {
                        trySend(Resource.Error("Error al deserializar el perfil."))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    override fun logout() {
        firebaseAuth.signOut()
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

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
