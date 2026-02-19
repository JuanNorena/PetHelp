package com.pethelp.app.features.notifications.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Lista de notificaciones del usuario.
 *
 * TODO (Fase 2): lista mock de PetNotification con distintos tipos.
 * TODO (Fase 3): carga desde Firestore con paginación.
 *   - Marcar como leída al tocar.
 *   - Navegar a la publicación relacionada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Lista de notificaciones (Fase 2)",
                style = MaterialTheme.typography.bodyLarge)
        }
    }
}
