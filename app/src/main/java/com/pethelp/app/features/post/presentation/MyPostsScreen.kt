/**
 * Pantalla de gestión de publicaciones creadas por el usuario actual.
 *
 * Presenta pestañas por estado (Activas, En revisión, Finalizadas),
 * acciones de edición, eliminación, pausa o resolución sobre cada publicación.
 *
 * Usa [MyPostsViewModel] para cargar los posts del usuario autenticado
 * y filtrarlos dinámicamente según la pestaña seleccionada.
 */
package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.pethelp.app.R
import com.pethelp.app.core.common.UiText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpCard
import com.pethelp.app.core.ui.components.PetHelpEmptyState
import com.pethelp.app.core.ui.components.PetHelpShimmerLoading
import com.pethelp.app.core.ui.components.PetHelpStatusBadge
import com.pethelp.app.core.ui.components.PetHelpStatus
import com.pethelp.app.core.ui.components.pethelpFadeScaleIn
import com.pethelp.app.core.ui.components.PETHELP_STAGGER_DELAY
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de gestion de publicaciones creadas por el usuario actual.
 *
 * Presenta pestañas por estado, acciones de edicion/borrado y operaciones de
 * pausa o resolucion sobre cada publicacion.
 */
@Composable
fun MyPostsScreen(
    navController: NavController,
    viewModel: MyPostsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredPosts by viewModel.filteredPosts.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { uiText ->
            snackbarHostState.showSnackbar(uiText.asString(context))
        }
    }

    uiState.postPendingDelete?.let { post ->
        DeleteConfirmationDialog(
            petName = post.title,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MyPostsTopBar(
                onBackClick = { navController.popBackStack() },
                onAddClick  = { navController.navigate(Screen.CreatePost) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreatePost) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.my_posts_create_desc))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MyPostsTabRow(
                selectedTab   = uiState.selectedTab,
                onTabSelected = viewModel::selectTab
            )

            when {
                uiState.isLoading && uiState.allPosts.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        repeat(4) {
                            MyPostShimmerCard()
                        }
                    }
                }
                uiState.error != null && uiState.allPosts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.error!!.asString(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                filteredPosts.isEmpty() -> {
                    val message = when (uiState.selectedTab) {
                        MyPostsTab.ACTIVE    -> stringResource(R.string.my_posts_empty_active)
                        MyPostsTab.IN_REVIEW -> stringResource(R.string.my_posts_empty_in_review)
                        MyPostsTab.RESOLVED  -> stringResource(R.string.my_posts_empty_resolved)
                    }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PetHelpEmptyState(
                            title = stringResource(R.string.my_posts_empty_title),
                            subtitle = message,
                            icon = Icons.Default.Pets
                        )
                    }
                }
                else -> {
                    MyPostsList(
                        posts           = filteredPosts,
                        isDeleting      = uiState.isDeleting,
                        onEditClick     = { navController.navigate(Screen.EditPost(it.id)) },
                        onDeleteClick   = { viewModel.requestDelete(it) },
                        onPauseClick    = { viewModel.pausePost(it.id) },
                        onResolvedClick = { viewModel.markAsResolved(it.id) }
                    )
                }
            }
        }
    }
}

/**
 * Barra superior con título, botón de regreso y acción de crear post.
 *
 * @param onBackClick Acción al pulsar el botón de regreso.
 * @param onAddClick Acción al pulsar el botón de crear publicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPostsTopBar(onBackClick: () -> Unit, onAddClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.my_posts_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.my_posts_create_desc),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Fila de pestañas para filtrar publicaciones por estado (Activas, En revisión, Finalizadas).
 *
 * @param selectedTab Pestaña actualmente seleccionada.
 * @param onTabSelected Callback al cambiar de pestaña.
 */
@Composable
private fun MyPostsTabRow(
    selectedTab: MyPostsTab,
    onTabSelected: (MyPostsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MyPostsTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Lista vertical de publicaciones del usuario con acciones de edición, eliminación, pausa y resolución.
 *
 * @param posts Lista de publicaciones a mostrar.
 * @param isDeleting Indica si una eliminación está en curso.
 * @param onEditClick Acción al editar una publicación.
 * @param onDeleteClick Acción al eliminar una publicación.
 * @param onPauseClick Acción al pausar una publicación.
 * @param onResolvedClick Acción al marcar una publicación como resuelta.
 */
@Composable
private fun MyPostsList(
    posts: List<Post>,
    isDeleting: Boolean,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onPauseClick: (Post) -> Unit,
    onResolvedClick: (Post) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            MyPostCard(
                post            = post,
                isDeleting      = isDeleting,
                onEditClick     = { onEditClick(post) },
                onDeleteClick   = { onDeleteClick(post) },
                onPauseClick    = { onPauseClick(post) },
                onResolvedClick = { onResolvedClick(post) }
            )
        }
    }
}

/**
 * Tarjeta individual de publicación del usuario con imagen, estado y acciones.
 *
 * @param post Datos de la publicación.
 * @param isDeleting Indica si una eliminación está en curso.
 * @param onEditClick Acción al editar.
 * @param onDeleteClick Acción al eliminar.
 * @param onPauseClick Acción al pausar.
 * @param onResolvedClick Acción al marcar como resuelta.
 * @param modifier Modificador para ajustar layout.
 */
@Composable
private fun MyPostCard(
    post: Post,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResolvedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PetHelpCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderAlpha = 0.4f,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column {
            // ── Fila principal: imagen + datos ────────────────────────────────
            Row(verticalAlignment = Alignment.Top) {
                // Miniatura
                if (post.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrls.first(),
                        contentDescription = post.title,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Contenido derecho
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Badge de estado (siempre arriba, por sí solo)
                    PostStatusBadge(post.status)

                    // Nombre del post
                    Text(
                        text = post.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Tipo · Raza
                    val animalInfo = listOfNotNull(
                        post.animalType.takeIf { it.isNotBlank() },
                        post.breed.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    if (animalInfo.isNotBlank()) {
                        Text(
                            text = animalInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Ubicación
                    if (post.locationName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = post.locationName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Fecha
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatPostDate(post.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Motivo de rechazo (solo si REJECTED) ─────────────────────────
            if (post.status == PostStatus.REJECTED && !post.rejectionReason.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                RejectionReasonBox(reason = post.rejectionReason)
            }

            // ── Botones de acción ─────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(2.dp))
            PostActionRow(
                post            = post,
                isDeleting      = isDeleting,
                onEditClick     = onEditClick,
                onDeleteClick   = onDeleteClick,
                onPauseClick    = onPauseClick,
                onResolvedClick = onResolvedClick
            )
        }
    }
}

/**
 * Badge semántico que muestra el estado de una publicación del usuario.
 *
 * @param status Estado actual del post.
 */
@Composable
private fun PostStatusBadge(status: PostStatus) {
    val (petStatus, label) = when (status) {
        PostStatus.VERIFIED, PostStatus.ACTIVE -> PetHelpStatus.SUCCESS to UiText.fromStatus(status).asString()
        PostStatus.PENDING -> PetHelpStatus.WARNING to UiText.fromStatus(status).asString()
        PostStatus.REJECTED -> PetHelpStatus.ERROR to UiText.fromStatus(status).asString()
        PostStatus.RESOLVED, PostStatus.ADOPTED, PostStatus.PAUSED -> PetHelpStatus.INFO to UiText.fromStatus(status).asString()
    }
    PetHelpStatusBadge(label = label, status = petStatus)
}

/**
 * Caja informativa con la razón de rechazo de una publicación.
 *
 * @param reason Texto explicativo del rechazo.
 */
@Composable
private fun RejectionReasonBox(reason: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = reason,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            lineHeight = 16.sp
        )
    }
}

/**
 * Fila de acciones disponibles para una publicación del usuario.
 *
 * @param post Datos de la publicación.
 * @param isDeleting Indica si una eliminación está en curso.
 * @param onEditClick Acción al editar.
 * @param onDeleteClick Acción al eliminar.
 * @param onPauseClick Acción al pausar.
 * @param onResolvedClick Acción al marcar como resuelta.
 */
@Composable
private fun PostActionRow(
    post: Post,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResolvedClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (post.status) {
            PostStatus.VERIFIED, PostStatus.ACTIVE -> {
                ActionIconButton(Icons.Default.Edit, stringResource(R.string.action_edit), onEditClick)
                ActionIconButton(Icons.Default.PauseCircle, stringResource(R.string.action_pause), onPauseClick)
                ActionIconButton(Icons.Default.CheckCircle, stringResource(R.string.action_resolve), onResolvedClick)
                Spacer(Modifier.weight(1f))
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDeleteClick,
                    tint = MaterialTheme.colorScheme.error,
                    enabled = !isDeleting
                )
            }
            PostStatus.PENDING -> {
                ActionIconButton(Icons.Default.Edit, stringResource(R.string.action_edit), onEditClick)
                ActionIconButton(Icons.Default.PauseCircle, stringResource(R.string.action_pause), onPauseClick)
                Spacer(Modifier.weight(1f))
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDeleteClick,
                    tint = MaterialTheme.colorScheme.error,
                    enabled = !isDeleting
                )
            }
            PostStatus.REJECTED -> {
                ActionIconButton(Icons.Default.Edit, stringResource(R.string.action_edit), onEditClick)
                Spacer(Modifier.weight(1f))
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDeleteClick,
                    tint = MaterialTheme.colorScheme.error,
                    enabled = !isDeleting
                )
            }
            PostStatus.PAUSED -> {
                ActionIconButton(Icons.Default.Edit, stringResource(R.string.action_edit), onEditClick)
                ActionIconButton(Icons.Default.PlayCircle, stringResource(R.string.action_resume), onPauseClick)
                Spacer(Modifier.weight(1f))
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDeleteClick,
                    tint = MaterialTheme.colorScheme.error,
                    enabled = !isDeleting
                )
            }
            PostStatus.RESOLVED, PostStatus.ADOPTED -> {
                Spacer(Modifier.weight(1f))
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDeleteClick,
                    tint = MaterialTheme.colorScheme.error,
                    enabled = !isDeleting
                )
            }
        }
    }
}

/**
 * Botón de acción con icono reutilizable para la fila de acciones del post.
 *
 * @param icon Icono vectorial.
 * @param contentDescription Descripción de accesibilidad.
 * @param onClick Acción al pulsar.
 * @param tint Color del icono.
 * @param enabled Indica si el botón está habilitado.
 */
@Composable
private fun ActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Diálogo de confirmación antes de eliminar una publicación.
 *
 * @param petName Nombre de la mascota a eliminar.
 * @param onConfirm Acción al confirmar la eliminación.
 * @param onDismiss Acción al cancelar.
 */
@Composable
private fun DeleteConfirmationDialog(
    petName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(stringResource(R.string.delete_post_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = stringResource(R.string.delete_post_confirmation, petName),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

/**
 * Placeholder shimmer para las tarjetas de publicaciones mientras se cargan.
 */
@Composable
private fun MyPostShimmerCard() {
    PetHelpCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            PetHelpShimmerLoading(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
            }
        }
    }
}

/**
 * Formatea un timestamp a fecha legible "dd MMM, yyyy".
 *
 * @param timestampMillis Timestamp en milisegundos.
 * @return Fecha formateada.
 */
private fun formatPostDate(timestampMillis: Long): String =
    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        .format(Date(timestampMillis))
