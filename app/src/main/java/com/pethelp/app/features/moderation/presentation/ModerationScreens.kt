package com.pethelp.app.features.moderation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.pethelp.app.core.ui.theme.*
import com.pethelp.app.features.ai.domain.repository.ModerationAiAnalysis
import com.pethelp.app.features.ai.domain.repository.ModerationRiskLevel
import com.pethelp.app.features.auth.presentation.AuthUiState
import com.pethelp.app.features.auth.presentation.AuthViewModel
import coil.compose.AsyncImage
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
                    uiState.selectedPost?.let { selectedPost ->
                        ModeratorPostDetailContent(
                            post = selectedPost,
                            aiAnalysis = uiState.aiAnalysis,
                            isAiAnalysisLoading = uiState.isAiAnalysisLoading,
                            aiAnalysisError = uiState.aiAnalysisError,
                            isActionLoading = uiState.isActionLoading,
                            padding = padding,
                            onApprove = { viewModel.approvePost(postId) },
                            onReject = { showRejectDialog = true }
                        )
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

@Composable
private fun ModeratorPostDetailContent(
    post: Post,
    aiAnalysis: ModerationAiAnalysis?,
    isAiAnalysisLoading: Boolean,
    aiAnalysisError: UiText?,
    isActionLoading: Boolean,
    padding: PaddingValues,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ModerationHeroSection(post = post)
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModerationAiAnalysisCard(
                    analysis = aiAnalysis,
                    isLoading = isAiAnalysisLoading,
                    error = aiAnalysisError
                )
                ModerationAuthorCard(post = post)
                ModerationDescriptionCard(post = post)
                ModerationPetInfoCard(post = post)
                ModerationHealthCard(post = post)
                ModerationLocationCard(post = post)
                ModerationPreviousRejectionCard(post = post)
                ModerationDecisionActions(
                    isActionLoading = isActionLoading,
                    onApprove = onApprove,
                    onReject = onReject
                )
            }
        }
    }
}

@Composable
private fun ModerationHeroSection(post: Post) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (post.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = stringResource(R.string.moderation_photo_main_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.moderation_no_photos),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.moderation_priority_review),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = post.title.ifBlank { stringResource(R.string.moderation_untitled_post) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(post.status)
                InfoChip(
                    icon = Icons.Default.Category,
                    label = UiText.fromCategory(post.category).asString()
                )
            }
        }

        if (post.imageUrls.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(post.imageUrls) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = stringResource(R.string.moderation_photo_thumbnail_desc),
                        modifier = Modifier
                            .size(width = 96.dp, height = 72.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun ModerationAiAnalysisCard(
    analysis: ModerationAiAnalysis?,
    isLoading: Boolean,
    error: UiText?
) {
    val riskColors = when (analysis?.riskLevel) {
        ModerationRiskLevel.HIGH -> StatusErrorBg to StatusError
        ModerationRiskLevel.MEDIUM -> StatusWarningBg to StatusWarning
        else -> StatusSuccessBg to StatusSuccess
    }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.moderation_ai_review_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.moderation_ai_review_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (analysis != null) {
                Surface(shape = RoundedCornerShape(50), color = riskColors.first) {
                    Text(
                        text = stringResource(R.string.moderation_ia_match, analysis.confidence),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = riskColors.second,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            isLoading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text(
                    text = stringResource(R.string.moderation_ai_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            error != null -> Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            analysis != null -> {
                Text(
                    text = analysis.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                ModerationAnalysisList(stringResource(R.string.moderation_ai_strengths), analysis.strengths, StatusSuccess)
                ModerationAnalysisList(stringResource(R.string.moderation_ai_concerns), analysis.concerns, StatusWarning)
                ModerationAnalysisList(stringResource(R.string.moderation_ai_missing), analysis.missingFields, StatusError)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.moderation_ai_recommendation, analysis.recommendation),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ModerationAnalysisList(title: String, items: List<String>, color: Color) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.padding(top = 7.dp).size(6.dp).background(color, CircleShape))
                Text(item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ModerationAuthorCard(post: Post) {
    SectionCard {
        SectionTitle(Icons.Default.Person, stringResource(R.string.moderation_author_section))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (post.authorPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = post.authorPhotoUrl,
                        contentDescription = stringResource(R.string.profile_avatar_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = post.authorName.firstOrNull()?.uppercase().orEmpty().ifBlank { "?" },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(post.authorName.ifBlank { stringResource(R.string.post_detail_unknown_user) }, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.moderation_author_id, post.authorId.ifBlank { "-" }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.moderation_detail_published_label, formatDate(post.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModerationDescriptionCard(post: Post) {
    SectionCard {
        SectionTitle(Icons.Default.Description, stringResource(R.string.post_desc_hint))
        Text(
            text = post.description.ifBlank { stringResource(R.string.moderation_empty_description) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun ModerationPetInfoCard(post: Post) {
    val context = LocalContext.current
    val behaviorText = post.behavior
        .joinToString { behavior -> UiText.fromBehavior(behavior).asString(context) }
        .ifBlank { "-" }

    SectionCard {
        SectionTitle(Icons.Default.Pets, stringResource(R.string.post_summary_details))
        InfoGrid(
            listOf(
                Triple(Icons.Default.Pets, stringResource(R.string.post_summary_animal_type), post.animalType.ifBlank { "-" }),
                Triple(Icons.Default.Badge, stringResource(R.string.post_summary_breed), post.breed.ifBlank { "-" }),
                Triple(Icons.Default.Cake, stringResource(R.string.post_summary_age), UiText.fromAge(post.age).asString()),
                Triple(Icons.Default.Wc, stringResource(R.string.post_summary_gender), UiText.fromGender(post.gender).asString()),
                Triple(Icons.Default.Straighten, stringResource(R.string.post_summary_size), UiText.fromSize(post.size).asString()),
                Triple(Icons.Default.ChatBubble, stringResource(R.string.post_summary_behavior), behaviorText)
            )
        )
    }
}

@Composable
private fun ModerationHealthCard(post: Post) {
    SectionCard {
        SectionTitle(Icons.Default.HealthAndSafety, stringResource(R.string.post_summary_health))
        ChipGrid(
            listOf(
                stringResource(R.string.post_vaccinated_label) to post.vaccinated,
                stringResource(R.string.post_dewormed_label) to post.dewormed,
                stringResource(R.string.post_sterilized_label) to post.sterilized,
                stringResource(R.string.edit_post_health_special) to post.specialCares
            )
        )
    }
}

@Composable
private fun ModerationLocationCard(post: Post) {
    SectionCard {
        SectionTitle(Icons.Default.LocationOn, stringResource(R.string.post_summary_location))
        InfoGrid(
            listOf(
                Triple(Icons.Default.Place, stringResource(R.string.post_location_selected_label), post.locationName.ifBlank { "-" }),
                Triple(Icons.Default.LocationCity, stringResource(R.string.post_summary_city), post.city.ifBlank { "-" }),
                Triple(Icons.Default.Map, stringResource(R.string.post_neighborhood_label), post.neighborhood.ifBlank { "-" }),
                Triple(Icons.Default.PinDrop, stringResource(R.string.common_location), "${post.latitude}, ${post.longitude}")
            )
        )
    }
}

@Composable
private fun ModerationPreviousRejectionCard(post: Post) {
    if (post.rejectionReason.isNullOrBlank()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(Icons.Default.Report, stringResource(R.string.moderation_previous_rejection_title), StatusError)
            Text(
                text = post.rejectionReason,
                style = MaterialTheme.typography.bodyMedium,
                color = StatusError
            )
        }
    }
}

@Composable
private fun ModerationDecisionActions(
    isActionLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onApprove,
                enabled = !isActionLoading,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_approve))
            }

            OutlinedButton(
                onClick = onReject,
                enabled = !isActionLoading,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_reject))
            }
        }

        if (isActionLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
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

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun InfoGrid(items: List<Triple<ImageVector, String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    InfoTile(icon = item.first, label = item.second, value = item.third, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun BooleanChip(label: String, enabled: Boolean) {
    val color = if (enabled) StatusSuccess else StatusNeutral
    val bg = if (enabled) StatusSuccessBg else StatusNeutralBg

    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(if (enabled) Icons.Default.Check else Icons.Default.Close, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ChipGrid(items: List<Pair<String, Boolean>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        BooleanChip(item.first, item.second)
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
            Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(74.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (post.imageUrls.isNotEmpty()) {
                        AsyncImage(
                            model = post.imageUrls.first(),
                            contentDescription = stringResource(R.string.moderation_photo_thumbnail_desc),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ImageNotSupported, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title.ifBlank { stringResource(R.string.moderation_untitled_post) },
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

                MatchBadge(percentage = post.iaMatchPercentage ?: 0)
            }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(Icons.Default.Category, UiText.fromCategory(post.category).asString())
                    InfoChip(Icons.Default.PhotoLibrary, stringResource(R.string.moderation_photo_count, post.imageUrls.size))
                }
                
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
