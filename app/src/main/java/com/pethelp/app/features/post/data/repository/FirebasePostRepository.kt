package com.pethelp.app.features.post.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.features.post.domain.model.AdoptionRequest
import com.pethelp.app.features.post.domain.model.AdoptionRequestStatus
import com.pethelp.app.features.post.domain.repository.PostRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion de [PostRepository] sobre Firebase (Firestore + Auth).
 *
 * Expone operaciones reactivas para publicaciones, votos, comentarios, moderacion y
 * solicitudes de adopcion. Los metodos de lectura continua usan `callbackFlow` con
 * `addSnapshotListener`, y los de escritura usan `flow` con operaciones suspendidas.
 *
 * @property firestore Cliente principal para acceder a colecciones y transacciones.
 * @property firebaseAuth Proveedor del usuario autenticado para acciones de moderacion.
 */
@Singleton
class FirebasePostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : PostRepository {

    /** Coleccion principal de publicaciones. */
    private val postsCollection: CollectionReference = firestore.collection(Constants.COLLECTION_POSTS)

    /** Coleccion de comentarios asociados a publicaciones. */
    private val commentsCollection: CollectionReference = firestore.collection(Constants.COLLECTION_COMMENTS)

    /** Coleccion de votos/favoritos por usuario y publicacion. */
    private val votesCollection: CollectionReference = firestore.collection(Constants.COLLECTION_VOTES)

    /** Coleccion de solicitudes de adopcion enviadas por usuarios. */
    private val adoptionRequestsCollection: CollectionReference = firestore.collection(Constants.COLLECTION_ADOPTION_REQUESTS)

    override fun getPostById(postId: String): Flow<Resource<Post>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = postsCollection.document(postId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error al obtener el post")))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                snapshotToPost(snapshot)?.let { trySend(Resource.Success(it)) }
            } else {
                trySend(Resource.Error(UiText.DynamicString("Publicación no encontrada")))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getPosts(category: String?): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        var query: Query = postsCollection.whereIn("status", listOf(PostStatus.ACTIVE.name, PostStatus.VERIFIED.name))
        if (category != null) {
            query = query.whereEqualTo("category", category)
        }
        val listener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error")))
                return@addSnapshotListener
            }
            val posts = snapshots?.documents?.mapNotNull { snapshotToPost(it) }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(Resource.Success(posts))
        }
        awaitClose { listener.remove() }
    }

    override fun getPostsByUser(userId: String): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = postsCollection.whereEqualTo("authorId", userId).addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error")))
                return@addSnapshotListener
            }
            val posts = snapshots?.documents?.mapNotNull { snapshotToPost(it) }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(Resource.Success(posts))
        }
        awaitClose { listener.remove() }
    }

    override fun getFavoritePosts(userId: String): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = votesCollection.whereEqualTo("userId", userId).addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error")))
                return@addSnapshotListener
            }
            val postIds = snapshots?.documents?.mapNotNull { it.getString("postId") } ?: emptyList()
            if (postIds.isEmpty()) {
                trySend(Resource.Success(emptyList()))
            } else {
                postsCollection.whereIn(FieldPath.documentId(), postIds.take(10)).get().addOnSuccessListener { postSnapshots ->
                    val posts = postSnapshots.documents.mapNotNull { snapshotToPost(it) }
                    trySend(Resource.Success(posts))
                }
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getPendingPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = postsCollection.whereEqualTo("status", PostStatus.PENDING.name).addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error")))
                return@addSnapshotListener
            }
            val posts = snapshots?.documents?.mapNotNull { snapshotToPost(it) }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(Resource.Success(posts))
        }
        awaitClose { listener.remove() }
    }

    override fun approvePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(postId).update(
                mapOf(
                    "status" to PostStatus.VERIFIED.name,
                    "moderatedBy" to (firebaseAuth.currentUser?.uid ?: "unknown"),
                    "moderatedAt" to System.currentTimeMillis()
                )
            ).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun rejectPost(postId: String, reason: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(postId).update(
                mapOf(
                    "status" to PostStatus.REJECTED.name,
                    "rejectionReason" to reason,
                    "moderatedBy" to (firebaseAuth.currentUser?.uid ?: "unknown"),
                    "moderatedAt" to System.currentTimeMillis()
                )
            ).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun getModeratedPostsToday(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val startOfDay = getStartOfDay()
        val listener = postsCollection
            .whereIn("status", listOf(PostStatus.VERIFIED.name, PostStatus.REJECTED.name))
            .whereGreaterThanOrEqualTo("moderatedAt", startOfDay)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error")))
                    return@addSnapshotListener
                }
                val posts = snapshots?.documents?.mapNotNull { snapshotToPost(it) } ?: emptyList()
                trySend(Resource.Success(posts))
            }
        awaitClose { listener.remove() }
    }

    override fun getGlobalMetrics(): Flow<Resource<Map<String, Any>>> = flow {
        emit(Resource.Loading())
        try {
            val totalUsers = firestore.collection(Constants.COLLECTION_USERS).get().await().size()
            val totalAdoptions = postsCollection.whereEqualTo("status", PostStatus.ADOPTED.name).get().await().size()
            val activeReports = firestore.collection("reports").whereEqualTo("status", "PENDING").get().await().size()
            
            val metrics = mapOf(
                "totalUsers" to totalUsers,
                "totalAdoptions" to totalAdoptions,
                "activeReports" to activeReports
            )
            emit(Resource.Success(metrics))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error metrics")))
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(postId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun togglePostStatus(postId: String, isPaused: Boolean): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val newStatus = if (isPaused) PostStatus.PAUSED.name else PostStatus.ACTIVE.name
            postsCollection.document(postId).update("status", newStatus).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun markAsResolved(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(postId).update("status", PostStatus.RESOLVED.name).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun createPost(post: Post): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val docRef = postsCollection.document()
            val finalPost = post.copy(id = docRef.id)
            docRef.set(postToMap(finalPost)).await()
            emit(Resource.Success(finalPost))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun updatePost(post: Post): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            postsCollection.document(post.id).set(postToMap(post)).await()
            emit(Resource.Success(post))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun votePost(postId: String, userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            val voteId = "${postId}_${userId}"
            votesCollection.document(voteId).set(mapOf("postId" to postId, "userId" to userId, "createdAt" to System.currentTimeMillis())).await()
            val newVotes = firestore.runTransaction { tx ->
                val ref = postsCollection.document(postId)
                val count = tx.get(ref).getLong("votes")?.toInt() ?: 0
                tx.update(ref, "votes", count + 1)
                count + 1
            }.await()
            emit(Resource.Success(newVotes))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun unvotePost(postId: String, userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            votesCollection.document("${postId}_${userId}").delete().await()
            val newVotes = firestore.runTransaction { tx ->
                val ref = postsCollection.document(postId)
                val count = tx.get(ref).getLong("votes")?.toInt() ?: 0
                val next = maxOf(0, count - 1)
                tx.update(ref, "votes", next)
                next
            }.await()
            emit(Resource.Success(newVotes))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun hasUserVoted(postId: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val doc = votesCollection.document("${postId}_${userId}").get().await()
            emit(Resource.Success(doc.exists()))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun toggleFavorite(postId: String, userId: String, isFavorite: Boolean): Flow<Resource<Int>> {
        return if (isFavorite) votePost(postId, userId) else unvotePost(postId, userId)
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = commentsCollection.whereEqualTo("postId", postId).addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Error")))
                return@addSnapshotListener
            }
            val comments = snapshots?.documents?.mapNotNull { doc ->
                val createdAtRaw = doc.get("createdAt")
                val createdAtMillis = when (createdAtRaw) {
                    is Long -> createdAtRaw
                    is com.google.firebase.Timestamp -> createdAtRaw.toDate().time
                    else -> System.currentTimeMillis()
                }
                Comment(
                    id = doc.id,
                    postId = doc.getString("postId") ?: "",
                    authorId = doc.getString("authorId") ?: "",
                    authorName = doc.getString("authorName") ?: "Usuario",
                    authorPhotoUrl = doc.getString("authorPhotoUrl") ?: "",
                    text = doc.getString("text") ?: "",
                    createdAt = createdAtMillis
                )
            }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(Resource.Success(comments))
        }
        awaitClose { listener.remove() }
    }

    override fun addComment(comment: Comment): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val docRef = commentsCollection.document()
            val finalComment = comment.copy(id = docRef.id)
            docRef.set(finalComment).await()
            emit(Resource.Success(finalComment))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    /**
     * Crea una solicitud de adopcion para una publicacion.
     *
     * Obtiene datos de perfil del usuario solicitante para enriquecer la solicitud y dejar
     * el estado inicial como [AdoptionRequestStatus.PENDING].
     */
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
            // Obtener datos del usuario solicitante
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            // Intentar obtener el nombre - puede estar en el campo "name"
            val userName = userDoc.getString("name") ?: ""
            
            // Intentar obtener la foto - puede estar en "photoUrl", "photo", o "avatar"
            val userPhoto = userDoc.getString("photoUrl") 
                ?: userDoc.getString("photo") 
                ?: userDoc.getString("avatar") 
                ?: ""

            val request = mapOf(
                "postId" to postId,
                "requesterId" to userId,
                "requesterName" to userName,
                "requesterPhotoUrl" to userPhoto,
                "message" to message,
                "housingType" to housingType,
                "hasOutdoorSpace" to hasOutdoorSpace,
                "hasExperience" to hasExperience,
                "phone" to phone,
                "contactPreference" to contactPreference,
                "status" to AdoptionRequestStatus.PENDING.name,
                "createdAt" to System.currentTimeMillis()
            )
            adoptionRequestsCollection.add(request).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    /**
     * Lista solicitudes recibidas por el autor de publicaciones.
     *
     * Primero consulta los posts del autor y luego busca solicitudes en bloques de 10 IDs
     * para cumplir la limitacion de `whereIn` en Firestore.
     */
    override fun getAdoptionRequestsForUser(userId: String): Flow<Resource<List<AdoptionRequest>>> = flow {
        emit(Resource.Loading())
        try {
            // Primero obtenemos los posts del usuario para saber cuáles le pertenecen
            val myPosts = postsCollection.whereEqualTo("authorId", userId).get().await()
            val myPostIds = myPosts.documents.map { it.id }
            val postStatusById = myPosts.documents.associate { doc ->
                val status = doc.getString("status")
                doc.id to (status?.let {
                    try { PostStatus.valueOf(it) } catch (_: Exception) { PostStatus.ACTIVE }
                } ?: PostStatus.ACTIVE)
            }

            if (myPostIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Luego buscamos solicitudes para esos posts en lotes de 10 (límite de whereIn)
            val list = mutableListOf<AdoptionRequest>()
            myPostIds.chunked(10).forEach { chunk ->
                val requests = adoptionRequestsCollection
                    .whereIn("postId", chunk)
                    .get().await()
                list += requests.documents.mapNotNull { docToAdoptionRequest(it) }
            }

            // Enriquecer solicitudes con datos del usuario si faltan
            val enrichedList = list.map { request ->
                // Si falta el nombre o la foto, intentar cargar del perfil del usuario
                if (request.requesterName.isBlank() || request.requesterPhotoUrl.isBlank()) {
                    try {
                        val requesterDoc = firestore.collection("users").document(request.requesterId).get().await()
                        request.copy(
                            requesterName = request.requesterName.ifBlank { requesterDoc.getString("name") ?: "" },
                            requesterPhotoUrl = request.requesterPhotoUrl.ifBlank { 
                                requesterDoc.getString("photoUrl") 
                                    ?: requesterDoc.getString("photo") 
                                    ?: requesterDoc.getString("avatar") 
                                    ?: ""
                            },
                            postStatus = postStatusById[request.postId]
                        )
                    } catch (_: Exception) {
                        request.copy(postStatus = postStatusById[request.postId])
                    }
                } else {
                    request.copy(postStatus = postStatusById[request.postId])
                }
            }

            val sortedList = enrichedList.sortedByDescending { it.createdAt }
            emit(Resource.Success(sortedList))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    override fun getAdoptionRequestsForPost(postId: String): Flow<Resource<List<AdoptionRequest>>> = flow {
        emit(Resource.Loading())
        try {
            val requests = adoptionRequestsCollection
                .whereEqualTo("postId", postId)
                .get().await()

            val list = requests.documents.mapNotNull { docToAdoptionRequest(it) }
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Error")))
        }
    }

    /**
     * Acepta una solicitud y marca el post como adoptado en una sola transaccion.
     */
    override fun acceptAdoptionRequest(requestId: String, postId: String): Flow<Resource<Unit>> = flow<Resource<Unit>> {
        emit(Resource.Loading())
        firestore.runTransaction<Unit> { transaction ->
            val requestRef = adoptionRequestsCollection.document(requestId)
            transaction.update(requestRef, "status", AdoptionRequestStatus.ACCEPTED.name)

            val postRef = postsCollection.document(postId)
            transaction.update(postRef, "status", PostStatus.ADOPTED.name)
            Unit
        }.await()

        emit(Resource.Success(Unit))
    }

    /**
     * Rechaza una solicitud de adopcion cambiando su estado a REJECTED.
     */
    override fun rejectAdoptionRequest(requestId: String): Flow<Resource<Unit>> = flow<Resource<Unit>> {
        emit(Resource.Loading())
        adoptionRequestsCollection.document(requestId)
            .update("status", AdoptionRequestStatus.REJECTED.name).await()
        emit(Resource.Success(Unit))
    }

    /**
     * Convierte un documento Firestore en [AdoptionRequest].
     *
     * Retorna `null` si el documento no cumple el formato esperado.
     */
    private fun docToAdoptionRequest(doc: DocumentSnapshot): AdoptionRequest? {
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
                status = AdoptionRequestStatus.valueOf(doc.getString("status") ?: AdoptionRequestStatus.PENDING.name),
                postStatus = null,
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Convierte un documento de Firestore en [Post] normalizado para la capa de dominio.
     */
    private fun snapshotToPost(doc: DocumentSnapshot): Post? {
        return try {
            val createdAtRaw = doc.get("createdAt")
            val createdAtMillis = when (createdAtRaw) {
                is Long -> createdAtRaw
                is com.google.firebase.Timestamp -> createdAtRaw.toDate().time
                else -> System.currentTimeMillis()
            }
            Post(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                category = doc.getString("category")?.let { PostCategory.valueOf(it) } ?: PostCategory.ADOPTION,
                animalType = doc.getString("animalType") ?: "",
                breed = doc.getString("breed") ?: "",
                age = doc.getString("age")?.let { AnimalAge.valueOf(it) } ?: AnimalAge.ADULT,
                gender = doc.getString("gender")?.let { AnimalGender.valueOf(it) } ?: AnimalGender.UNKNOWN,
                size = doc.getString("size")?.let { AnimalSize.valueOf(it) } ?: AnimalSize.MEDIUM,
                status = doc.getString("status")?.let { PostStatus.valueOf(it) } ?: PostStatus.ACTIVE,
                authorId = doc.getString("authorId") ?: "",
                authorName = doc.getString("authorName") ?: "",
                authorPhotoUrl = doc.getString("authorPhotoUrl") ?: "",
                locationName = doc.getString("locationName") ?: "",
                street = doc.getString("street") ?: "",
                neighborhood = doc.getString("neighborhood") ?: "",
                city = doc.getString("city") ?: "",
                latitude = doc.getDouble("latitude") ?: 0.0,
                longitude = doc.getDouble("longitude") ?: 0.0,
                votes = doc.getLong("votes")?.toInt() ?: 0,
                commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                iaMatchPercentage = doc.getLong("iaMatchPercentage")?.toInt(),
                iaSummary = doc.getString("iaSummary"),
                vaccinated = doc.getBoolean("vaccinated") ?: false,
                dewormed = doc.getBoolean("dewormed") ?: false,
                sterilized = doc.getBoolean("sterilized") ?: false,
                specialCares = doc.getBoolean("specialCares") ?: false,
                behavior = (doc.get("behavior") as? List<*>)?.mapNotNull { it?.toString()?.let { b -> try { PetBehavior.valueOf(b) } catch (_: Exception) { null } } } ?: emptyList(),
                rejectionReason = doc.getString("rejectionReason"),
                moderatedBy = doc.getString("moderatedBy"),
                moderatedAt = doc.getLong("moderatedAt"),
                createdAt = createdAtMillis,
                updatedAt = doc.getLong("updatedAt") ?: createdAtMillis
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Serializa [Post] al formato `Map<String, Any?>` que consume Firestore.
     */
    private fun postToMap(post: Post): Map<String, Any?> {
        return mapOf(
            "title" to post.title,
            "description" to post.description,
            "imageUrls" to post.imageUrls,
            "category" to post.category.name,
            "animalType" to post.animalType,
            "breed" to post.breed,
            "age" to post.age.name,
            "gender" to post.gender.name,
            "size" to post.size.name,
            "status" to post.status.name,
            "authorId" to post.authorId,
            "authorName" to post.authorName,
            "authorPhotoUrl" to post.authorPhotoUrl,
            "locationName" to post.locationName,
            "street" to post.street,
            "neighborhood" to post.neighborhood,
            "city" to post.city,
            "latitude" to post.latitude,
            "longitude" to post.longitude,
            "votes" to post.votes,
            "commentsCount" to post.commentsCount,
            "iaMatchPercentage" to post.iaMatchPercentage,
            "iaSummary" to post.iaSummary,
            "vaccinated" to post.vaccinated,
            "dewormed" to post.dewormed,
            "sterilized" to post.sterilized,
            "specialCares" to post.specialCares,
            "behavior" to post.behavior.map { it.name },
            "rejectionReason" to post.rejectionReason,
            "moderatedBy" to post.moderatedBy,
            "moderatedAt" to post.moderatedAt,
            "createdAt" to post.createdAt,
            "updatedAt" to post.updatedAt
        )
    }
}
