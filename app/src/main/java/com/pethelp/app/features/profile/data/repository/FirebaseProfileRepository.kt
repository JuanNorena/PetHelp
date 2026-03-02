package com.pethelp.app.features.profile.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

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
                    trySend(Resource.Error(error.localizedMessage ?: "Error al obtener el perfil."))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    } else {
                        trySend(Resource.Error("Error al leer los datos del perfil."))
                    }
                } else {
                    trySend(Resource.Error("El usuario no existe en la base de datos."))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun updateProfile(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            emit(Resource.Error("No hay sesión activa."))
            return@flow
        }

        try {
            // No podemos cambiar el email o UID directamente desde aquí igual que el resto, 
            // el email requiere reutenticación. Solo actualizamos Firestore.
            val userMap = mapOf(
                "name" to user.name,
                "username" to user.username,
                "bio" to user.bio,
                "city" to user.city,
                "petPreferences" to user.petPreferences,
                "alertsNearMe" to user.alertsNearMe,
                "photoUrl" to user.photoUrl
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .update(userMap)
                .await()

            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al actualizar el perfil."))
        }
    }

    override fun updateProfilePhoto(imageUri: String): Flow<Resource<String>> = flow {
        // Placeholder hasta integrar Cloudinary
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                emit(Resource.Error("No hay sesión activa."))
                return@flow
            }
            
            // Simular subida a Cloudinary
            val fakeUrl = "https://fake.url.com/photo.jpg"
            
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .update("photoUrl", fakeUrl)
                .await()
                
            emit(Resource.Success(fakeUrl))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al subir la foto."))
        }
    }

    override fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val firebaseUser = firebaseAuth.currentUser
        
        if (firebaseUser == null || firebaseUser.email == null) {
            emit(Resource.Error("No hay sesión activa."))
            return@flow
        }
        
        try {
            val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, currentPassword)
            firebaseUser.reauthenticate(credential).await()
            firebaseUser.updatePassword(newPassword).await()
            emit(Resource.Success(Unit))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error("La contraseña actual es incorrecta."))
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            emit(Resource.Error("Debes volver a iniciar sesión para cambiar la contraseña."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al cambiar la contraseña."))
        }
    }

    override fun deleteAccount(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val firebaseUser = firebaseAuth.currentUser
        
        if (firebaseUser == null) {
            emit(Resource.Error("No hay sesión activa."))
            return@flow
        }
        
        try {
            // Delete from Firestore
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .delete()
                .await()
                
            // Delete Auth account
            firebaseUser.delete().await()
            emit(Resource.Success(Unit))
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            emit(Resource.Error("Debes volver a iniciar sesión para eliminar la cuenta."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al eliminar la cuenta."))
        }
    }
}
