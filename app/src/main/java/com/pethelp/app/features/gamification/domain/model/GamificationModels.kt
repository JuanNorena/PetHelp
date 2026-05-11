package com.pethelp.app.features.gamification.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamificationAction {
    CREATE_POST,
    COMMENT,
    REQUEST_ADOPTION,
    VOTE,
    OPEN_APP
}

@Serializable
enum class MissionType {
    DAILY,
    ONE_TIME
}

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
    val isCompleted: Boolean
        get() = completedAt != null
}

@Serializable
data class GamificationStats(
    val postsCreated: Int = 0,
    val commentsAdded: Int = 0,
    val adoptionRequests: Int = 0,
    val votesGiven: Int = 0,
    val lastActiveEpochDay: Long = 0
)

@Serializable
data class GamificationStreak(
    val current: Int = 0,
    val best: Int = 0,
    val lastActiveEpochDay: Long = 0
)

@Serializable
data class GamificationBadge(
    val id: String,
    val earnedAt: Long = System.currentTimeMillis()
)

@Serializable
data class GamificationState(
    val missions: List<Mission> = emptyList(),
    val badges: List<GamificationBadge> = emptyList(),
    val stats: GamificationStats = GamificationStats(),
    val streak: GamificationStreak = GamificationStreak(),
    val lastMissionRefreshEpochDay: Long = 0
)

@Serializable
data class GamificationEvent(
    val action: GamificationAction,
    val count: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
