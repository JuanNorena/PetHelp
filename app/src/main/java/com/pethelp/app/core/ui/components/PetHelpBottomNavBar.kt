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
import com.pethelp.app.core.ui.theme.PetHelpPrimary
import com.pethelp.app.core.ui.theme.PetHelpSecondary

@Composable
fun PetHelpBottomNavBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    // ── Contenedor principal ─────────────────────────────────────────────────

    // El Box exterior NO usa offset negativo para el FAB — en su lugar el Row
    // tiene padding(top = 24.dp) para ceder espacio, y el FAB se posiciona con
    // Alignment.TopCenter dentro del mismo Box sin salir de sus límites.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(Color.White),
        contentAlignment = Alignment.TopCenter
    ) {
        // Línea divisoria superior (#F3F4F6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF3F4F6))
        )

        // ── Fila de ítems ─────────────────────────────────────────────────────
        // padding(top = 24.dp) deja espacio para el FAB en la parte superior.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Inicio ────────────────────────────────────────────────────────
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

            // ── Mapa ──────────────────────────────────────────────────────────
            NavBarItem(
                icon = Icons.Filled.LocationOn,
                label = stringResource(R.string.nav_map),
                selected = currentDestination?.hasRoute<Screen.Map>() == true,
                badgeCount = 0,
                onClick = { /* TODO Fase 3 */ }
            )

            // Espacio reservado para el FAB central (no es un ítem real)
            Spacer(modifier = Modifier.size(64.dp))

            // ── Chat con badge ────────────────────────────────────────────────
            NavBarItem(
                icon = Icons.Filled.ChatBubble,
                label = stringResource(R.string.nav_chat),
                selected = false,
                badgeCount = 2,
                onClick = { /* TODO: Pantalla de chat */ }
            )

            // ── Perfil ────────────────────────────────────────────────────────
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

        // ── FAB central "Crear publicación" ───────────────────────────────────
        // Posicionado en TopCenter del Box exterior con offset(y=8.dp) para que
        // quede 8dp desde el borde superior — siempre dentro de los límites del
        // componente, sin ser recortado por Scaffold ni por el shadow.
        Box(
            modifier = Modifier
                .offset(y = 8.dp)
                .size(56.dp)
                .rotate(45f)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = PetHelpSecondary.copy(alpha = 0.4f),
                    ambientColor = PetHelpSecondary.copy(alpha = 0.4f)
                )
                .background(color = PetHelpSecondary, shape = RoundedCornerShape(16.dp))
                .clickable { navController.navigate(Screen.CreatePost) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.nav_create),
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(-45f)
            )
        }
    }
}

/**
 * Ítem individual de la barra inferior.
 *
 * @param icon       Icono de Material Icons a mostrar.
 * @param label      Etiqueta (solo visible cuando [selected] = true, igual que Figma).
 * @param selected   Activo: fondo círculo verde 10% opacidad + etiqueta verde.
 * @param badgeCount Notificaciones. 0 = sin badge.
 * @param onClick    Callback de navegación.
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
        // Contenedor del icono + fondo + badge
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Fondo verde circular (Figma: bg-[rgba(76,175,80,0.1)], solo ítem activo)
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PetHelpPrimary.copy(alpha = 0.1f), CircleShape)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) PetHelpPrimary else Color(0xFF9CA3AF),
                modifier = Modifier.size(22.dp)
            )

            // Badge rojo (Figma: bg-[#fb2c36], border blanca, font-bold 10px)
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(15.dp)
                        .border(1.5.dp, Color.White, CircleShape)
                        .background(Color(0xFFFB2C36), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // Etiqueta solo cuando está seleccionado
        if (selected) {
            Text(
                text = label,
                color = PetHelpPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
