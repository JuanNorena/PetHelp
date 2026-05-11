package com.pethelp.app.features.ai.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.navigation.Screen
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIResultsScreen(
    recommendations: String,
    navController: NavController,
    quizAnswers: Map<String, String>,
    onRestart: () -> Unit
) {
    var matchedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoadingPosts by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    androidx.compose.runtime.LaunchedEffect(quizAnswers) {
        if (quizAnswers.isEmpty()) {
            isLoadingPosts = false
            return@LaunchedEffect
        }

        val petType = quizAnswers["pet_type"]
        if (petType.isNullOrBlank()) {
            isLoadingPosts = false
            return@LaunchedEffect
        }

        db.collection("posts")
            .whereEqualTo("animalType", petType)
            .limit(6)
            .get()
            .addOnSuccessListener { snapshot ->
                matchedPosts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                isLoadingPosts = false
            }
            .addOnFailureListener {
                matchedPosts = emptyList()
                isLoadingPosts = false
            }
    }
    
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Text(
                        text = stringResource(R.string.ai_results_searching_matches),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

                items(matchedPosts) { post ->
                    CompatiblePostCard(
                        post = post,
                        onClick = { navController.navigate(Screen.PostDetail(post.id)) }
                    )
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.ai_results_no_matches),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        shadowElevation = 2.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (post.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = stringResource(R.string.moderation_photo_thumbnail_desc),
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = if (post.imageUrls.isEmpty()) 16.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = post.title.ifBlank { post.animalType },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.ai_results_view_post))
                }
                Spacer(Modifier.height(4.dp))
            }
        }
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
