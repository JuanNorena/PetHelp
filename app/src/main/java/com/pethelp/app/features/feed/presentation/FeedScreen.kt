package com.pethelp.app.features.feed.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "PetHelp",
                            color = PetHelpGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color(0xFF6A7282)
                            )
                        }
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable { navController.navigate(Screen.Profile.route) }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = PetHelpGreen
                    )
                )

                // Category Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterChipUI("Todos", true) }
                    item { FilterChipUI("Adopción", false) }
                    item { FilterChipUI("Perdidos", false) }
                    item { FilterChipUI("Encontrados", false) }
                }
                
                HorizontalDivider(color = Color(0xFFF3F4F6))
            }
        },
        bottomBar = { PetHelpBottomNavBar(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TODO Fase 3: reemplazar con publicaciones reales de Firestore
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🐾",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "No hay publicaciones aún",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF101828)
                        )
                        Text(
                            text = "Sé el primero en publicar\nuna mascota",
                            fontSize = 14.sp,
                            color = Color(0xFF99A1AF),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipUI(label: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (selected) PetHelpGreen else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (selected) PetHelpGreen.copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* TODO Flow states */ }
    ) {
        Text(
            text = label,
            color = if (selected) PetHelpGreen else Color(0xFF4A5565),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PetCardMock(name: String, breed: String, distance: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Image section (Mock)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.LightGray)
                ) {
                    // Distance badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PetHelpGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text = distance, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101828))
                        }
                    }
                    
                    // Like button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp)
                            .background(Color(0xFFFFF7ED), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(24.dp))
                    }
                }
                
                // Info section
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF101828))
                    Text(text = breed, color = Color(0xFF6A7282), fontSize = 14.sp)
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Tags
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TagChip("Macho")
                        TagChip("Mediano")
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = Color(0xFF4A5565), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}