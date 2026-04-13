package com.pethelp.app.features.profile.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.core.domain.upload.ImageUploader
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
    private val firestore: FirebaseFirestore,
    private val imageUploader: ImageUploader
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
                "photoUrl" to user.photoUrl,
                "profileVisibility" to user.profileVisibility,
                "showEmail" to user.showEmail,
                "showCity" to user.showCity,
                "pushNotificationsEnabled" to user.pushNotificationsEnabled,
                "emailAlertsEnabled" to user.emailAlertsEnabled,
                "twoFactorEnabled" to user.twoFactorEnabled
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .update(userMap)
                .await()

            propagateAuthorDataChanges(
                userId = firebaseUser.uid,
                newAuthorName = user.name.ifBlank { null },
                newAuthorPhotoUrl = user.photoUrl.ifBlank { null }
            )

            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al actualizar el perfil."))
        }
    }

    override fun updateProfilePhoto(imageUri: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                emit(Resource.Error("No hay sesión activa."))
                return@flow
            }

            if (imageUri.isBlank()) {
                emit(Resource.Error("Selecciona una imagen válida."))
                return@flow
            }

            val uploadedPhotoUrl = imageUploader.uploadImage(
                localUri = imageUri,
                folder = Constants.CLOUDINARY_FOLDER_AVATARS
            )

            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .update("photoUrl", uploadedPhotoUrl)
                .await()

            propagateAuthorDataChanges(
                userId = firebaseUser.uid,
                newAuthorPhotoUrl = uploadedPhotoUrl
            )

            emit(Resource.Success(uploadedPhotoUrl))
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

    /**
     * Propaga cambios de metadatos denormalizados del autor en publicaciones y comentarios.
     *
     * Esto permite que cambios en perfil (nombre/foto) se reflejen también en contenido
     * histórico sin depender de joins en cliente al renderizar.
     */
    private suspend fun propagateAuthorDataChanges(
        userId: String,
        newAuthorName: String? = null,
        newAuthorPhotoUrl: String? = null
    ) {
        if (userId.isBlank()) return

        val postUpdates = mutableMapOf<String, Any>()
        val commentUpdates = mutableMapOf<String, Any>()

        newAuthorName?.let {
            postUpdates["authorName"] = it
            commentUpdates["authorName"] = it
        }

        newAuthorPhotoUrl?.let {
            postUpdates["authorPhotoUrl"] = it
            commentUpdates["authorPhotoUrl"] = it
        }

        if (postUpdates.isEmpty() && commentUpdates.isEmpty()) return

        val postsDocs = firestore.collection(Constants.COLLECTION_POSTS)
            .whereEqualTo("authorId", userId)
            .get()
            .await()
            .documents

        val commentDocs = firestore.collection(Constants.COLLECTION_COMMENTS)
            .whereEqualTo("authorId", userId)
            .get()
            .await()
            .documents

        postsDocs.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc ->
                batch.update(doc.reference, postUpdates)
            }
            batch.commit().await()
        }

        commentDocs.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc ->
                batch.update(doc.reference, commentUpdates)
            }
            batch.commit().await()
        }
    }
}
