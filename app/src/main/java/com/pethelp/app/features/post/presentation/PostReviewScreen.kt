package com.pethelp.app.features.post.presentation

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.domain.model.AnimalAge
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.PetBehavior
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReviewScreen(
    navController: NavController,
    postData: Screen.PostReview,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    // Colores dinámicos
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) White else TextPrimary
    val secondaryTextColor = if (isDark) White.copy(alpha = 0.7f) else TextSecondary
    val outlineColor = if (isDark) PetHelpOutlineDark else PetHelpOutline
    val cardBackgroundColor = if (isDark) SurfaceVariantDark.copy(alpha = 0.3f) else SurfaceVariantLight.copy(alpha = 0.3f)

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

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Screen.Feed) {
                popUpTo(Screen.CreatePost) { inclusive = true }
            }
        }
    }

    Scaffold(
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
                            contentDescription = "Volver",
                            modifier = Modifier.size(22.dp),
                            tint = textColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = stringResource(R.string.post_review_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.post_step_4_of_4),
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = PetHelpPrimary,
                        modifier = Modifier.size(22.dp)
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
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.createPost()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetHelpPrimary
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.btn_publish),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PetHelpPrimary,
                trackColor = if (isDark) SurfaceVariantDark else SurfaceVariantLight
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    stringResource(R.string.post_review_instruction),
                    style = MaterialTheme.typography.bodyLarge,
                    color = secondaryTextColor
                )
                
                // Resumen de la publicación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            stringResource(R.string.post_summary_general),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PetHelpPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        SummaryRow(label = stringResource(R.string.post_summary_title), value = postData.title, textColor = textColor, secondaryTextColor = secondaryTextColor)
                        SummaryRow(label = stringResource(R.string.post_summary_category), value = categoryNameToDisplay(postData.category), textColor = textColor, secondaryTextColor = secondaryTextColor)
                        SummaryRow(label = stringResource(R.string.post_summary_animal_type), value = postData.animalType, textColor = textColor, secondaryTextColor = secondaryTextColor)
                        SummaryRow(label = stringResource(R.string.post_summary_breed), value = postData.breed, textColor = textColor, secondaryTextColor = secondaryTextColor)
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = outlineColor.copy(alpha = 0.5f)
                        )
                        
                        Text(
                            stringResource(R.string.post_summary_location),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PetHelpPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        SummaryRow(label = stringResource(R.string.post_summary_address), value = "${postData.street}, ${postData.neighborhood}", textColor = textColor, secondaryTextColor = secondaryTextColor)
                        SummaryRow(label = stringResource(R.string.post_summary_city), value = postData.city, textColor = textColor, secondaryTextColor = secondaryTextColor)

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = outlineColor.copy(alpha = 0.5f)
                        )

                        Text(
                            stringResource(R.string.post_summary_details),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PetHelpPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        SummaryRow(label = stringResource(R.string.post_summary_age), value = ageNameToDisplay(postData.age), textColor = textColor, secondaryTextColor = secondaryTextColor)
                        SummaryRow(label = stringResource(R.string.post_summary_gender), value = genderNameToDisplay(postData.gender), textColor = textColor, secondaryTextColor = secondaryTextColor)
                        SummaryRow(label = stringResource(R.string.post_summary_size), value = sizeNameToDisplay(postData.size), textColor = textColor, secondaryTextColor = secondaryTextColor)
                        
                        val healthStates = mutableListOf<String>()
                        if (postData.vaccinated) healthStates.add(stringResource(R.string.post_vaccinated_label))
                        if (postData.dewormed) healthStates.add(stringResource(R.string.post_dewormed_label))
                        if (postData.sterilized) healthStates.add(stringResource(R.string.post_sterilized_label))
                        
                        if (healthStates.isNotEmpty()) {
                            SummaryRow(label = stringResource(R.string.post_summary_health), value = healthStates.joinToString(", "), textColor = textColor, secondaryTextColor = secondaryTextColor)
                        }

                        if (postData.behavior.isNotEmpty()) {
                            SummaryRow(label = stringResource(R.string.post_summary_behavior), value = behaviorNamesToDisplay(postData.behavior), textColor = textColor, secondaryTextColor = secondaryTextColor)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun categoryNameToDisplay(rawValue: String): String {
    val category = PostCategory.entries.find { it.name == rawValue } ?: return rawValue
    return when (category) {
        PostCategory.ADOPTION -> stringResource(R.string.category_adoption)
        PostCategory.LOST -> stringResource(R.string.category_lost)
        PostCategory.FOUND -> stringResource(R.string.category_found)
        PostCategory.TEMP_HOME -> stringResource(R.string.category_temp_home)
        PostCategory.VET_EVENT -> stringResource(R.string.category_vet_event)
    }
}

@Composable
private fun ageNameToDisplay(rawValue: String): String {
    val age = AnimalAge.entries.find { it.name == rawValue } ?: return rawValue
    return age.toDisplayName()
}

@Composable
private fun genderNameToDisplay(rawValue: String): String {
    val gender = AnimalGender.entries.find { it.name == rawValue } ?: return rawValue
    return gender.toDisplayName()
}

@Composable
private fun sizeNameToDisplay(rawValue: String): String {
    val size = AnimalSize.entries.find { it.name == rawValue } ?: return rawValue
    return when (size) {
        AnimalSize.SMALL -> stringResource(R.string.tag_small)
        AnimalSize.MEDIUM -> stringResource(R.string.tag_medium)
        AnimalSize.LARGE -> stringResource(R.string.tag_large)
    }
}

@Composable
private fun behaviorNamesToDisplay(values: List<String>): String {
    val labels = mutableListOf<String>()
    for (value in values) {
        labels.add(behaviorNameToDisplay(value))
    }
    return labels.joinToString(", ")
}

@Composable
private fun behaviorNameToDisplay(rawValue: String): String {
    val behavior = PetBehavior.entries.find { it.name == rawValue } ?: return rawValue
    return behavior.toDisplayName()
}

@Composable
fun SummaryRow(label: String, value: String, textColor: Color, secondaryTextColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = secondaryTextColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
