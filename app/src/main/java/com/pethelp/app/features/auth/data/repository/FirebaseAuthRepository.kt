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

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val trimmedEmail = email.trim().lowercase()

        try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("No user found")

            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                    ?: createDefaultUser(firebaseUser.uid, firebaseUser.email ?: trimmedEmail)
            } else {
                val defaultUser = createDefaultUser(
                    firebaseUser.uid,
                    firebaseUser.email ?: trimmedEmail
                )
                saveUserToFirestore(defaultUser)
                defaultUser
            }

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

    override fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()

        try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("User creation failed")

            firebaseUser.sendEmailVerification().await()

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

    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val trimmedEmail = email.trim().lowercase()

        try {
            firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
            emit(Resource.Success(Unit))
        } catch (e: FirebaseAuthInvalidUserException) {
            emit(Resource.Success(Unit))
        } catch (e: FirebaseNetworkException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_no_internet)))
        } catch (e: FirebaseTooManyRequestsException) {
            emit(Resource.Error(UiText.StringResource(R.string.error_auth_too_many_requests)))
        } catch (e: Exception) {
            emit(Resource.Error(mapGenericError(e, R.string.error_reset_failed)))
        }
    }

    override fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    override fun getCurrentUser(): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            trySend(Resource.Error(UiText.StringResource(R.string.error_auth_no_session)))
            close()
            return@callbackFlow
        }

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

        awaitClose { listener.remove() }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

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
