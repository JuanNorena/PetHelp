package com.pethelp.app.features.feed.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pethelp.app.R
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.features.auth.presentation.AuthUiState
import com.pethelp.app.features.auth.presentation.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla principal del Feed de publicaciones en PetHelp.
 *
 * **Responsabilidad:**
 * Mostrar una lista cronológica de mascotas en adopción o reportadas, permitiendo al usuario filtrar por
 * categorías y acceder a los detalles de cada publicación. También sirve como punto de entrada al perfil
 * y a las notificaciones.
 *
 * **Arquitectura y Reactividad:**
 * - **ViewModel ([FeedViewModel]):** Gestiona el estado de la lista de publicaciones y los filtros aplicados.
 * - **State Management:** Utiliza `collectAsStateWithLifecycle()` para observar los cambios en el estado
 *   de forma segura según el ciclo de vida de Android.
 * - **Navegación:** Utiliza [NavController] para transitar hacia el perfil, notificaciones o detalles de post.
 *
 * **Flujo de UI (Condicional):**
 * 1. **Cargando:** Muestra un indicador de progreso central si la lista está vacía.
 * 2. **Error:** Muestra un mensaje descriptivo y un botón de reintento.
 * 3. **Vacío:** Muestra una ilustración amigable e informativo si no hay posts que coincidan con el filtro.
 * 4. **Lista (Éxito):** Despliega un `LazyColumn` optimizado con tarjetas interactivas.
 *
 * **Notas para Junior Developers:**
 * - El uso de `Scaffold` permite estructurar la pantalla con barras superiores e inferiores estándar.
 * - Se integra con [AuthViewModel] únicamente para obtener la foto de perfil del usuario en la barra superior.
 * - `LazyRow` y `LazyColumn` se utilizan para listas horizontales y verticales respectivamente, cargando solo
 *   los elementos visibles para ahorrar memoria.
 *
 * @param navController Controlador de navegación para mover al usuario entre pantallas.
 * @param viewModel Instancia del ViewModel encargada de la lógica del Feed (inyectada por Hilt).
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see FeedViewModel Para la lógica de filtrado y carga de datos.
 * @see PetHelpBottomNavBar Componente de navegación global.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel()
) {
    // PASO 1: Observar los estados de los ViewModels.
    // Usamos collectAsStateWithLifecycle para que la recolección se detenga si la app está en segundo plano.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // PASO 2: Estructura base de la pantalla con Scaffold.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // La barra superior incluye el título, acciones y los chips de filtrado.
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.feed_title),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    },
                    actions = {
                        // Icono de notificaciones.
                        IconButton(onClick = { navController.navigate(Screen.Notifications) }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = stringResource(R.string.feed_notifications_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Avatar dinámico del usuario autenticado.
                        FeedProfileAvatar(
                            authState = authState,
                            onClick = { navController.navigate(Screen.Profile) }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )

                // PASO 3: Carrusel horizontal de filtros por categoría.
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        // Opción para limpiar filtros y ver todo.
                        FilterChipUI(
                            label = stringResource(R.string.filter_all),
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) }
                        )
                    }

                    // Genera dinámicamente un chip por cada categoría definida en el Enum.
                    items(PostCategory.entries) { category ->
                        FilterChipUI(
                            label = UiText.fromCategory(category).asString(),
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }

                // Línea divisoria sutil para separar la cabecera del contenido.
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = { PetHelpBottomNavBar(navController) }
    ) { padding ->
        // PASO 4: Manejo reactivo del contenido principal (Máquina de estados).
        when {
            // ESTADO: Cargando datos por primera vez.
            uiState.isLoading && uiState.allPublicPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // ESTADO: Ocurrió un error crítico de red o servidor.
            uiState.error != null && uiState.allPublicPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.error?.asString() ?: "",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Button(onClick = { viewModel.loadPublicPosts() }) {
                            Text(text = stringResource(R.string.common_retry))
                        }
                    }
                }
            }

            // ESTADO: No hay publicaciones disponibles o ninguna coincide con el filtro.
            uiState.filteredPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (uiState.selectedCategory == null) {
                                stringResource(R.string.feed_empty_posts_title)
                            } else {
                                stringResource(R.string.feed_empty_filtered)
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Text(
                            text = stringResource(R.string.feed_empty_posts_subtitle),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ESTADO: Éxito. Mostramos la lista de tarjetas.
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Usamos una clave única (id) para optimizar el rendimiento de las recomposiciones.
                    items(uiState.filteredPosts, key = { it.id }) { post ->
                        FeedPostCard(
                            post = post,
                            onClick = { navController.navigate(Screen.PostDetail(post.id)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente de avatar circular para la barra superior.
 *
 * Muestra la foto del usuario si está autenticado y tiene una URL de imagen,
 * de lo contrario muestra un icono de persona genérico.
 *
 * @param authState Estado actual de la autenticación del usuario.
 * @param onClick Acción a ejecutar al presionar el avatar (normalmente ir al perfil).
 */
@Composable
private fun FeedProfileAvatar(
    authState: AuthUiState,
    onClick: () -> Unit
) {
    // Extraemos la URL de la foto solo si el estado es Authenticated.
    val photoUrl = (authState as? AuthUiState.Authenticated)?.user?.photoUrl.orEmpty()

    Surface(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        if (photoUrl.isNotBlank()) {
            // Imagen remota cargada con Coil.
            AsyncImage(
                model = photoUrl,
                contentDescription = stringResource(R.string.profile_avatar_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder si no hay imagen.
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile_avatar_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta interactiva que representa una publicación en el feed.
 *
 * **Diseño Visual:**
 * - Imagen superior con bordes redondeados.
 * - Información textual clara: Título, Categoría y etiquetas.
 * - Indicador de ubicación y fecha de creación.
 *
 * @param post Objeto con todos los datos de la publicación.
 * @param onClick Acción al presionar la tarjeta para ver el detalle.
 */
@Composable
private fun FeedPostCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column {
            // PASO 1: Sección de imagen de cabecera.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (post.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrls.first(),
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Icono de huella como placeholder si no hay fotos.
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // PASO 2: Cuerpo informativo de la tarjeta.
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = UiText.fromCategory(post.category).asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Etiquetas rápidas de sexo y tamaño.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip(label = UiText.fromGender(post.gender).asString())
                    TagChip(label = UiText.fromSize(post.size).asString())
                }

                // Ubicación (si está disponible).
                if (post.locationName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = post.locationName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Fecha de publicación formateada.
                Text(
                    text = formatDate(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Componente visual para los botones de filtrado (Chips).
 *
 * @param label Texto a mostrar en el chip.
 * @param selected Indica si el filtro está actualmente activo.
 * @param onClick Acción al presionar el chip.
 */
@Composable
private fun FilterChipUI(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

/**
 * Pequeño indicador visual de etiquetas (Tags) para atributos de la mascota.
 *
 * @param label Texto de la etiqueta (ej. "Macho", "Grande").
 */
@Composable
private fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Convierte un timestamp en milisegundos a un formato de fecha legible por humanos.
 *
 * @param timestamp Milisegundos desde la época Unix.
 * @return String formateado como "dd/MM/yyyy HH:mm" o "-" si el valor es inválido.
 */
private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "-"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
