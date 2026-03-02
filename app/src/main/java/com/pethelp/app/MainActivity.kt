package com.pethelp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pethelp.app.core.navigation.PetHelpNavGraph
import com.pethelp.app.core.ui.theme.PetHelpTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Única Activity de la aplicación (arquitectura Implement this design from Figma.
 * @https://www.figma.com/design/ycJwGybKvspYBQbnGWmR64/PetHelp?node-id=4-2&m=devSingle-Activity).
 *
 * - @AndroidEntryPoint habilita la inyección de dependencias via Hilt.
 * - Toda la navegación se gestiona dentro del NavGraph de Compose.
 * - enableEdgeToEdge() activa el diseño pantalla completa (Material You).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar Splash Screen antes de super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PetHelpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PetHelpNavGraph()
                }
            }
        }
    }
}
