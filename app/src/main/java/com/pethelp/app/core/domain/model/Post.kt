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
    val authorPhotoUrl: String = "",
    val title: String        = "",
    val description: String  = "",
    val category: PostCategory = PostCategory.ADOPTION,
    val status: PostStatus   = PostStatus.PENDING,
    val animalType: String   = "",
    val breed: String        = "",
    val age: AnimalAge       = AnimalAge.YOUNG,
    val gender: AnimalGender = AnimalGender.UNKNOWN,
    val size: AnimalSize     = AnimalSize.MEDIUM,
    val vaccinated: Boolean  = false,
    val dewormed: Boolean    = false,
    val sterilized: Boolean  = false,
    val specialCares: Boolean = false,
    val behavior: List<PetBehavior> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val street: String       = "",
    val neighborhood: String = "",
    val city: String         = "",
    val latitude: Double     = 0.0,
    val longitude: Double    = 0.0,
    val locationName: String = "",
    val votes: Int           = 0,
    val commentsCount: Int   = 0,
    val iaMatchPercentage: Int? = null,
    val iaSummary: String? = null,
    val rejectionReason: String? = null,
    val moderatedBy: String? = null,
    val moderatedAt: Long?   = null,
    val createdAt: Long      = System.currentTimeMillis(),
    val updatedAt: Long      = System.currentTimeMillis()
)

enum class PostCategory {
    ADOPTION,
    LOST,
    FOUND,
    TEMP_HOME,
    VET_EVENT
}

enum class PostStatus {
    ACTIVE,
    PAUSED,
    ADOPTED,
    PENDING,
    VERIFIED,
    REJECTED,
    RESOLVED
}

enum class AnimalSize {
    SMALL,
    MEDIUM,
    LARGE
}

enum class AnimalAge {
    PUPPY,
    YOUNG,
    ADULT,
    SENIOR
}

enum class AnimalGender {
    MALE,
    FEMALE,
    UNKNOWN
}

enum class PetBehavior {
    PLAYFUL,
    CALM,
    PROTECTIVE,
    SHY,
    SOCIABLE,
    INDEPENDENT,
    AFFECTIONATE,
    ACTIVE
}
