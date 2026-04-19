package com.pethelp.app.features.post.presentation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import coil.compose.AsyncImage
import com.pethelp.app.R
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReviewScreen(
    navController: NavController,
    postData: Screen.PostReview,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    LaunchedEffect(postData) {
        viewModel.updateTitle(postData.title)
        viewModel.updateDescription(postData.description)
        viewModel.updateCategory(PostCategory.valueOf(postData.category))
        viewModel.updateAnimalType(postData.animalType)
        viewModel.updateBreed(postData.breed)
        viewModel.updateAge(AnimalAge.valueOf(postData.age))
        viewModel.updateGender(AnimalGender.valueOf(postData.gender))
        viewModel.updateSize(AnimalSize.valueOf(postData.size))
        viewModel.updateVaccinated(postData.vaccinated)
        viewModel.updateDewormed(postData.dewormed)
        viewModel.updateSterilized(postData.sterilized)
        
        postData.behavior.forEach { behaviorName ->
            val behavior = PetBehavior.valueOf(behaviorName)
            if (!uiState.behavior.contains(behavior)) {
                viewModel.toggleBehavior(behavior)
            }
        }
        
        postData.imageUris.forEach { uriString ->
            val uri = Uri.parse(uriString)
            if (!uiState.imageUris.contains(uri)) {
                viewModel.addImage(uri)
            }
        }
        viewModel.updateAddress(postData.street, postData.neighborhood, postData.city)
        viewModel.updateLocation(postData.latitude, postData.longitude, postData.locationName)
    }

    // Manejar el botón de atrás cuando se muestra el éxito
    BackHandler(enabled = uiState.isSuccess) {
        navController.navigate(Screen.Feed) {
            popUpTo(Screen.Feed) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.post_review_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.post_step_4_of_4),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(24.dp)
                    ) {
                        Button(
                            onClick = { viewModel.createPost() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.Publish, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_publish_pet), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Cuadro de información (Adaptado a Dark Mode)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.post_review_info_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Card Principal de Datos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // SECCIÓN: Datos principales
                        SectionHeader(
                            title = stringResource(R.string.post_review_section_main), 
                            icon = Icons.Default.Widgets, 
                            textColor = MaterialTheme.colorScheme.onSurface,
                            onEdit = { 
                                navController.popBackStack(Screen.CreatePost, inclusive = false)
                            }
                        )
                        
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AsyncImage(
                                model = postData.imageUris.firstOrNull(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = postData.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Tag(text = UiText.fromCategory(PostCategory.valueOf(postData.category)).asString().uppercase(), color = MaterialTheme.colorScheme.primaryContainer, textColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Tag(text = postData.animalType.uppercase(), color = MaterialTheme.colorScheme.surfaceVariant, textColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Tag(text = UiText.fromSize(AnimalSize.valueOf(postData.size)).asString().uppercase(), color = MaterialTheme.colorScheme.surfaceVariant, textColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = postData.description,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // SECCIÓN: Ubicación
                        SectionHeader(
                            title = stringResource(R.string.post_review_section_location), 
                            icon = Icons.Default.LocationOn, 
                            iconColor = MaterialTheme.colorScheme.error, 
                            textColor = MaterialTheme.colorScheme.onSurface,
                            onEdit = { 
                                navController.popBackStack(Screen.LocationSelection::class, inclusive = false)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${postData.street}, ${postData.neighborhood}, ${postData.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // SECCIÓN: Detalles y Salud
                        SectionHeader(
                            title = stringResource(R.string.post_review_section_health), 
                            icon = Icons.Default.HealthAndSafety, 
                            iconColor = MaterialTheme.colorScheme.secondary, 
                            textColor = MaterialTheme.colorScheme.onSurface,
                            onEdit = { 
                                navController.popBackStack(Screen.PostDetails::class, inclusive = false)
                            }
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                InfoLabel(label = stringResource(R.string.post_review_label_age_gender))
                                Text(
                                    "${UiText.fromAge(AnimalAge.valueOf(postData.age)).asString()} • ${UiText.fromGender(AnimalGender.valueOf(postData.gender)).asString()}", 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                InfoLabel(label = stringResource(R.string.post_review_label_health))
                                val health = mutableListOf<String>()
                                if (postData.vaccinated) health.add(stringResource(R.string.post_review_vaccinated))
                                if (postData.dewormed) health.add(stringResource(R.string.post_review_dewormed))
                                if (postData.sterilized) health.add(stringResource(R.string.post_review_sterilized))
                                Text(
                                    health.joinToString(", ").ifEmpty { stringResource(R.string.post_review_no_data) }, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (postData.behavior.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            InfoLabel(label = stringResource(R.string.post_review_label_behavior))
                            Row(
                                modifier = Modifier.padding(top = 4.dp), 
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                postData.behavior.forEach { b ->
                                    Tag(text = UiText.fromBehavior(PetBehavior.valueOf(b)).asString(), color = MaterialTheme.colorScheme.surfaceVariant, textColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // --- Overlay de Publicación Exitosa (Figma Match) ---
        AnimatedVisibility(
            visible = uiState.isSuccess,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Círculo Central con Check
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Círculos de colores decorativos
                        Box(modifier = Modifier.size(140.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(80.dp))
                        }
                        
                        // Decoración flotante (Círculos pequeños simulados)
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 20.dp).size(24.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 40.dp, end = 10.dp).size(16.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                        Box(modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 10.dp).size(12.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.post_success_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.post_success_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    // Botón: Ver publicación
                    Button(
                        onClick = {
                            uiState.createdPostId?.let { id ->
                                navController.navigate(Screen.PostDetail(id)) {
                                    popUpTo(Screen.Feed)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.btn_view_post), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón: Volver a Inicio
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.Feed) {
                                popUpTo(Screen.Feed) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.btn_back_home), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector, iconColor: Color = MaterialTheme.colorScheme.primary, textColor: Color, onEdit: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.common_edit),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { onEdit() }
        )
    }
}

@Composable
fun Tag(text: String, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun InfoLabel(label: String) {
    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
}