/**
 * Implementación de [NotificationRepository] que usa Firebase Firestore
 * para leer, actualizar y observar notificaciones del usuario.
 *
 * Observa en tiempo real la colección `notifications` filtrada por `userId`,
 * mapea documentos a [PetNotification] y provee operaciones para marcar
 * notificaciones como leídas (individual o masivamente).
 */
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

/**
 * Implementación de [NotificationRepository] que usa Firebase Firestore
 * para leer, actualizar y observar notificaciones del usuario.
 *
 * **Responsabilidad Principal:**
 * - Observar en tiempo real la colección `notifications` filtrada por `userId`.
 * - Mapear documentos de Firestore a objetos [PetNotification] con conversión de tipos.
 * - Proveer operaciones para marcar notificaciones como leídas (individual o masivamente).
 *
 * **Conversión de Tipos:**
 * El campo `createdAt` puede ser un [Timestamp] de Firestore o un [Long] en milisegundos.
 * Este repositorio normaliza ambos a [Long] para mantener consistencia en la capa de dominio.
 *
 * @param auth Instancia de Firebase Auth para obtener el UID del usuario actual.
 * @param firestore Instancia de Firestore para acceder a la colección de notificaciones.
 */
@Singleton
class FirebaseNotificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    /**
     * Escucha en tiempo real las notificaciones del usuario autenticado.
     *
     * Normaliza el campo `createdAt` (puede ser Timestamp o Long) y ordena
     * por fecha descendente para mostrar las más recientes primero.
     *
     * @return Flujo reactivo con lista de notificaciones.
     */
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

    /**
     * Marca una notificación individual como leída.
     *
     * @param notificationId Identificador de la notificación.
     * @return Flujo con éxito o error de la operación.
     */
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

    /**
     * Marca todas las notificaciones no leídas del usuario como leídas.
     *
     * Procesa en lotes de 500 documentos por batch (límite de Firestore).
     *
     * @return Flujo con éxito o error de la operación.
     */
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
