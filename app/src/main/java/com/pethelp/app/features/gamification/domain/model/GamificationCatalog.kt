package com.pethelp.app.features.gamification.domain.model

data class MissionTemplate(
    val id: String,
    val title: String,
    val description: String,
    val action: GamificationAction,
    val targetCount: Int,
    val rewardPoints: Int,
    val type: MissionType
)

data class GamificationBadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val rewardPoints: Int,
    val iconName: String,
    val requirement: BadgeRequirement
)

sealed class BadgeRequirement {
    data class ActionCount(val action: GamificationAction, val count: Int) : BadgeRequirement()
    data class StreakDays(val days: Int) : BadgeRequirement()
}

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
