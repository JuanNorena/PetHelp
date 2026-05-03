package com.pethelp.app.features.post.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.NotificationType
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.features.post.domain.model.AdoptionRequest
import com.pethelp.app.features.post.domain.model.AdoptionRequestStatus
import com.pethelp.app.features.post.domain.repository.PostRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta del repositorio de publicaciones
 * usando Firebase Firestore.
 */
@Singleton
class FirebasePostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : PostRepository {

    private val postsCollection get() = firestore.collection(Constants.COLLECTION_POSTS)
    private val commentsCollection get() = firestore.collection(Constants.COLLECTION_COMMENTS)
    private val votesCollection get() = firestore.collection(Constants.COLLECTION_VOTES)
    private val adoptionRequestsCollection get() = firestore.collection(Constants.COLLECTION_ADOPTION_REQUESTS)
    private val notificationsCollection get() = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)

    // ── Obtener publicación por ID (con listener en tiempo real) ─────────────
    override fun getPostById(postId: String): Flow<Resource<Post>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = postsCollection.document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(
                        error.localizedMessage ?: "Error al obtener la publicación."
                    ))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val post = snapshotToPost(snapshot)
                    if (post != null) {
                        trySend(Resource.Success(post))
                    } else {
                        trySend(Resource.Error("Error al leer los datos de la publicación."))
                    }
                } else {
                    trySend(Resource.Error("La publicación no existe."))
                }
            }

        awaitClose { listener.remove() }
    }

    // ── Obtener publicaciones (con filtro opcional) ──────────────────────────
    override fun getPosts(category: String?): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())

        var query: Query = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING)

        if (!category.isNullOrBlank()) {
            query = query.whereEqualTo("category", category)
        }

        val listener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Resource.Error(
                    error.localizedMessage ?: "Error al obtener las publicaciones."
                ))
                return@addSnapshotListener
            }
            val posts = snapshots?.documents?.mapNotNull { doc ->
                snapshotToPost(doc)
            } ?: emptyList()
            trySend(Resource.Success(posts))
        }

        awaitClose { listener.remove() }
    }

    // ── Obtener publicaciones por usuario ─────────────────────────────────────
    override fun getPostsByUser(userId: String): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = postsCollection
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(Resource.Error(
                        error.localizedMessage ?: "Error al obtener tus publicaciones."
                    ))
                    return@addSnapshotListener
                }
                val posts = snapshots?.documents?.mapNotNull { doc ->
                    snapshotToPost(doc)
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(Resource.Success(posts))
            }

        awaitClose { listener.remove() }
    }

    // ── Obtener publicaciones pendientes de moderación ───────────────────────
    override fun getPendingPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = postsCollection
            .whereEqualTo("status", PostStatus.PENDING.name)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error al obtener publicaciones pendientes."))
                    return@addSnapshotListener
                }

                val posts = snapshots?.documents
                    ?.mapNotNull { snapshotToPost(it) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()

                trySend(Resource.Success(posts))
            }

        awaitClose { listener.remove() }
    }

    // ── Aprobar publicación ───────────────────────────────────────────────────
    override fun approvePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val moderatorId = firebaseAuth.currentUser?.uid
                ?: throw IllegalStateException("Debes iniciar sesión como moderador.")
            val now = System.currentTimeMillis()

            val postSnapshot = postsCollection.document(postId).get().await()
            if (!postSnapshot.exists()) {
                throw IllegalStateException("La publicación no existe.")
            }

            val authorId = postSnapshot.getString("authorId").orEmpty()

            val postRef = postsCollection.document(postId)
            val batch = firestore.batch()
            batch.update(
                postRef,
                mapOf(
                    "status" to PostStatus.VERIFIED.name,
                    "rejectionReason" to null,
                    "moderatedBy" to moderatorId,
                    "moderatedAt" to now,
                    "updatedAt" to now
                )
            )

            if (authorId.isNotBlank()) {
                val notificationRef = notificationsCollection.document()
                batch.set(
                    notificationRef,
                    mapOf(
                        "userId" to authorId,
                        "type" to NotificationType.POST_APPROVED.name,
                        "title" to "Publicación aprobada",
                        "body" to "Tu publicación fue aprobada por moderación.",
                        "relatedPostId" to postId,
                        "isRead" to false,
                        "createdAt" to now
                    )
                )
            }

            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al aprobar la publicación."))
        }
    }

    // ── Rechazar publicación ──────────────────────────────────────────────────
    override fun rejectPost(postId: String, reason: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val normalizedReason = reason.trim()
        if (normalizedReason.isBlank()) {
            emit(Resource.Error("Debes ingresar un motivo de rechazo."))
            return@flow
        }

        try {
            val moderatorId = firebaseAuth.currentUser?.uid
                ?: throw IllegalStateException("Debes iniciar sesión como moderador.")
            val now = System.currentTimeMillis()

            val postSnapshot = postsCollection.document(postId).get().await()
            if (!postSnapshot.exists()) {
                throw IllegalStateException("La publicación no existe.")
            }

            val authorId = postSnapshot.getString("authorId").orEmpty()

            val postRef = postsCollection.document(postId)
            val batch = firestore.batch()
            batch.update(
                postRef,
                mapOf(
                    "status" to PostStatus.REJECTED.name,
                    "rejectionReason" to normalizedReason,
                    "moderatedBy" to moderatorId,
                    "moderatedAt" to now,
                    "updatedAt" to now
                )
            )

            if (authorId.isNotBlank()) {
                val notificationRef = notificationsCollection.document()
                batch.set(
                    notificationRef,
                    mapOf(
                        "userId" to authorId,
                        "type" to NotificationType.POST_REJECTED.name,
                        "title" to "Publicación rechazada",
                        "body" to "Tu publicación fue rechazada. Motivo: $normalizedReason",
                        "relatedPostId" to postId,
                        "isRead" to false,
                        "createdAt" to now
                    )
                )
            }

            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al rechazar la publicación."))
        }
    }

    // ── Eliminar publicación ────────────────────────────────────────────────
    override fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(postId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al eliminar la publicación."))
        }
    }

    // ── Pausar o reanudar publicación ───────────────────────────────────────
    override fun togglePostStatus(postId: String, isPaused: Boolean): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val newStatus = if (isPaused) PostStatus.PENDING else PostStatus.VERIFIED // O un estado PAUSED si existiera
            // Por ahora usemos una lógica simple: si está "resuelta" no se toca, si no, se cambia.
            // Para la imagen, asumiremos que existe un campo "isPaused" o similar, 
            // pero para ser fieles al modelo actual usaremos VERIFIED vs PENDING (o similar)
            postsCollection.document(postId).update("status", if (isPaused) "PENDING" else "VERIFIED").await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al cambiar el estado."))
        }
    }

    // ── Marcar como resuelta (Adoptado) ──────────────────────────────────────
    override fun markAsResolved(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(postId).update("status", PostStatus.RESOLVED.name).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al marcar como resuelta."))
        }
    }

    override fun getModeratedPostsToday(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())

        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val moderatedPosts = postsCollection.get().await().documents.mapNotNull { snapshotToPost(it) }
                .filter { post -> post.updatedAt >= startOfDay && post.status != PostStatus.PENDING }
                .sortedByDescending { it.updatedAt }

            emit(Resource.Success(moderatedPosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al obtener las publicaciones moderadas."))
        }
    }

    override fun getGlobalMetrics(): Flow<Resource<Map<String, Any>>> = flow {
        emit(Resource.Loading())

        try {
            val posts = postsCollection.get().await().documents.mapNotNull { snapshotToPost(it) }
            val metrics = mapOf(
                "totalPosts" to posts.size,
                "pendingPosts" to posts.count { it.status == PostStatus.PENDING },
                "verifiedPosts" to posts.count { it.status == PostStatus.VERIFIED },
                "rejectedPosts" to posts.count { it.status == PostStatus.REJECTED },
                "resolvedPosts" to posts.count { it.status == PostStatus.RESOLVED },
                "adoptedPosts" to posts.count { it.status == PostStatus.ADOPTED }
            )
            emit(Resource.Success(metrics))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al obtener métricas globales."))
        }
    }

    // ── Crear publicación ───────────────────────────────────────────────────
    override fun createPost(post: Post): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = firebaseAuth.currentUser
                ?: throw Exception("Debes iniciar sesión para publicar.")

            val docRef = postsCollection.document()
            val newPost = post.copy(
                id = docRef.id,
                authorId = currentUser.uid,
                status = PostStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val postMap = postToMap(newPost)
            docRef.set(postMap).await()

            emit(Resource.Success(newPost))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al crear la publicación."))
        }
    }

    // ── Actualizar publicación ──────────────────────────────────────────────
    override fun updatePost(post: Post): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())

        try {
            val updatedPost = post.copy(updatedAt = System.currentTimeMillis())
            val postMap = postToMap(updatedPost)
            postsCollection.document(post.id).set(postMap).await()
            emit(Resource.Success(updatedPost))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al actualizar la publicación."))
        }
    }

    // ── Votar publicación ───────────────────────────────────────────────────
    override fun votePost(postId: String, userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())

        try {
            val voteId = "${postId}_${userId}"
            val voteDoc = mapOf(
                "postId" to postId,
                "userId" to userId,
                "createdAt" to System.currentTimeMillis()
            )
            votesCollection.document(voteId).set(voteDoc).await()

            // Incrementar contador de votos en la publicación
            firestore.runTransaction { transaction ->
                val postRef = postsCollection.document(postId)
                val snapshot = transaction.get(postRef)
                val currentVotes = snapshot.getLong("votes")?.toInt() ?: 0
                transaction.update(postRef, "votes", currentVotes + 1)
                currentVotes + 1
            }.await().let { newVotes ->
                emit(Resource.Success(newVotes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al votar."))
        }
    }

    // ── Eliminar voto ───────────────────────────────────────────────────────
    override fun unvotePost(postId: String, userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())

        try {
            val voteId = "${postId}_${userId}"
            votesCollection.document(voteId).delete().await()

            firestore.runTransaction { transaction ->
                val postRef = postsCollection.document(postId)
                val snapshot = transaction.get(postRef)
                val currentVotes = snapshot.getLong("votes")?.toInt() ?: 0
                val newVotes = maxOf(0, currentVotes - 1)
                transaction.update(postRef, "votes", newVotes)
                newVotes
            }.await().let { newVotes ->
                emit(Resource.Success(newVotes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al quitar el voto."))
        }
    }

    // ── Verificar si el usuario votó ────────────────────────────────────────
    override fun hasUserVoted(postId: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val voteId = "${postId}_${userId}"
            val doc = votesCollection.document(voteId).get().await()
            emit(Resource.Success(doc.exists()))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al verificar el voto."))
        }
    }

    override fun toggleFavorite(postId: String, userId: String, isFavorite: Boolean): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())

        try {
            if (isFavorite) {
                votesCollection.document("${postId}_${userId}").delete().await()
            } else {
                val voteDoc = mapOf(
                    "postId" to postId,
                    "userId" to userId,
                    "createdAt" to System.currentTimeMillis()
                )
                votesCollection.document("${postId}_${userId}").set(voteDoc).await()
            }

            val snapshot = postsCollection.document(postId).get().await()
            val currentVotes = snapshot.getLong("votes")?.toInt() ?: 0
            val updatedVotes = if (isFavorite) maxOf(0, currentVotes - 1) else currentVotes + 1
            postsCollection.document(postId).update("votes", updatedVotes).await()
            emit(Resource.Success(updatedVotes))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al actualizar favoritos."))
        }
    }

    override fun getFavoritePosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())

        try {
            val votesSnapshot = votesCollection.whereEqualTo("userId", userId).get().await()
            val favoritePosts = votesSnapshot.documents.mapNotNull { voteDoc ->
                val postId = voteDoc.getString("postId") ?: return@mapNotNull null
                val postSnapshot = postsCollection.document(postId).get().await()
                snapshotToPost(postSnapshot)
            }.sortedByDescending { it.createdAt }

            emit(Resource.Success(favoritePosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al obtener favoritos."))
        }
    }

    // ── Obtener comentarios en tiempo real ──────────────────────────────────
    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = commentsCollection
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(Resource.Error(
                        error.localizedMessage ?: "Error al obtener los comentarios."
                    ))
                    return@addSnapshotListener
                }
                val comments = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Resource.Success(comments))
            }

        awaitClose { listener.remove() }
    }

    // ── Agregar comentario ──────────────────────────────────────────────────
    override fun addComment(comment: Comment): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())

        try {
            val docRef = commentsCollection.document()
            val newComment = comment.copy(
                id = docRef.id,
                createdAt = System.currentTimeMillis()
            )
            docRef.set(newComment).await()

            // Incrementar contador de comentarios en la publicación
            firestore.runTransaction { transaction ->
                val postRef = postsCollection.document(comment.postId)
                val snapshot = transaction.get(postRef)
                val count = snapshot.getLong("commentsCount")?.toInt() ?: 0
                transaction.update(postRef, "commentsCount", count + 1)
            }.await()

            emit(Resource.Success(newComment))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al publicar el comentario."))
        }
    }

    // ── Solicitar adopción ──────────────────────────────────────────────────
    override fun requestAdoption(
        postId: String,
        userId: String,
        message: String,
        housingType: String,
        hasOutdoorSpace: String,
        hasExperience: String,
        phone: String,
        contactPreference: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val requestDoc = mapOf(
                "postId" to postId,
                "requesterId" to userId,
                "message" to message,
                "housingType" to housingType,
                "hasOutdoorSpace" to hasOutdoorSpace,
                "hasExperience" to hasExperience,
                "phone" to phone,
                "contactPreference" to contactPreference,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis()
            )
            adoptionRequestsCollection.document().set(requestDoc).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al enviar la solicitud de adopción."))
        }
    }

    override fun getAdoptionRequestsForUser(userId: String): Flow<Resource<List<AdoptionRequest>>> = flow {
        emit(Resource.Loading())

        try {
            val postIds = postsCollection.whereEqualTo("authorId", userId).get().await().documents.map { it.id }
            val requests = if (postIds.isEmpty()) {
                emptyList()
            } else {
                adoptionRequestsCollection.whereIn("postId", postIds.take(30)).get().await().documents.mapNotNull { snapshotToAdoptionRequest(it) }
            }
            emit(Resource.Success(requests.sortedByDescending { it.createdAt }))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al obtener las solicitudes de adopción."))
        }
    }

    override fun getAdoptionRequestsForPost(postId: String): Flow<Resource<List<AdoptionRequest>>> = flow {
        emit(Resource.Loading())

        try {
            val requests = adoptionRequestsCollection.whereEqualTo("postId", postId).get().await().documents.mapNotNull { snapshotToAdoptionRequest(it) }
            emit(Resource.Success(requests.sortedByDescending { it.createdAt }))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al obtener las solicitudes de adopción."))
        }
    }

    override fun acceptAdoptionRequest(requestId: String, postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            adoptionRequestsCollection.document(requestId).update("status", AdoptionRequestStatus.ACCEPTED.name).await()
            postsCollection.document(postId).update("status", PostStatus.ADOPTED.name).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al aceptar la solicitud de adopción."))
        }
    }

    override fun rejectAdoptionRequest(requestId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            adoptionRequestsCollection.document(requestId).update("status", AdoptionRequestStatus.REJECTED.name).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al rechazar la solicitud de adopción."))
        }
    }

    // ── Helpers privados ────────────────────────────────────────────────────

    private fun snapshotToPost(doc: com.google.firebase.firestore.DocumentSnapshot): Post? {
        return try {
            Post(
                id = doc.id,
                authorId = doc.getString("authorId") ?: "",
                authorName = doc.getString("authorName") ?: "",
                authorPhotoUrl = doc.getString("authorPhotoUrl") ?: "",
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                category = try {
                    PostCategory.valueOf(doc.getString("category") ?: "ADOPTION")
                } catch (_: Exception) { PostCategory.ADOPTION },
                status = try {
                    PostStatus.valueOf(doc.getString("status") ?: "PENDING")
                } catch (_: Exception) { PostStatus.PENDING },
                animalType = doc.getString("animalType") ?: "",
                breed = doc.getString("breed") ?: "",
                size = try {
                    AnimalSize.valueOf(doc.getString("size") ?: "MEDIUM")
                } catch (_: Exception) { AnimalSize.MEDIUM },
                vaccinated = doc.getBoolean("vaccinated") ?: false,
                imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                latitude = doc.getDouble("latitude") ?: 0.0,
                longitude = doc.getDouble("longitude") ?: 0.0,
                locationName = doc.getString("locationName") ?: "",
                votes = doc.getLong("votes")?.toInt() ?: 0,
                commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                rejectionReason = doc.getString("rejectionReason"),
                moderatedBy = doc.getString("moderatedBy"),
                moderatedAt = doc.getLong("moderatedAt"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }
        private fun snapshotToAdoptionRequest(doc: com.google.firebase.firestore.DocumentSnapshot): AdoptionRequest? {
            return try {
                AdoptionRequest(
                    id = doc.id,
                    postId = doc.getString("postId") ?: "",
                    requesterId = doc.getString("requesterId") ?: "",
                    requesterName = doc.getString("requesterName") ?: "",
                    requesterPhotoUrl = doc.getString("requesterPhotoUrl") ?: "",
                    message = doc.getString("message") ?: "",
                    housingType = doc.getString("housingType") ?: "",
                    hasOutdoorSpace = doc.getString("hasOutdoorSpace") ?: "",
                    hasExperience = doc.getString("hasExperience") ?: "",
                    phone = doc.getString("phone") ?: "",
                    contactPreference = doc.getString("contactPreference") ?: "",
                    status = runCatching {
                        AdoptionRequestStatus.valueOf(doc.getString("status") ?: AdoptionRequestStatus.PENDING.name)
                    }.getOrDefault(AdoptionRequestStatus.PENDING),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (_: Exception) {
                null
            }
        }

    private fun postToMap(post: Post): Map<String, Any?> = mapOf(
        "authorId" to post.authorId,
        "authorName" to post.authorName,
        "authorPhotoUrl" to post.authorPhotoUrl,
        "title" to post.title,
        "description" to post.description,
        "category" to post.category.name,
        "status" to post.status.name,
        "animalType" to post.animalType,
        "breed" to post.breed,
        "size" to post.size.name,
        "vaccinated" to post.vaccinated,
        "imageUrls" to post.imageUrls,
        "latitude" to post.latitude,
        "longitude" to post.longitude,
        "locationName" to post.locationName,
        "votes" to post.votes,
        "commentsCount" to post.commentsCount,
        "rejectionReason" to post.rejectionReason,
        "moderatedBy" to post.moderatedBy,
        "moderatedAt" to post.moderatedAt,
        "createdAt" to post.createdAt,
        "updatedAt" to post.updatedAt
    )

}
