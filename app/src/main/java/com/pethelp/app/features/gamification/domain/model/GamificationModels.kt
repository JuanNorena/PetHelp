package com.pethelp.app.features.gamification.domain.model

import kotlinx.serialization.Serializable

/**
 * Acciones que un usuario puede realizar dentro de la app y que el sistema
 * de gamificación reconoce para avanzar misiones o desbloquear insignias.
 *
 * Cada valor representa un evento de negocio que el [GamificationEngine]
 * escucha mediante [GamificationEvent] para actualizar progreso y otorgar puntos.
 */
@Serializable
enum class GamificationAction {
    /** El usuario crea una nueva publicación. */
    CREATE_POST,

    /** El usuario publica un comentario en un post. */
    COMMENT,

    /** El usuario envía una solicitud de adopción. */
    REQUEST_ADOPTION,

    /** El usuario da like o voto a una publicación. */
    VOTE,

    /** El usuario abre la aplicación (usado para calcular racha diaria). */
    OPEN_APP
}

/**
 * Clasificación de una misión según su duración y periodicidad.
 *
 * - [DAILY]: Se refresca cada día; típicamente tiene una ventana de 24 horas.
 * - [ONE_TIME]: Solo puede completarse una vez en la vida del usuario.
 */
@Serializable
enum class MissionType {
    DAILY,
    ONE_TIME
}

/**
 * Representa una misión individual con su progreso y recompensa.
 *
 * Las misiones se generan a partir de [MissionTemplate] del catálogo y se
 * persisten en [GamificationState] para recordar el avance del usuario.
 *
 * @param id Identificador único de la misión (ej. "daily_post", "first_comment").
 * @param title Título legible mostrado en la UI.
 * @param description Descripción corta explicando qué debe hacer el usuario.
 * @param action Acción de gamificación que debe realizar para avanzar.
 * @param targetCount Cantidad de veces que debe ejecutarse la acción para completar.
 * @param currentCount Progreso actual; inicia en 0.
 * @param rewardPoints Puntos otorgados al completar la misión.
 * @param type Si es diaria o de una sola vez.
 * @param completedAt Timestamp de finalización; null mientras no esté completa.
 * @param expiresAtEpochDay Día límite (en epoch day) para completar misiones diarias.
 */
@Serializable
data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val action: GamificationAction,
    val targetCount: Int,
    val currentCount: Int = 0,
    val rewardPoints: Int = 0,
    val type: MissionType = MissionType.DAILY,
    val completedAt: Long? = null,
    val expiresAtEpochDay: Long? = null
) {
    /** Indica si la misión ya alcanzó el progreso requerido. */
    val isCompleted: Boolean
        get() = completedAt != null
}

/**
 * Estadísticas acumulativas de las acciones realizadas por el usuario.
 *
 * Estos contadores se usan para evaluar si el usuario cumple los requisitos
 * de las insignias de tipo [BadgeRequirement.ActionCount].
 *
 * @param postsCreated Número total de publicaciones creadas.
 * @param commentsAdded Número total de comentarios escritos.
 * @param adoptionRequests Número total de solicitudes de adopción enviadas.
 * @param votesGiven Número total de likes/votos otorgados.
 * @param lastActiveEpochDay Último día (en epoch day) en que el usuario abrió la app.
 */
@Serializable
data class GamificationStats(
    val postsCreated: Int = 0,
    val commentsAdded: Int = 0,
    val adoptionRequests: Int = 0,
    val votesGiven: Int = 0,
    val lastActiveEpochDay: Long = 0
)

/**
 * Información sobre la racha diaria de uso de la aplicación.
 *
 * La racha aumenta cuando el usuario abre la app en días consecutivos.
 *
 * @param current Racha actual en días consecutivos.
 * @param best Racha máxima histórica alcanzada por el usuario.
 * @param lastActiveEpochDay Último día de actividad registrado.
 */
@Serializable
data class GamificationStreak(
    val current: Int = 0,
    val best: Int = 0,
    val lastActiveEpochDay: Long = 0
)

/**
 * Representa una insignia que el usuario ha desbloqueado.
 *
 * Solo contiene el ID y el momento de desbloqueo; los metadatos (nombre,
 * descripción, icono) se obtienen desde [GamificationCatalog.badgeDefinitions].
 *
 * @param id Identificador que coincide con una definición del catálogo.
 * @param earnedAt Timestamp en milisegundos cuando se desbloqueó.
 */
@Serializable
data class GamificationBadge(
    val id: String,
    val earnedAt: Long = System.currentTimeMillis()
)

/**
 * Estado completo de gamificación persistido localmente.
 *
 * Este objeto se serializa a JSON y se guarda en DataStore mediante
 * [GamificationLocalDataSource]. Contiene todo el progreso necesario para
 * restaurar la experiencia de gamificación entre sesiones.
 *
 * @param missions Lista de misiones activas con su progreso actual.
 * @param badges Lista de insignias ya desbloqueadas.
 * @param stats Contadores de acciones realizadas.
 * @param streak Datos de la racha diaria.
 * @param lastMissionRefreshEpochDay Último día en que se refrescaron las misiones diarias.
 */
@Serializable
data class GamificationState(
    val missions: List<Mission> = emptyList(),
    val badges: List<GamificationBadge> = emptyList(),
    val stats: GamificationStats = GamificationStats(),
    val streak: GamificationStreak = GamificationStreak(),
    val lastMissionRefreshEpochDay: Long = 0
)

/**
 * Evento que notifica al [GamificationEngine] que ocurrió una acción relevante.
 *
 * Los ViewModels y Repositories emiten estos eventos después de operaciones
 * exitosas (crear post, comentar, votar, etc.) para que el engine actualice
 * misiones, estadísticas y posiblemente otorgue puntos.
 *
 * @param action Tipo de acción realizada.
 * @param count Cuántas unidades de la acción ocurrieron (por defecto 1).
 * @param timestamp Momento en que ocurrió el evento.
 */
@Serializable
data class GamificationEvent(
    val action: GamificationAction,
    val count: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
