package com.pethelp.app.features.notifications.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.NotificationType
import com.pethelp.app.core.domain.model.PetNotification
import com.pethelp.app.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseNotificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun observeNotifications(): Flow<Resource<List<PetNotification>>> = callbackFlow {
        trySend(Resource.Loading())

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(Resource.Success(emptyList()))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading notifications"))
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    val typeRaw = doc.getString("type") ?: NotificationType.NEW_POST_NEARBY.name
                    val type = runCatching { NotificationType.valueOf(typeRaw) }
                        .getOrElse { NotificationType.NEW_POST_NEARBY }

                    val createdAt = when (val raw = doc.get("createdAt")) {
                        is Long -> raw
                        is Timestamp -> raw.toDate().time
                        else -> System.currentTimeMillis()
                    }

                    PetNotification(
                        id = doc.id,
                        userId = doc.getString("userId") ?: uid,
                        type = type,
                        title = doc.getString("title") ?: "",
                        body = doc.getString("body") ?: "",
                        relatedPostId = doc.getString("relatedPostId"),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = createdAt
                    )
                } ?: emptyList()

                val sortedNotifications = notifications.sortedByDescending { it.createdAt }

                trySend(Resource.Success(sortedNotifications))
            }

        awaitClose { listener.remove() }
    }

    override fun markAsRead(notificationId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(Resource.Error("No authenticated user"))
            return@flow
        }

        try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "readAt" to System.currentTimeMillis()
                    )
                )
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error marking notification as read"))
        }
    }

    override fun markAllAsRead(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(Resource.Error("No authenticated user"))
            return@flow
        }

        try {
            val snapshot = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            snapshot.documents.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { doc ->
                    batch.update(
                        doc.reference,
                        mapOf(
                            "isRead" to true,
                            "readAt" to System.currentTimeMillis()
                        )
                    )
                }
                batch.commit().await()
            }

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error marking all notifications as read"))
        }
    }
}
