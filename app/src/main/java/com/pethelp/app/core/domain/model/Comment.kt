/**
 * Modelo de dominio que representa un comentario en una publicación.
 *
 * Contiene texto, autor, timestamp y referencia al post comentado.
 */
package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio que representa un comentario de un usuario en una publicación.
 *
 * Este objeto se usa en la lógica de la app y en la UI para renderizar
 * comentarios junto al post correspondiente.
 */
data class Comment(
    /**
     * Identificador único del comentario.
     * Normalmente proviene de Firestore o del backend.
     */
    val id: String = "",

    /**
     * Identificador de la publicación a la que pertenece el comentario.
     */
    val postId: String = "",

    /**
     * Identificador del autor del comentario.
     */
    val authorId: String = "",

    /**
     * Nombre para mostrar del autor.
     */
    val authorName: String = "",

    /**
     * URL de la foto de perfil del autor.
     * Puede estar vacía si el usuario no tiene avatar.
     */
    val authorPhotoUrl: String = "",

    /**
     * Texto del comentario.
     */
    val text: String = "",

    /**
     * Fecha de creación del comentario en milisegundos desde Epoch.
     */
    val createdAt: Long = System.currentTimeMillis()
)
