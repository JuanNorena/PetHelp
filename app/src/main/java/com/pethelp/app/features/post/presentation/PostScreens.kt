package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ─── Detalle de publicación ───────────────────────────────────────────────────
/**
 * TODO (Fase 2): mostrar todos los campos de la publicación.
 *   - Campos: título, descripción, categoría, tipo animal, raza, tamaño, vacunación.
 *   - Galería de imágenes.
 *   - Mini-mapa de ubicación.
 *   - Lista de comentarios.
 *   - Botones: "Me interesa", "Me interesa adoptar" (si categoría = Adopción).
 *
 * TODO (Fase 3): datos desde Firestore.
 */
@Composable
fun PostDetailScreen(postId: String, navController: NavController) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Detalle de publicación", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("ID: $postId", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}

// ─── Crear publicación ────────────────────────────────────────────────────────
/**
 * TODO (Fase 2): formulario completo con todos los campos de Post.
 *   - Selector de categoría, tipo animal, raza, tamaño, vacunación.
 *   - Picker de ubicación en mapa.
 *   - Selector/cámara de imágenes.
 *
 * TODO (Fase 3): subir imágenes a Cloudinary + guardar en Firestore.
 * TODO (Fase 3): llamar al LLM para clasificación automática (EP-11).
 */
@Composable
fun CreatePostScreen(navController: NavController) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crear publicación", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Guardar (placeholder)") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { navController.popBackStack() }) { Text("Cancelar") }
        }
    }
}

// ─── Editar publicación ───────────────────────────────────────────────────────
/**
 * TODO (Fase 2): igual que crear, pero precargado con los datos actuales.
 */
@Composable
fun EditPostScreen(postId: String, navController: NavController) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Editar publicación", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("ID: $postId", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Guardar (placeholder)") }
        }
    }
}
