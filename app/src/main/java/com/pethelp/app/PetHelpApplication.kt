package com.pethelp.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application de PetHelp.
 *
 * @HiltAndroidApp inicializa el grafo de dependencias de Hilt.
 * Esta clase es el punto de entrada de Hilt; sin ella, ninguna
 * inyección de dependencias funcionará.
 */
@HiltAndroidApp
class PetHelpApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ── Firebase Auth: configuración de idioma ──
        // Establece el idioma de los correos de verificación y recuperación
        // de contraseña a español (Colombia).
        FirebaseAuth.getInstance().setLanguageCode("es")

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
}
