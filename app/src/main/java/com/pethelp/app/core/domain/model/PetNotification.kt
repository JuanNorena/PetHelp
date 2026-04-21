package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio que representa una notificación recibida por el usuario.
 *
 * Esta clase se usa para almacenar y mostrar notificaciones dentro de la app,
 * tanto si vienen de Firebase Cloud Messaging como si se generan localmente.
 */
data class PetNotification(
    /**
     * Identificador único de la notificación.
     */
    val id: String = "",

    /**
     * Identificador del usuario destinatario de la notificación.
     */
    val userId: String = "",

    /**
     * Tipo de notificación que describe el evento ocurrido.
     */
    val type: NotificationType = NotificationType.NEW_POST_NEARBY,

    /**
     * Título corto que se muestra en la notificación.
     */
    val title: String = "",

    /**
     * Texto principal de la notificación.
     */
    val body: String = "",

    /**
     * Identificador del post relacionado con la notificación, si aplica.
     */
    val relatedPostId: String? = null,

    /**
     * Indica si el usuario ya vio o leyó la notificación.
     */
    val isRead: Boolean = false,

    /**
     * Marca de tiempo en milisegundos cuando se creó la notificación.
     */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tipos de notificaciones que la app puede manejar.
 */
enum class NotificationType {
    /**
     * Hay una nueva publicación en la zona del usuario.
     */
    NEW_POST_NEARBY,

    /**
     * Alguien comentó en una publicación del usuario.
     */
    NEW_COMMENT,

    /**
     * Una publicación del usuario fue aprobada por un moderador.
     */
    POST_APPROVED,

    /**
     * Una publicación del usuario fue rechazada por un moderador.
     */
    POST_REJECTED,

    /**
     * El usuario obtuvo una nueva insignia.
     */
    NEW_BADGE,

    /**
     * El usuario subió de nivel dentro del sistema de reputación.
     */
    LEVEL_UP
}
