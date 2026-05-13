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
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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

/**
 * Componente principal que define la barra de navegación inferior personalizada para la aplicación PetHelp.
 *
 * **Responsabilidad Principal:**
 * Su función primordial es proporcionar un acceso rápido y visual a las secciones clave de la app:
 * Alimentación (Feed), Mapa, Chat y Perfil. Además, actúa como el punto de entrada para crear
 * nuevas publicaciones a través de un botón central destacado (Floating Action Button).
 *
 * **Propósito y Diseño:**
 * Sigue los lineamientos de diseño de Material Design 3, integrando un botón flotante (FAB) central
 * que sobresale visualmente. El diseño está pensado para ser intuitivo, resaltando el ítem
 * actualmente activo con un fondo circular suave y una etiqueta de texto.
 *
 * **Lógica de Funcionamiento (Paso a Paso):**
 * 1. **Estado de Navegación:** Obtiene el destino actual mediante [navController.currentBackStackEntryAsState()].
 * 2. **Detección de Selección:** Compara la ruta activa con las constantes de [Screen] para marcar el ítem correcto.
 * 3. **Contenedor Principal:** Utiliza un [Box] con alineación superior para permitir que el FAB central "flote" sin recortes.
 * 4. **Distribución:** Emplea una [Row] con `Arrangement.SpaceBetween` para espaciar uniformemente los iconos laterales.
 * 5. **Navegación Segura:** Al hacer clic, se usa `launchSingleTop` para evitar pilas de pantallas infinitas.
 *
 * **Ejemplo de Uso Práctico:**
 * ```kotlin
 * // Dentro de tu MainScreen o Activity principal:
 * Scaffold(
 *     bottomBar = { PetHelpBottomNavBar(navController = myNavController) }
 * ) { padding ->
 *     // Tu contenido aquí
 * }
 * ```
 * @param navController El controlador de navegación de Jetpack Compose que gestiona el flujo entre pantallas.
 * **Es obligatorio** y debe estar vinculado al `NavHost` de la aplicación.
 * @return No devuelve ningún valor (función Composable de UI).
 * @throws IllegalStateException Si el `navController` no está inicializado correctamente en el contexto de Compose.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see Screen para ver las rutas disponibles.
 * @see NavBarItem para el diseño de cada opción individual.
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f),
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)
            )
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter
    ) {
        // PASO 3: Dibujamos una línea divisoria sutil en la parte superior para separar del contenido.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // PASO 4: Fila que contiene los botones de navegación.
        // El padding superior de 24.dp es CRÍTICO para dejar espacio visual al FAB.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp)
                .navigationBarsPadding(), // Respeta el área de gestos del sistema Android.
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón: Inicio
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

            // Botón: Mapa
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

            // PASO 5: Espaciador central. Deja el hueco exacto donde caerá el botón flotante.
            Spacer(modifier = Modifier.size(64.dp))

            // Botón: Chat (Con ejemplo de badge/notificación)
            NavBarItem(
                icon = Icons.Filled.ChatBubble,
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

            // Botón: Perfil
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

        // PASO 6: Botón de Acción Flotante (FAB) central.
        // Se posiciona con un offset para que parezca que "rompe" la línea de la barra.
        Box(
            modifier = Modifier
                .offset(y = 8.dp) // Lo bajamos un poco para centrarlo visualmente.
                .size(56.dp)
                .rotate(45f) // Rotación estética para el diseño de rombo/cruz.
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
            // El icono se rota inversamente (-45f) para que se vea derecho (+) a pesar de que el Box esté rotado.
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
 * Representa un ítem individual (botón con icono y texto) dentro de la barra de navegación.
 *
 * **Responsabilidad Principal:**
 * Renderizar una opción de menú con sus estados visuales: normal, seleccionado y con notificaciones.
 *
 * **Lógica de UI (Paso a Paso):**
 * 1. **Contenedor Vertical:** Alinea el icono arriba y el texto abajo mediante un [Column].
 * 2. **Estado Seleccionado:** Si `selected` es verdadero, dibuja un círculo semitransparente [PetHelpPrimary]
 *    detrás del icono para resaltarlo.
 * 3. **Iconografía:** Cambia el color del icono dinámicamente según el estado de selección.
 * 4. **Sistema de Insignias (Badges):** Si `badgeCount > 0`, dibuja un pequeño círculo rojo en la
 *    esquina superior derecha con el número de notificaciones.
 * 5. **Etiqueta Dinámica:** El texto descriptivo solo aparece cuando el ítem está seleccionado,
 *    reduciendo el ruido visual.
 *
 * **Ejemplo de Uso:**
 * ```kotlin
 * NavBarItem(
 *     icon = Icons.Default.Home,
 *     label = "Inicio",
 *     selected = true,
 *     badgeCount = 0,
 *     onClick = { /* Navegar */ }
 * )
 * ```
 *
 * **Errores Comunes:**
 * - **Iconos muy grandes:** Usar más de 24.dp para el icono puede romper el alineamiento.
 * - **Texto largo:** Etiquetas de más de 10-12 caracteres pueden causar desbordamientos en pantallas pequeñas.
 *
 * @param icon El vector de imagen ([ImageVector]) que se mostrará.
 * @param label Texto descriptivo (se muestra solo al estar seleccionado).
 * @param selected Booleano que define si el ítem es el activo actualmente.
 * @param badgeCount Número de notificaciones a mostrar. Si es 0, no se muestra el badge.
 * @param onClick Acción a ejecutar al pulsar el ítem.
 * @return No devuelve ningún valor (función Composable).
 *
 * @author Equipo de Desarrollo PetHelp
 */
@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    // PASO 1: Estructura vertical del ítem.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // PASO 2: Área del icono con posible fondo de selección y badge.
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Fondo circular si está seleccionado.
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                )
            }

            // El icono principal.
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            // PASO 3: Lógica de renderizado del Badge de notificación.
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Lo posicionamos arriba a la derecha.
                        .size(15.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape) // Borde para que resalte.
                        .background(MaterialTheme.colorScheme.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "+9" else badgeCount.toString(),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // PASO 4: Texto descriptivo (solo visible si está seleccionado).
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
