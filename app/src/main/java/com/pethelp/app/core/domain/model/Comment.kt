package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio — Comentario en una publicación.
 */
data class Comment(
    val id: String       = "",
    val postId: String   = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val text: String     = "",
    val createdAt: Long  = System.currentTimeMillis()
)
