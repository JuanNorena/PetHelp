package com.pethelp.app.features.post.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.features.post.domain.repository.PostRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
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
        message: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val requestDoc = mapOf(
                "postId" to postId,
                "requesterId" to userId,
                "message" to message,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis()
            )
            adoptionRequestsCollection.document().set(requestDoc).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error al enviar la solicitud de adopción."))
        }
    }

    // ── Helpers privados ────────────────────────────────────────────────────

    private fun snapshotToPost(doc: com.google.firebase.firestore.DocumentSnapshot): Post? {
        return try {
            Post(
                id = doc.id,
                authorId = doc.getString("authorId") ?: "",
                authorName = doc.getString("authorName") ?: "",
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
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }

    private fun postToMap(post: Post): Map<String, Any?> = mapOf(
        "authorId" to post.authorId,
        "authorName" to post.authorName,
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
        "createdAt" to post.createdAt,
        "updatedAt" to post.updatedAt
    )
}
