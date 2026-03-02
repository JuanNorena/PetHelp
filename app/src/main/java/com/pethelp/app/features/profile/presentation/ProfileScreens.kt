package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.WarmOrange

// ─── Profile Screen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
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
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PetHelpGreen)
                }
            }
            is ProfileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text((uiState as ProfileUiState.Error).message)
                }
            }
            is ProfileUiState.Success -> {
                val user = (uiState as ProfileUiState.Success).user
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Section (Level + Photo + Name + Role)
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                         // Background subtle shapes (from Figma)
                        Box(
                            modifier = Modifier
                                .offset(x = (-80).dp, y = 40.dp)
                                .size(280.dp)
                                .background(Color(0xFF4CAF50).copy(alpha = 0.05f), CircleShape)
                        )

                        Column(
                            modifier = Modifier.fillMaxSize().padding(top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             // Level Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF66BB6A).copy(alpha = 0.15f))
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Nivel ${user.level.ordinal + 1}", color = PetHelpGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(24.dp))

                            // Avatar
                            Box(modifier = Modifier.size(140.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .align(Alignment.Center)
                                        .shadow(10.dp, CircleShape)
                                        .background(Color.White, CircleShape)
                                        .padding(4.dp)
                                ) {
                                    // Placeholder for actual Coil image
                                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray))
                                }

                                // Verification check
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-16).dp, y = (-20).dp)
                                        .background(Brush.verticalGradient(listOf(PetHelpGreen, Color(0xFF66BB6A))), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("72%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF101828))
                            Text(text = if(user.username.isNotEmpty()) "@${user.username}" else "", color = Color(0xFF99A1AF), fontSize = 14.sp)

                            Spacer(Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF66BB6A).copy(alpha = 0.1f))
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.25f), RoundedCornerShape(50))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("👑 ${user.level.displayName}", color = Color(0xFF101828), fontSize = 14.sp)
                            }
                        }
                    }
                    
                    // Points Card (Orange gradient)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(140.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Color(0xFFFF8A65), Color(0xFFF4511E))))
                                .padding(24.dp)
                        ) {
                            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                                Text("PUNTOS TOTALES", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(user.points.toString(), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Stats Grid (2x2) mocks
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.List, value = "42", label = "Publicaciones")
                        StatCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Favorite, value = "128", label = "Favoritos", iconTint = WarmOrange)
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Quick Access
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                    ) {
                        QuickAccessItem(icon = Icons.AutoMirrored.Filled.List, label = "Mis publicaciones", onClick = {})
                        HorizontalDivider(color = Color(0xFFF9FAFB))
                        QuickAccessItem(icon = Icons.Filled.Favorite, label = "Favoritos", onClick = {})
                        HorizontalDivider(color = Color(0xFFF9FAFB))
                        QuickAccessItem(icon = Icons.Filled.Settings, label = "Configuración", onClick = { navController.navigate(Screen.EditProfile.route) })
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Logout
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
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFF99A1AF))
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar sesión", color = Color(0xFF99A1AF), fontWeight = FontWeight.Medium)
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, iconTint: Color = PetHelpGreen) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                 modifier = Modifier
                     .size(40.dp)
                     .background(iconTint.copy(alpha = 0.1f), CircleShape),
                 contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF101828))
            Text(label, color = Color(0xFF6A7282), fontSize = 12.sp)
        }
    }
}

@Composable
fun QuickAccessItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(PetHelpGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PetHelpGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, color = Color(0xFF1E2939))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF99A1AF))
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