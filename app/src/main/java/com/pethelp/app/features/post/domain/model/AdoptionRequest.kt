package com.pethelp.app.features.post.domain.model

import com.pethelp.app.core.domain.model.PostStatus

/**
 * Modelo de dominio para una solicitud de adopcion enviada por un usuario.
 *
 * Esta entidad captura la informacion de contacto y contexto del solicitante para que
 * el autor de una publicacion pueda revisar y decidir si acepta o rechaza la adopcion.
 *
 * @property id Identificador unico de la solicitud en la base de datos.
 * @property postId Identificador de la publicacion objetivo.
 * @property requesterId Identificador del usuario que solicita adoptar.
 * @property requesterName Nombre visible del solicitante.
 * @property requesterPhotoUrl URL de foto de perfil del solicitante.
 * @property message Mensaje principal con la motivacion de adopcion.
 * @property housingType Tipo de vivienda declarada por el solicitante.
 * @property hasOutdoorSpace Respuesta sobre disponibilidad de espacio exterior.
 * @property hasExperience Respuesta sobre experiencia previa con mascotas.
 * @property phone Telefono de contacto proporcionado por el solicitante.
 * @property contactPreference Canal preferido para continuar la conversacion.
 * @property status Estado de revision de la solicitud.
 * @property postStatus Estado actual del post asociado (enriquecido al consultar listados).
 * @property createdAt Marca de tiempo en milisegundos de creacion.
 */
data class AdoptionRequest(
    val id: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val postImageUrl: String = "",
    val postAuthorId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterPhotoUrl: String = "",
    val message: String = "",
    val housingType: String = "",
    val hasOutdoorSpace: String = "",
    val hasExperience: String = "",
    val phone: String = "",
    val contactPreference: String = "",
    val status: AdoptionRequestStatus = AdoptionRequestStatus.PENDING,
    val postStatus: PostStatus? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Estado del ciclo de vida de una solicitud de adopcion.
 */
enum class AdoptionRequestStatus {
    /** Solicitud enviada y pendiente de decision del autor. */
    PENDING,

    /** Solicitud aprobada; normalmente implica marcar el post como adoptado. */
    ACCEPTED,

    /** Solicitud rechazada por el autor de la publicacion. */
    REJECTED
}
