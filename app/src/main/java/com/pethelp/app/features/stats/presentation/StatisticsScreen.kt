package com.pethelp.app.features.stats.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Dashboard de estadísticas personales del usuario.
 *
 * TODO (Fase 2): mostrar conteos calculados en memoria.
 *   - Publicaciones activas, finalizadas, pendientes de verificación.
 *
 * TODO (Fase 3): carga desde Firestore con queries agregadas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StatCard(label = "Publicaciones activas",           value = "—")
            Spacer(Modifier.height(12.dp))
            StatCard(label = "Publicaciones finalizadas",       value = "—")
            Spacer(Modifier.height(12.dp))
            StatCard(label = "Pendientes de verificación",      value = "—")
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}
