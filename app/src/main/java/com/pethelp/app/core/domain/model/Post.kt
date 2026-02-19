package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio central — Publicación de mascota.
 * Este es el modelo que circula entre capas (domain → presentation).
 * La capa data tiene su propio PostDto/PostEntity que se mapea a este.
 */
data class Post(
    val id: String           = "",
    val authorId: String     = "",
    val authorName: String   = "",
    val title: String        = "",
    val description: String  = "",
    val category: PostCategory = PostCategory.ADOPTION,
    val status: PostStatus   = PostStatus.PENDING,
    val animalType: String   = "",
    val breed: String        = "",
    val size: AnimalSize     = AnimalSize.MEDIUM,
    val vaccinated: Boolean  = false,
    val imageUrls: List<String> = emptyList(),
    val latitude: Double     = 0.0,
    val longitude: Double    = 0.0,
    val locationName: String = "",
    val votes: Int           = 0,
    val commentsCount: Int   = 0,
    val rejectionReason: String? = null,
    val createdAt: Long      = System.currentTimeMillis(),
    val updatedAt: Long      = System.currentTimeMillis()
)

enum class PostCategory(val displayName: String) {
    ADOPTION("Adopción"),
    LOST("Perdidos"),
    FOUND("Encontrados"),
    TEMP_HOME("Hogar temporal"),
    VET_EVENT("Eventos veterinarios")
}

enum class PostStatus(val displayName: String) {
    PENDING("Pendiente de verificación"),
    VERIFIED("Verificada"),
    REJECTED("Rechazada"),
    RESOLVED("Resuelta/Finalizada")
}

enum class AnimalSize(val displayName: String) {
    SMALL("Pequeño"),
    MEDIUM("Mediano"),
    LARGE("Grande")
}
