package com.pethelp.app.features.profile.presentation

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.domain.model.User
import com.pethelp.app.core.domain.model.UserLevel
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.WarmOrange
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
        containerColor = Color(0xFFFAFAFA),
        bottomBar = { PetHelpBottomNavBar(navController) }
    ) { padding ->
        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PetHelpGreen)
                }
            }
            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text((uiState as ProfileUiState.Error).message)
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
                    item { QuickAccessSection(navController) }
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
                .background(Color(0xFF4CAF50).copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 196.dp, y = 200.dp)
                .size(256.dp)
                .background(Color(0xFF8DA399).copy(alpha = 0.08f), CircleShape)
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
                                listOf(Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF66BB6A).copy(alpha = 0.15f))
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(50))
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
                                    brush = Brush.verticalGradient(listOf(PetHelpGreen, Color(0xFF66BB6A))),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                        Text("Nivel ${user.level.ordinal + 1}", color = PetHelpGreen, fontSize = 12.sp)
                    }
                }

                // Icono de configuración
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(Screen.EditProfile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, null, tint = Color(0xFF6A7282), modifier = Modifier.size(22.dp))
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
                color = Color(0xFF101828),
                letterSpacing = (-0.6).sp
            )

            // Username
            Text(
                text = if (user.username.isNotEmpty()) "@${user.username}" else "@usuario",
                color = Color(0xFF99A1AF),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(12.dp))

            // Badge de nivel (Figma: pill degradado verde + "👑 Héroe de las Mascotas")
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF66BB6A).copy(alpha = 0.1f))
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.25f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("👑 ${user.level.displayName}", color = Color(0xFF0A0A0A), fontSize = 14.sp)
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
    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        // Anillo dibujado con Canvas (Figma: circular progress verde)
        Canvas(modifier = Modifier.size(140.dp)) {
            val strokeW = 4.dp.toPx()
            val diameter = size.minDimension - strokeW * 2
            val tl = Offset(strokeW, strokeW)
            val arcSize = Size(diameter, diameter)
            // Track gris claro
            drawArc(
                color = Color(0xFFE8F5E9),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(strokeW, cap = StrokeCap.Round),
                topLeft = tl, size = arcSize
            )
            // Arco de progreso verde
            drawArc(
                color = Color(0xFF4CAF50),
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
                .border(3.dp, Color.White, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFB0BEC5))
        )

        // Badge de porcentaje debajo del avatar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(50), spotColor = Color(0xFF4CAF50).copy(alpha = 0.3f))
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(10.dp))
                Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
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
                spotColor = Color(0xFFFF8A65).copy(alpha = 0.2f),
                ambientColor = Color(0xFFFF8A65).copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Fondo degradado naranja (Figma: from-[#ff8a65] to-[#f4511e])
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFF8A65), Color(0xFFF4511E)))
            )
        )
        // Círculos decorativos internos
        Box(Modifier.offset(x = 230.dp, y = (-32).dp).size(128.dp).background(Color.White.copy(alpha = 0.10f), CircleShape))
        Box(Modifier.offset(x = (-16).dp, y = 68.dp).size(96.dp).background(Color.White.copy(alpha = 0.05f), CircleShape))
        Box(Modifier.offset(x = 230.dp, y = 84.dp).size(48.dp).background(Color.White.copy(alpha = 0.05f), CircleShape))

        // Icono de llama (izquierda, ligeramente rotado)
        Icon(
            Icons.Filled.Whatshot, null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp).align(Alignment.CenterStart).offset(x = 24.dp).rotate(-1f)
        )
        // Icono de estrella (derecha, ligeramente rotado)
        Icon(
            Icons.Filled.Star, null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp).align(Alignment.CenterEnd).offset(x = (-24).dp).rotate(-4f)
        )

        // Contenido central
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PUNTOS TOTALES",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = points.toString(),
                color = Color.White,
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
                iconTint = PetHelpGreen, iconBg = PetHelpGreen.copy(alpha = 0.1f),
                value = "$levelNum", label = "Nivel actual",
                icon = Icons.Filled.Star
            )
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = PetHelpGreen, iconBg = PetHelpGreen.copy(alpha = 0.1f),
                value = memberSince, label = "Miembro desde",
                icon = Icons.Filled.CalendarToday, smallValue = true
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = WarmOrange, iconBg = WarmOrange.copy(alpha = 0.1f),
                value = "$badgeCount", label = "Insignias",
                icon = Icons.Filled.EmojiEvents
            )
            StatCard2(
                modifier = Modifier.weight(1f),
                iconTint = WarmOrange, iconBg = WarmOrange.copy(alpha = 0.1f),
                value = "$prefCount", label = "Preferencias",
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
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
                color = Color(0xFF101828),
                letterSpacing = if (smallValue) (-0.45).sp else (-0.75).sp,
                lineHeight = if (smallValue) 28.sp else 36.sp
            )
            Text(text = label, fontSize = 12.sp, color = Color(0xFF99A1AF), lineHeight = 16.sp)
        }
    }
}

// ─── Accesos Rápidos ──────────────────────────────────────────────────────────
@Composable
private fun QuickAccessSection(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            "Accesos rápidos",
            fontSize = 14.sp, fontWeight = FontWeight.Medium,
            color = Color(0xFF6A7282), letterSpacing = 0.35.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                QuickAccessRow(Icons.AutoMirrored.Filled.List, PetHelpGreen.copy(alpha = 0.1f), PetHelpGreen, "Mis publicaciones", {}, true)
                QuickAccessRow(Icons.Filled.Favorite, WarmOrange.copy(alpha = 0.1f), WarmOrange, "Favoritos", {}, true)
                QuickAccessRow(Icons.Filled.Shield, PetHelpGreen.copy(alpha = 0.1f), PetHelpGreen, "Panel de Moderación",
                    { navController.navigate(Screen.ModeratorPanel.route) }, true)
                QuickAccessRow(Icons.Filled.Settings, Color(0xFFF3F4F6), Color(0xFF6A7282), "Configuración",
                    { navController.navigate(Screen.EditProfile.route) }, false)
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
            Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E2939))
            Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF99A1AF), modifier = Modifier.size(18.dp))
        }
        if (showDivider) HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
    }
}

// ─── Cerrar Sesión ────────────────────────────────────────────────────────────
@Composable
private fun LogoutSection(navController: NavController, viewModel: ProfileViewModel) {
    OutlinedButton(
        onClick = {
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Feed.route) { inclusive = true }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFF99A1AF), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Cerrar sesión", color = Color(0xFF99A1AF), fontWeight = FontWeight.Medium, fontSize = 14.sp)
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

    if (uiState is ProfileUiState.Success) {
        val user = (uiState as ProfileUiState.Success).user
        
        var name by remember { mutableStateOf(user.name) }
        var bio by remember { mutableStateOf(user.bio) }
        var city by remember { mutableStateOf(user.city) }
        var alertsNearMe by remember { mutableStateOf(user.alertsNearMe) }

        // Mock preferences for now
        val preferences = remember { mutableStateListOf(*user.petPreferences.toTypedArray()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
                        colors = ButtonDefaults.buttonColors(containerColor = PetHelpGreen)
                    ) {
                        Text(stringResource(R.string.btn_save_changes), fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                             .background(Color.LightGray, CircleShape)
                     )

                     // Camera overlay
                     Box(
                         modifier = Modifier
                             .align(Alignment.BottomEnd)
                             .size(32.dp)
                             .background(PetHelpGreen, CircleShape)
                             .border(2.dp, Color.White, CircleShape),
                         contentAlignment = Alignment.Center
                     ) {
                         Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                     }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.edit_profile_change_photo),
                    color = PetHelpGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(32.dp))

                // Form Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = PetHelpGreen
                    )
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = { Text("Biografía") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = PetHelpGreen
                    )
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = { Text("Ciudad") },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = PetHelpGreen
                    )
                )

                Spacer(Modifier.height(24.dp))

                Text(stringResource(R.string.edit_profile_pet_preferences), fontWeight = FontWeight.Bold, color = Color(0xFF101828))
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreferenceChip("Perro", preferences)
                    PreferenceChip("Gato", preferences)
                    PreferenceChip("Conejo", preferences)
                    PreferenceChip("Ave", preferences)
                }

                Spacer(Modifier.height(24.dp))

                // Alerts Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = Color(0xFF6A7282))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.edit_profile_alerts_near_me), fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = alertsNearMe,
                        onCheckedChange = { alertsNearMe = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PetHelpGreen)
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PetHelpGreen)
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
                color = if (selected) PetHelpGreen else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (selected) Color(0xFFE8F5E9) else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (selected) preferences.remove(label) else preferences.add(label)
            }
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF2E7D32) else Color(0xFF4A5565),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}