package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pethelp.app.R
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla de publicaciones favoritas del usuario.
 *
 * Muestra el listado en modo grilla o lista, permite filtrar por categoria y
 * navegar al detalle de cada publicacion.
 *
 * @param navController Controlador de navegacion para volver y abrir detalle/feed.
 * @param viewModel ViewModel que provee estado y acciones de favoritos.
 */
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.favorites_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            stringResource(R.string.favorites_subtitle, uiState.posts.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    ViewModeToggle(
                        isGridView = uiState.isGridView,
                        onToggle = viewModel::toggleViewMode
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FavoritesFilterRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )

            val displayPosts = uiState.filteredPosts

            Spacer(Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.favorites_results, displayPosts.size),
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (displayPosts.isEmpty()) {
                EmptyFavorites()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        if (uiState.isGridView) {
                            FavoritesGrid(
                                posts = displayPosts,
                                onPostClick = { navController.navigate(Screen.PostDetail(it.id)) },
                                onFavoriteClick = { viewModel.toggleFavorite(it.id) }
                            )
                        } else {
                            FavoritesList(
                                posts = displayPosts,
                                onPostClick = { navController.navigate(Screen.PostDetail(it.id)) },
                                onFavoriteClick = { viewModel.toggleFavorite(it.id) }
                            )
                        }
                    }

                    item {
                        ExploreBanner(onExploreClick = {
                            navController.navigate(Screen.Feed) {
                                popUpTo(Screen.Favorites) { inclusive = true }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(isGridView: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isGridView) Color.White else Color.Transparent)
                    .clickable { if (!isGridView) onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.GridView,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (!isGridView) Color.White else Color.Transparent)
                    .clickable { if (isGridView) onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = if (!isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FavoritesFilterRow(
    selectedCategory: PostCategory?,
    onCategorySelected: (PostCategory?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChipUI(
                label = stringResource(R.string.filter_all),
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }

        items(PostCategory.entries) { category ->
            FilterChipUI(
                label = UiText.fromCategory(category).asString(),
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

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

@Composable
private fun FavoritesGrid(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onFavoriteClick: (Post) -> Unit
) {
    // Non-scrollable grid inside LazyColumn
    val columns = 2
    val rows = (posts.size + columns - 1) / columns
    
    Column(modifier = Modifier.padding(16.dp)) {
        for (i in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (j in 0 until columns) {
                    val index = i * columns + j
                    if (index < posts.size) {
                        Box(modifier = Modifier.weight(1f)) {
                            FavoriteGridCard(posts[index], onPostClick, onFavoriteClick)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (i < rows - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FavoriteGridCard(
    post: Post,
    onPostClick: (Post) -> Unit,
    onFavoriteClick: (Post) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPostClick(post) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                AsyncImage(
                    model = post.imageUrls.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                // Badge category
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        UiText.fromCategory(post.category).asString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp
                    )
                }
                // Favorite Button
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onFavoriteClick(post) }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
                // Distance badge
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(stringResource(R.string.map_distance_km, 2.0), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Icon(
                        if(post.gender.name == "MALE") Icons.Default.Male else Icons.Default.Female,
                        null,
                        tint = if(post.gender.name == "MALE") Color(0xFF2196F3) else Color(0xFFE91E63),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        UiText.fromSize(post.size).asString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(post.breed, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onPostClick(post) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.btn_adopt), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FavoritesList(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onFavoriteClick: (Post) -> Unit
) {
    // Non-scrollable list inside LazyColumn
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        posts.forEach { post ->
            FavoriteListCard(post, onPostClick, onFavoriteClick)
        }
    }
}

@Composable
private fun FavoriteListCard(
    post: Post,
    onPostClick: (Post) -> Unit,
    onFavoriteClick: (Post) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPostClick(post) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            UiText.fromCategory(post.category).asString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(post.breed, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Text(
                        stringResource(R.string.map_distance_km, 2.0),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if(post.gender.name == "MALE") Icons.Default.Male else Icons.Default.Female,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        UiText.fromSize(post.size).asString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                    Text(
                        post.city,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp),
                        maxLines = 1
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onFavoriteClick(post) }) {
                    Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ExploreBanner(onExploreClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.favorites_explore_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF00796B)
                )
                Text(
                    stringResource(R.string.favorites_explore_subtitle),
                    fontSize = 12.sp,
                    color = Color(0xFF00796B).copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onExploreClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.favorites_explore_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(60.dp).padding(start = 8.dp),
                tint = Color(0xFF00BFA5).copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.favorites_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.favorites_empty_desc),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
