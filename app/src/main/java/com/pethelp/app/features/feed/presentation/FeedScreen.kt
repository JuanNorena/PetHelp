/**
 * Pantalla principal de Feed (muro de publicaciones) de PetHelp.
 *
 * Muestra la lista de publicaciones verificadas ordenadas por fecha,
 * con filtros por categoría, búsqueda por texto, y acciones de
 * favorito, voto y navegación al detalle de cada post.
 *
 * La lista se carga desde Firestore usando snapshot listeners para
 * mantener los datos actualizados en tiempo real.
 */
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.components.PetHelpCard
import com.pethelp.app.core.ui.components.PetHelpEmptyState
import com.pethelp.app.core.ui.components.PetHelpShimmerLoading
import com.pethelp.app.core.ui.components.PetHelpCategoryBadge
import com.pethelp.app.core.ui.components.PetHelpTagChip
import com.pethelp.app.core.ui.components.pethelpFadeScaleIn
import com.pethelp.app.core.ui.components.PETHELP_STAGGER_DELAY
import com.pethelp.app.core.ui.components.pethelpHeartbeatSpec
import com.pethelp.app.features.auth.presentation.AuthUiState
import com.pethelp.app.features.auth.presentation.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla principal de Feed (muro de publicaciones).
 *
 * Muestra la lista de publicaciones verificadas ordenadas por fecha,
 * con filtros por categoría, búsqueda por texto, y acciones de
 * favorito, voto y navegación al detalle de cada post.
 *
 * La lista se carga desde Firestore usando snapshot listeners para
 * mantener los datos actualizados en tiempo real.
 *
 * @param navController Controlador de navegación para transiciones entre pantallas.
 * @param viewModel ViewModel que gestiona el estado del feed y favoritos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
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
                        IconButton(onClick = { navController.navigate(Screen.Favorites) }) {
                            Icon(
                                imageVector = Icons.Filled.FavoriteBorder,
                                contentDescription = stringResource(R.string.feed_favorites_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { navController.navigate(Screen.AIQuiz) }) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = stringResource(R.string.ai_quiz_title),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { navController.navigate(Screen.Chat) }) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = stringResource(R.string.feed_messages_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChipUI(
                            label = stringResource(R.string.filter_all),
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) }
                        )
                    }

                    items(PostCategory.entries) { category ->
                        FilterChipUI(
                            label = categoryToDisplayName(category),
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = { PetHelpBottomNavBar(navController) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.allPublicPosts.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    repeat(4) {
                        FeedShimmerCard()
                    }
                }
            }

            uiState.error != null && uiState.allPublicPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    PetHelpEmptyState(
                        title = stringResource(R.string.error_generic),
                        subtitle = uiState.error?.asString().orEmpty(),
                        icon = Icons.Default.Warning,
                        actionLabel = stringResource(R.string.common_retry),
                        onAction = { viewModel.loadPublicPosts() }
                    )
                }
            }

            uiState.filteredPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    PetHelpEmptyState(
                        title = if (uiState.selectedCategory == null) {
                            stringResource(R.string.feed_empty_posts_title)
                        } else {
                            stringResource(R.string.feed_empty_filtered)
                        },
                        subtitle = stringResource(R.string.feed_empty_posts_subtitle),
                        icon = Icons.Default.Pets
                    )
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
                    items(uiState.filteredPosts, key = { it.id }) { post ->
                        val isFavorite = uiState.favoritesSet.contains(post.id)
                        val index = uiState.filteredPosts.indexOf(post)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = pethelpFadeScaleIn(delay = index * PETHELP_STAGGER_DELAY)
                        ) {
                            FeedPostCard(
                                post = post,
                                isFavorite = isFavorite,
                                onFavoriteClick = { viewModel.toggleFavorite(post.id) },
                                onClick = { navController.navigate(Screen.PostDetail(post.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Avatar del usuario autenticado mostrado en la parte superior del feed.
 *
 * @param authState Estado de autenticación del usuario.
 * @param onClick Acción al pulsar el avatar.
 */
@Composable
private fun FeedProfileAvatar(
    authState: AuthUiState,
    onClick: () -> Unit
) {
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
            AsyncImage(
                model = photoUrl,
                contentDescription = stringResource(R.string.profile_avatar_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
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
 * Tarjeta individual de publicación mostrada en el feed principal.
 *
 * @param post Datos de la publicación.
 * @param isFavorite Indica si está en favoritos.
 * @param onFavoriteClick Acción al alternar favorito.
 */
@Composable
private fun FeedPostCard(
    post: Post,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val heartScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = if (isFavorite) pethelpHeartbeatSpec() else androidx.compose.animation.core.tween(150),
        label = "heart_scale"
    )

    PetHelpCard(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.clickable(onClick = onClick)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
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
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    PetHelpCategoryBadge(
                        label = categoryToDisplayName(post.category),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (post.description.isNotBlank()) {
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PetHelpTagChip(label = genderToDisplayName(post.gender))
                        PetHelpTagChip(label = sizeToDisplayName(post.size))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        Text(
                            text = formatDate(post.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar favorito" else "Agregar favorito",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp * heartScale)
                    )
                }
            }
        }
    }
}

/**
 * Placeholder shimmer para las tarjetas de publicación mientras se cargan.
 */
@Composable
private fun FeedShimmerCard() {
    PetHelpCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            PetHelpShimmerLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp))
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PetHelpShimmerLoading(modifier = Modifier.width(60.dp).height(24.dp), shape = RoundedCornerShape(10.dp))
                    PetHelpShimmerLoading(modifier = Modifier.width(60.dp).height(24.dp), shape = RoundedCornerShape(10.dp))
                }
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
            }
        }
    }
}

/**
 * Chip de filtro por categoría en la barra superior del feed.
 *
 * @param label Texto del chip.
 * @param selected Indica si está activo.
 * @param onClick Acción al pulsar el chip.
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
 * Convierte una categoría de dominio a su nombre legible localizado.
 *
 * @param category Categoría del post.
 * @return Nombre traducido de la categoría.
 */
@Composable
private fun categoryToDisplayName(category: PostCategory): String {
    return when (category) {
        PostCategory.ADOPTION -> stringResource(R.string.category_adoption)
        PostCategory.LOST -> stringResource(R.string.category_lost)
        PostCategory.FOUND -> stringResource(R.string.category_found)
        PostCategory.TEMP_HOME -> stringResource(R.string.category_temp_home)
        PostCategory.VET_EVENT -> stringResource(R.string.category_vet_event)
    }
}

/**
 * Convierte un género animal a su nombre legible localizado.
 *
 * @param gender Género del animal.
 * @return Nombre traducido del género.
 */
@Composable
private fun genderToDisplayName(gender: AnimalGender): String {
    return when (gender) {
        AnimalGender.MALE -> stringResource(R.string.post_gender_male)
        AnimalGender.FEMALE -> stringResource(R.string.post_gender_female)
        AnimalGender.UNKNOWN -> stringResource(R.string.post_gender_unknown)
    }
}

/**
 * Convierte un tamaño animal a su nombre legible localizado.
 *
 * @param size Tamaño del animal.
 * @return Nombre traducido del tamaño.
 */
@Composable
private fun sizeToDisplayName(size: AnimalSize): String {
    return when (size) {
        AnimalSize.SMALL -> stringResource(R.string.tag_small)
        AnimalSize.MEDIUM -> stringResource(R.string.tag_medium)
        AnimalSize.LARGE -> stringResource(R.string.tag_large)
    }
}

/**
 * Formatea un timestamp a fecha legible "dd/MM/yyyy HH:mm".
 *
 * @param timestamp Timestamp en milisegundos.
 * @return Fecha formateada o "-" si es inválido.
 */
private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "-"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
