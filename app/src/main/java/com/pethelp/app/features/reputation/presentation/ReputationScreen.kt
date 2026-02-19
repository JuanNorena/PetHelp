package com.pethelp.app.features.reputation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Pantalla de reputación y logros del usuario.
 *
 * TODO (Fase 2): mostrar puntos, nivel actual, progreso al siguiente nivel e insignias.
 *   - Datos en memoria / mock.
 *   - Mostrar los 4 niveles: Amigo Animal, Protector, Guardián, Héroe de las Mascotas.
 *
 * TODO (Fase 3): carga desde Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Reputación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text("⭐ Puntos totales: —", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Nivel: Amigo Animal", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Insignias obtenidas:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("(ninguna aún — Fase 2)",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
