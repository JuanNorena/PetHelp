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

data class BadgeDisplay(
    val definition: GamificationBadgeDefinition,
    val earnedAt: Long?
) {
    val isUnlocked: Boolean
        get() = earnedAt != null
}

data class GamificationUiState(
    val missions: List<Mission> = emptyList(),
    val badges: List<BadgeDisplay> = emptyList(),
    val stats: GamificationStats = GamificationStats(),
    val streak: GamificationStreak = GamificationStreak()
)

@HiltViewModel
class GamificationViewModel @Inject constructor(
    localDataSource: GamificationLocalDataSource,
    private val gamificationEngine: GamificationEngine
) : ViewModel() {

    val uiState: StateFlow<GamificationUiState> = localDataSource.stateFlow
        .map { state ->
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

    fun onAppOpen() {
        viewModelScope.launch {
            gamificationEngine.onAppOpen()
        }
    }
}
