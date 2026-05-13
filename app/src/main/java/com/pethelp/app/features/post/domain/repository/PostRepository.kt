/**
 * Interfaz concreta del repositorio de publicaciones.
 * Define las operaciones de lectura, creación, actualización, eliminación
 * y moderación de publicaciones, todas retornando `Flow<Resource<T>>`.
 */
package com.pethelp.app.features.post.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.model.AdoptionRequest
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de publicaciones.
 *
 * Cada método retorna un Flow<Resource<T>> para estados reactivos
 * Loading → Success/Error consumidos por los ViewModels.
 */
interface PostRepository {

    /** Obtiene una publicación por su ID. */
    fun getPostById(postId: String): Flow<Resource<Post>>

    /** Obtiene todas las publicaciones (opcionalmente filtradas por categoría). */
    fun getPosts(category: String? = null): Flow<Resource<List<Post>>>

    /** Obtiene las publicaciones de un usuario específico. */
    fun getPostsByUser(userId: String): Flow<Resource<List<Post>>>

    /** Obtiene las publicaciones favoritas del usuario. */
    fun getFavoritePosts(userId: String): Flow<Resource<List<Post>>>

    /** Crea una nueva publicación en Firestore. */
    fun createPost(post: Post): Flow<Resource<Post>>

    /** Actualiza una publicación existente. */
    fun updatePost(post: Post): Flow<Resource<Post>>

    /** Vota (like) una publicación. Retorna el nuevo conteo de votos. */
    fun votePost(postId: String, userId: String): Flow<Resource<Int>>

    /** Elimina un voto de una publicación. Retorna el nuevo conteo de votos. */
    fun unvotePost(postId: String, userId: String): Flow<Resource<Int>>

    /** Verifica si el usuario ya votó una publicación (favorito). */
    fun hasUserVoted(postId: String, userId: String): Flow<Resource<Boolean>>

    /** Alterna el estado de favorito de una publicación. */
    fun toggleFavorite(postId: String, userId: String, isFavorite: Boolean): Flow<Resource<Int>>

    /** Obtiene los comentarios de una publicación. */
    fun getComments(postId: String): Flow<Resource<List<Comment>>>

    /** Agrega un comentario a una publicación. */
    fun addComment(comment: Comment): Flow<Resource<Comment>>

    /** Solicita adopción de una publicación. */
    fun requestAdoption(
        postId: String,
        userId: String,
        message: String,
        housingType: String,
        hasOutdoorSpace: String,
        hasExperience: String,
        phone: String,
        contactPreference: String
    ): Flow<Resource<Unit>>

    /** Obtiene las solicitudes de adopción recibidas por un usuario (como autor de posts). */
    fun getAdoptionRequestsForUser(userId: String): Flow<Resource<List<AdoptionRequest>>>

    /** Obtiene las solicitudes de adopción enviadas por un usuario solicitante. */
    fun getAdoptionRequestsByRequester(userId: String): Flow<Resource<List<AdoptionRequest>>>

    /** Obtiene la solicitud de adopción existente de un usuario para un post, si existe. */
    fun getAdoptionRequestForUserAndPost(postId: String, userId: String): Flow<Resource<AdoptionRequest?>>

    /** Obtiene las solicitudes de adopción para un post específico. */
    fun getAdoptionRequestsForPost(postId: String): Flow<Resource<List<AdoptionRequest>>>

    /** Acepta una solicitud de adopción y marca el post como adoptado. */
    fun acceptAdoptionRequest(requestId: String, postId: String): Flow<Resource<Unit>>

    /** Rechaza una solicitud de adopción. */
    fun rejectAdoptionRequest(requestId: String): Flow<Resource<Unit>>

    /** Elimina una publicación. */
    fun deletePost(postId: String): Flow<Resource<Unit>>

    /** Pausa o reanuda una publicación. */
    fun togglePostStatus(postId: String, isPaused: Boolean): Flow<Resource<Unit>>

    /** Marca una publicación como resuelta (adoptada/encontrada). */
    fun markAsResolved(postId: String): Flow<Resource<Unit>>

    /** Obtiene publicaciones pendientes de moderación. */
    fun getPendingPosts(): Flow<Resource<List<Post>>>

    /** Aprueba una publicación pendiente y la marca como VERIFIED. */
    fun approvePost(postId: String): Flow<Resource<Unit>>

    /** Rechaza una publicación pendiente y guarda el motivo obligatorio. */
    fun rejectPost(postId: String, reason: String): Flow<Resource<Unit>>

    /** Obtiene las publicaciones moderadas hoy. */
    fun getModeratedPostsToday(): Flow<Resource<List<Post>>>

    /** Obtiene métricas globales para el dashboard de moderación. */
    fun getGlobalMetrics(): Flow<Resource<Map<String, Any>>>
}
