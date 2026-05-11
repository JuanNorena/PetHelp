package com.pethelp.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.MapsInitializer
import com.pethelp.app.core.navigation.PetHelpNavGraph
import com.pethelp.app.core.ui.theme.PetHelpTheme
import com.pethelp.app.features.gamification.presentation.GamificationViewModel
import com.pethelp.app.features.profile.presentation.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Única Activity de la aplicación (arquitectura Implement this design from Figma.
 * @https://www.figma.com/design/ycJwGybKvspYBQbnGWmR64/PetHelp?node-id=4-2&m=devSingle-Activity).
 *
 * - @AndroidEntryPoint habilita la inyección de dependencias via Hilt.
 * - Toda la navegación se gestiona dentro del NavGraph de Compose.
 * - enableEdgeToEdge() activa el diseño pantalla completa (Material You).
 */
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar Splash Screen antes de super.onCreate()
        installSplashScreen()

        // Initialize Maps SDK to avoid CameraUpdateFactory NPE
        MapsInitializer.initialize(applicationContext)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val gamificationViewModel: GamificationViewModel = hiltViewModel()
            val isDarkMode by profileViewModel.isDarkMode.collectAsState()
            val language by profileViewModel.language.collectAsState()

            LaunchedEffect(Unit) {
                gamificationViewModel.onAppOpen()
            }

            // Update locale dynamically
            LaunchedEffect(language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                
                // For modern Android (N and above)
                resources.updateConfiguration(config, resources.displayMetrics)
                
                // Trigger an activity recreation to ensure all strings are refreshed
                // if we are not at the initial state.
                // recreate() // Optional: uncomment if you want full restart on change
            }

            PetHelpTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PetHelpNavGraph()
                }
            }
        }
    }
}
