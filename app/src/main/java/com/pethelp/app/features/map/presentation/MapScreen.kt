package com.pethelp.app.features.map.presentation

import androidx.compose.ui.res.stringResource
import com.pethelp.app.R
import android.Manifest
import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.components.PetHelpCard
import com.pethelp.app.core.ui.components.PetHelpTagChip
import com.pethelp.app.core.ui.components.pethelpSlideUpFadeIn
import com.pethelp.app.core.ui.components.pethelpFadeScaleIn
import com.pethelp.app.core.ui.components.PETHELP_STAGGER_DELAY
import com.pethelp.app.core.ui.theme.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

/**
 * Pantalla de Mapa interactivo para la visualización geolocalizada de mascotas.
 *
 * **Responsabilidad Principal:**
 * Proporcionar una interfaz visual basada en Google Maps donde los usuarios pueden localizar mascotas
 * perdidas, encontradas o en adopción cerca de su ubicación actual o en áreas específicas.
 *
 * **Arquitectura y Componentes:**
 * - **Google Maps SDK:** Integración mediante `google-maps-compose` para renderizado reactivo.
 * - **Hilt ViewModel ([MapViewModel]):** Gestión del estado de búsqueda, filtrado por categorías y obtención de posts.
 * - **Material Design 3:** Implementación de `ModalNavigationDrawer`, `Scaffold`, `ModalBottomSheet` y componentes personalizados.
 * - **Permisos:** Gestión de permisos de ubicación en tiempo real usando Accompanist.
 *
 * **Funcionalidades Clave:**
 * 1. **Búsqueda y Filtrado:** Barra de búsqueda flotante y carrusel de categorías dinámico.
 * 2. **Marcadores Personalizados:** Visualización de la foto de la mascota directamente en el mapa.
 * 3. **Detalles Cercanos:** Bottom Sheet que muestra información extendida y otras mascotas en la misma zona.
 * 4. **Controles de Mapa:** Zoom, centrado en ubicación actual y cambio de tipo de mapa (Normal/Satélite).
 *
 * **Notas para Junior Developers:**
 * - El estado del mapa se controla mediante `cameraPositionState`, permitiendo animaciones fluidas.
 * - Se utiliza `ModalNavigationDrawer` para opciones secundarias como capas de mapa y ajustes.
 * - La lógica de distancia se calcula dinámicamente usando `Location.distanceBetween` en el Bottom Sheet.
 *
 * @param navController Controlador para navegar a detalles de post o perfil.
 * @param viewModel Lógica de negocio y estado del mapa (inyectado por Hilt).
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see MapViewModel Para la lógica de filtrado y búsqueda.
 * @see PetMapMarker Componente personalizado para los pines del mapa.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    // PASO 1: Inicialización de estados y utilidades de Compose.
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var didCenterOnInitialLocation by rememberSaveable { mutableStateOf(false) }
    val locationPermissionMessage = stringResource(R.string.map_location_permission_required)
    val locationUnavailableMessage = stringResource(R.string.map_location_unavailable)

    // PASO 2: Recolección de estado desde el ViewModel (UDF).
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val postsResource by viewModel.filteredPosts.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()

    // Estados locales para la interacción con marcadores y BottomSheet.
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(false) }

    // Colores semánticos del tema actual.
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    val armenia = LatLng(4.535, -75.675)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(armenia, 14f)
    }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // PASO 3: Gestión de permisos de ubicación.
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    suspend fun resolveUserLocation(): LatLng? {
        if (!locationPermissionsState.allPermissionsGranted) return null

        val freshLocation = runCatching {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        }.getOrNull()

        val location = freshLocation ?: runCatching {
            fusedLocationClient.lastLocation.await()
        }.getOrNull()

        return location?.let { LatLng(it.latitude, it.longitude) }
    }

    suspend fun centerCameraOnUserLocation() {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
            snackbarHostState.showSnackbar(locationPermissionMessage)
            return
        }

        val latLng = resolveUserLocation()
        if (latLng == null) {
            snackbarHostState.showSnackbar(locationUnavailableMessage)
            return
        }

        userLocation = latLng
        if (isMapLoaded) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
        }
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted, isMapLoaded) {
        if (!locationPermissionsState.allPermissionsGranted && !permissionRequested) {
            permissionRequested = true
            locationPermissionsState.launchMultiplePermissionRequest()
        }

        if (locationPermissionsState.allPermissionsGranted && !didCenterOnInitialLocation) {
            val latLng = resolveUserLocation()
            if (latLng != null) {
                userLocation = latLng
                didCenterOnInitialLocation = true
                if (isMapLoaded) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            }
        }
    }

    // Punto de referencia para calcular distancias (posición actual o centro del mapa)
    val referenceLocation = remember(userLocation, cameraPositionState.position) {
        userLocation ?: cameraPositionState.position.target
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    // PASO 4: Estructura de Navegación Lateral (Drawer).
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = surfaceColor,
                drawerContentColor = textColor
            ) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(stringResource(R.string.map_drawer_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stringResource(R.string.map_drawer_subtitle), color = secondaryTextColor, fontSize = 12.sp)
                    }
                }
                HorizontalDivider(color = outlineColor)
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.map_drawer_my_location)) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            centerCameraOnUserLocation()
                        }
                    },
                    icon = { Icon(Icons.Default.MyLocation, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.map_drawer_layers)) },
                    selected = false,
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Layers, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = outlineColor)
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.map_drawer_settings)) },
                    selected = false,
                    onClick = { /* Implementar navegación a ajustes */ },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { PetHelpBottomNavBar(navController) }
        ) {
            padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (!locationPermissionsState.allPermissionsGranted) {
                    LocationPermissionBanner(
                        onRequest = { locationPermissionsState.launchMultiplePermissionRequest() }
                    )
                }
                // PASO 5: Integración del Mapa de Google.
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissionsState.allPermissionsGranted,
                        mapStyleOptions = if (isDark) MapStyleOptions(MapStyles.DARK) else null,
                        mapType = mapType
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false,
                        compassEnabled = false
                    ),
                    onMapLoaded = { isMapLoaded = true }
                ) {
                    if (postsResource is Resource.Success) {
                        postsResource.data?.forEach { post ->
                            if (post.latitude != 0.0 && post.longitude != 0.0) {
                                PetMapMarker(
                                    position = LatLng(post.latitude, post.longitude),
                                    imageUrl = post.imageUrls.firstOrNull() ?: "",
                                    title = post.title,
                                    isSelected = selectedPost?.id == post.id,
                                    onClick = {
                                        selectedPost = post
                                        showSheet = true
                                    }
                                )
                            }
                        }
                    }
                }

                // PASO 6: Bottom Sheet de Detalles Rápidos.
                if (showSheet && selectedPost != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showSheet = false },
                        sheetState = sheetState,
                        containerColor = surfaceColor,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = outlineColor) }
                    ) {
                        NearbyPetsSheetContent(
                            selectedPost = selectedPost!!,
                            allPosts = (postsResource as? Resource.Success)?.data ?: emptyList(),
                            navController = navController,
                            userLocation = referenceLocation,
                            onPostClick = { 
                                selectedPost = it
                            }
                        )
                    }
                }

                // PASO 7: Interfaz Flotante (Búsqueda y Filtros).
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() 
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Barra de Búsqueda Funcional
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = surfaceColor,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = textColor)
                            }
                            
                            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.map_search_hint),
                                        color = secondaryTextColor,
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(it) },
                                    textStyle = TextStyle(color = textColor, fontSize = 14.sp),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, tint = secondaryTextColor, modifier = Modifier.size(18.dp))
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Filtros Dinámicos y Deslizables
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(categories) { categoryUiText ->
                            PetMapFilterChip(
                                label = categoryUiText.asString(),
                                isSelected = selectedCategory == categoryUiText,
                                onClick = { viewModel.onCategorySelect(categoryUiText) }
                            )
                        }
                    }
                }

                // PASO 8: Botones de Control del Mapa (Derecha).
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MapControlBtn(Icons.Default.MyLocation, surfaceColor, MaterialTheme.colorScheme.primary) {
                        scope.launch {
                            centerCameraOnUserLocation()
                        }
                    }
                    MapControlBtn(Icons.Default.Layers, surfaceColor, textColor) {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceColor,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                            }) {
                                Icon(Icons.Default.Add, null, tint = textColor)
                            }
                            HorizontalDivider(modifier = Modifier.width(20.dp), color = outlineColor)
                            IconButton(onClick = {
                                scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                            }) {
                                Icon(Icons.Default.Remove, null, tint = textColor)
                            }
                        }
                    }
                }

                // PASO 9: Indicador Inferior de Resultados.
                if (postsResource is Resource.Success) {
                    val count = postsResource.data?.size ?: 0
                    if (count > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 24.dp)
                                .clickable { 
                                    postsResource.data?.let { data ->
                                        if (data.isNotEmpty()) {
                                            selectedPost = data.first()
                                            showSheet = true
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(20.dp),
                            color = surfaceColor,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                Text(
                                    " " + stringResource(R.string.map_nearby_indicator, count),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionBanner(onRequest: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.map_permission_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.map_permission_body),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onRequest) {
                Text(stringResource(R.string.common_allow))
            }
        }
    }
}

/**
 * Chip personalizado para filtrar categorías en el mapa.
 *
 * @param label Texto legible de la categoría.
 * @param isSelected Estado de selección.
 * @param onClick Evento al presionar el chip.
 */
@Composable
fun PetMapFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    // Selección dinámica de iconos según la categoría.
    val icon = when (label) {
        stringResource(R.string.category_adoption) -> Icons.Default.Pets
        stringResource(R.string.category_lost) -> Icons.Default.Warning
        stringResource(R.string.category_found) -> Icons.Default.CheckCircle
        stringResource(R.string.category_temp_home) -> Icons.Default.Home
        stringResource(R.string.category_vet_event) -> Icons.Default.MedicalServices
        else -> Icons.Default.Map
    }

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Botón genérico para controles flotantes sobre el mapa.
 */
@Composable
fun MapControlBtn(icon: ImageVector, bgColor: Color, iconColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = 4.dp,
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * Marcador personalizado que muestra la imagen de la mascota.
 *
 * **Lógica Visual:**
 * - El marcador se agranda y añade un borde primario cuando está seleccionado.
 * - Muestra el título de la publicación sobre un fondo blanco si está activo.
 *
 * @param position Coordenadas LatLng.
 * @param imageUrl URL de la foto de la mascota.
 * @param title Nombre o título descriptivo.
 * @param isSelected Indica si el marcador ha sido presionado.
 * @param onClick Evento de selección.
 */
@Composable
fun PetMapMarker(
    position: LatLng, 
    imageUrl: String, 
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState(position = position)
    
    MarkerComposable(
        state = markerState,
        onClick = { onClick(); true },
        title = title
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(if (isSelected) 56.dp else 44.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .allowHardware(false)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            if (isSelected) {
                Surface(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Contenido detallado del Bottom Sheet cuando se selecciona una mascota.
 *
 * Muestra una tarjeta principal con la mascota elegida y una lista de "Otras mascotas cerca"
 * para incentivar la navegación y el descubrimiento.
 *
 * @param selectedPost Publicación actualmente enfocada.
 * @param allPosts Lista total de posts para filtrar los cercanos.
 * @param userLocation Ubicación central de referencia para el cálculo de distancias.
 * @param onPostClick Evento para cambiar el foco a otra mascota de la lista.
 */
@Composable
fun NearbyPetsSheetContent(
    selectedPost: Post,
    allPosts: List<Post>,
    navController: NavController,
    userLocation: LatLng,
    onPostClick: (Post) -> Unit
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    /**
     * Calcula la distancia entre dos puntos y retorna un texto formateado.
     */
    fun getDistanceLabel(postLat: Double, postLng: Double): String {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            postLat, postLng,
            results
        )
        val distanceInKm = results[0] / 1000
        return if (distanceInKm < 1) {
            context.getString(R.string.map_distance_m, results[0].toInt())
        } else {
            context.getString(R.string.map_distance_km, distanceInKm)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado "Cerca de ti"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(
                " " + stringResource(R.string.map_sheet_near_you),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Tarjeta principal de la mascota seleccionada
        PetHelpCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            borderAlpha = 0.4f,
            contentPadding = PaddingValues(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = selectedPost.imageUrls.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        selectedPost.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Text(
                        "${UiText.fromCategory(selectedPost.category).asString()} · ${selectedPost.breed}",
                        fontSize = 13.sp,
                        color = secondaryTextColor
                    )
                }

                Button(
                    onClick = { navController.navigate(Screen.PostDetail(selectedPost.id)) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(stringResource(R.string.map_sheet_view_details), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sección "Otros cerca"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.map_sheet_others_nearby),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Text(
                stringResource(R.string.map_sheet_view_all),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Lista de otras mascotas
        val otherPosts = allPosts.filter { it.id != selectedPost.id }.take(3)
        if (otherPosts.isEmpty()) {
            Text(
                stringResource(R.string.map_sheet_no_more_nearby),
                color = secondaryTextColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            otherPosts.forEachIndexed { index, post ->
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = pethelpFadeScaleIn(delay = index * PETHELP_STAGGER_DELAY)
                ) {
                    PetHelpCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onPostClick(post) },
                        shape = RoundedCornerShape(16.dp),
                        borderAlpha = 0.3f
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = post.imageUrls.firstOrNull(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(post.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                                Text("${post.breed} • ${getDistanceLabel(post.latitude, post.longitude)}", fontSize = 12.sp, color = secondaryTextColor)
                            }

                            // Badge de categoría
                            PetHelpTagChip(
                                label = UiText.fromCategory(post.category).asString()
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { /* Expandir */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, null, tint = secondaryTextColor)
            Text(" " + stringResource(R.string.map_sheet_more_pets), color = secondaryTextColor, fontSize = 13.sp)
        }
    }
}
