/**
 * ViewModel de la pantalla de gamificación.
 *
 * Expone el estado actual de misiones, insignias, rachas y puntos
 * del usuario autenticado. Coordina con [GamificationEngine] para
 * registrar eventos de usuario y refrescar progreso diario.
 */
package com.pethelp.app.features.gamification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.features.gamification.data.local.GamificationLocalDataSource
import com.pethelp.app.features.gamification.domain.GamificationEngine
import com.pethelp.app.features.gamification.domain.model.GamificationBadgeDefinition
import com.pethelp.app.features.gamification.domain.model.GamificationCatalog
import com.pethelp.app.features.gamification.domain.model.GamificationStats
import com.pethelp.app.features.gamification.domain.model.GamificationStreak
import com.pethelp.app.features.gamification.domain.model.Mission
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Representación visual de una insignia en la capa de UI.
 *
 * Combina la definición estática de la insignia (nombre, descripción, puntos)
 * con el estado dinámico de si el usuario ya la desbloqueó y cuándo.
 *
 * @param definition Datos inmutables de la insignia provenientes del catálogo.
 * @param earnedAt Timestamp en milisegundos cuando el usuario obtuvo la insignia;
 *                 null si aún no está desbloqueada.
 */
data class BadgeDisplay(
    val definition: GamificationBadgeDefinition,
    val earnedAt: Long?
) {
    /** Indica si la insignia ya fue desbloqueada por el usuario. */
    val isUnlocked: Boolean
        get() = earnedAt != null
}

/**
 * Estado inmutable que expone la capa de gamificación hacia la UI.
 *
 * Contiene misiones, insignias, estadísticas de acciones y racha diaria.
 * El [GamificationViewModel] transforma el estado interno del engine en
 * este objeto para que los composables de la pantalla de perfil lo consuman.
 *
 * @param missions Lista de misiones diarias y únicas con su progreso actual.
 * @param badges Lista de todas las insignias disponibles y su estado de desbloqueo.
 * @param stats Contadores de acciones realizadas por el usuario (posts, votos, etc.).
 * @param streak Información de la racha diaria de uso de la aplicación.
 */
data class GamificationUiState(
    val missions: List<Mission> = emptyList(),
    val badges: List<BadgeDisplay> = emptyList(),
    val stats: GamificationStats = GamificationStats(),
    val streak: GamificationStreak = GamificationStreak()
)

/**
 * ViewModel que expone el estado de gamificación a la capa de presentación.
 *
 * **Responsabilidad Principal:**
 * - Transformar el estado interno persistido por [GamificationLocalDataSource] en un
 *   [GamificationUiState] observable por los composables.
 * - Delegar eventos de negocio (por ejemplo, abrir la app) al [GamificationEngine].
 *
 * **Arquitectura:**
 * - Recibe `localDataSource` como parámetro de constructor (inyectado por Hilt) para
 *   leer el estado actual sin exponer la capa de datos directamente a la UI.
 * - Expone `uiState` como un [StateFlow] que se actualiza automáticamente cuando
 *   cambian las preferencias locales de gamificación.
 *
 * @param localDataSource Fuente de datos local que persiste el estado de gamificación.
 * @param gamificationEngine Motor que calcula progreso, misiones, insignias y puntos.
 */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    localDataSource: GamificationLocalDataSource,
    private val gamificationEngine: GamificationEngine
) : ViewModel() {

    /**
     * Flujo de estado observable que emite un nuevo [GamificationUiState]
     * cada vez que cambian las preferencias locales de gamificación.
     *
     * **Transformación:**
     * 1. Lee el estado crudo desde `localDataSource.stateFlow`.
     * 2. Mapea cada [GamificationBadge] del usuario a un [BadgeDisplay],
     *    combinándolo con las definiciones estáticas del catálogo para obtener
     *    nombre, descripción e icono incluso si aún no está desbloqueado.
     * 3. Empaqueta misiones, insignias, estadísticas y racha en [GamificationUiState].
     *
     * **Ciclo de Vida:**
     * Usa `SharingStarted.WhileSubscribed(5_000)` para mantener el flujo activo
     * mientras haya suscriptores y liberar recursos 5 segundos después de que
     * la UI deje de observarlo (optimización de memoria).
     */
    val uiState: StateFlow<GamificationUiState> = localDataSource.stateFlow
        .map { state ->
            // Empareja cada definición del catálogo con el progreso del usuario.
            val badges = GamificationCatalog.badgeDefinitions.map { def ->
                val earned = state.badges.firstOrNull { it.id == def.id }
                BadgeDisplay(definition = def, earnedAt = earned?.earnedAt)
            }
            GamificationUiState(
                missions = state.missions,
                badges = badges,
                stats = state.stats,
                streak = state.streak
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GamificationUiState())

    /**
     * Notifica al engine de gamificación que el usuario abrió la aplicación.
     *
     * Esto dispara la lógica de:
     * - Actualización de la racha diaria (streak).
     * - Refresco de misiones diarias si cambió el día.
     * - Otorgamiento de puntos o insignias si se cumplieron condiciones.
     *
     * Se llama típicamente desde [MainActivity] al iniciar la app.
     */
    fun onAppOpen() {
        viewModelScope.launch {
            gamificationEngine.onAppOpen()
        }
    }
}
