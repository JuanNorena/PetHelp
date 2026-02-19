package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pethelp.app.core.navigation.Screen

// ─── Perfil ───────────────────────────────────────────────────────────────────
/**
 * TODO (Fase 2): mostrar datos del usuario + estadísticas resumidas + nivel + badges.
 *   - Botones: Editar perfil, Ver estadísticas, Ver reputación, Cerrar sesión, Eliminar cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Perfil") }) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Perfil del usuario", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { navController.navigate(Screen.EditProfile.route) }) {
                Text("Editar perfil")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Screen.Statistics.route) }) {
                Text("Mis estadísticas")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Screen.Reputation.route) }) {
                Text("Mi reputación")
            }
        }
    }
}

// ─── Editar perfil ────────────────────────────────────────────────────────────
/**
 * TODO (Fase 2): formulario para editar nombre, foto, radio de notificaciones.
 * TODO (Fase 3): persistir en Firestore + Cloudinary para foto.
 */
@Composable
fun EditProfileScreen(navController: NavController) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Editar perfil", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Guardar (placeholder)") }
        }
    }
}
