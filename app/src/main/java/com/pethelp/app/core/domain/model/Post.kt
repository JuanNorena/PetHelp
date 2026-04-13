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
    val rejectionReason: String? = null,
    val moderatedBy: String? = null,
    val moderatedAt: Long?   = null,
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

enum class AnimalAge(val displayName: String) {
    PUPPY("Cachorro"),
    YOUNG("Joven"),
    ADULT("Adulto"),
    SENIOR("Senior")
}

enum class AnimalGender(val displayName: String) {
    MALE("Macho"),
    FEMALE("Hembra"),
    UNKNOWN("No lo sé")
}

enum class PetBehavior(val displayName: String) {
    PLAYFUL("Juguetón"),
    CALM("Tranquilo"),
    PROTECTIVE("Protector"),
    SHY("Tímido"),
    SOCIABLE("Sociable"),
    INDEPENDENT("Independiente"),
    AFFECTIONATE("Cariñoso"),
    ACTIVE("Activo")
}
