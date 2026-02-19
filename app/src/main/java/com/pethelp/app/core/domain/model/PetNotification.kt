package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio — Notificación push recibida.
 */
data class PetNotification(
    val id: String           = "",
    val userId: String       = "",
    val type: NotificationType = NotificationType.NEW_POST_NEARBY,
    val title: String        = "",
    val body: String         = "",
    val relatedPostId: String? = null,
    val isRead: Boolean      = false,
    val createdAt: Long      = System.currentTimeMillis()
)

enum class NotificationType {
    NEW_POST_NEARBY,   // nueva publicación en la zona del usuario
    NEW_COMMENT,       // alguien comentó en mi publicación
    POST_APPROVED,     // el moderador aprobó mi publicación
    POST_REJECTED,     // el moderador rechazó mi publicación
    NEW_BADGE,         // obtuve una nueva insignia
    LEVEL_UP           // subí de nivel
}
