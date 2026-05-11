package com.pethelp.app.features.gamification.domain

import com.pethelp.app.features.gamification.data.local.GamificationLocalDataSource
import com.pethelp.app.features.gamification.data.remote.GamificationPointsRepository
import com.pethelp.app.features.gamification.domain.model.BadgeRequirement
import com.pethelp.app.features.gamification.domain.model.GamificationAction
import com.pethelp.app.features.gamification.domain.model.GamificationBadge
import com.pethelp.app.features.gamification.domain.model.GamificationCatalog
import com.pethelp.app.features.gamification.domain.model.GamificationEvent
import com.pethelp.app.features.gamification.domain.model.GamificationState
import com.pethelp.app.features.gamification.domain.model.GamificationStats
import com.pethelp.app.features.gamification.domain.model.GamificationStreak
import com.pethelp.app.features.gamification.domain.model.Mission
import com.pethelp.app.features.gamification.domain.model.MissionTemplate
import com.pethelp.app.features.gamification.domain.model.MissionType
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class GamificationEngine @Inject constructor(
    private val localDataSource: GamificationLocalDataSource,
    private val pointsRepository: GamificationPointsRepository
) {
    private val mutex = Mutex()

    suspend fun onAppOpen() {
        mutex.withLock {
            val today = currentEpochDay()
            val state = localDataSource.getState()
            val refreshed = refreshDailyMissionsIfNeeded(state, today)
            val updatedStreak = updateStreak(refreshed.streak, today)
            val updatedState = refreshed.copy(
                streak = updatedStreak,
                stats = refreshed.stats.copy(lastActiveEpochDay = today)
            )
            localDataSource.saveState(updatedState)
        }
    }

    suspend fun trackEvent(event: GamificationEvent) {
        mutex.withLock {
            val today = currentEpochDay()
            val state = localDataSource.getState()
            val refreshed = refreshDailyMissionsIfNeeded(state, today)
            val updatedStats = updateStats(refreshed.stats, event, today)
            val updatedStreak = updateStreak(refreshed.streak, today)
            val missionResult = updateMissions(refreshed.missions, event, today)
            val updatedMissions = missionResult.missions
            val badgesResult = updateBadges(
                refreshed.badges,
                updatedStats,
                updatedStreak
            )

            val updatedState = refreshed.copy(
                stats = updatedStats,
                streak = updatedStreak,
                missions = updatedMissions,
                badges = badgesResult.badges
            )

            localDataSource.saveState(updatedState)

            val totalPoints = missionResult.pointsAwarded + badgesResult.pointsAwarded
            if (totalPoints > 0) {
                pointsRepository.addPoints(totalPoints)
            }
        }
    }

    private fun refreshDailyMissionsIfNeeded(
        state: GamificationState,
        today: Long
    ): GamificationState {
        if (state.lastMissionRefreshEpochDay == today) return state

        val daily = buildDailyMissions(today)
        val oneTime = state.missions.filter { it.type == MissionType.ONE_TIME }
            .ifEmpty { GamificationCatalog.oneTimeMissions.map { it.toMission(today) } }

        return state.copy(
            missions = oneTime + daily,
            lastMissionRefreshEpochDay = today
        )
    }

    private fun buildDailyMissions(today: Long): List<Mission> {
        val templates = GamificationCatalog.dailyMissionTemplates
        if (templates.isEmpty()) return emptyList()

        val startIndex = (today % templates.size).toInt()
        val selected = (0 until minOf(3, templates.size))
            .map { templates[(startIndex + it) % templates.size] }

        return selected.map { it.toMission(today) }
    }

    private fun updateStats(
        stats: GamificationStats,
        event: GamificationEvent,
        today: Long
    ): GamificationStats {
        return when (event.action) {
            GamificationAction.CREATE_POST -> stats.copy(
                postsCreated = stats.postsCreated + event.count,
                lastActiveEpochDay = today
            )
            GamificationAction.COMMENT -> stats.copy(
                commentsAdded = stats.commentsAdded + event.count,
                lastActiveEpochDay = today
            )
            GamificationAction.REQUEST_ADOPTION -> stats.copy(
                adoptionRequests = stats.adoptionRequests + event.count,
                lastActiveEpochDay = today
            )
            GamificationAction.VOTE -> stats.copy(
                votesGiven = stats.votesGiven + event.count,
                lastActiveEpochDay = today
            )
            GamificationAction.OPEN_APP -> stats.copy(lastActiveEpochDay = today)
        }
    }

    private data class MissionUpdateResult(
        val missions: List<Mission>,
        val pointsAwarded: Int
    )

    private fun updateMissions(
        missions: List<Mission>,
        event: GamificationEvent,
        today: Long
    ): MissionUpdateResult {
        var pointsAwarded = 0
        val updated = missions.map { mission ->
            if (mission.isCompleted) return@map mission
            if (mission.action != event.action) return@map mission

            val newCount = mission.currentCount + event.count
            val completedAt = if (newCount >= mission.targetCount) {
                mission.completedAt ?: event.timestamp
            } else {
                null
            }

            if (completedAt != null && mission.completedAt == null) {
                pointsAwarded += mission.rewardPoints
            }

            mission.copy(
                currentCount = newCount.coerceAtMost(mission.targetCount),
                completedAt = completedAt
            )
        }

        return MissionUpdateResult(updated, pointsAwarded)
    }

    private data class BadgeUpdateResult(
        val badges: List<GamificationBadge>,
        val pointsAwarded: Int
    )

    private fun updateBadges(
        currentBadges: List<GamificationBadge>,
        stats: GamificationStats,
        streak: GamificationStreak
    ): BadgeUpdateResult {
        val unlockedIds = currentBadges.map { it.id }.toSet()
        val newlyUnlocked = GamificationCatalog.badgeDefinitions.filter { def ->
            if (unlockedIds.contains(def.id)) return@filter false
            when (val requirement = def.requirement) {
                is BadgeRequirement.ActionCount -> when (requirement.action) {
                    GamificationAction.CREATE_POST -> stats.postsCreated >= requirement.count
                    GamificationAction.COMMENT -> stats.commentsAdded >= requirement.count
                    GamificationAction.REQUEST_ADOPTION -> stats.adoptionRequests >= requirement.count
                    GamificationAction.VOTE -> stats.votesGiven >= requirement.count
                    GamificationAction.OPEN_APP -> false
                }
                is BadgeRequirement.StreakDays -> streak.current >= requirement.days
            }
        }

        if (newlyUnlocked.isEmpty()) {
            return BadgeUpdateResult(currentBadges, 0)
        }

        val updatedBadges = currentBadges + newlyUnlocked.map {
            GamificationBadge(id = it.id)
        }
        val pointsAwarded = newlyUnlocked.sumOf { it.rewardPoints }
        return BadgeUpdateResult(updatedBadges, pointsAwarded)
    }

    private fun updateStreak(streak: GamificationStreak, today: Long): GamificationStreak {
        if (streak.lastActiveEpochDay == today) return streak

        val nextCurrent = if (streak.lastActiveEpochDay == today - 1) {
            maxOf(1, streak.current + 1)
        } else {
            1
        }
        val best = maxOf(streak.best, nextCurrent)
        return streak.copy(
            current = nextCurrent,
            best = best,
            lastActiveEpochDay = today
        )
    }

    private fun currentEpochDay(): Long {
        return LocalDate.now(ZoneId.systemDefault()).toEpochDay()
    }

    private fun MissionTemplate.toMission(
        today: Long
    ): Mission {
        return Mission(
            id = id,
            title = title,
            description = description,
            action = action,
            targetCount = targetCount,
            currentCount = 0,
            rewardPoints = rewardPoints,
            type = type,
            completedAt = null,
            expiresAtEpochDay = if (type == MissionType.DAILY) today else null
        )
    }
}
