package com.pethelp.app.features.map.presentation

import androidx.compose.ui.res.stringResource
import com.pethelp.app.R
import java.util.Locale
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val postsResource by viewModel.filteredPosts.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()

    var selectedPost by remember { mutableStateOf<Post?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    val armenia = LatLng(4.535, -75.675)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(armenia, 14f)
    }

    // Punto de referencia para calcular distancias (posición actual o centro del mapa)
    val referenceLocation = remember(cameraPositionState.position) {
        cameraPositionState.position.target
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }

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
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(armenia, 15f))
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
                    onClick = { /* Navegar a ajustes */ },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { PetHelpBottomNavBar(navController) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── GOOGLE MAPS ─────────────────────────────────────────────────────
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
                    )
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

                // ── BOTTOM SHEET DE DETALLES ────────────────────────────────────────
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

                // ── BARRA SUPERIOR (Búsqueda y Filtros) ─────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Mantiene la barra debajo de los iconos de sistema
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp), // Reducimos top para subirla un poco más
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

                // ── CONTROLES FLOTANTES (Derecha) ──────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MapControlBtn(Icons.Default.MyLocation, surfaceColor, MaterialTheme.colorScheme.primary) {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(armenia, 15f)
                            )
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

                // ── INDICADOR INFERIOR DINÁMICO ─────────────────────────────────────
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
fun PetMapFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
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

@Composable
fun PetMapMarker(
    position: LatLng, 
    imageUrl: String, 
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
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

@Composable
fun NearbyPetsSheetContent(
    selectedPost: Post,
    allPosts: List<Post>,
    navController: NavController,
    userLocation: LatLng,
    onPostClick: (Post) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Función para calcular distancia
    val context = LocalContext.current
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
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
                    onClick = { navController.navigate("post_detail/${selectedPost.id}") },
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
            otherPosts.forEach { post ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onPostClick(post) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.imageUrls.firstOrNull(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                            Text("  ${post.breed}", fontSize = 12.sp, color = secondaryTextColor)
                        }
                        Text(
                            text = getDistanceLabel(post.latitude, post.longitude),
                            fontSize = 12.sp, 
                            color = secondaryTextColor
                        )
                    }

                    // Badge de categoría
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when(post.category) {
                            PostCategory.ADOPTION -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            PostCategory.LOST -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = UiText.fromCategory(post.category).asString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(post.category) {
                                PostCategory.ADOPTION -> MaterialTheme.colorScheme.primary
                                PostCategory.LOST -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
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
