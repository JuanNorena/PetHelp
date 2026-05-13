/**
 * Modelo de dominio que representa una notificación recibida por el usuario.
 *
 * Puede provenir de Firebase Cloud Messaging o generarse localmente
 * dentro de la aplicación.
 */
package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio que representa una notificacion recibida por el usuario.
 *
 * Esta clase se usa para almacenar y mostrar notificaciones dentro de la app,
 * tanto si vienen de Firebase Cloud Messaging como si se generan localmente.
 */
data class PetNotification(
    /**
     * Identificador unico de la notificacion.
     */
    val id: String = "",

    /**
     * Identificador del usuario destinatario de la notificacion.
     */
    val userId: String = "",

    /**
     * Tipo de notificacion que describe el evento ocurrido.
     */
    val type: NotificationType = NotificationType.NEW_POST_NEARBY,

    /**
     * Titulo corto que se muestra en la notificacion.
     */
    val title: String = "",

    /**
     * Texto principal de la notificacion.
     */
    val body: String = "",

    /**
     * Identificador del post relacionado con la notificacion, si aplica.
     */
    val relatedPostId: String? = null,

    /**
     * Indica si el usuario ya vio o leyo la notificacion.
     */
    val isRead: Boolean = false,

    /**
     * Marca de tiempo en milisegundos cuando se creo la notificacion.
     */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tipos de notificaciones que la app puede manejar.
 */
enum class NotificationType {
    /**
     * Hay una nueva publicacion en la zona del usuario.
     */
    NEW_POST_NEARBY,

    /**
     * Alguien comento en una publicacion del usuario.
     */
    NEW_COMMENT,

    NEW_MESSAGE,

    /**
     * Una publicacion del usuario fue aprobada por un moderador.
     */
    POST_APPROVED,

    /**
     * Una publicacion del usuario fue rechazada por un moderador.
     */
    POST_REJECTED,

    /**
     * El usuario obtuvo una nueva insignia.
     */
    NEW_BADGE,

    /**
     * El usuario subio de nivel dentro del sistema de reputacion.
     */
    LEVEL_UP,

    /**
     * El autor de una publicacion recibio una nueva solicitud de adopcion.
     */
    ADOPTION_REQUEST_RECEIVED,

    /**
     * La solicitud de adopcion del usuario fue aceptada por el autor del post.
     */
    ADOPTION_REQUEST_ACCEPTED,

    /**
     * La solicitud de adopcion del usuario fue rechazada por el autor del post.
     */
    ADOPTION_REQUEST_REJECTED
}
