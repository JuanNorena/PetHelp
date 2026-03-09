package com.pethelp.app.features.post.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.Post
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

    /** Crea una nueva publicación en Firestore. */
    fun createPost(post: Post): Flow<Resource<Post>>

    /** Actualiza una publicación existente. */
    fun updatePost(post: Post): Flow<Resource<Post>>

    /** Vota (like) una publicación. Retorna el nuevo conteo de votos. */
    fun votePost(postId: String, userId: String): Flow<Resource<Int>>

    /** Elimina un voto de una publicación. Retorna el nuevo conteo de votos. */
    fun unvotePost(postId: String, userId: String): Flow<Resource<Int>>

    /** Verifica si el usuario ya votó una publicación. */
    fun hasUserVoted(postId: String, userId: String): Flow<Resource<Boolean>>

    /** Obtiene los comentarios de una publicación. */
    fun getComments(postId: String): Flow<Resource<List<Comment>>>

    /** Agrega un comentario a una publicación. */
    fun addComment(comment: Comment): Flow<Resource<Comment>>

    /** Solicita adopción de una publicación. */
    fun requestAdoption(postId: String, userId: String, message: String): Flow<Resource<Unit>>
}
