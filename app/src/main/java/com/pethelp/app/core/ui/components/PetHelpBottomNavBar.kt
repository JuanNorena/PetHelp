/**
 * Barra de navegación inferior personalizada de PetHelp.
 *
 * Muestra las pestañas principales (Inicio, Mapa, Chat, Perfil) con
 * indicadores de selección, badges de notificación para mensajes no leídos
 * y un botón flotante central para crear publicaciones.
 *
 * Se integra con [PetHelpNavGraph] para mantener la navegación sincronizada
 * con el estado actual de la ruta.
 */
package com.pethelp.app.core.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.MainActivity
import com.pethelp.app.R
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.PetHelpSecondary
import com.pethelp.app.core.ui.theme.White

/**
 * Muestra una notificación local cuando hay mensajes de chat no leídos.
 *
 * @param context Contexto para acceder al NotificationManager.
 * @param unreadCount Cantidad de mensajes no leídos.
 */
private fun showChatLocalNotification(context: Context, unreadCount: Int) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(context.getString(R.string.chat_notification_title))
        .setContentText(context.getString(R.string.chat_notification_body, unreadCount))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(2001, notification)
}

@Composable
fun PetHelpBottomNavBar(
    navController: NavController,
    unreadChatCount: Int = 0
) {
    val context = LocalContext.current
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var globalUnreadCount by remember { mutableIntStateOf(unreadChatCount) }
    var previousUnreadCount by remember { mutableIntStateOf(0) }

    DisposableEffect(uid) {
        if (uid.isBlank()) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("threads")
                .whereArrayContains("participants", uid)
                .addSnapshotListener { snap, _ ->
                    val count = snap?.documents?.sumOf { doc ->
                        val unreadByUser = doc.get("unreadByUser") as? Map<*, *>
                        val ownUnread = unreadByUser?.get(uid)
                        when (ownUnread) {
                            is Long -> ownUnread.toInt()
                            is Double -> ownUnread.toInt()
                            is Int -> ownUnread
                            else -> (doc.getLong("unreadCount") ?: 0L).toInt()
                        }
                    } ?: 0
                    globalUnreadCount = count

                    if (count > previousUnreadCount) {
                        val onChatScreen = currentDestination?.hasRoute<Screen.Chat>() == true
                                || currentDestination?.hasRoute<Screen.ChatThread>() == true
                        if (!onChatScreen) {
                            showChatLocalNotification(context, count)
                        }
                    }
                    previousUnreadCount = count
                }
            onDispose { listener.remove() }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // ── Barra glassmórfica ─────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.06f),
                    ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.04f)
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Borde superior sutil primario
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavBarItem(
                        unselectedIcon = Icons.Outlined.Home,
                        selectedIcon = Icons.Filled.Home,
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
                        unselectedIcon = Icons.Outlined.LocationOn,
                        selectedIcon = Icons.Filled.LocationOn,
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

                    // Espacio reservado para el FAB central
                    Spacer(modifier = Modifier.width(60.dp))

                    NavBarItem(
                        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
                        selectedIcon = Icons.Filled.ChatBubble,
                        label = stringResource(R.string.nav_chat),
                        selected = currentDestination?.hasRoute<Screen.Chat>() == true,
                        badgeCount = globalUnreadCount,
                        onClick = {
                            if (currentDestination?.hasRoute<Screen.Chat>() != true) {
                                navController.navigate(Screen.Chat) {
                                    popUpTo<Screen.Feed>() { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )

                    NavBarItem(
                        unselectedIcon = Icons.Outlined.Person,
                        selectedIcon = Icons.Filled.Person,
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
            }
        }

        // ── Notch simulado (círculo del color de fondo de la app) ─────────
        Box(
            modifier = Modifier
                .offset(y = (-22).dp)
                .size(74.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = CircleShape
                )
        )

        // ── FAB central circular ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(60.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = PetHelpSecondary.copy(alpha = 0.35f),
                    ambientColor = PetHelpSecondary.copy(alpha = 0.25f)
                )
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    CircleShape
                )
                .border(
                    width = 2.5.dp,
                    color = White.copy(alpha = 0.45f),
                    shape = CircleShape
                )
                .clickable { navController.navigate(Screen.CreatePost) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.nav_create),
                tint = White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Ítem individual de la barra de navegación con animaciones de escala,
 * indicador de punto y etiquetas con opacidad animada.
 *
 * @param unselectedIcon Icono en estado no seleccionado (outline).
 * @param selectedIcon Icono en estado seleccionado (filled).
 * @param label Etiqueta descriptiva del ítem.
 * @param selected Si el ítem está actualmente seleccionado.
 * @param badgeCount Número de notificaciones. Si es 0, no se muestra.
 * @param onClick Acción al pulsar el ítem.
 */
@Composable
private fun NavBarItem(
    unselectedIcon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    selected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "iconScale"
    )

    val glowScale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.0f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "glowScale"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.6f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "labelAlpha"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "iconTint"
    )

    val icon = if (selected) selectedIcon else unselectedIcon

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .widthIn(min = 56.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow/aura sutil detrás del icono seleccionado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(glowScale)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        CircleShape
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier
                    .size(26.dp)
                    .scale(iconScale)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(18.dp)
                        .shadow(2.dp, CircleShape)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .background(MaterialTheme.colorScheme.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "+9" else badgeCount.toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        // Indicador de punto centrado para evitar expansión a barra
        AnimatedVisibility(
            visible = selected,
            modifier = Modifier.width(20.dp),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }

        // Etiqueta siempre visible con opacidad animada
        Text(
            text = label,
            color = iconTint,
            fontSize = if (selected) 11.sp else 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier
                .padding(top = 2.dp)
                .alpha(labelAlpha)
        )
    }
}
