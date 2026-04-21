package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio central que representa una publicación de mascota.
 *
 * Este modelo se utiliza en el flujo de la aplicación entre la capa de
 * dominio y la capa de presentación. Los datos pueden venir de Firestore,
 * de una API o de una conversión desde un DTO/Entity.
 */
data class Post(
    /** Identificador único de la publicación. */
    val id: String = "",

    /** Identificador del usuario que creó la publicación. */
    val authorId: String = "",

    /** Nombre para mostrar del autor de la publicación. */
    val authorName: String = "",

    /** URL de la foto de perfil del autor. */
    val authorPhotoUrl: String = "",

    /** Título corto de la publicación. */
    val title: String = "",

    /** Descripción detallada de la publicación. */
    val description: String = "",

    /** Categoría del post (adopción, perdido, encontrado, etc.). */
    val category: PostCategory = PostCategory.ADOPTION,

    /** Estado actual de la publicación dentro del proceso de moderación / ciclo. */
    val status: PostStatus = PostStatus.PENDING,

    /** Tipo de animal involucrado en la publicación. */
    val animalType: String = "",

    /** Raza del animal si está disponible. */
    val breed: String = "",

    /** Edad del animal. */
    val age: AnimalAge = AnimalAge.YOUNG,

    /** Género del animal. */
    val gender: AnimalGender = AnimalGender.UNKNOWN,

    /** Tamaño del animal. */
    val size: AnimalSize = AnimalSize.MEDIUM,

    /** Indica si el animal está vacunado. */
    val vaccinated: Boolean = false,

    /** Indica si el animal está desparasitado. */
    val dewormed: Boolean = false,

    /** Indica si el animal está esterilizado. */
    val sterilized: Boolean = false,

    /** Indica si el animal necesita cuidados especiales. */
    val specialCares: Boolean = false,

    /** Lista de comportamientos que describen al animal. */
    val behavior: List<PetBehavior> = emptyList(),

    /** URLs de las imágenes asociadas a la publicación. */
    val imageUrls: List<String> = emptyList(),

    /** Calle o dirección aproximada donde se ubica el animal o publicación. */
    val street: String = "",

    /** Barrio o zona donde se ubica la publicación. */
    val neighborhood: String = "",

    /** Ciudad donde se ubica la publicación. */
    val city: String = "",

    /** Latitud del lugar asociado a la publicación. */
    val latitude: Double = 0.0,

    /** Longitud del lugar asociado a la publicación. */
    val longitude: Double = 0.0,

    /** Nombre legible de la ubicación (por ejemplo, parque, casa, refugio). */
    val locationName: String = "",

    /** Contador de votos que ha recibido la publicación. */
    val votes: Int = 0,

    /** Cantidad de comentarios que tiene la publicación. */
    val commentsCount: Int = 0,

    /**
     * Porcentaje de coincidencia generado por IA, si aplica.
     * Puede ser nulo si no se generó ninguna sugerencia.
     */
    val iaMatchPercentage: Int? = null,

    /** Resumen generado por IA sobre la publicación, si está disponible. */
    val iaSummary: String? = null,

    /** Razón de rechazo cuando la publicación es marcada como rechazada. */
    val rejectionReason: String? = null,

    /** Identificador del moderador que revisó la publicación, si aplica. */
    val moderatedBy: String? = null,

    /** Fecha de moderación en milisegundos desde Epoch, si se moderó. */
    val moderatedAt: Long? = null,

    /** Fecha de creación de la publicación en milisegundos desde Epoch. */
    val createdAt: Long = System.currentTimeMillis(),

    /** Fecha de última modificación de la publicación. */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Categorías generales que puede tener una publicación.
 */
enum class PostCategory {
    ADOPTION,
    LOST,
    FOUND,
    TEMP_HOME,
    VET_EVENT
}

/**
 * Estados posibles de una publicación dentro del flujo de la aplicación.
 */
enum class PostStatus {
    ACTIVE,
    PAUSED,
    ADOPTED,
    PENDING,
    VERIFIED,
    REJECTED,
    RESOLVED
}

/**
 * Tamaños posibles de un animal.
 */
enum class AnimalSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Rangos de edad que describen al animal.
 */
enum class AnimalAge {
    PUPPY,
    YOUNG,
    ADULT,
    SENIOR
}

/**
 * Género del animal.
 */
enum class AnimalGender {
    MALE,
    FEMALE,
    UNKNOWN
}

/**
 * Comportamientos que pueden aplicarse a la mascota.
 */
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
