package com.pethelp.app.features.gamification.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pethelp.app.features.gamification.domain.model.GamificationState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.gamificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gamification"
)

@Singleton
class GamificationLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val stateKey = stringPreferencesKey("gamification_state")

    val stateFlow: Flow<GamificationState> = context.gamificationDataStore.data.map { prefs ->
        decodeState(prefs[stateKey])
    }

    suspend fun getState(): GamificationState {
        return stateFlow.first()
    }

    suspend fun saveState(state: GamificationState) {
        val encoded = json.encodeToString(GamificationState.serializer(), state)
        context.gamificationDataStore.edit { prefs ->
            prefs[stateKey] = encoded
        }
    }

    suspend fun updateState(transform: (GamificationState) -> GamificationState): GamificationState {
        val updated = transform(getState())
        saveState(updated)
        return updated
    }

    private fun decodeState(raw: String?): GamificationState {
        if (raw.isNullOrBlank()) return GamificationState()
        return runCatching {
            json.decodeFromString(GamificationState.serializer(), raw)
        }.getOrDefault(GamificationState())
    }
}
