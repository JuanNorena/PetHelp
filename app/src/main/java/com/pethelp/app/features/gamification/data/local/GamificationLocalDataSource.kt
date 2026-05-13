/**
 * Fuente de datos local para persistir el estado de gamificación.
 *
 * Usa DataStore para guardar misiones, insignias, rachas y estadísticas
 * del usuario entre sesiones de la aplicación.
 */
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

/**
 * Extensión que crea una instancia de DataStore nombrada "gamification"
 * vinculada al contexto de la aplicación.
 *
 * DataStore reemplaza a SharedPreferences para almacenamiento tipado y seguro.
 */
private val Context.gamificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gamification"
)

/**
 * Fuente de datos local que persiste el estado de gamificación usando DataStore.
 *
 * **Responsabilidad Principal:**
 * Serializar y deserializar el objeto [GamificationState] a JSON, guardándolo
 * en DataStore bajo la clave `"gamification_state"`. Esto permite que el progreso
 * de misiones, insignias, estadísticas y racha sobreviva a las muertes del proceso.
 *
 * **Formato de Almacenamiento:**
 * Usa kotlinx.serialization para convertir [GamificationState] a una cadena JSON.
 * Si el JSON está corrupto o no existe, devuelve un estado por defecto sin lanzar excepciones.
 *
 * @param context Contexto de aplicación necesario para acceder a DataStore.
 */
@Singleton
class GamificationLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Instancia de Json configurada para serialización de gamificación.
     *
     * - `encodeDefaults = true`: incluye valores por defecto en el JSON para
     *   asegurar compatibilidad futura si se agregan campos nuevos.
     * - `ignoreUnknownKeys = true`: permite deserializar JSON que contenga
     *   campos desconocidos sin fallar, facilitando migraciones de esquema.
     */
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /** Clave de DataStore donde se almacena el estado serializado. */
    private val stateKey = stringPreferencesKey("gamification_state")

    /**
     * Flujo reactivo que emite el estado de gamificación cada vez que cambian
     * las preferencias subyacentes.
     *
     * Los consumidores (como [GamificationViewModel]) pueden observar este flujo
     * para actualizar la UI automáticamente cuando el usuario completa una misión.
     */
    val stateFlow: Flow<GamificationState> = context.gamificationDataStore.data.map { prefs ->
        decodeState(prefs[stateKey])
    }

    /**
     * Lee el estado actual de forma suspendida.
     *
     * Útil cuando se necesita un snapshot puntual sin suscribirse al flujo.
     */
    suspend fun getState(): GamificationState {
        return stateFlow.first()
    }

    /**
     * Persiste un nuevo estado de gamificación en DataStore.
     *
     * @param state El estado completo a serializar y guardar.
     */
    suspend fun saveState(state: GamificationState) {
        val encoded = json.encodeToString(GamificationState.serializer(), state)
        context.gamificationDataStore.edit { prefs ->
            prefs[stateKey] = encoded
        }
    }

    /**
     * Actualiza el estado aplicando una transformación sobre el valor actual.
     *
     * Este patrón de "leer-transformar-escribir" es thread-safe porque
     * DataStore maneja la serialización de escrituras internamente.
     *
     * @param transform Lambda que recibe el estado actual y devuelve el nuevo estado.
     * @return El estado resultante después de aplicar la transformación.
     */
    suspend fun updateState(transform: (GamificationState) -> GamificationState): GamificationState {
        val updated = transform(getState())
        saveState(updated)
        return updated
    }

    /**
     * Deserializa una cadena JSON almacenada en un objeto [GamificationState].
     *
     * Si el raw es null, vacío o corrupto, devuelve un estado por defecto
     * para evitar que la app falle al iniciar.
     *
     * @param raw Cadena JSON leída de DataStore, o null si no existe.
     * @return Estado deserializado o estado por defecto en caso de error.
     */
    private fun decodeState(raw: String?): GamificationState {
        if (raw.isNullOrBlank()) return GamificationState()
        return runCatching {
            json.decodeFromString(GamificationState.serializer(), raw)
        }.getOrDefault(GamificationState())
    }
}
