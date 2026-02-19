package com.pethelp.app.features.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pethelp.app.core.navigation.Screen

// ─── Splash ───────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(navController: NavController) {
    // TODO (Fase 2): verificar sesión activa en Firebase Auth
    //   → autenticado: navegar a Screen.Feed
    //   → no autenticado: navegar a Screen.Login
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🐾", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text("PetHelp", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

// ─── Login ────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(navController: NavController) {
    // TODO (Fase 2): implementar formulario de login
    //   → Firebase Auth email/password
    //   → Detección de rol (USER/MODERATOR) y redirección
    PlaceholderScreen(
        title = "Iniciar sesión",
        onNavigate = { navController.navigate(Screen.Feed.route) }
    )
}

// ─── Register ─────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(navController: NavController) {
    // TODO (Fase 2): implementar formulario de registro
    PlaceholderScreen(
        title = "Crear cuenta",
        onNavigate = { navController.navigate(Screen.Feed.route) }
    )
}

// ─── Forgot Password ──────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    // TODO (Fase 3): Firebase Auth sendPasswordResetEmail
    PlaceholderScreen(
        title = "Recuperar contraseña",
        onNavigate = { navController.popBackStack() }
    )
}

// ─── Helper placeholder ───────────────────────────────────────────────────────
@Composable
private fun PlaceholderScreen(title: String, onNavigate: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onNavigate) {
                Text("Continuar (placeholder)")
            }
        }
    }
}
