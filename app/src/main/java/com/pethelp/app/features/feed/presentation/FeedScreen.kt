package com.pethelp.app.features.feed.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pethelp.app.core.navigation.Screen

/**
 * Pantalla principal del feed de publicaciones.
 *
 * TODO (Fase 2):
 *   - Implementar lista de tarjetas de publicaciones (datos en memoria).
 *   - Toggle lista / mapa.
 *   - Filtros por categoría y ubicación.
 *
 * TODO (Fase 3):
 *   - Conectar con FeedViewModel → Firestore.
 *   - Vista de mapa con marcadores.
 *   - Filtro por radio geográfico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavController) {
    var isMapView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetHelp 🐾") },
                actions = {
                    // Toggle lista/mapa
                    IconButton(onClick = { isMapView = !isMapView }) {
                        Icon(
                            imageVector = if (isMapView) Icons.Default.List else Icons.Default.Map,
                            contentDescription = if (isMapView) "Vista lista" else "Vista mapa"
                        )
                    }
                    // Notificaciones
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nueva publicación") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { navController.navigate(Screen.CreatePost.route) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isMapView) {
                Text("🗺️ Vista de mapa — Fase 3",
                    style = MaterialTheme.typography.bodyLarge)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋 Feed de publicaciones", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("(datos en memoria — Fase 2 / Firestore — Fase 3)",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
