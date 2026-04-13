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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.pethelp.app.core.ui.theme.PetHelpSecondary
import com.pethelp.app.core.ui.theme.Orange

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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.post_details_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.post_step_3_of_4),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
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
                                city = postData.city
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BFA5)
                    )
                ) {
                    Icon(painterResource(id = android.R.drawable.ic_menu_edit), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.btn_next_review),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LinearProgressIndicator(
                progress = { 0.75f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF00BFA5),
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            // Edad Aproximada
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.post_approx_age),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalAge.entries.forEach { age ->
                        FilterChip(
                            selected = selectedAge == age,
                            onClick = { selectedAge = age },
                            label = { Text(age.toDisplayName()) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0F2F1),
                                selectedLabelColor = Color(0xFF00BFA5),
                                selectedLeadingIconColor = Color(0xFF00BFA5)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedAge == age,
                                borderColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedBorderColor = Color(0xFF00BFA5)
                            ),
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                    }
                }
            }

            // Sexo
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.post_gender),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalGender.entries.forEach { gender ->
                        FilterChip(
                            selected = selectedGender == gender,
                            onClick = { selectedGender = gender },
                            label = { Text(gender.toDisplayName()) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0F2F1),
                                selectedLabelColor = Color(0xFF00BFA5)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedGender == gender,
                                borderColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedBorderColor = Color(0xFF00BFA5)
                            ),
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                    }
                }
            }

            // Estado de Salud
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(id = android.R.drawable.ic_menu_myplaces), contentDescription = null, tint = Color(0xFF00BFA5), modifier = Modifier.size(20.dp))
                    Text(
                        stringResource(R.string.post_health_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(id = android.R.drawable.ic_menu_view), contentDescription = null, tint = PetHelpSecondary, modifier = Modifier.size(20.dp))
                    Text(
                        stringResource(R.string.post_behavior),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PetBehavior.entries.forEach { behavior ->
                        val isSelected = selectedBehaviors.contains(behavior)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedBehaviors.remove(behavior)
                                else selectedBehaviors.add(behavior)
                            },
                            label = { Text(behavior.toDisplayName()) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFF3E0),
                                selectedLabelColor = PetHelpSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedBorderColor = PetHelpSecondary
                            )
                        )
                    }
                }
                
                Text(
                    stringResource(R.string.post_behavior_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(80.dp))
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
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00BFA5))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, fontSize = 12.sp, color = Color.Gray)
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
