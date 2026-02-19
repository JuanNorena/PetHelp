package com.pethelp.app

import android.app.Application
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
        // Aquí se pueden inicializar SDKs que requieran contexto de Application,
        // por ejemplo: Cloudinary.init(), Timber, etc.
    }
}
