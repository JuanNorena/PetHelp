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
import androidx.compose.material.icons.filled.Pets
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
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.domain.model.UserRole
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.features.auth.presentation.AuthUiState
import com.pethelp.app.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorPanelScreen(
    navController: NavController,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = hiltViewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingPosts()
    }

    ModeratorAccessGate(navController = navController) {
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(stringResource(R.string.btn_logout)) },
                text = { Text("¿Deseas cerrar sesión del panel de moderación?") },
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
                    title = { Text(stringResource(R.string.moderation_panel_title)) },
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = stringResource(R.string.btn_logout)
                            )
                        }
                    }
                )
            }
        ) { padding ->
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
                            text = uiState.error ?: "No fue posible cargar pendientes.",
                            color = Color(0xFFB42318),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                uiState.pendingPosts.isEmpty() -> {
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
                                imageVector = Icons.Filled.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "No hay publicaciones pendientes por moderar.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.pendingPosts, key = { it.id }) { post ->
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

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.actionCompleted.collectLatest {
            navController.popBackStack()
        }
    }

    ModeratorAccessGate(navController = navController) {
        if (showRejectDialog) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Motivo del rechazo") },
                text = {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Motivo") },
                        placeholder = { Text("Describe por qué rechazas la publicación") },
                        supportingText = { Text("Campo obligatorio") },
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
                        Text("Confirmar rechazo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Detalle de moderación") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
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
                            text = uiState.error ?: "No fue posible cargar la publicación.",
                            color = Color(0xFFB42318),
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

                        Text(
                            text = "Autor: ${post?.authorName.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Categoría: ${post?.category?.displayName.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Publicada: ${formatDate(post?.createdAt ?: 0L)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Text(
                            text = post?.description.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!post?.rejectionReason.isNullOrBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F4)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Motivo de rechazo previo",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFFB42318),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = post?.rejectionReason.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF7A271A)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.approvePost(postId) },
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Aprobar")
                            }

                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Rechazar")
                            }
                        }

                        if (uiState.isActionLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Guardando decisión...",
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
                        Text("No se encontró la publicación.")
                    }
                }
            }
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = post.status)
            }

            Text(
                text = "Autor: ${post.authorName.ifBlank { "Usuario" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Categoría: ${post.category.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatDate(post.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusBadge(status: PostStatus) {
    val (background, foreground) = when (status) {
        PostStatus.PENDING -> Color(0xFFFFF4E5) to Color(0xFF7A2E0E)
        PostStatus.VERIFIED -> Color(0xFFE8F7EE) to Color(0xFF146C2E)
        PostStatus.REJECTED -> Color(0xFFFFECEB) to Color(0xFFA12622)
        PostStatus.RESOLVED -> Color(0xFFEFF4FF) to Color(0xFF174EA6)
        PostStatus.ACTIVE -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        PostStatus.PAUSED -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
        PostStatus.ADOPTED -> Color(0xFFF0FDF4) to Color(0xFF166534)
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
            text = status.displayName,
            color = foreground,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "-"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
