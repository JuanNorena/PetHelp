package com.pethelp.app.features.notifications.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.pethelp.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Pantalla que visualiza el historial de notificaciones del usuario.
 *
 * **Responsabilidad:**
 * Proporcionar un centro de mensajes donde el usuario pueda revisar alertas sobre sus publicaciones,
 * actualizaciones de estado de moderación y otras interacciones relevantes en PetHelp.
 *
 * **Estado Actual (Fase de Desarrollo):**
 * Actualmente se encuentra en una fase inicial de esqueleto (UI básica), mostrando un mensaje
 * predeterminado cuando no hay notificaciones.
 *
 * **Arquitectura y Componentes:**
 * - **Material Design 3:** Utiliza [Scaffold] y [TopAppBar] para mantener la consistencia visual.
 * - **Navegación:** Implementa un botón de retroceso funcional para regresar a la pantalla anterior.
 *
 * **Hoja de Ruta (Próximas Fases):**
 * - **Fase 2:** Implementación de una lista mock usando `PetNotification` para definir los tipos de alerta.
 * - **Fase 3:** Integración con Firestore para carga de datos reales, paginación y marcado de lectura.
 *
 * **Notas para Junior Developers:**
 * - El uso de `Modifier.fillMaxSize()` en combinación con `Box` y `Alignment.Center` es el patrón
 *   estándar en Compose para centrar contenido cuando la pantalla está vacía.
 * - Es fundamental usar `stringResource` para todos los textos, facilitando la futura internacionalización.
 *
 * @param navController Controlador para gestionar el flujo de navegación hacia atrás o hacia detalles.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    // PASO 1: Definición de la estructura base con Scaffold.
    Scaffold(
        topBar = {
            // Barra superior con título y navegación.
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    // Botón para volver a la pantalla anterior.
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        // PASO 2: Contenedor principal del contenido.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Mensaje de estado vacío (Placeholder).
            Text(
                text = stringResource(R.string.notifications_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
