package com.pethelp.app.features.gamification.domain.model

/**
 * Plantilla para generar misiones diarias o de una sola vez.
 *
 * Define la acción requerida, la cantidad objetivo y los puntos de recompensa.
 *
 * @param id Identificador único de la misión.
 * @param title Título legible para mostrar en la UI.
 * @param description Descripción de qué debe hacer el usuario.
 * @param action Tipo de acción de gamificación que cuenta para esta misión.
 * @param targetCount Cantidad de veces que debe realizar la acción.
 * @param rewardPoints Puntos otorgados al completar la misión.
 * @param type Tipo de misión: diaria o de una sola vez.
 */
data class MissionTemplate(
    val id: String,
    val title: String,
    val description: String,
    val action: GamificationAction,
    val targetCount: Int,
    val rewardPoints: Int,
    val type: MissionType
)

/**
 * Definición de una insignia desbloqueable en el sistema de gamificación.
 *
 * @param id Identificador único de la insignia.
 * @param name Nombre legible de la insignia.
 * @param description Descripción de cómo se obtiene.
 * @param rewardPoints Puntos otorgados al desbloquear la insignia.
 * @param iconName Nombre del recurso de icono asociado.
 * @param requirement Requisito que debe cumplirse para obtener la insignia.
 */
data class GamificationBadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val rewardPoints: Int,
    val iconName: String,
    val requirement: BadgeRequirement
)

/**
 * Requisitos posibles para desbloquear una insignia.
 */
sealed class BadgeRequirement {
    /** Requiere realizar una acción específica un número de veces. */
    data class ActionCount(val action: GamificationAction, val count: Int) : BadgeRequirement()

    /** Requiere mantener una racha de días consecutivos. */
    data class StreakDays(val days: Int) : BadgeRequirement()
}

/**
 * Catálogo central de misiones e insignias disponibles en PetHelp.
 *
 * Contiene las listas estáticas de [MissionTemplate] y [GamificationBadgeDefinition]
 * que el [GamificationEngine] usa para generar misiones diarias y evaluar
 * el progreso hacia insignias.
 */
object GamificationCatalog {
    val dailyMissionTemplates = listOf(
        MissionTemplate(
            id = "daily_post",
            title = "Publica una mascota",
            description = "Crea una publicacion hoy.",
            action = GamificationAction.CREATE_POST,
            targetCount = 1,
            rewardPoints = 15,
            type = MissionType.DAILY
        ),
        MissionTemplate(
            id = "daily_comment",
            title = "Comparte tu opinion",
            description = "Agrega 2 comentarios.",
            action = GamificationAction.COMMENT,
            targetCount = 2,
            rewardPoints = 10,
            type = MissionType.DAILY
        ),
        MissionTemplate(
            id = "daily_adoption",
            title = "Da un paso",
            description = "Envia una solicitud de adopcion.",
            action = GamificationAction.REQUEST_ADOPTION,
            targetCount = 1,
            rewardPoints = 15,
            type = MissionType.DAILY
        ),
        MissionTemplate(
            id = "daily_like",
            title = "Apoya a otros",
            description = "Da 3 likes.",
            action = GamificationAction.VOTE,
            targetCount = 3,
            rewardPoints = 8,
            type = MissionType.DAILY
        )
    )

    val oneTimeMissions = listOf(
        MissionTemplate(
            id = "first_post",
            title = "Primer aporte",
            description = "Crea tu primera publicacion.",
            action = GamificationAction.CREATE_POST,
            targetCount = 1,
            rewardPoints = 20,
            type = MissionType.ONE_TIME
        ),
        MissionTemplate(
            id = "first_comment",
            title = "Primera voz",
            description = "Publica tu primer comentario.",
            action = GamificationAction.COMMENT,
            targetCount = 1,
            rewardPoints = 10,
            type = MissionType.ONE_TIME
        ),
        MissionTemplate(
            id = "first_adoption",
            title = "Primer paso",
            description = "Solicita tu primera adopcion.",
            action = GamificationAction.REQUEST_ADOPTION,
            targetCount = 1,
            rewardPoints = 12,
            type = MissionType.ONE_TIME
        )
    )

    val badgeDefinitions = listOf(
        GamificationBadgeDefinition(
            id = "badge_first_post",
            name = "Primer aporte",
            description = "Crea tu primera publicacion.",
            rewardPoints = 20,
            iconName = "paw",
            requirement = BadgeRequirement.ActionCount(GamificationAction.CREATE_POST, 1)
        ),
        GamificationBadgeDefinition(
            id = "badge_first_comment",
            name = "Conversador",
            description = "Haz tu primer comentario.",
            rewardPoints = 10,
            iconName = "chat",
            requirement = BadgeRequirement.ActionCount(GamificationAction.COMMENT, 1)
        ),
        GamificationBadgeDefinition(
            id = "badge_helper",
            name = "Ayudante",
            description = "Completa 10 comentarios.",
            rewardPoints = 25,
            iconName = "message",
            requirement = BadgeRequirement.ActionCount(GamificationAction.COMMENT, 10)
        ),
        GamificationBadgeDefinition(
            id = "badge_adopter",
            name = "Nuevo hogar",
            description = "Solicita 3 adopciones.",
            rewardPoints = 30,
            iconName = "heart",
            requirement = BadgeRequirement.ActionCount(GamificationAction.REQUEST_ADOPTION, 3)
        ),
        GamificationBadgeDefinition(
            id = "badge_streak_7",
            name = "Constante",
            description = "Mantiene una racha de 7 dias.",
            rewardPoints = 40,
            iconName = "bolt",
            requirement = BadgeRequirement.StreakDays(7)
        )
    )
}
