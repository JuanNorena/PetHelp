/**
 * Dashboard de estadísticas personales del usuario con diseño visual moderno.
 *
 * Muestra métricas como publicaciones activas, finalizadas, en revisión
 * y solicitudes de adopción enviadas/recibidas. Incluye gráficos animados
 * y tarjetas con indicadores visuales de progreso.
 *
 * Actualmente usa datos de ejemplo; en futuras fases se conectará
 * con repositorios de publicaciones y adopciones en tiempo real.
 */
package com.pethelp.app.features.stats.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.ui.components.PetHelpCard

/**
 * Dashboard de estadísticas personales del usuario con diseño visual moderno.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.stats_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Header con gradiente
            StatsHeader(
                totalPosts = 12,
                totalAdoptions = 4
            )

            Spacer(Modifier.height(24.dp))

            // Grid de estadísticas 2x2
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedStatCard(
                    label = stringResource(R.string.stats_active),
                    value = 5,
                    icon = Icons.Filled.Pets,
                    iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AnimatedStatCard(
                    label = stringResource(R.string.stats_finished),
                    value = 4,
                    icon = Icons.Filled.CheckCircle,
                    iconBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedStatCard(
                    label = stringResource(R.string.stats_pending),
                    value = 3,
                    icon = Icons.Filled.HourglassEmpty,
                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                AnimatedStatCard(
                    label = stringResource(R.string.stats_favorites),
                    value = 8,
                    icon = Icons.Filled.Favorite,
                    iconBg = Color(0xFFFFECEB),
                    iconTint = Color(0xFFA12622),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Circular progress section
            PetHelpCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.stats_success_rate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicatorAnimated(
                        progress = 0.75f,
                        size = 160.dp,
                        strokeWidth = 14.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "75%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.stats_adoptions_helped),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Recent activity section
            Text(
                text = stringResource(R.string.stats_recent_activity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(12.dp))

            ActivityItem(
                icon = Icons.Filled.Pets,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.stats_activity_post_created),
                subtitle = stringResource(R.string.stats_activity_post_created_sub),
                time = "2h"
            )
            Spacer(Modifier.height(8.dp))
            ActivityItem(
                icon = Icons.Filled.EmojiEvents,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = stringResource(R.string.stats_activity_badge_earned),
                subtitle = stringResource(R.string.stats_activity_badge_earned_sub),
                time = "1d"
            )
            Spacer(Modifier.height(8.dp))
            ActivityItem(
                icon = Icons.Filled.Favorite,
                iconTint = Color(0xFFA12622),
                title = stringResource(R.string.stats_activity_adoption_complete),
                subtitle = stringResource(R.string.stats_activity_adoption_complete_sub),
                time = "3d"
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Encabezado del dashboard de estadísticas con totales de publicaciones y adopciones.
 *
 * @param totalPosts Número total de publicaciones del usuario.
 * @param totalAdoptions Número total de adopciones completadas.
 */
@Composable
private fun StatsHeader(totalPosts: Int, totalAdoptions: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_header_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(R.string.stats_header_subtitle, totalPosts, totalAdoptions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }

        Icon(
            imageVector = Icons.Filled.Insights,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(80.dp),
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
        )
    }
}

/**
 * Tarjeta de estadística individual con animación de contador.
 *
 * @param label Etiqueta descriptiva.
 * @param value Valor numérico a mostrar.
 * @param icon Icono vectorial.
 * @param iconBg Color de fondo del icono.
 * @param iconTint Color del icono.
 * @param modifier Modificador para ajustar layout.
 */
@Composable
private fun AnimatedStatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    var targetValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(value) { targetValue = value }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "stat_counter"
    )

    PetHelpCard(
        modifier = modifier.height(130.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = animatedValue.toInt().toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Indicador circular de progreso con animación suave.
 *
 * @param progress Progreso normalizado entre 0.0 y 1.0.
 * @param size Tamaño del indicador.
 * @param strokeWidth Grosor del trazo.
 */
@Composable
private fun CircularProgressIndicatorAnimated(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val diameter = size.toPx() - strokePx * 2
        val topLeft = Offset(strokePx, strokePx)
        val arcSize = Size(diameter, diameter)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(strokePx, cap = StrokeCap.Round),
            topLeft = topLeft,
            size = arcSize
        )

        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            style = Stroke(strokePx, cap = StrokeCap.Round),
            topLeft = topLeft,
            size = arcSize
        )
    }
}

/**
 * Item individual de actividad reciente en el dashboard de estadísticas.
 *
 * @param icon Icono vectorial.
 * @param iconTint Color del icono.
 * @param title Título de la actividad.
 * @param subtitle Descripción corta.
 * @param time Fecha/hora de la actividad.
 */
@Composable
private fun ActivityItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    time: String
) {
    PetHelpCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
