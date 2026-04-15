package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    navController: NavController,
    postData: Screen.LocationSelection
) {
    val isDark = isSystemInDarkTheme()
    
    // Colores dinámicos
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) White else TextPrimary
    val secondaryTextColor = if (isDark) White.copy(alpha = 0.7f) else TextSecondary
    val hintColor = if (isDark) White.copy(alpha = 0.5f) else TextHint
    val outlineColor = if (isDark) PetHelpOutlineDark else PetHelpOutline
    val warningContainerColor = if (isDark) PetHelpTertiaryDark.copy(alpha = 0.15f) else TertiaryLightContainer
    val onWarningContainerColor = if (isDark) PetHelpTertiary else OnTertiaryLightContainer

    var street by remember { mutableStateOf(postData.street) }
    var neighborhood by remember { mutableStateOf(postData.neighborhood) }
    var city by remember { mutableStateOf(postData.city) }

    // Estado del mapa
    var selectedLatitude by remember { mutableDoubleStateOf(postData.latitude) }
    var selectedLongitude by remember { mutableDoubleStateOf(postData.longitude) }
    var locationName by remember { mutableStateOf(postData.locationName) }

    val initialLatLng = remember {
        if (selectedLatitude != 0.0 || selectedLongitude != 0.0) {
            LatLng(selectedLatitude, selectedLongitude)
        } else {
            LatLng(4.535, -75.675) // Default Armenia, Quindío
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    val isFormValid = street.isNotBlank() && neighborhood.isNotBlank() && city.isNotBlank()

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
                            contentDescription = stringResource(R.string.common_back),
                            modifier = Modifier.size(22.dp),
                            tint = textColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = stringResource(R.string.post_location),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.post_step_2_of_4),
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = PetHelpPrimary,
                        modifier = Modifier.size(24.dp)
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
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(
                                Screen.PostDetails(
                                    title = postData.title,
                                    description = postData.description,
                                    category = postData.category,
                                    animalType = postData.animalType,
                                    breed = postData.breed,
                                    size = postData.size,
                                    imageUris = postData.imageUris,
                                    street = street,
                                    neighborhood = neighborhood,
                                    city = city,
                                    latitude = selectedLatitude,
                                    longitude = selectedLongitude,
                                    locationName = locationName
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetHelpPrimary,
                            disabledContainerColor = PetHelpPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.btn_next_details),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            LinearProgressIndicator(
                progress = { 0.5f },
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
                    stringResource(R.string.post_location_instruction),
                    fontSize = 16.sp,
                    color = secondaryTextColor
                )

                // MAPA
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                mapStyleOptions = if (isDark) MapStyleOptions(MapStyles.DARK) else null
                            ),
                            onMapClick = { latLng ->
                                selectedLatitude = latLng.latitude
                                selectedLongitude = latLng.longitude
                                locationName = "Lat: %.5f, Lng: %.5f".format(
                                    Locale.US,
                                    latLng.latitude,
                                    latLng.longitude
                                )
                            }
                        ) {
                            val markerState = remember(selectedLatitude, selectedLongitude) {
                                MarkerState(position = LatLng(selectedLatitude, selectedLongitude))
                            }
                            Marker(
                                state = markerState,
                                title = stringResource(R.string.post_location_selected_label),
                                snippet = locationName
                            )
                        }

                        // Botón "Mi ubicación" (Simulado o real si tienes permisos)
                        IconButton(
                            onClick = { /* Acción para centrar en mi ubicación */ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(surfaceColor, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = PetHelpPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // FORMULARIO
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Calle y número
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.post_street_label),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            placeholder = { Text(stringResource(R.string.post_street_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = hintColor) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = outlineColor,
                                focusedBorderColor = PetHelpPrimary,
                                unfocusedPlaceholderColor = hintColor,
                                focusedPlaceholderColor = hintColor,
                                unfocusedTextColor = textColor,
                                focusedTextColor = textColor
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Barrio
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.post_neighborhood_label),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            OutlinedTextField(
                                value = neighborhood,
                                onValueChange = { neighborhood = it },
                                placeholder = { Text(stringResource(R.string.post_neighborhood_hint)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = outlineColor,
                                    focusedBorderColor = PetHelpPrimary,
                                    unfocusedPlaceholderColor = hintColor,
                                    focusedPlaceholderColor = hintColor,
                                    unfocusedTextColor = textColor,
                                    focusedTextColor = textColor
                                )
                            )
                        }
                        // Ciudad
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.post_city_label),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                placeholder = { Text(stringResource(R.string.post_city_hint)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = outlineColor,
                                    focusedBorderColor = PetHelpPrimary,
                                    unfocusedPlaceholderColor = hintColor,
                                    focusedPlaceholderColor = hintColor,
                                    unfocusedTextColor = textColor,
                                    focusedTextColor = textColor
                                )
                            )
                        }
                    }
                }

                // ADVERTENCIA DE PRIVACIDAD
                Surface(
                    color = warningContainerColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = onWarningContainerColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.post_location_warning),
                            fontSize = 12.sp,
                            color = onWarningContainerColor,
                            lineHeight = 16.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
