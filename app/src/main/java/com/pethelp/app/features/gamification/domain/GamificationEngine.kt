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

/**
 * Motor central del sistema de gamificación de PetHelp.
 *
 * **Responsabilidad Principal:**
 * - Registrar eventos de usuario (crear post, comentar, votar, etc.) y traducirlos
 *   en progreso de misiones, insignias y rachas.
 * - Gestionar la lógica de misiones diarias y de una sola vez, refrescándolas
 *   automáticamente cuando cambia el día.
 * - Calcular y otorgar puntos cuando se completan misiones o se desbloquean insignias.
 * - Mantener el estado local de gamificación usando [GamificationLocalDataSource].
 * - Sincronizar puntos totales con Firestore a través de [GamificationPointsRepository].
 *
 * **Concurrencia:**
 * Todas las operaciones de escritura usan un [Mutex] para evitar condiciones de carrera
 * cuando múltiples eventos ocurren simultáneamente.
 *
 * @param localDataSource Fuente de datos local (DataStore) para persistir el estado.
 * @param pointsRepository Repositorio remoto para sincronizar puntos con Firestore.
 */
@Singleton
class GamificationEngine @Inject constructor(
    private val localDataSource: GamificationLocalDataSource,
    private val pointsRepository: GamificationPointsRepository
) {
    /** Mutex para proteger operaciones de lectura-escritura del estado de gamificación. */
    private val mutex = Mutex()

    /**
     * Dispara el pipeline de gamificación cuando el usuario abre la app.
     *
     * **Flujo:**
     * 1. Obtiene el día actual en epoch day.
     * 2. Refresca las misiones diarias si cambió el día.
     * 3. Actualiza la racha diaria de uso.
     * 4. Persiste el estado actualizado en DataStore.
     *
     * Se llama típicamente desde [MainActivity] o [GamificationViewModel.onAppOpen].
     */
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

    /**
     * Registra un evento de usuario y actualiza misiones, insignias, estadísticas
     * y racha en un solo pipeline atómico.
     *
     * **Flujo:**
     * 1. Refresca misiones diarias si cambió el día.
     * 2. Actualiza estadísticas según el tipo de acción.
     * 3. Procesa progreso de misiones y otorga puntos si se completan.
     * 4. Evalúa desbloqueo de nuevas insignias.
     * 5. Actualiza la racha diaria.
     * 6. Persiste el estado local y sincroniza puntos con Firestore.
     *
     * @param event Evento de gamificación con acción, conteo y timestamp.
     */
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

    /**
     * Refresca las misiones diarias si el día cambió desde la última vez.
     *
     * Si no ha cambiado el día, retorna el estado sin modificar para evitar
     * reprocesamiento innecesario. Las misiones de una sola vez (ONE_TIME)
     * se conservan y se completan con las nuevas diarias.
     *
     * @param state Estado actual de gamificación.
     * @param today Día actual en epoch day.
     * @return Estado con misiones actualizadas.
     */
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

    /**
     * Construye la lista de misiones diarias rotando sobre el catálogo.
     *
     * Usa el día actual como índice de inicio para que cada día presente
     * un conjunto diferente de misiones, hasta un máximo de 3.
     *
     * @param today Día actual en epoch day.
     * @return Lista de misiones diarias con progreso en cero.
     */
    private fun buildDailyMissions(today: Long): List<Mission> {
        val templates = GamificationCatalog.dailyMissionTemplates
        if (templates.isEmpty()) return emptyList()

        val startIndex = (today % templates.size).toInt()
        val selected = (0 until minOf(3, templates.size))
            .map { templates[(startIndex + it) % templates.size] }

        return selected.map { it.toMission(today) }
    }

    /**
     * Actualiza los contadores de estadísticas según la acción del evento.
     *
     * Suma el conteo del evento al campo correspondiente (posts, comentarios,
     * votos, solicitudes de adopción) y marca el último día activo.
     *
     * @param stats Estadísticas actuales.
     * @param event Evento con la acción y el conteo.
     * @param today Día actual en epoch day.
     * @return Estadísticas actualizadas.
     */
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

    /**
     * Resultado de procesar el progreso de misiones tras un evento.
     *
     * @param missions Lista de misiones con contadores actualizados.
     * @param pointsAwarded Puntos otorgados por misiones completadas en este ciclo.
     */
    private data class MissionUpdateResult(
        val missions: List<Mission>,
        val pointsAwarded: Int
    )

    /**
     * Procesa el progreso de cada misión ante un evento de usuario.
     *
     * Solo afecta misiones que coincidan con la acción del evento y que
     * aún no estén completadas. Si una misión alcanza su objetivo por
     * primera vez, se marca el timestamp y se acumulan sus puntos.
     *
     * @param missions Lista de misiones vigentes.
     * @param event Evento que dispara el progreso.
     * @param today Día actual en epoch day (no usado directamente aquí, pero
     *              se mantiene por consistencia con la firma del pipeline).
     * @return Resultado con misiones actualizadas y puntos otorgados.
     */
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

    /**
     * Resultado de evaluar desbloqueo de insignias.
     *
     * @param badges Lista completa de insignias (existentes + nuevas).
     * @param pointsAwarded Suma de puntos de las insignias recién desbloqueadas.
     */
    private data class BadgeUpdateResult(
        val badges: List<GamificationBadge>,
        val pointsAwarded: Int
    )

    /**
     * Evalúa si se cumplen los requisitos para desbloquear nuevas insignias.
     *
     * Compara las estadísticas actuales y la racha contra los umbrales
     * definidos en [GamificationCatalog.badgeDefinitions]. Las insignias ya
     * desbloqueadas se ignoran para no duplicar puntos.
     *
     * @param currentBadges Insignias que el usuario ya posee.
     * @param stats Estadísticas acumuladas de acciones del usuario.
     * @param streak Racha diaria actual.
     * @return Resultado con insignias actualizadas y puntos de nuevas.
     */
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

    /**
     * Actualiza la racha diaria de uso de la aplicación.
     *
     * Si el usuario abrió la app ayer, incrementa la racha actual.
     * Si saltó al menos un día, reinicia la racha a 1.
     * La mejor racha histórica se actualiza si la actual la supera.
     *
     * @param streak Racha actual.
     * @param today Día actual en epoch day.
     * @return Racha actualizada.
     */
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

    /**
     * Devuelve el día actual en formato epoch day (días desde 1970-01-01).
     *
     * Usa la zona horaria del sistema para consistencia con las rachas
     * y fechas de expiración de misiones diarias.
     *
     * @return Número de días desde epoch.
     */
    private fun currentEpochDay(): Long {
        return LocalDate.now(ZoneId.systemDefault()).toEpochDay()
    }

    /**
     * Convierte una plantilla de misión en una instancia concreta de [Mission].
     *
     * Inicializa el progreso en cero y establece la fecha de expiración
     * solo para misiones diarias (ONE_TIME no expiran).
     *
     * @param today Día actual en epoch day; usado como fecha de expiración
     *              para misiones diarias.
     * @return Instancia de [Mission] lista para ser incluida en el estado.
     */
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
