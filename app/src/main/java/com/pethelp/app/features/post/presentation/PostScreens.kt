/**
 * Pantallas relacionadas con las publicaciones (Posts) de PetHelp.
 *
 * Este archivo contiene:
 * - `PostDetailScreen`: muestra la información completa de una publicación, comentarios, votos y solicitud de adopción.
 * - `CreatePostScreen`: permite crear una publicación nueva con fotos, texto y categoría.
 * - `EditPostScreen`: permite editar una publicación existente con un diseño renovado.
 */
package com.pethelp.app.features.post.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pethelp.app.R
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════════════════════
// ─── DETALLE DE PUBLICACIÓN ──────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Pantalla de detalle de una publicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavController,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { padding ->
        when {
            uiState.isLoading && uiState.post == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PetHelpPrimary)
                }
            }
            uiState.error != null && uiState.post == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.post_detail_error),
                            color = PetHelpDestructive,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { navController.popBackStack() }) {
                            Text(stringResource(R.string.common_back))
                        }
                    }
                }
            }
            else -> {
                val post = uiState.post ?: return@Scaffold
                PostDetailContent(
                    post = post,
                    comments = uiState.comments,
                    hasVoted = uiState.hasVoted,
                    onBackClick = { navController.popBackStack() },
                    onVoteClick = { viewModel.toggleVote() },
                    onCommentSubmit = { viewModel.addComment(it) },
                    onAdoptClick = { viewModel.requestAdoption("Me interesa adoptar a ${post.title}") },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    post: Post,
    comments: List<Comment>,
    hasVoted: Boolean,
    onBackClick: () -> Unit,
    onVoteClick: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    onAdoptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // ── Imagen principal con botones superpuestos ────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(384.dp)
            ) {
                // Imagen
                if (post.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrls.first(),
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(SurfaceVariantLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextHint
                        )
                    }
                }

                // Gradiente superior
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Botón volver
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = White
                    )
                }

                // Botón compartir
                IconButton(
                    onClick = { /* TODO: Share */ },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.post_detail_share),
                        tint = White
                    )
                }
            }
        }

        // ── Contenido principal con esquinas redondeadas ────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .background(
                        SurfaceLight,
                        RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp)
            ) {
                // ── Barra decorativa ────────────────────────────────────
                Box(
                    Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .background(PetHelpOutline, RoundedCornerShape(50))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(24.dp))

                // ── Nombre + Autor ──────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = post.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.9).sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Chip del autor
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SurfaceVariantLight,
                        border = BorderStroke(1.dp, PetHelpOutline)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 5.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PetHelpOutline)
                                    .border(1.dp, SurfaceLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (post.authorPhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = post.authorPhotoUrl,
                                        contentDescription = "Foto del autor",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.post_detail_published_by),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary.copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = post.authorName.ifBlank { stringResource(R.string.post_detail_unknown_user) },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Info chips (2x2 grid) ───────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChipCard(
                        icon = Icons.Default.Pets,
                        label = post.breed.ifBlank { post.animalType.ifBlank { stringResource(R.string.post_detail_unknown_pet) } },
                        backgroundColor = PetHelpPrimary.copy(alpha = 0.1f),
                        borderColor = PetHelpPrimary.copy(alpha = 0.2f),
                        textColor = PetHelpPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChipCard(
                        icon = Icons.Default.Female,
                        label = post.animalType.ifBlank { stringResource(R.string.post_detail_na) },
                        backgroundColor = PetHelpSecondary.copy(alpha = 0.1f),
                        borderColor = PetHelpSecondary.copy(alpha = 0.2f),
                        textColor = PetHelpSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChipCard(
                        icon = Icons.Default.Straighten,
                        label = sizeToDisplayName(post.size),
                        backgroundColor = PetHelpSecondary.copy(alpha = 0.1f),
                        borderColor = PetHelpSecondary.copy(alpha = 0.2f),
                        textColor = PetHelpSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChipCard(
                        icon = Icons.Default.HealthAndSafety,
                        label = if (post.vaccinated) stringResource(R.string.post_detail_vacunada) else stringResource(R.string.post_detail_no_vacunas),
                        backgroundColor = PetHelpPrimary.copy(alpha = 0.1f),
                        borderColor = PetHelpPrimary.copy(alpha = 0.2f),
                        textColor = PetHelpPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Descripción ─────────────────────────────────────────
                Text(
                    text = stringResource(R.string.post_detail_about, post.title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = post.description,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    lineHeight = 24.sp,
                    letterSpacing = 0.375.sp
                )

                Spacer(Modifier.height(24.dp))

                // ── Votos + Botón adopción ──────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Votos
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onVoteClick() }
                            .background(
                                if (hasVoted) PetHelpSecondary.copy(alpha = 0.1f) else SurfaceVariantLight,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (hasVoted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Votar",
                            tint = PetHelpSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "${post.votes}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasVoted) PetHelpSecondary else TextSecondary
                        )
                    }

                    // Botón adopción (solo si es categoría adopción)
                    if (post.category == PostCategory.ADOPTION) {
                        Button(
                            onClick = onAdoptClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PetHelpPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.post_detail_request_adoption),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Ubicación ───────────────────────────────────────────
                if (post.locationName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.post_location),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = post.locationName,
                                fontSize = 14.sp,
                                color = TextSecondary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Mapa placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, PetHelpOutline, RoundedCornerShape(16.dp))
                            .background(SurfaceVariantLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PetHelpDestructive,
                                modifier = Modifier.size(32.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = White.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = post.locationName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // ── Comentarios ─────────────────────────────────────────
                Text(
                    stringResource(R.string.post_detail_comments),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                // Input de comentario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantLight, RoundedCornerShape(16.dp))
                        .border(1.dp, PetHelpOutline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PetHelpOutline),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.post_detail_comment_hint),
                                color = TextHint,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = PetHelpPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commentText.isNotBlank()) {
                                    onCommentSubmit(commentText)
                                    commentText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )
                    TextButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onCommentSubmit(commentText)
                                commentText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ) {
                        Text(
                            stringResource(R.string.post_detail_comment_post),
                            color = PetHelpPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Lista de comentarios ────────────────────────────────────────
        if (comments.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.post_detail_no_comments),
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

/**
 * Tarjeta simple con icono + texto.
 */
@Composable
private fun InfoChipCard(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CommentItem(comment: Comment, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale("es", "CO")) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PetHelpPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (comment.authorPhotoUrl.isNotBlank()) {
                AsyncImage(
                    model = comment.authorPhotoUrl,
                    contentDescription = "Foto del autor del comentario",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = comment.authorName.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PetHelpPrimary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName.ifBlank { stringResource(R.string.post_detail_unknown_user) },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = dateFormat.format(Date(comment.createdAt)),
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// ─── CREAR PUBLICACIÓN ──────────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    // Colores dinámicos basados en el tema
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) White else TextPrimary
    val secondaryTextColor = if (isDark) White.copy(alpha = 0.7f) else TextSecondary
    val hintColor = if (isDark) White.copy(alpha = 0.5f) else TextHint
    val outlineColor = if (isDark) PetHelpOutlineDark else PetHelpOutline
    val containerPrimaryColor = if (isDark) PetHelpPrimaryDark.copy(alpha = 0.15f) else PetHelpPrimary.collectContainerColor()
    val containerTertiaryColor = if (isDark) PetHelpTertiaryDark.copy(alpha = 0.15f) else PetHelpTertiary.collectContainerColor()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.addImage(it) } }
    )

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Screen.Feed) {
                popUpTo(Screen.CreatePost) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        topBar = {
            Surface(
                color = surfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(68.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            modifier = Modifier.size(22.dp),
                            tint = textColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = stringResource(R.string.post_create_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.post_step_1_of_4),
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = PetHelpPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = surfaceColor,
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(
                                Screen.LocationSelection(
                                    title = uiState.title,
                                    description = uiState.description,
                                    category = uiState.category.name,
                                    animalType = uiState.animalType,
                                    breed = uiState.breed,
                                    size = uiState.size.name,
                                    imageUris = uiState.imageUris.map { it.toString() },
                                    street = uiState.street,
                                    neighborhood = uiState.neighborhood,
                                    city = uiState.city,
                                    latitude = uiState.latitude,
                                    longitude = uiState.longitude,
                                    locationName = uiState.locationName
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetHelpPrimary
                        ),
                        enabled = uiState.title.isNotBlank() && uiState.description.isNotBlank() && uiState.imageUris.isNotEmpty()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.post_next_button_location),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            LinearProgressIndicator(
                progress = { 0.25f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PetHelpPrimary,
                trackColor = if (isDark) SurfaceVariantDark else SurfaceVariantLight
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // ── Zona de Fotos ───────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = containerPrimaryColor,
                    border = BorderStroke(1.dp, PetHelpPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icono y Texto de ayuda
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(PetHelpPrimary.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, PetHelpPrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = PetHelpPrimary
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.post_photo_limit_label),
                                color = PetHelpPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Lista de fotos seleccionadas
                        if (uiState.imageUris.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                uiState.imageUris.forEachIndexed { index, uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Badge de número (1/5)
                                        Surface(
                                            modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "${index + 1}/5",
                                                color = White,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        // Botón eliminar
                                        IconButton(
                                            onClick = { viewModel.removeImage(index) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                if (uiState.imageUris.size < 5) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(surfaceColor)
                                            .border(1.dp, PetHelpPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = PetHelpPrimary, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Título ──────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.post_title_label),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = { Text(stringResource(R.string.post_title_placeholder), color = hintColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PetHelpPrimary,
                            unfocusedBorderColor = outlineColor,
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                }
            }

            // ── Descripción ─────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.post_description_label),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = { Text(stringResource(R.string.post_description_placeholder), color = hintColor) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PetHelpPrimary,
                            unfocusedBorderColor = outlineColor,
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                }
            }

            // ── Categoría IA ────────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = containerTertiaryColor,
                    border = BorderStroke(1.dp, PetHelpTertiary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).background(PetHelpTertiary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PetHelpTertiary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.post_ai_category_title), fontWeight = FontWeight.Bold, color = textColor)
                                Text(stringResource(R.string.post_ai_category_subtitle), fontSize = 12.sp, color = secondaryTextColor)
                            }
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PetHelpTertiary.copy(alpha = 0.5f))
                        }

                        var expanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = surfaceColor,
                            border = BorderStroke(1.dp, PetHelpTertiary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().height(56.dp).clickable { expanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PetHelpTertiary, modifier = Modifier.size(18.dp))
                                    Text(categoryToDisplayName(uiState.category), fontWeight = FontWeight.Bold, color = textColor)
                                }
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = secondaryTextColor)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                PostCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(categoryToDisplayName(category)) },
                                        onClick = {
                                            viewModel.updateCategory(category)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Tipo de Mascota y Raza ─────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.post_animal_type_label), fontWeight = FontWeight.Bold, color = textColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val types = listOf(
                            Triple(stringResource(R.string.post_animal_type_dog), Icons.Default.Pets, "Perro"),
                            Triple(stringResource(R.string.post_animal_type_cat), Icons.Default.Pets, "Gato"),
                            Triple(stringResource(R.string.post_animal_type_other), Icons.AutoMirrored.Filled.Help, "Otro")
                        )
                        types.forEach { (typeLabel, icon, value) ->
                            SelectableChip(
                                label = typeLabel,
                                icon = icon,
                                selected = uiState.animalType == value,
                                accentColor = PetHelpPrimary,
                                onClick = { viewModel.updateAnimalType(value) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Campo de Raza (Breed)
                    Text(stringResource(R.string.post_breed_label), fontWeight = FontWeight.Bold, color = textColor)
                    OutlinedTextField(
                        value = uiState.breed,
                        onValueChange = { viewModel.updateBreed(it) },
                        placeholder = { Text(stringResource(R.string.post_breed_placeholder), color = hintColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PetHelpPrimary,
                            unfocusedBorderColor = outlineColor,
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                }
            }

            // ── Tamaño ──────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.post_size_label), fontWeight = FontWeight.Bold, color = textColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnimalSize.entries.forEach { size ->
                            SelectableChip(
                                label = sizeToDisplayName(size),
                                selected = uiState.size == size,
                                accentColor = PetHelpPrimary,
                                onClick = { viewModel.updateSize(size) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
}

@Composable
fun Color.collectContainerColor(): Color {
    val isDark = isSystemInDarkTheme()
    return if (isDark) this.copy(alpha = 0.15f) else this.copy(alpha = 0.08f)
}


@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(50),
        color = if (selected) accentColor.copy(alpha = 0.1f) else SurfaceLight,
        border = BorderStroke(
            1.dp,
            if (selected) accentColor else PetHelpOutline
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selected) accentColor else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) accentColor else TextSecondary,
                    textAlign = TextAlign.Center
                )
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun categoryToDisplayName(category: PostCategory): String {
    return when(category) {
        PostCategory.ADOPTION -> stringResource(R.string.category_adoption)
        PostCategory.LOST -> stringResource(R.string.category_lost)
        PostCategory.FOUND -> stringResource(R.string.category_found)
        PostCategory.TEMP_HOME -> stringResource(R.string.category_temp_home)
        PostCategory.VET_EVENT -> stringResource(R.string.category_vet_event)
    }
}

@Composable
private fun sizeToDisplayName(size: AnimalSize): String {
    return when(size) {
        AnimalSize.SMALL -> stringResource(R.string.tag_small)
        AnimalSize.MEDIUM -> stringResource(R.string.tag_medium)
        AnimalSize.LARGE -> stringResource(R.string.tag_large)
    }
}

@Composable
private fun ageToDisplayName(age: AnimalAge): String {
    return when(age) {
        AnimalAge.PUPPY -> stringResource(R.string.post_age_puppy)
        AnimalAge.YOUNG -> stringResource(R.string.post_age_young)
        AnimalAge.ADULT -> stringResource(R.string.post_age_adult)
        AnimalAge.SENIOR -> stringResource(R.string.post_age_senior)
    }
}

@Composable
private fun genderToDisplayName(gender: AnimalGender): String {
    return when(gender) {
        AnimalGender.MALE -> stringResource(R.string.post_gender_male)
        AnimalGender.FEMALE -> stringResource(R.string.post_gender_female)
        AnimalGender.UNKNOWN -> stringResource(R.string.post_gender_unknown)
    }
}

@Composable
private fun behaviorToDisplayName(behavior: PetBehavior): String {
    return when(behavior) {
        PetBehavior.CALM -> stringResource(R.string.post_behavior_calm)
        PetBehavior.ACTIVE -> stringResource(R.string.post_behavior_active)
        PetBehavior.SOCIABLE -> stringResource(R.string.post_behavior_sociable)
        PetBehavior.SHY -> stringResource(R.string.post_behavior_shy)
        PetBehavior.PROTECTIVE -> stringResource(R.string.post_behavior_protective)
        PetBehavior.PLAYFUL -> stringResource(R.string.post_behavior_playful)
        PetBehavior.INDEPENDENT -> stringResource(R.string.post_behavior_independent)
        PetBehavior.AFFECTIONATE -> stringResource(R.string.post_behavior_affectionate)
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// ─── EDITAR PUBLICACIÓN ──────────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    postId: String,
    navController: NavController,
    viewModel: EditPostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) White else TextPrimary
    val secondaryTextColor = if (isDark) White.copy(alpha = 0.7f) else TextSecondary
    val outlineColor = if (isDark) PetHelpOutlineDark else PetHelpOutline

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.addImage(it.toString()) } }
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is EditPostViewModel.EditPostEvent.PostUpdated -> {
                    navController.popBackStack()
                }
                is EditPostViewModel.EditPostEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        topBar = {
            Surface(
                color = surfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(68.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textColor)
                    }
                    Text(
                        text = "Editar Publicación",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PetHelpPrimary)
                }
            }
        },
        bottomBar = {
            Surface(
                color = surfaceColor,
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawLine(color = outlineColor, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, PetHelpOutline)
                    ) {
                        Text("Cancelar", color = textColor, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.savePost() },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PetHelpPrimary),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PetHelpPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // ── FOTOS ─────────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Fotos de la mascota", fontWeight = FontWeight.Bold, color = textColor)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(uiState.imageUrls) { index, url ->
                                Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp))) {
                                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    if (index == 0) {
                                        Surface(
                                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                            color = PetHelpPrimary,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Principal", color = White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeImage(url) },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(0.4f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(surfaceColor)
                                        .border(1.dp, PetHelpOutline, RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (uiState.imageUrls.size < 5) {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            } else {
                                                viewModel.onShowSnackbar("Máximo 5 fotos permitidas")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = PetHelpPrimary)
                                        Text("Agregar", color = PetHelpPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        Text("Mantén presionado para reordenar las fotos", fontSize = 12.sp, color = secondaryTextColor)
                    }
                }

                // ── INFO BÁSICA ───────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        EditField(label = "Nombre", value = uiState.title, onValueChange = viewModel::onTitleChange, textColor = textColor, outlineColor = outlineColor)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Tipo de mascota", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("Perro", "Gato", "Otro").forEach { type ->
                                    SelectableChip(
                                        label = type,
                                        selected = uiState.animalType == type,
                                        accentColor = PetHelpPrimary,
                                        onClick = { viewModel.onAnimalTypeChange(type) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Edad", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                                DropdownSelector(
                                    currentValue = ageToDisplayName(uiState.age),
                                    options = AnimalAge.entries.map { ageToDisplayName(it) to it },
                                    onSelect = { viewModel.onAgeChange(it) },
                                    textColor = textColor,
                                    outlineColor = outlineColor
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Sexo", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                                DropdownSelector(
                                    currentValue = genderToDisplayName(uiState.gender),
                                    options = AnimalGender.entries.map { genderToDisplayName(it) to it },
                                    onSelect = { viewModel.onGenderChange(it) },
                                    textColor = textColor,
                                    outlineColor = outlineColor
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Tamaño", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(if (isDark) SurfaceVariantDark else SurfaceVariantLight).padding(4.dp)
                            ) {
                                AnimalSize.entries.forEach { size ->
                                    val isSelected = uiState.size == size
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(if (isSelected) PetHelpPrimary else Color.Transparent).clickable { viewModel.onSizeChange(size) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(sizeToDisplayName(size), color = if (isSelected) White else secondaryTextColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── DESCRIPCIÓN ───────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Descripción", fontWeight = FontWeight.Bold, color = textColor)
                            TextButton(onClick = { viewModel.improveDescriptionWithAI() }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = PetHelpTertiary)
                                Spacer(Modifier.width(4.dp))
                                Text("Mejorar con IA", color = PetHelpTertiary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PetHelpPrimary, unfocusedBorderColor = outlineColor, focusedTextColor = textColor, unfocusedTextColor = textColor),
                            supportingText = { Text("${uiState.description.length}/500", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
                        )
                    }
                }

                // ── UBICACIÓN ─────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Ubicación", fontWeight = FontWeight.Bold, color = textColor)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) SurfaceVariantDark else SurfaceVariantLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PetHelpDestructive)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(uiState.locationName.ifBlank { "Sin ubicación" }, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                                    Text("${uiState.street}, ${uiState.neighborhood}", fontSize = 12.sp, color = secondaryTextColor)
                                }
                                TextButton(onClick = { 
                                    navController.navigate(
                                        Screen.LocationSelection(
                                            title = uiState.title,
                                            description = uiState.description,
                                            category = uiState.category.name,
                                            animalType = uiState.animalType,
                                            breed = uiState.breed,
                                            size = uiState.size.name,
                                            imageUris = uiState.imageUrls,
                                            street = uiState.street,
                                            neighborhood = uiState.neighborhood,
                                            city = uiState.city,
                                            latitude = uiState.latitude,
                                            longitude = uiState.longitude,
                                            locationName = uiState.locationName
                                        )
                                    )
                                }) {
                                    Text("Cambiar", color = PetHelpPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ── ESTADO ────────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Estado de la publicación", fontWeight = FontWeight.Bold, color = textColor)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                Triple(PostStatus.ACTIVE, "Activa", "Visible para todos los usuarios."),
                                Triple(PostStatus.PAUSED, "Pausada", "Nadie podrá verla temporalmente."),
                                Triple(PostStatus.ADOPTED, "Adoptada", "Se marcará como un final feliz.")
                            ).forEach { (status, title, desc) ->
                                val isSelected = uiState.status == status
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (isSelected) PetHelpPrimary.copy(0.1f) else Color.Transparent).border(1.dp, if (isSelected) PetHelpPrimary else outlineColor, RoundedCornerShape(16.dp)).clickable { viewModel.onStatusChange(status) }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { viewModel.onStatusChange(status) }, colors = RadioButtonDefaults.colors(selectedColor = PetHelpPrimary))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, color = textColor)
                                        Text(desc, fontSize = 12.sp, color = secondaryTextColor)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── SALUD ─────────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Información de salud", fontWeight = FontWeight.Bold, color = textColor)
                        val gridItems = listOf(
                            HealthItemData(uiState.vaccinated, "Vacunado", Icons.Default.Vaccines, viewModel::onVaccinatedChange),
                            HealthItemData(uiState.sterilized, "Esterilizado", Icons.Default.ContentCut, viewModel::onSterilizedChange),
                            HealthItemData(uiState.dewormed, "Desparasitado", Icons.Default.BugReport, viewModel::onDewormedChange),
                            HealthItemData(uiState.specialCares, "Cuidados Esp.", Icons.Default.MedicalServices, viewModel::onSpecialCaresChange)
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in 0 until gridItems.size step 2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    HealthGridItem(gridItems[i], modifier = Modifier.weight(1f), isDark = isDark)
                                    if (i + 1 < gridItems.size) {
                                        HealthGridItem(gridItems[i+1], modifier = Modifier.weight(1f), isDark = isDark)
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

private data class HealthItemData(
    val isSelected: Boolean,
    val label: String,
    val icon: ImageVector,
    val onToggle: (Boolean) -> Unit
)

@Composable
private fun HealthGridItem(item: HealthItemData, modifier: Modifier, isDark: Boolean) {
    val isSelected = item.isSelected
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PetHelpPrimary.copy(0.1f) else (if (isDark) SurfaceDark else White),
        border = BorderStroke(1.dp, if (isSelected) PetHelpPrimary else (if (isDark) PetHelpOutlineDark else PetHelpOutline)),
        onClick = { item.onToggle(!isSelected) }
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(item.icon, contentDescription = null, tint = if (isSelected) PetHelpPrimary else TextHint, modifier = Modifier.size(20.dp))
            Text(item.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) PetHelpPrimary else (if (isDark) White else TextPrimary))
        }
    }
}

@Composable
private fun EditField(label: String, value: String, onValueChange: (String) -> Unit, textColor: Color, outlineColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PetHelpPrimary, unfocusedBorderColor = outlineColor, focusedTextColor = textColor, unfocusedTextColor = textColor)
        )
    }
}

@Composable
private fun <T> DropdownSelector(currentValue: String, options: List<Pair<String, T>>, onSelect: (T) -> Unit, textColor: Color, outlineColor: Color) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PetHelpPrimary) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = outlineColor, unfocusedBorderColor = outlineColor, focusedTextColor = textColor, unfocusedTextColor = textColor),
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { if (it is androidx.compose.foundation.interaction.PressInteraction.Release) expanded = true }
                    }
                }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.4f)) {
            options.forEach { (label, value) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}
