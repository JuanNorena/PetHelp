/**
 * Pantallas relacionadas con las publicaciones (Posts) de PetHelp.
 *
 * Este archivo contiene:
 * - `PostDetailScreen`: muestra la información completa de una publicación, comentarios, votos y solicitud de adopción.
 * - `CreatePostScreen`: permite crear una publicación nueva con fotos, texto y categoría.
 * - `EditPostScreen`: permite editar una publicación existente con un diseño renovado.
 */
package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import android.text.format.DateUtils
import com.pethelp.app.features.post.domain.model.AdoptionRequest
import com.pethelp.app.features.post.domain.model.AdoptionRequestStatus
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.pethelp.app.core.ui.theme.*
import com.pethelp.app.R
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.core.navigation.Screen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlin.random.Random

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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading && uiState.post == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.error != null && uiState.post == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error?.asString() ?: stringResource(R.string.post_detail_error),
                            color = MaterialTheme.colorScheme.error,
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
                
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        Surface(
                            shadowElevation = 16.dp,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.drawWithContent {
                                drawContent()
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Botón Adopción
                                Button(
                                    onClick = { navController.navigate(Screen.AdoptionRequest(post.id, post.title)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        stringResource(R.string.post_detail_request_adoption),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    PostDetailContent(
                        post = post,
                        comments = uiState.comments,
                        onBackClick = { navController.popBackStack() },
                        onCommentSubmit = { viewModel.addComment(it) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChipCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PostDetailContent(
    post: Post,
    comments: List<Comment>,
    onBackClick: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var showAllComments by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberLazyListState()
    
    // Calcular comentarios a mostrar
    val displayedComments = remember(comments, showAllComments) {
        if (showAllComments || comments.size <= 5) {
            comments
        } else {
            comments.take(5)
        }
    }

    // Altura de la imagen basada en el scroll
    val configuration = LocalConfiguration.current
    val imageHeight = 420.dp

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Imagen principal (Espaciador) ──
            item {
                Box(modifier = Modifier.fillMaxWidth().height(imageHeight))
            }

            // ── Cuerpo de la Publicación ──────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                        )
                        .padding(horizontal = 24.dp)
                        .padding(top = 20.dp)
                ) {
                    // Barra decorativa superior
                    Box(
                        Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(24.dp))

                    // Título y Autor
                    Text(
                        text = post.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp,
                        lineHeight = 38.sp
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant)) {
                                AsyncImage(
                                    model = post.authorPhotoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column {
                                Text(stringResource(R.string.post_detail_published_by), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(post.authorName.ifBlank { stringResource(R.string.post_detail_unknown_user) }, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Chips de Info
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoChipCard(Icons.Default.Pets, post.breed.ifBlank { post.animalType }, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                            InfoChipCard(
                                Icons.Default.Transgender,
                                if (post.animalType.lowercase().contains("hembra") || post.description.lowercase().contains("hembra"))
                                    stringResource(R.string.tag_female)
                                else
                                    stringResource(R.string.tag_male),
                                MaterialTheme.colorScheme.secondary,
                                Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoChipCard(Icons.Default.Straighten, UiText.fromSize(post.size).asString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                            InfoChipCard(
                                Icons.Default.Vaccines,
                                if (post.vaccinated) stringResource(R.string.post_detail_vacunada) else stringResource(R.string.post_detail_no_vacunas),
                                MaterialTheme.colorScheme.secondary,
                                Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(stringResource(R.string.post_detail_about, post.title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(text = post.description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)

                    Spacer(Modifier.height(32.dp))

                    // Ubicación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.post_location), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.filter_nearby), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Mapa
                    val isDark = isSystemInDarkTheme()
                    val petLocation = remember(post.latitude, post.longitude) { LatLng(post.latitude, post.longitude) }
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(petLocation, 15f) },
                            properties = MapProperties(mapStyleOptions = if (isDark) MapStyleOptions(MapStyles.DARK) else null),
                            uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false)
                        ) { Marker(state = rememberMarkerState(position = petLocation), title = post.title) }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Sección Comentarios Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.post_detail_comments), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (comments.isNotEmpty()) {
                            Text(
                                "${comments.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Input Comentario
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text(stringResource(R.string.post_detail_comment_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                                singleLine = true
                            )
                            TextButton(onClick = {
                                if (commentText.isNotBlank()) {
                                    onCommentSubmit(commentText)
                                    commentText = ""
                                    focusManager.clearFocus()
                                }
                            }) { Text(stringResource(R.string.post_detail_comment_post), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Lista de Comentarios (con fondo blanco para consistencia) ──
            if (comments.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.post_detail_no_comments), color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                if (comments.size > 5 && !showAllComments) {
                    item {
                        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                            TextButton(onClick = { showAllComments = true }, Modifier.padding(horizontal = 24.dp)) {
                                Text(stringResource(R.string.common_view_more_count, comments.size - 5), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(
                    items = displayedComments,
                    key = { it.id } // Usar ID de Firebase es lo más seguro
                ) { comment ->
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                        CommentItem(comment, Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                    }
                }

                if (showAllComments && comments.size > 5) {
                    item {
                        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                            TextButton(onClick = { showAllComments = false }, Modifier.padding(horizontal = 24.dp)) {
                                Text(stringResource(R.string.common_view_less), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Espaciador final con fondo para que no se vea el "hueco"
            item {
                Box(Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surface))
            }
        }

        // ── Imagen fija con parallax detrás ──
        val firstItemOffset by remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset } }
        val firstItemIndex by remember { derivedStateOf { scrollState.firstVisibleItemIndex } }

        if (firstItemIndex == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .graphicsLayer {
                        translationY = -firstItemOffset.toFloat() * 0.5f // Efecto parallax
                        alpha = 1f - (firstItemOffset.toFloat() / imageHeight.toPx()).coerceIn(0f, 1f)
                    }
            ) {
                if (post.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrls.first(),
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Pets, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // ── Botones de acción (Volver) fijos ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
    postId: String,
    petName: String,
    navController: NavController,
    viewModel: AdoptionRequestViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AdoptionRequestViewModel.UiEvent.Success -> {
                    navController.navigate(Screen.AdoptionSuccess) {
                        popUpTo(Screen.AdoptionRequest(postId, petName)) { inclusive = true }
                    }
                }
                is AdoptionRequestViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.uiText?.asString(context) ?: "Error"
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Solicitud de adopción", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(AdoptionRequestEvent.Submit(postId)) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Enviar solicitud", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cuadro informativo
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Estás a punto de solicitar la adopción de ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(petName)
                        }
                        append(". Cuéntale al publicador por qué serías un buen hogar.")
                    },
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }

            // Sección 1: Mensaje
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mensaje para el publicador", fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = state.message,
                    onValueChange = { viewModel.onEvent(AdoptionRequestEvent.OnMessageChange(it)) },
                    placeholder = { Text("Hola, estoy interesado en adoptar a $petName...", color = Color.Gray.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Sección 2: Información del hogar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏠 Información del hogar", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("(Opcional)", fontSize = 12.sp, color = Color.Gray)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tipo de vivienda", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectableChip(
                                label = "Casa",
                                icon = Icons.Default.Home,
                                selected = state.housingType == "Casa",
                                onClick = { viewModel.onEvent(AdoptionRequestEvent.OnHousingTypeChange("Casa")) },
                                modifier = Modifier.weight(1f)
                            )
                            SelectableChip(
                                label = "Apartamento",
                                icon = Icons.Default.LocationCity,
                                selected = state.housingType == "Apartamento",
                                onClick = { viewModel.onEvent(AdoptionRequestEvent.OnHousingTypeChange("Apartamento")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("¿Tienes espacio exterior?", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectableChip(
                                label = "Sí",
                                icon = Icons.Default.Nature, // Placeholder
                                selected = state.hasOutdoorSpace == "Sí",
                                onClick = { viewModel.onEvent(AdoptionRequestEvent.OnOutdoorSpaceChange("Sí")) },
                                modifier = Modifier.weight(1f)
                            )
                            SelectableChip(
                                label = "No",
                                icon = Icons.Default.Block,
                                selected = state.hasOutdoorSpace == "No",
                                onClick = { viewModel.onEvent(AdoptionRequestEvent.OnOutdoorSpaceChange("No")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Experiencia con mascotas", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectableChip(
                                label = "Sí tengo",
                                icon = Icons.Default.Pets,
                                selected = state.hasExperience == "Sí tengo",
                                onClick = { viewModel.onEvent(AdoptionRequestEvent.OnExperienceChange("Sí tengo")) },
                                modifier = Modifier.weight(1f)
                            )
                            SelectableChip(
                                label = "Soy nuevo",
                                icon = Icons.Default.CheckCircle,
                                selected = state.hasExperience == "Soy nuevo",
                                onClick = { viewModel.onEvent(AdoptionRequestEvent.OnExperienceChange("Soy nuevo")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Sección 3: Información de contacto
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Información de contacto", fontWeight = FontWeight.Bold)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Teléfono", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { viewModel.onEvent(AdoptionRequestEvent.OnPhoneChange(it)) },
                            placeholder = { Text("Ej: 55 1234 5678", color = Color.Gray.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color.Gray) }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Preferencia de contacto", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Column(Modifier.selectableGroup()) {
                            ContactPreferenceOption("Chat en PetHelp", state.contactPreference == "Chat en PetHelp") { viewModel.onEvent(AdoptionRequestEvent.OnContactPreferenceChange("Chat en PetHelp")) }
                            ContactPreferenceOption("WhatsApp", state.contactPreference == "WhatsApp") { viewModel.onEvent(AdoptionRequestEvent.OnContactPreferenceChange("WhatsApp")) }
                            ContactPreferenceOption("Llamada telefónica", state.contactPreference == "Llamada telefónica") { viewModel.onEvent(AdoptionRequestEvent.OnContactPreferenceChange("Llamada telefónica")) }
                        }
                    }
                }
            }

            // Info Distancia
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("El publicador verá que te encuentras aproximadamente a ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("3 km") }
                            append(" de distancia.")
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, 
                null, 
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label, 
                fontSize = 14.sp, 
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContactPreferenceOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestsScreen(
    navController: NavController,
    viewModel: AdoptionRequestsViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val pendingRequests = remember(state.requests) {
        state.requests.filter { request ->
            request.status == AdoptionRequestStatus.PENDING && request.postStatus != PostStatus.ADOPTED
        }
    }
    val historyRequests = remember(state.requests) {
        state.requests.filter { request ->
            request.status != AdoptionRequestStatus.PENDING || request.postStatus == PostStatus.ADOPTED
        }
    }
    val visibleRequests = if (selectedTab == 0) pendingRequests else historyRequests

    LaunchedEffect(pendingRequests.size, historyRequests.size) {
        if (selectedTab == 0 && pendingRequests.isEmpty() && historyRequests.isNotEmpty()) {
            selectedTab = 1
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.adoption_requests_title), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.adoption_requests_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AdoptionRequestsTabs(
                    pendingCount = pendingRequests.size,
                    historyCount = historyRequests.size,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                if (state.isActionLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (visibleRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdoptionRequestsEmptyState(
                            title = if (selectedTab == 0) {
                                if (state.requests.isEmpty()) R.string.adoption_requests_empty else R.string.adoption_requests_empty_pending
                            } else {
                                R.string.adoption_requests_empty_history
                            },
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = spacedBy(16.dp)
                    ) {
                        items(visibleRequests) { request ->
                            AdoptionRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptRequest(request) },
                                onReject = { viewModel.rejectRequest(request.id) },
                                isLoading = state.isActionLoading
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionRequestsTabs(
    pendingCount: Int,
    historyCount: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdoptionTabPill(
            text = stringResource(R.string.adoption_requests_tab_pending, pendingCount),
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        AdoptionTabPill(
            text = stringResource(R.string.adoption_requests_tab_history_count, historyCount),
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AdoptionTabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        tonalElevation = if (selected) 2.dp else 0.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AdoptionRequestsEmptyState(
    title: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AdoptionRequestItem(
    request: AdoptionRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    isLoading: Boolean
) {
    val displayStatus = if (request.status == AdoptionRequestStatus.PENDING && request.postStatus == PostStatus.ADOPTED) {
        AdoptionRequestStatus.REJECTED
    } else {
        request.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = request.requesterPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            request.requesterName.ifBlank { stringResource(R.string.common_none) },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        AdoptionRequestStatusChip(displayStatus)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.adoption_request_item_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatRelativeTime(request.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = request.message.ifBlank { stringResource(R.string.common_none) },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(text = UiText.fromHousingType(request.housingType).asString(), icon = Icons.Default.Home)
                    InfoChip(text = UiText.fromYesNo(request.hasOutdoorSpace).asString(), icon = Icons.Default.CheckCircle)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(text = UiText.fromExperience(request.hasExperience).asString(), icon = Icons.Default.Pets)
                    InfoChip(text = UiText.fromContactPreference(request.contactPreference).asString(), icon = Icons.Default.Chat)
                }
                if (request.phone.isNotBlank()) {
                    InfoChip(text = request.phone, icon = Icons.Default.Phone)
                }
            }

            if (displayStatus == AdoptionRequestStatus.PENDING) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.adoption_request_action_reject))
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.adoption_request_action_accept))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionRequestStatusChip(status: AdoptionRequestStatus) {
    val (containerColor, contentColor, labelRes) = when (status) {
        AdoptionRequestStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            R.string.adoption_request_status_pending
        )
        AdoptionRequestStatus.ACCEPTED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            R.string.adoption_request_status_accepted
        )
        AdoptionRequestStatus.REJECTED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            R.string.adoption_request_status_rejected
        )
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun InfoChip(text: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun formatRelativeTime(createdAt: Long): String {
    if (createdAt <= 0L) return stringResource(R.string.common_none)
    return DateUtils.getRelativeTimeSpanString(
        createdAt,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
@Composable
fun AdoptionSuccessScreen(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition()
    val confettiAnim = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Effect
        ConfettiEffect(progress = confettiAnim.value)

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(32.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Pets,
                        null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.adoption_success_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    stringResource(R.string.adoption_success_body),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { 
                        // Navegar al chat (asumiendo que hay una pantalla de chat o lista de chats)
                        navController.navigate(Screen.Feed) { 
                            popUpTo(Screen.Feed) { inclusive = true } 
                        } 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.adoption_success_btn_chat), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { navController.navigate(Screen.Feed) { popUpTo(Screen.Feed) { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Default.Home, null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.adoption_success_btn_home), color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConfettiEffect(progress: Float) {
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                color = listOf(Color.Yellow, Color.Cyan, Color.Magenta, Color.White).random(),
                size = Random.nextFloat() * 10f + 5f,
                speed = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val currentY = (p.y + progress * p.speed) % 1f
            drawRect(
                color = p.color,
                topLeft = Offset(p.x * size.width, currentY * size.height),
                size = androidx.compose.ui.geometry.Size(p.size, p.size),
                alpha = 0.7f
            )
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speed: Float
)

@Composable
private fun CommentItem(comment: Comment, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("es", "CO")) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar circular con inicial
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (comment.authorPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = comment.authorPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = comment.authorName.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Burbuja de comentario
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.authorName.ifBlank { "Usuario PetHelp" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(comment.createdAt)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
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
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.addImage(it) } }
    )

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message.asString(context))
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = (if (Color.Black == Color.Black) Color.Unspecified else Color.Transparent), // Placeholder for actual logic below
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                // To avoid drawing logic complexity inside the drawWithContent which might need hoisted colors
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                Box(modifier = Modifier.drawWithContent {
                    drawContent()
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }) {
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = stringResource(R.string.post_create_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.post_step_1_of_4),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                }
            }
        },
        bottomBar = {
            val outlineColor = MaterialTheme.colorScheme.outlineVariant
            Surface(
                color = MaterialTheme.colorScheme.surface,
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
                            containerColor = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.post_photo_limit_label),
                                color = MaterialTheme.colorScheme.primary,
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
                                                color = Color.White,
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
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                if (uiState.imageUris.size < 5) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = { Text(stringResource(R.string.post_title_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = { Text(stringResource(R.string.post_description_placeholder)) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // ── Categoría IA ────────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.post_ai_category_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(stringResource(R.string.post_ai_category_subtitle), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                        }

                        var expanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().height(56.dp).clickable { expanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                    Text(UiText.fromCategory(uiState.category).asString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                PostCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(UiText.fromCategory(category).asString()) },
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
                    Text(stringResource(R.string.post_animal_type_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                                onClick = { viewModel.updateAnimalType(value) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Campo de Raza (Breed)
                    Text(stringResource(R.string.post_breed_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = uiState.breed,
                        onValueChange = { viewModel.updateBreed(it) },
                        placeholder = { Text(stringResource(R.string.post_breed_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // ── Tamaño ──────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.post_size_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnimalSize.entries.forEach { size ->
                            SelectableChip(
                                label = UiText.fromSize(size).asString(),
                                selected = uiState.size == size,
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
private fun HealthGridItem(item: HealthItemData, modifier: Modifier) {
    val isSelected = item.isSelected
    val accentColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor.copy(0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant),
        onClick = { item.onToggle(!isSelected) }
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(item.icon, contentDescription = null, tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Text(item.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface)
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
private fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun <T> DropdownSelector(currentValue: String, options: List<Pair<String, T>>, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.outlineVariant, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
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

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val accentColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(50),
        color = if (selected) accentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) accentColor else MaterialTheme.colorScheme.outlineVariant
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
                        tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
        }
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
    val context = LocalContext.current

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
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val outlineColor = MaterialTheme.colorScheme.outlineVariant
            Surface(
                color = MaterialTheme.colorScheme.surface,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = stringResource(R.string.edit_post_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        bottomBar = {
            val outlineColor = MaterialTheme.colorScheme.outlineVariant
            Surface(
                color = MaterialTheme.colorScheme.surface,
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(stringResource(R.string.edit_post_cancel), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.savePost() },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.edit_post_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                        Text(stringResource(R.string.edit_post_photos_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(uiState.imageUrls) { index, url ->
                                Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp))) {
                                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    if (index == 0) {
                                        Surface(
                                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(stringResource(R.string.edit_post_photo_primary), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeImage(url) },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(0.4f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (uiState.imageUrls.size < 5) {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            } else {
                                                val errorMsg = context.getString(R.string.edit_post_photo_limit_error)
                                                viewModel.onShowSnackbar(errorMsg)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text(stringResource(R.string.edit_post_photo_add), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        Text(stringResource(R.string.edit_post_photo_reorder), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // ── INFO BÁSICA ───────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        EditField(label = stringResource(R.string.edit_post_name_label), value = uiState.title, onValueChange = viewModel::onTitleChange)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.edit_post_animal_type_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf(
                                    stringResource(R.string.post_animal_type_dog) to "Perro",
                                    stringResource(R.string.post_animal_type_cat) to "Gato",
                                    stringResource(R.string.post_animal_type_other) to "Otro"
                                ).forEach { (label, type) ->
                                    SelectableChip(
                                        label = label,
                                        selected = uiState.animalType == type,
                                        onClick = { viewModel.onAnimalTypeChange(type) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.edit_post_age_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                DropdownSelector(
                                    currentValue = UiText.fromAge(uiState.age).asString(),
                                    options = AnimalAge.entries.map { UiText.fromAge(it).asString() to it },
                                    onSelect = { viewModel.onAgeChange(it) }
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.edit_post_gender_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                DropdownSelector(
                                    currentValue = UiText.fromGender(uiState.gender).asString(),
                                    options = AnimalGender.entries.map { UiText.fromGender(it).asString() to it },
                                    onSelect = { viewModel.onGenderChange(it) }
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.edit_post_size_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)
                            ) {
                                AnimalSize.entries.forEach { size ->
                                    val isSelected = uiState.size == size
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { viewModel.onSizeChange(size) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(UiText.fromSize(size).asString(), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
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
                            Text(stringResource(R.string.edit_post_description_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            TextButton(onClick = { viewModel.improveDescriptionWithAI() }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.edit_post_ai_improve), color = MaterialTheme.colorScheme.tertiary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                            supportingText = { Text("${uiState.description.length}/500", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
                        )
                    }
                }

                // ── UBICACIÓN ─────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.edit_post_location_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(uiState.locationName.ifBlank { stringResource(R.string.edit_post_location_none) }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                    Text("${uiState.street}, ${uiState.neighborhood}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Text(stringResource(R.string.edit_post_location_change), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ── ESTADO ────────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.edit_post_status_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                Triple(PostStatus.ACTIVE, stringResource(R.string.edit_post_status_active), stringResource(R.string.edit_post_status_active_desc)),
                                Triple(PostStatus.PAUSED, stringResource(R.string.edit_post_status_paused), stringResource(R.string.edit_post_status_paused_desc)),
                                Triple(PostStatus.ADOPTED, stringResource(R.string.edit_post_status_adopted), stringResource(R.string.edit_post_status_adopted_desc))
                            ).forEach { (status, title, desc) ->
                                val isSelected = uiState.status == status
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (isSelected) MaterialTheme.colorScheme.primary.copy(0.1f) else Color.Transparent).border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)).clickable { viewModel.onStatusChange(status) }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { viewModel.onStatusChange(status) }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── SALUD ─────────────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.edit_post_health_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        val gridItems = listOf(
                            HealthItemData(uiState.vaccinated, stringResource(R.string.edit_post_health_vaccinated), Icons.Default.Vaccines, viewModel::onVaccinatedChange),
                            HealthItemData(uiState.sterilized, stringResource(R.string.edit_post_health_sterilized), Icons.Default.ContentCut, viewModel::onSterilizedChange),
                            HealthItemData(uiState.dewormed, stringResource(R.string.edit_post_health_dewormed), Icons.Default.BugReport, viewModel::onDewormedChange),
                            HealthItemData(uiState.specialCares, stringResource(R.string.edit_post_health_special), Icons.Default.MedicalServices, viewModel::onSpecialCaresChange)
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in 0 until gridItems.size step 2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    HealthGridItem(gridItems[i], modifier = Modifier.weight(1f))
                                    if (i + 1 < gridItems.size) {
                                        HealthGridItem(gridItems[i+1], modifier = Modifier.weight(1f))
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
