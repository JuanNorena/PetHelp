package com.pethelp.app.features.moderation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ─── Panel de moderación ──────────────────────────────────────────────────────
/**
 * TODO (Fase 2): lista de publicaciones con estado PENDING.
 *   - Cada ítem: título, categoría, autor, fecha.
 *   - Navegar al detalle de moderación al tocar.
 *
 * TODO (Fase 3): carga desde Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorPanelScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Panel de Moderación") }) }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Lista de publicaciones pendientes (Fase 2)",
                style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ─── Detalle de moderación ────────────────────────────────────────────────────
/**
 * TODO (Fase 2): mostrar publicación completa + botones Aprobar/Rechazar.
 *   - Rechazar: campo obligatorio para ingresar motivo.
 *
 * TODO (Fase 3): actualizar estado en Firestore + notificar al autor.
 * TODO (Fase 3): mostrar resumen generado por LLM (si se elige EP-11 opción 4).
 */
@Composable
fun ModeratorDetailScreen(postId: String, navController: NavController) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Moderación de publicación", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("ID: $postId", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.popBackStack() }) { Text("Aprobar") }
                OutlinedButton(onClick = { navController.popBackStack() }) { Text("Rechazar") }
            }
        }
    }
}
