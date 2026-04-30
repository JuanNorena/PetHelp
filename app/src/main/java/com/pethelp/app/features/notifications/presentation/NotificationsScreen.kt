package com.pethelp.app.features.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pethelp.app.core.domain.model.PetNotification
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllAsRead() },
                        enabled = uiState.notifications.any { !it.isRead }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = stringResource(R.string.notifications_mark_all_read)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage?.asString() ?: stringResource(R.string.error_generic),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.notifications_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                if (!notification.relatedPostId.isNullOrBlank()) {
                                    navController.navigate(Screen.PostDetail(notification.relatedPostId!!))
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: PetNotification,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatter.format(Date(notification.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        }
    }
}
