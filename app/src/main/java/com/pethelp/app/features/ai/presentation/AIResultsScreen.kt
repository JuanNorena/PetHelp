package com.pethelp.app.features.ai.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.ui.components.PetHelpCard
import com.pethelp.app.core.ui.components.PetHelpEmptyState
import com.pethelp.app.core.ui.components.PetHelpShimmerLoading
import com.pethelp.app.core.ui.components.PetHelpTagChip
import com.pethelp.app.core.ui.components.pethelpFadeScaleIn
import com.pethelp.app.core.ui.components.PETHELP_STAGGER_DELAY
import androidx.compose.runtime.remember
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.navigation.Screen
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIResultsScreen(
    recommendations: String,
    navController: NavController,
    matchedPosts: List<Post>,
    isLoadingPosts: Boolean,
    onRestart: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.ai_results_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = stringResource(R.string.ai_results_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Encabezado con icono
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.ai_results_ready_title),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.ai_results_ready_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                PetHelpCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    RecommendationContent(recommendations = recommendations)
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                
                // Botón para ver más detalles (navegar a feed con filtros)
                Button(
                    onClick = {
                        navController.popBackStack()
                        // Aquí podrías pasar los filtros a FeedScreen
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ai_results_view_available),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Botón para reiniciar cuestionario
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.weight(0.5f))
                    Text(
                        text = stringResource(R.string.ai_results_restart),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            if (isLoadingPosts) {
                item {
                    Spacer(Modifier.height(8.dp))
                    repeat(3) {
                        AIResultShimmerCard()
                        Spacer(Modifier.height(12.dp))
                    }
                }
            } else if (matchedPosts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ai_results_available_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(matchedPosts, key = { it.id }) { post ->
                    val index = matchedPosts.indexOf(post)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = pethelpFadeScaleIn(delay = index * PETHELP_STAGGER_DELAY)
                    ) {
                        CompatiblePostCard(
                            post = post,
                            onClick = { navController.navigate(Screen.PostDetail(post.id)) }
                        )
                    }
                }
            } else {
                item {
                    PetHelpEmptyState(
                        title = stringResource(R.string.ai_results_no_matches_title),
                        subtitle = stringResource(R.string.ai_results_no_matches_subtitle),
                        icon = Icons.Default.Pets
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationContent(recommendations: String) {
    val sections = remember(recommendations) { parseRecommendationSections(recommendations) }

    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = stringResource(R.string.ai_results_recommendations_title),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (sections.isEmpty()) {
            Text(
                text = recommendations,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 21.sp
            )
        } else {
            sections["PERFIL_IDEAL"]?.let {
                RecommendationBlock(stringResource(R.string.ai_results_profile_title), listOf(it))
            }
            sections["RECOMENDACIONES"]?.let {
                RecommendationBlock(stringResource(R.string.ai_results_recommended_pets_title), splitAiList(it))
            }
            sections["CUIDADOS"]?.let {
                RecommendationBlock(stringResource(R.string.ai_results_care_title), splitAiList(it))
            }
            sections["SIGUIENTE_PASO"]?.let {
                RecommendationBlock(stringResource(R.string.ai_results_next_step_title), listOf(it))
            }
        }
    }
}

@Composable
private fun RecommendationBlock(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        items.filter { it.isNotBlank() }.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CompatiblePostCard(post: Post, onClick: () -> Unit) {
    PetHelpCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (post.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrls.first(),
                        contentDescription = stringResource(R.string.moderation_photo_thumbnail_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PetHelpTagChip(
                    label = categoryToDisplayName(post.category),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = post.title.ifBlank { post.animalType },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PetHelpTagChip(label = genderToDisplayName(post.gender))
                    PetHelpTagChip(label = sizeToDisplayName(post.size))
                }
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
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.ai_results_view_post))
                }
            }
        }
    }
}

@Composable
private fun AIResultShimmerCard() {
    PetHelpCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            PetHelpShimmerLoading(
                modifier = Modifier.fillMaxWidth().height(168.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PetHelpShimmerLoading(modifier = Modifier.fillMaxWidth(0.65f).height(18.dp))
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

@Composable
private fun genderToDisplayName(gender: AnimalGender): String {
    return when (gender) {
        AnimalGender.MALE -> stringResource(R.string.post_gender_male)
        AnimalGender.FEMALE -> stringResource(R.string.post_gender_female)
        AnimalGender.UNKNOWN -> stringResource(R.string.post_gender_unknown)
    }
}

@Composable
private fun sizeToDisplayName(size: AnimalSize): String {
    return when (size) {
        AnimalSize.SMALL -> stringResource(R.string.tag_small)
        AnimalSize.MEDIUM -> stringResource(R.string.tag_medium)
        AnimalSize.LARGE -> stringResource(R.string.tag_large)
    }
}

private fun parseRecommendationSections(content: String): Map<String, String> {
    val keys = setOf("PERFIL_IDEAL", "RECOMENDACIONES", "CUIDADOS", "SIGUIENTE_PASO")
    return content.lineSequence()
        .mapNotNull { line ->
            val index = line.indexOf("=")
            if (index <= 0) return@mapNotNull null
            val key = line.substring(0, index).trim().uppercase()
            val value = line.substring(index + 1).trim()
            if (key in keys && value.isNotBlank()) key to value else null
        }
        .toMap()
}

private fun splitAiList(value: String): List<String> {
    return value.split("|").map { it.trim().trim('-', '•') }.filter { it.isNotBlank() }
}
