package com.pethelp.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen

/**
 * Barra de navegación inferior personalizada para PetHelp.
 *
 * Esta barra se muestra en la parte inferior de la aplicación y contiene los
 * accesos directos principales para ir a Home, Mapa, Chat y Perfil. Además,
 * incluye un botón central flotante (FAB) para crear una nueva publicación.
 *
 * El diseño respeta la guía visual de Figma: tiene una línea superior divisoria,
 * sombra ligera, ítems con selección destacada y un FAB central rotado.
 *
 * @param navController controlador de navegación usado para mover al usuario
 * entre pantallas. El componente no gestiona la navegación directamente, solo
 * dispara eventos cuando el usuario pulsa una opción.
 */
@Composable
fun PetHelpBottomNavBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    // Box principal que actúa como fondo de la barra.
    // Se usa contentAlignment = Alignment.TopCenter para posicionar el FAB
    // dentro del mismo contenedor sin usar offsets negativos que puedan recortar el contenido.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter
    ) {
        // Línea superior que separa visualmente la barra del contenido encima.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // Fila principal de ítems.
        // padding(top = 24.dp) deja espacio para que el FAB central no se superponga.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.nav_home),
                selected = currentDestination?.hasRoute<Screen.Feed>() == true,
                badgeCount = 0,
                onClick = {
                    if (currentDestination?.hasRoute<Screen.Feed>() != true) {
                        navController.navigate(Screen.Feed) {
                            popUpTo<Screen.Feed>() { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )

            NavBarItem(
                icon = Icons.Filled.LocationOn,
                label = stringResource(R.string.nav_map),
                selected = currentDestination?.hasRoute<Screen.Map>() == true,
                badgeCount = 0,
                onClick = {
                    if (currentDestination?.hasRoute<Screen.Map>() != true) {
                        navController.navigate(Screen.Map) {
                            popUpTo<Screen.Feed>() { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )

            // Espacio reservado para el botón flotante central.
            Spacer(modifier = Modifier.size(64.dp))

            NavBarItem(
                icon = Icons.Filled.ChatBubble,
                label = stringResource(R.string.nav_chat),
                selected = false,
                badgeCount = 2,
                onClick = { /* TODO: Pantalla de chat */ }
            )

            NavBarItem(
                icon = Icons.Filled.Person,
                label = stringResource(R.string.nav_profile),
                selected = currentDestination?.hasRoute<Screen.Profile>() == true,
                badgeCount = 0,
                onClick = {
                    if (currentDestination?.hasRoute<Screen.Profile>() != true) {
                        navController.navigate(Screen.Profile) {
                            popUpTo<Screen.Feed>() { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Botón de acción flotante central para crear una nueva publicación.
        // El ícono se rota 45 grados para obtener el estilo de “cruz inclinada”.
        Box(
            modifier = Modifier
                .offset(y = 8.dp)
                .size(56.dp)
                .rotate(45f)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { navController.navigate(Screen.CreatePost) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.nav_create),
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(-45f)
            )
        }
    }
}

/**
 * Componente que representa un ítem de la barra inferior.
 *
 * El diseño incluye un icono, una etiqueta opcional y un badge de notificación.
 * La etiqueta solo se muestra cuando el ítem está seleccionado, igual que en
 * el diseño de Figma.
 */
@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(15.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .background(MaterialTheme.colorScheme.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                }
            }
        }

        if (selected) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
