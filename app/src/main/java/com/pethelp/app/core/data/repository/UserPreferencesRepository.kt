package com.pethelp.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para leer y escribir preferencias de usuario locales.
 *
 * Usa DataStore de Android para almacenar configuraciones simples como modo
 * oscuro y el idioma seleccionado. Al centralizar esta lógica en un repositorio,
 * otros componentes de la app pueden acceder a preferencias de forma segura y
 * sin repetir código.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Clave booleana para el modo oscuro.
     */
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    /**
     * Clave de texto para el idioma preferido.
     */
    private val LANGUAGE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("language")

    /**
     * Flujo que emite el valor actual de la preferencia de modo oscuro.
     *
     * Si no existe un valor guardado, devuelve `false` por defecto.
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    /**
     * Flujo que emite el idioma seleccionado por el usuario.
     *
     * Si no se ha guardado ningún idioma, devuelve `"es"` (español) por defecto.
     */
    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "es"
        }

    /**
     * Cambia la preferencia de modo oscuro.
     *
     * Este método es `suspend` porque escribe en DataStore.
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    /**
     * Cambia el idioma preferido del usuario.
     *
     * @param languageCode código de idioma como "es", "en" u otro soporte.
     */
    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}
