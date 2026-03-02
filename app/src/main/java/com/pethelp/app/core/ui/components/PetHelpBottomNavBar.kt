package com.pethelp.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.WarmOrange

@Composable
fun PetHelpBottomNavBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Container for Bottom Nav - white background, top border/shadow, rounded top corners
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Feed)
            NavBarItem(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.nav_home),
                selected = currentRoute == Screen.Feed.route,
                onClick = {
                     if (currentRoute != Screen.Feed.route) {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Feed.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )

            // Map
            NavBarItem(
                icon = Icons.Filled.Map,
                label = stringResource(R.string.nav_map),
                selected = false, // Placeholder until map screen
                onClick = { /* TODO Phase 3: Navigate to Map Screen */ }
            )

            // FAB Create Post
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-12).dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = WarmOrange.copy(alpha = 0.5f),
                        ambientColor = WarmOrange.copy(alpha = 0.5f)
                    )
                    .background(color = WarmOrange, shape = CircleShape)
                    .clickable { navController.navigate(Screen.CreatePost.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nav_create),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Chat/Messages Placeholder
            NavBarItem(
                icon = Icons.Filled.ChatBubbleOutline,
                label = stringResource(R.string.nav_chat),
                selected = false,
                onClick = { /* TODO: Notification/Chat phase */ }
            )

            // Profile
            NavBarItem(
                icon = Icons.Filled.Person,
                label = stringResource(R.string.nav_profile),
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    if (currentRoute != Screen.Profile.route) {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Feed.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) PetHelpGreen else Color(0xFF9CA3AF)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}
