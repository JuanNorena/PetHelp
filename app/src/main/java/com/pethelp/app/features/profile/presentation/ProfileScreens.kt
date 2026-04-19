package com.pethelp.app.features.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.core.domain.model.UserLevel
import com.pethelp.app.core.domain.model.UserRole
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Profile Screen ───────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PetHelpBottomNavBar(navController) }
    ) { padding ->
        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text((uiState as ProfileUiState.Error).uiText.asString())
                }
            }
            is ProfileUiState.Success -> {
                val user = (uiState as ProfileUiState.Success).user
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { ProfileHeaderSection(user, navController) }
                    item { Spacer(Modifier.height(20.dp)) }
                    item { PointsCardSection(user.points) }
                    item { Spacer(Modifier.height(20.dp)) }
                    item { StatsGrid2x2Section(user) }
                    item { Spacer(Modifier.height(24.dp)) }
                    item { QuickAccessSection(navController, user.role) }
                    item { Spacer(Modifier.height(20.dp)) }
                    item { LogoutSection(navController, viewModel) }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeaderSection(user: com.pethelp.app.core.domain.model.User, navController: NavController) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Círculos decorativos de fondo (Figma: blur radial verde/gris)
        Box(
            modifier = Modifier
                .offset(x = (-96).dp, y = 40.dp)
                .size(288.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 196.dp, y = 200.dp)
                .size(256.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila superior: Level pill + Botón configuración
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pill de nivel (Figma: gradient + border verde)
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                        .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(10.dp))
                        }
                        Text(
                            stringResource(R.string.profile_level_label, user.level.ordinal + 1),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Icono de configuración
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(Screen.Settings) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Avatar con ring de progreso
            ProfileAvatarWithRing(user = user)

            Spacer(Modifier.height(16.dp))

            // Nombre
            Text(
                text = user.name,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.6).sp
            )

            // Username
            Text(
                text = if (user.username.isNotEmpty()) "${stringResource(R.string.profile_username_prefix)}${user.username}" else "${stringResource(R.string.profile_username_prefix)}usuario",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(12.dp))

            // Badge de nivel (Figma: pill degradado verde + "👑 Héroe de las Mascotas")
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "👑 ${UiText.fromUserLevel(user.level).asString()}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── Calcula el progreso de nivel (0.0–1.0) basado en puntos reales ────────────
private fun levelProgress(user: User): Float {
    return when (user.level) {
        UserLevel.FRIEND    -> (user.points.toFloat() / 50f).coerceIn(0f, 1f)
        UserLevel.PROTECTOR -> ((user.points - 50).toFloat() / 100f).coerceIn(0f, 1f)
        UserLevel.GUARDIAN  -> ((user.points - 150).toFloat() / 200f).coerceIn(0f, 1f)
        UserLevel.HERO      -> 1f
    }
}

// ─── Avatar con anillo de progreso ────────────────────────────────────────────
@Composable
private fun ProfileAvatarWithRing(user: User) {
    val progress = levelProgress(user)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        // Anillo dibujado con Canvas (Figma: circular progress verde)
        Canvas(modifier = Modifier.size(140.dp)) {
            val strokeW = 4.dp.toPx()
            val diameter = size.minDimension - strokeW * 2
            val tl = Offset(strokeW, strokeW)
            val arcSize = Size(diameter, diameter)
            // Track gris claro
            drawArc(
                color = trackColor,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(strokeW, cap = StrokeCap.Round),
                topLeft = tl, size = arcSize
            )
            // Arco de progreso verde
            drawArc(
                color = primaryColor,
                startAngle = -90f, sweepAngle = progress * 360f, useCenter = false,
                style = Stroke(strokeW, cap = StrokeCap.Round),
                topLeft = tl, size = arcSize
            )
        }

        // Foto de perfil
        Box(
            modifier = Modifier
                .size(116.dp)
                .shadow(8.dp, CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (user.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = stringResource(R.string.profile_avatar_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        // Badge de porcentaje debajo del avatar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(50), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(10.dp))
                Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Tarjeta de Puntos ────────────────────────────────────────────────────────
@Composable
private fun PointsCardSection(points: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(140.dp)
            .shadow(
                8.dp, RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Fondo degradado naranja
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
        )
        // Círculos decorativos internos
        Box(Modifier.offset(x = 230.dp, y = (-32).dp).size(128.dp).background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.10f), CircleShape))
        Box(Modifier.offset(x = (-16).dp, y = 68.dp).size(96.dp).background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.05f), CircleShape))
        Box(Modifier.offset(x = 230.dp, y = 84.dp).size(48.dp).background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.05f), CircleShape))

        // Icono de llama (izquierda, ligeramente rotado)
        Icon(
            Icons.Filled.Whatshot, null,
            tint = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp).align(Alignment.CenterStart).offset(x = 24.dp).rotate(-1f)
        )
        // Icono de estrella (derecha, ligeramente rotado)
        Icon(
            Icons.Filled.Star, null,
            tint = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp).align(Alignment.CenterEnd).offset(x = (-24).dp).rotate(-4f)
        )

        // Contenido central
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.profile_total_points),
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = points.toString(),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 48.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-2.4).sp,
                lineHeight = 48.sp
            )
            Spacer(Modifier.height(4.dp))

        }
    }
}

// ─── Grid de estadísticas 2×2 ─────────────────────────────────────────────────
@Composable
private fun StatsGrid2x2Section(user: User) {
    // Datos reales únicamente — sin valores quemados
    val memberSince = remember(user.createdAt) {
        SimpleDateFormat("MMM yyyy", Locale("es", "MX")).format(Date(user.createdAt))
            .replaceFirstChar { it.uppercase() }
    }
    val levelNum = user.level.ordinal + 1
    val badgeCount = user.badges.size
    val prefCount = user.petPreferences.size

    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = MaterialTheme.colorScheme.primary, iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                value = "$levelNum", label = stringResource(R.string.profile_stats_level),
                icon = Icons.Filled.Star
            )
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = MaterialTheme.colorScheme.primary, iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                value = memberSince, label = stringResource(R.string.profile_stats_member_since),
                icon = Icons.Filled.CalendarToday, smallValue = true
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = MaterialTheme.colorScheme.secondary, iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                value = "$badgeCount", label = stringResource(R.string.profile_stats_badges),
                icon = Icons.Filled.EmojiEvents
            )
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = MaterialTheme.colorScheme.secondary, iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                value = "$prefCount", label = stringResource(R.string.profile_stats_preferences),
                icon = Icons.Filled.Favorite
            )
        }
    }
}

@Composable
private fun StatCard2(
    modifier: Modifier = Modifier,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String,
    icon: ImageVector,
    smallValue: Boolean = false
) {
    Card(
        modifier = modifier.height(146.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Box(
                modifier = Modifier.size(36.dp).background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = value,
                fontSize = if (smallValue) 18.sp else 30.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = if (smallValue) (-0.45).sp else (-0.75).sp,
                lineHeight = if (smallValue) 28.sp else 36.sp
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

// ─── Accesos Rápidos ──────────────────────────────────────────────────────────
@Composable
private fun QuickAccessSection(
    navController: NavController,
    userRole: UserRole
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            stringResource(R.string.profile_quick_access_title),
            fontSize = 14.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.35.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                QuickAccessRow(
                    Icons.AutoMirrored.Filled.List,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.primary,
                    stringResource(R.string.profile_my_posts),
                    { navController.navigate(Screen.MyPosts) },
                    true
                )
                QuickAccessRow(
                    Icons.Filled.Favorite,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.secondary,
                    stringResource(R.string.profile_favorites),
                    { navController.navigate(Screen.Favorites) },
                    true
                )
                QuickAccessRow(
                    Icons.Filled.Mail,
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.tertiary,
                    stringResource(R.string.profile_adoption_requests),
                    { navController.navigate(Screen.AdoptionRequests) },
                    true
                )
                if (userRole == UserRole.MODERATOR) {
                    QuickAccessRow(
                        Icons.Filled.Shield,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.primary,
                        stringResource(R.string.moderation_panel_title),
                        { navController.navigate(Screen.ModeratorPanel) },
                        true
                    )
                }
                QuickAccessRow(Icons.Filled.Settings, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, stringResource(R.string.profile_settings),
                    { navController.navigate(Screen.Settings) }, false)
            }
        }
    }
}

@Composable
private fun QuickAccessRow(
    icon: ImageVector, iconBg: Color, iconTint: Color,
    label: String, onClick: () -> Unit, showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(73.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        if (showDivider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

// ─── Cerrar Sesión ────────────────────────────────────────────────────────────
@Composable
private fun LogoutSection(navController: NavController, viewModel: ProfileViewModel) {
    OutlinedButton(
        onClick = {
            viewModel.logout()
            navController.navigate(Screen.Login) {
                popUpTo<Screen.Feed>() { inclusive = true }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.btn_logout), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

// ─── Edit Profile Screen ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // SnackBar
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { uiText ->
            snackbarHostState.showSnackbar(uiText.asString(context))
        }
    }

    if (uiState is ProfileUiState.Success) {
        val successState = uiState as ProfileUiState.Success
        val user = successState.user
        val isUploadingPhoto = successState.isUploadingPhoto
        
        var name by remember { mutableStateOf(user.name) }
        var bio by remember { mutableStateOf(user.bio) }
        var city by remember { mutableStateOf(user.city) }
        var alertsNearMe by remember { mutableStateOf(user.alertsNearMe) }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    viewModel.uploadProfilePhoto(uri.toString())
                }
            }
        )

        // Mock preferences for now
        val preferences = remember { mutableStateListOf(*user.petPreferences.toTypedArray()) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.edit_profile_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                )
            },
            bottomBar = {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
                    Button(
                        onClick = {
                            val updatedUser = user.copy(
                                name = name,
                                bio = bio,
                                city = city,
                                alertsNearMe = alertsNearMe,
                                petPreferences = preferences.toList()
                            )
                            viewModel.updateProfile(updatedUser)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.btn_save_changes), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Photo Edit
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                     Box(
                         modifier = Modifier
                             .size(100.dp)
                             .clip(CircleShape)
                             .background(MaterialTheme.colorScheme.surfaceVariant)
                             .clickable(enabled = !isUploadingPhoto) {
                                 photoPickerLauncher.launch(
                                     PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                 )
                             }
                     ) {
                         if (user.photoUrl.isNotBlank()) {
                             AsyncImage(
                                 model = user.photoUrl,
                                 contentDescription = "Foto de perfil",
                                 modifier = Modifier.fillMaxSize(),
                                 contentScale = ContentScale.Crop
                             )
                         } else {
                             Box(
                                 modifier = Modifier.fillMaxSize(),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     Icons.Filled.Person,
                                     contentDescription = null,
                                     tint = White,
                                     modifier = Modifier.size(42.dp)
                                 )
                             }
                         }
                     }

                     // Camera overlay
                     Box(
                         modifier = Modifier
                             .align(Alignment.BottomEnd)
                             .size(32.dp)
                             .background(MaterialTheme.colorScheme.primary, CircleShape)
                             .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                         contentAlignment = Alignment.Center
                     ) {
                         if (isUploadingPhoto) {
                             CircularProgressIndicator(
                                 modifier = Modifier.size(16.dp),
                                 color = MaterialTheme.colorScheme.onPrimary,
                                 strokeWidth = 2.dp
                             )
                         } else {
                             Icon(
                                 Icons.Filled.CameraAlt,
                                 contentDescription = null,
                                 tint = MaterialTheme.colorScheme.onPrimary,
                                 modifier = Modifier.size(16.dp)
                             )
                         }
                     }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.edit_profile_change_photo),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(32.dp))

                // Form Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.name_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = { Text(stringResource(R.string.edit_profile_bio_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = { Text(stringResource(R.string.edit_profile_city_hint)) },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(stringResource(R.string.edit_profile_pet_preferences), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreferenceChip(stringResource(R.string.preference_dog), preferences)
                    PreferenceChip(stringResource(R.string.preference_cat), preferences)
                    PreferenceChip(stringResource(R.string.preference_rabbit), preferences)
                    PreferenceChip(stringResource(R.string.preference_bird), preferences)
                }

                Spacer(Modifier.height(24.dp))

                // Alerts Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.edit_profile_alerts_near_me), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Switch(
                        checked = alertsNearMe,
                        onCheckedChange = { alertsNearMe = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PreferenceChip(label: String, preferences: MutableList<String>) {
    val selected = preferences.contains(label)
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (selected) preferences.remove(label) else preferences.add(label)
            }
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}