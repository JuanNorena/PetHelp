package com.pethelp.app.core.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.pethelp.app.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "pethelp_preferences")

/**
 * Administrador de idioma de la aplicación.
 *
 * Esta clase centraliza la lectura, almacenamiento y aplicación del idioma
 * preferido del usuario. Usa DataStore para persistir la preferencia y
 * AppCompatDelegate para actualizar la configuración de localización de la app.
 * También sincroniza el idioma con Firebase Auth para que los mensajes de
 * autenticación aparezcan en el mismo idioma.
 */
@Singleton
class AppLanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val preferredLanguageKey = stringPreferencesKey(Constants.DS_KEY_PREFERRED_LANGUAGE)

    /**
     * Flujo que emite el idioma preferido actual del usuario.
     *
     * Si no hay ninguna preferencia guardada, utiliza `DEFAULT_LANGUAGE`.
     */
    val preferredLanguage: Flow<String> = context.languageDataStore.data
        .map { preferences ->
            preferences[preferredLanguageKey] ?: DEFAULT_LANGUAGE
        }
        .distinctUntilChanged()

    /**
     * Aplica el idioma guardado en DataStore.
     *
     * Esto se usa típicamente al iniciar la aplicación para restaurar el idioma
     * seleccionado por el usuario en sesiones anteriores.
     */
    suspend fun applySavedLanguage() {
        val storedLanguage = preferredLanguage.first()
        applyLanguage(storedLanguage)
    }

    /**
     * Guarda el idioma preferido en DataStore y lo aplica inmediatamente.
     *
     * @param languageTag etiqueta de idioma recibida, por ejemplo "es" o "en-US".
     */
    suspend fun setPreferredLanguage(languageTag: String) {
        val normalizedLanguage = normalizeLanguage(languageTag)

        context.languageDataStore.edit { preferences ->
            preferences[preferredLanguageKey] = normalizedLanguage
        }

        applyLanguage(normalizedLanguage)
    }

    /**
     * Aplica el idioma en toda la aplicación.
     *
     * Cambia la configuración local de Compose/Android y también actualiza el
     * idioma usado por Firebase Auth.
     */
    fun applyLanguage(languageTag: String) {
        val normalizedLanguage = normalizeLanguage(languageTag)

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(normalizedLanguage)
        )

        FirebaseAuth.getInstance().setLanguageCode(normalizedLanguage)
    }

    /**
     * Normaliza una etiqueta de idioma para devolver solo los idiomas soportados.
     *
     * Por ejemplo, "es-MX" se convierte en "es" y "en-US" se convierte en "en".
     */
    fun normalizeLanguage(languageTag: String): String {
        val normalized = languageTag.trim().lowercase()

        return when {
            normalized.startsWith(LANGUAGE_ENGLISH) -> LANGUAGE_ENGLISH
            normalized.startsWith(LANGUAGE_SPANISH) -> LANGUAGE_SPANISH
            else -> DEFAULT_LANGUAGE
        }
    }

    /**
     * Verifica si la etiqueta de idioma es compatible con la aplicación.
     */
    fun isSupportedLanguage(languageTag: String): Boolean {
        val normalized = languageTag.trim().lowercase()
        return normalized.startsWith(LANGUAGE_ENGLISH) || normalized.startsWith(LANGUAGE_SPANISH)
    }

    companion object {
        const val LANGUAGE_SPANISH = "es"
        const val LANGUAGE_ENGLISH = "en"
        const val DEFAULT_LANGUAGE = LANGUAGE_SPANISH

        /**
         * Lista de idiomas que la aplicación soporta actualmente.
         */
        val supportedLanguages: Set<String> = setOf(LANGUAGE_SPANISH, LANGUAGE_ENGLISH)
    }
}
