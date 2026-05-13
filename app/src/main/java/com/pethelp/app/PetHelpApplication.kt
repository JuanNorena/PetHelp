/**
 * Clase Application de PetHelp.
 *
 * @HiltAndroidApp inicializa el grafo de dependencias de Hilt.
 * También configura Firebase App Check con Play Integrity y
 * aplica el idioma preferido del usuario al inicio de la app.
 */
package com.pethelp.app

import android.app.Application
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.pethelp.app.core.preferences.AppLanguageManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase Application de PetHelp.
 *
 * @HiltAndroidApp inicializa el grafo de dependencias de Hilt.
 * Esta clase es el punto de entrada de Hilt; sin ella, ninguna
 * inyección de dependencias funcionará.
 */
@HiltAndroidApp
class PetHelpApplication : Application() {

    @Inject
    lateinit var appLanguageManager: AppLanguageManager

    override fun onCreate() {
        super.onCreate()

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            getAppCheckProviderFactory()
        )

        // Aplica el idioma guardado antes de levantar pantallas.
        CoroutineScope(Dispatchers.Main.immediate).launch {
            appLanguageManager.applySavedLanguage()
        }

        // ── Firebase Auth: deshabilitar verificación de app (reCAPTCHA) ──
        // En modo debug se desactiva la verificación reCAPTCHA Enterprise
        // para evitar el error CONFIGURATION_NOT_FOUND. En producción esta
        // línea no se ejecuta — se debe configurar reCAPTCHA Enterprise
        // en Google Cloud Console si se desea protección anti-bot.
        if (BuildConfig.DEBUG) {
            FirebaseAuth.getInstance()
                .firebaseAuthSettings
                .setAppVerificationDisabledForTesting(true)
        }
    }

    private fun getAppCheckProviderFactory(): AppCheckProviderFactory {
        if (!BuildConfig.DEBUG) {
            return PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        return Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
            .getMethod("getInstance")
            .invoke(null) as AppCheckProviderFactory
    }
}
