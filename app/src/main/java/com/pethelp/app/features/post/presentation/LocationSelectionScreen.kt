package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
                            tint = TextPrimary
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = stringResource(R.string.post_location),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.post_step_2_of_4),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = PetHelpPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
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
                            navController.navigate(
                                Screen.PostDetails(
                                    title = postData.title,
                                    description = postData.description,
                                    category = postData.category,
                                    animalType = postData.animalType,
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
                            .height(64.dp),
                        shape = RoundedCornerShape(50),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetHelpPrimary,
                            disabledContainerColor = PetHelpPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            painterResource(id = android.R.drawable.ic_menu_edit),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.btn_next_details),
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
                trackColor = SurfaceVariantLight
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
                    color = TextSecondary
                )

                // MAPA
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, PetHelpOutline.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
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
                                .background(SurfaceLight, CircleShape)
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
                            color = TextPrimary
                        )
                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            placeholder = { Text(stringResource(R.string.post_street_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = PetHelpOutline,
                                focusedBorderColor = PetHelpPrimary,
                                unfocusedPlaceholderColor = TextHint,
                                focusedPlaceholderColor = TextHint
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
                                color = TextPrimary
                            )
                            OutlinedTextField(
                                value = neighborhood,
                                onValueChange = { neighborhood = it },
                                placeholder = { Text(stringResource(R.string.post_neighborhood_hint)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PetHelpOutline,
                                    focusedBorderColor = PetHelpPrimary,
                                    unfocusedPlaceholderColor = TextHint,
                                    focusedPlaceholderColor = TextHint
                                )
                            )
                        }
                        // Ciudad
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.post_city_label),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                placeholder = { Text(stringResource(R.string.post_city_hint)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PetHelpOutline,
                                    focusedBorderColor = PetHelpPrimary,
                                    unfocusedPlaceholderColor = TextHint,
                                    focusedPlaceholderColor = TextHint
                                )
                            )
                        }
                    }
                }

                // ADVERTENCIA DE PRIVACIDAD
                Surface(
                    color = TertiaryLightContainer,
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
                            tint = PetHelpTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.post_location_warning),
                            fontSize = 12.sp,
                            color = OnTertiaryLightContainer,
                            lineHeight = 16.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
