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

@Singleton
class AppLanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val preferredLanguageKey = stringPreferencesKey(Constants.DS_KEY_PREFERRED_LANGUAGE)

    val preferredLanguage: Flow<String> = context.languageDataStore.data
        .map { preferences ->
            preferences[preferredLanguageKey] ?: DEFAULT_LANGUAGE
        }
        .distinctUntilChanged()

    suspend fun applySavedLanguage() {
        val storedLanguage = preferredLanguage.first()
        applyLanguage(storedLanguage)
    }

    suspend fun setPreferredLanguage(languageTag: String) {
        val normalizedLanguage = normalizeLanguage(languageTag)

        context.languageDataStore.edit { preferences ->
            preferences[preferredLanguageKey] = normalizedLanguage
        }

        applyLanguage(normalizedLanguage)
    }

    fun applyLanguage(languageTag: String) {
        val normalizedLanguage = normalizeLanguage(languageTag)

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(normalizedLanguage)
        )

        FirebaseAuth.getInstance().setLanguageCode(normalizedLanguage)
    }

    fun normalizeLanguage(languageTag: String): String {
        val normalized = languageTag.trim().lowercase()

        return when {
            normalized.startsWith(LANGUAGE_ENGLISH) -> LANGUAGE_ENGLISH
            normalized.startsWith(LANGUAGE_SPANISH) -> LANGUAGE_SPANISH
            else -> DEFAULT_LANGUAGE
        }
    }

    fun isSupportedLanguage(languageTag: String): Boolean {
        val normalized = languageTag.trim().lowercase()
        return normalized.startsWith(LANGUAGE_ENGLISH) || normalized.startsWith(LANGUAGE_SPANISH)
    }

    companion object {
        const val LANGUAGE_SPANISH = "es"
        const val LANGUAGE_ENGLISH = "en"
        const val DEFAULT_LANGUAGE = LANGUAGE_SPANISH

        val supportedLanguages: Set<String> = setOf(LANGUAGE_SPANISH, LANGUAGE_ENGLISH)
    }
}
