package com.pethelp.app.features.moderation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.domain.model.UserRole
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.theme.*
import com.pethelp.app.features.auth.presentation.AuthUiState
import com.pethelp.app.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente de seguridad que restringe el acceso solo a usuarios con rol de MODERADOR.
 *
 * **Responsabilidad:**
 * Verificar el estado de autenticación y el rol del usuario antes de renderizar el contenido
 * protegido. Si el usuario no cumple los requisitos, lo redirige automáticamente.
 *
 * **Lógica de Redirección:**
 * 1. **No autenticado:** Redirige a la pantalla de Login.
 * 2. **Autenticado pero sin rol de moderador:** Redirige al Feed general.
 * 3. **Moderador:** Renderiza el contenido solicitado ([content]).
 *
 * **Nota para Junior Developers:**
 * El uso de [LaunchedEffect] con `authState` como clave garantiza que la validación se
 * ejecute cada vez que cambie el estado de la sesión, manteniendo la seguridad de la app.
 *
 * @param navController Controlador para gestionar las redirecciones de seguridad.
 * @param content Composable que representa la pantalla protegida a mostrar.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 */
@Composable
private fun ModeratorAccessGate(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (val state = authState) {
            AuthUiState.Unauthenticated -> {
                navController.navigate(Screen.Login) {
                    launchSingleTop = true
                }
            }

            is AuthUiState.Authenticated -> {
                if (state.user.role != UserRole.MODERATOR) {
                    navController.navigate(Screen.Feed) {
                        launchSingleTop = true
                    }
                }
            }

            else -> Unit
        }
    }

    if (authState is AuthUiState.Authenticated &&
        (authState as AuthUiState.Authenticated).user.role == UserRole.MODERATOR
    ) {
        content()
        return
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Pantalla principal del Panel de Moderación (Dashboard).
 *
 * **Responsabilidad:**
 * Ofrecer una visión global de la salud de la plataforma mediante métricas clave (posts pendientes,
 * usuarios totales, reportes activos) y listar las publicaciones que requieren revisión inmediata.
 *
 * **Funcionalidades:**
 * - Resumen de estadísticas diarias y métricas globales.
 * - Lista de publicaciones pendientes de validación.
 * - Gestión de cierre de sesión específico para moderadores.
 * - Refresco manual de datos mediante la barra superior.
 *
 * **Componentes Destacados:**
 * - [ModeratorAccessGate]: Garantiza que solo moderadores vean esta información sensible.
 * - [StatsSummaryRow]: Muestra indicadores rápidos de la gestión del día.
 * - [GlobalMetricsRow]: Muestra contadores totales del sistema.
 *
 * @param navController Navegación entre el dashboard y el detalle de publicaciones.
 * @param viewModel Lógica de negocio para cargar estadísticas y posts pendientes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorPanelScreen(
    navController: NavController,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = hiltViewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // PASO 1: Carga inicial de datos al entrar a la pantalla.
    LaunchedEffect(Unit) {
        viewModel.loadPendingPosts()
    }

    ModeratorAccessGate(navController = navController) {
        // Diálogo de confirmación para cerrar sesión.
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(stringResource(R.string.btn_logout)) },
                text = { Text(stringResource(R.string.moderation_logout_confirmation)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            authViewModel.logout()
                        }
                    ) {
                        Text(stringResource(R.string.btn_logout))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.moderation_dashboard_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadDashboardData() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.common_refresh)
                            )
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = stringResource(R.string.btn_logout)
                            )
                        }
                    }
                )
            },
            bottomBar = {} // Sin navbar en panel de moderación
        ) { padding ->
            // PASO 2: Manejo de estados de carga, error y visualización de datos.
            when {
                uiState.isLoading && uiState.pendingPosts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null && uiState.pendingPosts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error?.asString() ?: stringResource(R.string.moderation_error_load_pending),
                            color = ErrorText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Sección de Resumen Diario.
                        StatsSummaryRow(stats = uiState.stats)
                        
                        // Sección de Métricas Globales.
                        GlobalMetricsRow(stats = uiState.stats)

                        // Lista de Publicaciones que requieren atención.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.moderation_pending_posts_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (uiState.pendingPosts.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.moderation_empty_pending),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                // Mostramos solo los primeros 5 para el dashboard rápido.
                                uiState.pendingPosts.take(5).forEach { post ->
                                    PendingPostCard(
                                        post = post,
                                        onClick = { navController.navigate(Screen.ModeratorDetail(post.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pantalla de detalle de publicación para moderadores.
 *
 * **Responsabilidad:**
 * Permitir al moderador examinar el contenido completo de una publicación y tomar una decisión
 * (Aprobar o Rechazar).
 *
 * **Lógica de Rechazo:**
 * Al rechazar, se solicita obligatoriamente un motivo que será enviado al autor para que
 * pueda corregir la publicación.
 *
 * **Nota para Junior Developers:**
 * Se utiliza un [LaunchedEffect] con `snackbarMessage` para mostrar feedback visual sobre
 * el éxito o fracaso de las acciones de moderación (p. ej., "Publicación aprobada con éxito").
 *
 * @param postId ID de la publicación a moderar.
 * @param navController Navegación para retornar al panel tras completar la acción.
 * @param viewModel Lógica para aprobar/rechazar y cargar el detalle del post.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDetailScreen(
    postId: String,
    navController: NavController,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    // PASO 1: Carga de detalles del post específico.
    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // PASO 2: Observación de mensajes informativos (Snackbars).
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { uiText ->
            snackbarHostState.showSnackbar(uiText.asString(context))
        }
    }

    // PASO 3: Navegación automática hacia atrás cuando se completa una acción con éxito.
    LaunchedEffect(Unit) {
        viewModel.actionCompleted.collectLatest {
            navController.popBackStack()
        }
    }

    ModeratorAccessGate(navController = navController) {
        // Diálogo para capturar el motivo del rechazo.
        if (showRejectDialog) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text(stringResource(R.string.moderation_reject_reason_title)) },
                text = {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text(stringResource(R.string.moderation_reject_reason_label)) },
                        placeholder = { Text(stringResource(R.string.moderation_reject_reason_placeholder)) },
                        supportingText = { Text(stringResource(R.string.error_field_required)) },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.rejectPost(postId = postId, reason = rejectReason)
                            showRejectDialog = false
                            rejectReason = ""
                        },
                        enabled = rejectReason.trim().isNotBlank() && !uiState.isActionLoading
                    ) {
                        Text(stringResource(R.string.moderation_btn_confirm_reject))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.moderation_detail_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            // PASO 4: Renderizado de la información detallada del post.
            when {
                uiState.isLoading && uiState.selectedPost == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null && uiState.selectedPost == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error?.asString() ?: stringResource(R.string.moderation_error_load_detail),
                            color = ErrorText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                uiState.selectedPost != null -> {
                    val post = uiState.selectedPost
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = post?.title.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Metadatos de la publicación.
                        Text(
                            text = stringResource(R.string.moderation_detail_author_label, post?.authorName.orEmpty()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stringResource(R.string.moderation_detail_category_label, UiText.fromCategory(post?.category ?: com.pethelp.app.core.domain.model.PostCategory.ADOPTION).asString()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stringResource(R.string.moderation_detail_published_label, formatDate(post?.createdAt ?: 0L)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        // Descripción del contenido.
                        Text(
                            text = post?.description.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Historial de rechazos previos (si aplica).
                        if (!post?.rejectionReason.isNullOrBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = stringResource(R.string.moderation_previous_rejection_title),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ErrorText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = post?.rejectionReason.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = StatusError
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // PASO 5: Botones de acción del moderador.
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.approvePost(postId) },
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.btn_approve))
                            }

                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.btn_reject))
                            }
                        }

                        // Indicador de guardado en curso.
                        if (uiState.isActionLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.moderation_saving_decision),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.moderation_post_not_found))
                    }
                }
            }
        }
    }
}

/**
 * Fila de tarjetas con estadísticas rápidas sobre la gestión diaria de moderación.
 */
@Composable
private fun StatsSummaryRow(stats: ModerationStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.moderation_today_summary),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = stringResource(R.string.moderation_stat_pending),
                value = stats.pendingCount.toString(),
                containerColor = StatusWarningBg,
                contentColor = StatusWarning,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.moderation_stat_approved),
                value = stats.approvedToday.toString(),
                containerColor = StatusSuccessBg,
                contentColor = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.moderation_stat_rejected),
                value = stats.rejectedToday.toString(),
                containerColor = StatusErrorBg,
                contentColor = StatusError,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.moderation_stat_approval_rate),
                value = stringResource(R.string.moderation_percent_value, stats.approvalRate),
                containerColor = StatusIndigoBg,
                contentColor = StatusIndigo,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Fila de tarjetas con metricas globales de la plataforma.
 */
@Composable
private fun GlobalMetricsRow(stats: ModerationStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.moderation_global_metrics_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                label = stringResource(R.string.moderation_stat_total_users),
                value = stats.totalUsers.toString(),
                icon = Icons.Default.People,
                containerColor = StatusInfoBg,
                contentColor = StatusInfo,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = stringResource(R.string.moderation_stat_total_adoptions),
                value = stats.totalAdoptions.toString(),
                icon = Icons.Default.Pets,
                containerColor = StatusSuccessBg,
                contentColor = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = stringResource(R.string.moderation_stat_active_reports),
                value = stats.activeReports.toString(),
                icon = Icons.Default.Report,
                containerColor = StatusErrorBg,
                contentColor = StatusError,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

/**
 * Tarjeta individual para mostrar un valor estadistico con colores semanticos.
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

/**
 * Tarjeta interactiva para listar publicaciones pendientes en el panel.
 *
 * **Características Visuales:**
 * - Título y autor de la publicación.
 * - [MatchBadge]: Indicador visual de la confianza de la IA en el contenido.
 * - Resumen generado por IA para facilitar una revisión rápida sin entrar al detalle.
 */
@Composable
private fun PendingPostCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderDefault),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.moderation_post_card_author_info,
                            post.authorName.ifBlank { stringResource(R.string.post_detail_unknown_user) },
                            formatDate(post.createdAt)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge de coincidencia de IA.
                MatchBadge(percentage = post.iaMatchPercentage ?: 0)
            }

            // Resumen de IA (opcional).
            if (!post.iaSummary.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.moderation_ia_summary_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = post.iaSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.moderation_detail_category_label, UiText.fromCategory(post.category).asString()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = stringResource(R.string.moderation_view_details),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Etiqueta visual que muestra el porcentaje de confianza de la IA.
 * Utiliza colores semánticos (Verde, Naranja, Rojo) según el nivel de seguridad.
 */
@Composable
private fun MatchBadge(percentage: Int) {
    val color = when {
        percentage >= 80 -> StatusSuccess
        percentage >= 50 -> StatusWarning
        else -> StatusError
    }
    val bgColor = when {
        percentage >= 80 -> StatusSuccessBg
        percentage >= 50 -> StatusWarningBg
        else -> StatusErrorBg
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.moderation_ia_match, percentage),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Badge genérico para mostrar el estado actual de una publicación con estilo circular.
 */
@Composable
private fun StatusBadge(status: PostStatus) {
    val (background, foreground) = when (status) {
        PostStatus.PENDING -> StatusWarningBg to StatusWarning
        PostStatus.VERIFIED -> StatusSuccessBg to StatusSuccess
        PostStatus.REJECTED -> StatusErrorBg to StatusError
        PostStatus.RESOLVED -> StatusIndigoBg to StatusIndigo
        PostStatus.ACTIVE -> StatusInfoBg to StatusInfo
        PostStatus.PAUSED -> StatusNeutralBg to StatusNeutral
        PostStatus.ADOPTED -> StatusSuccessBg to StatusSuccess
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(background, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(foreground, shape = CircleShape)
        )
        Text(
            text = UiText.fromStatus(status).asString(),
            color = foreground,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Convierte un timestamp largo en un formato de fecha y hora legible.
 * @return String formateado como "dd/MM/yyyy HH:mm".
 */
private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "-"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
