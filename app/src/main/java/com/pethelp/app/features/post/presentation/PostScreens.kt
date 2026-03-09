package com.pethelp.app.features.post.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
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
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.WarmOrange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Colores de diseño (Figma) ────────────────────────────────────────────────
private val TealAccent = Color(0xFF00BCB4)
private val TextPrimary = Color(0xFF101828)
private val TextSecondary = Color(0xFF4A5565)
private val TextTertiary = Color(0xFF6A7282)
private val TextMuted = Color(0xFF99A1AF)
private val BorderLight = Color(0xFFF3F4F6)
private val BorderMedium = Color(0xFFE5E7EB)
private val SurfaceGray = Color(0xFFF9FAFB)
private val ChipGreenBg = Color(0xFF4CAF50).copy(alpha = 0.1f)
private val ChipGreenBorder = Color(0xFF4CAF50).copy(alpha = 0.2f)
private val ChipBlueBg = Color(0xFFEFF6FF)
private val ChipBlueBorder = Color(0xFFDBEAFE)
private val ChipBlueText = Color(0xFF1447E6)
private val ChipPurpleBg = Color(0xFFFAF5FF)
private val ChipPurpleBorder = Color(0xFFF3E8FF)
private val ChipPurpleText = Color(0xFF8200DB)

// ═══════════════════════════════════════════════════════════════════════════════
// ─── DETALLE DE PUBLICACIÓN ──────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavController,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        when {
            uiState.isLoading && uiState.post == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PetHelpGreen)
                }
            }
            uiState.error != null && uiState.post == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            color = Color(0xFFE57373),
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { navController.popBackStack() }) {
                            Text("Volver")
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
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
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
                                    Color.Black.copy(alpha = 0.3f),
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
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                // Botón compartir
                IconButton(
                    onClick = { /* TODO: Share */ },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color.White
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
                        Color.White,
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
                        .background(BorderMedium, RoundedCornerShape(50))
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
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.9).sp
                    )

                    // Chip del autor
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SurfaceGray,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(BorderLight, BorderLight))
                        )
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
                                    .background(Color.LightGray)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "PUBLICADO POR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = post.authorName.ifBlank { "Usuario" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
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
                        label = post.breed.ifBlank { post.animalType.ifBlank { "Mascota" } },
                        backgroundColor = ChipGreenBg,
                        borderColor = ChipGreenBorder,
                        textColor = PetHelpGreen,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChipCard(
                        icon = Icons.Default.Female,
                        label = post.animalType.ifBlank { "N/A" },
                        backgroundColor = ChipBlueBg,
                        borderColor = ChipBlueBorder,
                        textColor = ChipBlueText,
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
                        label = post.size.displayName,
                        backgroundColor = ChipPurpleBg,
                        borderColor = ChipPurpleBorder,
                        textColor = ChipPurpleText,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChipCard(
                        icon = Icons.Default.HealthAndSafety,
                        label = if (post.vaccinated) "Vacunada" else "Sin vacunas",
                        backgroundColor = ChipGreenBg,
                        borderColor = ChipGreenBorder,
                        textColor = PetHelpGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Descripción ─────────────────────────────────────────
                Text(
                    text = "Sobre ${post.title}",
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
                                if (hasVoted) WarmOrange.copy(alpha = 0.1f) else SurfaceGray,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (hasVoted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Votar",
                            tint = WarmOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "${post.votes}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasVoted) WarmOrange else TextSecondary
                        )
                    }

                    // Botón adopción (solo si es categoría adopción)
                    if (post.category == PostCategory.ADOPTION) {
                        Button(
                            onClick = onAdoptClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PetHelpGreen
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Solicitar Adopción",
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
                            "Ubicación",
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
                                tint = TextTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = post.locationName,
                                fontSize = 14.sp,
                                color = TextTertiary
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
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                            .background(SurfaceGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFFB2C36),
                                modifier = Modifier.size(32.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.9f)
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
                    "Comentarios",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                // Input de comentario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGray, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BorderMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                "Escribe un comentario...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
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
                            "Publicar",
                            color = WarmOrange,
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
                    text = "Aún no hay comentarios. ¡Sé el primero!",
                    color = TextMuted,
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
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        )
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
                .background(PetHelpGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorName.firstOrNull()?.uppercase() ?: "U",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PetHelpGreen
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName.ifBlank { "Usuario" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = dateFormat.format(Date(comment.createdAt)),
                    fontSize = 11.sp,
                    color = TextMuted
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
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.addImage(it) } }
    )

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Navegar al feed tras crear exitosamente
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            Surface(
                color = Color(0xFFFAFAFA).copy(alpha = 0.9f),
                shadowElevation = 0.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = BorderLight,
                    shape = RoundedCornerShape(0.dp)
                )
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
                            contentDescription = "Volver",
                            modifier = Modifier.size(22.dp),
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "Ayuda a un peludo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = TealAccent.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = BorderLight,
                    shape = RoundedCornerShape(0.dp)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 17.dp)
                ) {
                    Button(
                        onClick = { viewModel.createPost() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealAccent
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 10.dp
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Siguiente: Ubicación",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.45.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // ── Sección de fotos ────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = TealAccent.copy(alpha = 0.05f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(TealAccent.copy(alpha = 0.4f), TealAccent.copy(alpha = 0.4f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(21.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Área para agregar fotos
                        if (uiState.imageUris.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(TealAccent.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Agregar fotos",
                                        modifier = Modifier.size(30.dp),
                                        tint = TealAccent
                                    )
                                }
                                Row {
                                    Text(
                                        "Toca para agregar hasta ",
                                        fontSize = 14.sp,
                                        color = TextTertiary
                                    )
                                    Text(
                                        "5 fotos",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TealAccent
                                    )
                                }
                            }
                        }

                        // Miniaturas de fotos seleccionadas
                        if (uiState.imageUris.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(uiState.imageUris) { index, uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                            .shadow(4.dp, RoundedCornerShape(16.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Foto ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Botón eliminar
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                                .clickable { viewModel.removeImage(index) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        // Indicador de posición
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.4f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                "${index + 1}/5",
                                                fontSize = 10.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // Botón agregar más
                                if (uiState.imageUris.size < 5) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(
                                                    1.dp,
                                                    TealAccent.copy(alpha = 0.4f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .background(Color.White)
                                                .clickable {
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Agregar más",
                                                tint = TealAccent,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Título y descripción ────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Título
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Título de publicación",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextTertiary,
                            letterSpacing = 0.35.sp
                        )
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            placeholder = {
                                Text(
                                    "Ej: Perrito encontrado en la Condesa",
                                    color = TextMuted
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = BorderMedium,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    // Descripción
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Descripción de la situación",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextTertiary,
                            letterSpacing = 0.35.sp
                        )
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            placeholder = {
                                Text(
                                    "Describe cómo encontraste a este peludo, su estado de salud, comportamiento, etc.",
                                    color = TextMuted
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(154.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = BorderMedium,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            }

            // ── Categoría sugerida por IA ───────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(TealAccent.copy(alpha = 0.2f), TealAccent.copy(alpha = 0.2f))
                        )
                    ),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        TealAccent.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(21.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(TealAccent.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = TealAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Categoría sugerida por IA",
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Analizamos tu foto y texto automáticamente",
                                        fontSize = 11.sp,
                                        color = TealAccent.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TealAccent.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Selector de categoría
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White,
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.linearGradient(
                                            listOf(TealAccent.copy(alpha = 0.2f), TealAccent.copy(alpha = 0.2f))
                                        )
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .clickable { expanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 21.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = TealAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                uiState.category.displayName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = TextTertiary
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    PostCategory.entries.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.displayName) },
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
            }

            // ── Tipo de mascota ─────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Tipo de mascota",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary,
                        letterSpacing = 0.35.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            Triple("Perro", Icons.Default.Pets, uiState.animalType == "Perro"),
                            Triple("Gato", Icons.Default.Pets, uiState.animalType == "Gato"),
                            Triple("Otro", Icons.Default.Pets, uiState.animalType == "Otro")
                        ).forEach { (label, icon, selected) ->
                            SelectableChip(
                                label = label,
                                icon = icon,
                                selected = selected,
                                accentColor = TealAccent,
                                onClick = { viewModel.updateAnimalType(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Tamaño ─────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Tamaño",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary,
                        letterSpacing = 0.35.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimalSize.entries.forEach { size ->
                            SelectableChip(
                                label = size.displayName,
                                selected = uiState.size == size,
                                accentColor = TealAccent,
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
        color = if (selected) accentColor.copy(alpha = 0.1f) else Color.White,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                listOf(
                    if (selected) accentColor else BorderMedium,
                    if (selected) accentColor else BorderMedium
                )
            )
        ),
        shadowElevation = if (selected) 2.dp else 0.dp,
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
                        tint = if (selected) accentColor else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) accentColor else TextTertiary,
                    textAlign = TextAlign.Center
                )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// ─── EDITAR PUBLICACIÓN (placeholder — usa CreatePostScreen precargado) ─────
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun EditPostScreen(postId: String, navController: NavController) {
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
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
