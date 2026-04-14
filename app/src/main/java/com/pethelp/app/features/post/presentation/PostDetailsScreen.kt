package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.domain.model.AnimalAge
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.PetBehavior
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    postData: Screen.PostDetails
) {
    var selectedAge by remember { mutableStateOf(AnimalAge.YOUNG) }
    var selectedGender by remember { mutableStateOf(AnimalGender.MALE) }
    var vaccinated by remember { mutableStateOf(false) }
    var dewormed by remember { mutableStateOf(false) }
    var sterilized by remember { mutableStateOf(false) }
    val selectedBehaviors = remember { mutableStateListOf<PetBehavior>() }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Surface(
                color = SurfaceLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = PetHelpOutline,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
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
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.post_details_title),
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
                        tint = PetHelpPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = SurfaceLight,
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawLine(
                        color = PetHelpOutline,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
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
                            navController.navigate(
                                Screen.PostReview(
                                    title = postData.title,
                                    description = postData.description,
                                    category = postData.category,
                                    animalType = postData.animalType,
                                    age = selectedAge.name,
                                    gender = selectedGender.name,
                                    size = postData.size,
                                    vaccinated = vaccinated,
                                    dewormed = dewormed,
                                    sterilized = sterilized,
                                    behavior = selectedBehaviors.map { it.name },
                                    imageUris = postData.imageUris,
                                    street = postData.street,
                                    neighborhood = postData.neighborhood,
                                    city = postData.city,
                                    latitude = postData.latitude,
                                    longitude = postData.longitude,
                                    locationName = postData.locationName
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetHelpPrimary
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.btn_next_review),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.45.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PetHelpPrimary,
                    trackColor = SurfaceVariantLight
                )

                // Edad Aproximada
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.post_approx_age),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        letterSpacing = 0.35.sp
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimalAge.entries.forEach { age ->
                            val isSelected = selectedAge == age
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) PetHelpPrimary.copy(alpha = 0.1f) else SurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) PetHelpPrimary else PetHelpOutline),
                                onClick = { selectedAge = age }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = age.toDisplayName(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) PetHelpPrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Sexo
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.post_gender),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        letterSpacing = 0.35.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimalGender.entries.forEach { gender ->
                            val isSelected = selectedGender == gender
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) PetHelpPrimary.copy(alpha = 0.1f) else SurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) PetHelpPrimary else PetHelpOutline),
                                onClick = { selectedGender = gender }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = gender.toDisplayName(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) PetHelpPrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Estado de Salud
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PetHelpPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                tint = PetHelpPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.post_health_status),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    HealthStatusItem(
                        title = stringResource(R.string.post_vaccinated_label),
                        description = stringResource(R.string.post_vaccinated_desc),
                        selected = vaccinated,
                        onToggle = { vaccinated = it }
                    )
                    HealthStatusItem(
                        title = stringResource(R.string.post_dewormed_label),
                        description = stringResource(R.string.post_dewormed_desc),
                        selected = dewormed,
                        onToggle = { dewormed = it }
                    )
                    HealthStatusItem(
                        title = stringResource(R.string.post_sterilized_label),
                        description = stringResource(R.string.post_sterilized_desc),
                        selected = sterilized,
                        onToggle = { sterilized = it }
                    )
                }

                // Comportamiento
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PetHelpSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                tint = PetHelpSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.post_behavior),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PetBehavior.entries.forEach { behavior ->
                            val isSelected = selectedBehaviors.contains(behavior)
                            Surface(
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) PetHelpSecondary.copy(alpha = 0.1f) else SurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) PetHelpSecondary else PetHelpOutline),
                                onClick = {
                                    if (isSelected) selectedBehaviors.remove(behavior)
                                    else selectedBehaviors.add(behavior)
                                }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = behavior.toDisplayName(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) PetHelpSecondary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    
                    Text(
                        stringResource(R.string.post_behavior_instruction),
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun HealthStatusItem(
    title: String,
    description: String,
    selected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        onClick = { onToggle(!selected) },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) PetHelpPrimary.copy(alpha = 0.05f) else SurfaceLight,
        border = BorderStroke(
            1.dp, 
            if (selected) PetHelpPrimary.copy(alpha = 0.3f) else PetHelpOutline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PetHelpPrimary,
                    unselectedColor = PetHelpOutline
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun AnimalAge.toDisplayName(): String = when(this) {
    AnimalAge.PUPPY -> stringResource(R.string.post_age_puppy)
    AnimalAge.YOUNG -> stringResource(R.string.post_age_young)
    AnimalAge.ADULT -> stringResource(R.string.post_age_adult)
    AnimalAge.SENIOR -> stringResource(R.string.post_age_senior)
}

@Composable
fun AnimalGender.toDisplayName(): String = when(this) {
    AnimalGender.MALE -> stringResource(R.string.post_gender_male)
    AnimalGender.FEMALE -> stringResource(R.string.post_gender_female)
    AnimalGender.UNKNOWN -> stringResource(R.string.post_gender_unknown)
}

@Composable
fun PetBehavior.toDisplayName(): String = when(this) {
    PetBehavior.PLAYFUL -> stringResource(R.string.post_behavior_playful)
    PetBehavior.CALM -> stringResource(R.string.post_behavior_calm)
    PetBehavior.PROTECTIVE -> stringResource(R.string.post_behavior_protective)
    PetBehavior.SHY -> stringResource(R.string.post_behavior_shy)
    PetBehavior.SOCIABLE -> stringResource(R.string.post_behavior_sociable)
    PetBehavior.INDEPENDENT -> stringResource(R.string.post_behavior_independent)
    PetBehavior.AFFECTIONATE -> stringResource(R.string.post_behavior_affectionate)
    PetBehavior.ACTIVE -> stringResource(R.string.post_behavior_active)
}
