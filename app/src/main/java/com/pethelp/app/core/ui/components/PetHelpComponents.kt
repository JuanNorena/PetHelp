/**
 * Componentes visuales reutilizables de PetHelp.
 *
 * Incluye tarjetas personalizadas ([PetHelpCard]), indicadores de estado
 * ([PetHelpStatusBadge]), estados vacíos ([PetHelpEmptyState]), efectos
 * de carga ([PetHelpShimmerLoading]) y otros elementos de UI compartidos
 * entre múltiples pantallas de la aplicación.
 *
 * Estos componentes mantienen una estética consistente siguiendo el
 * design system de PetHelp con bordes redondeados, sombras suaves
 * y paleta de colores definida en [Color.kt].
 */
package com.pethelp.app.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tarjeta redondeada con borde sutil y elevación consistente.
 *
 * Usa [Card] de Material3 con colores del tema y un borde semitransparente.
 * Ideal para contener información estructurada en cualquier pantalla.
 *
 * @param modifier Modificador para ajustar layout externo.
 * @param shape Forma de la tarjeta; por defecto esquinas redondeadas de 24 dp.
 * @param elevation Elevación en dp para la sombra proyectada.
 * @param borderAlpha Opacidad del borde; 0.48f por defecto para sutileza.
 * @param contentPadding Padding interno aplicado al contenido.
 * @param content Contenido composable que se renderiza dentro de la tarjeta.
 */
@Composable
fun PetHelpCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Int = 3,
    borderAlpha: Float = 0.48f,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = borderAlpha)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp,
            pressedElevation = (elevation + 1).dp
        )
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Chip reutilizable para mostrar etiquetas cortas (tags).
 *
 * Renderiza un [Surface] con fondo [secondaryContainer] y texto en
 * [onSecondaryContainer], con esquinas redondeadas de 10 dp.
 *
 * @param label Texto que se muestra dentro del chip.
 * @param modifier Modificador para ajustar layout.
 * @param containerColor Color de fondo del chip.
 * @param contentColor Color del texto del chip.
 */
@Composable
fun PetHelpTagChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * Estado vacío con icono grande, título, subtítulo y botón de acción opcional.
 *
 * Se usa cuando una lista o pantalla no tiene datos para mostrar,
 * ofreciendo al usuario un mensaje amigable y una acción para continuar.
 *
 * @param title Título principal del estado vacío.
 * @param subtitle Descripción explicativa.
 * @param modifier Modificador para ajustar layout.
 * @param icon Icono vectorial que se muestra encima del título.
 * @param iconSize Tamaño del icono en dp.
 * @param actionLabel Texto del botón de acción; null si no se muestra.
 * @param onClick Acción a ejecutar al pulsar el botón.
 */
@Composable
fun PetHelpEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Pets,
    iconSize: Int = 64,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.0f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size((iconSize * 0.45).dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null) {
            Spacer(Modifier.height(20.dp))
            PetHelpButton(
                label = actionLabel,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

/**
 * Botón primario con sombra y elevación consistente con el design system.
 *
 * Usa esquinas redondeadas completas (50 dp) y un degradado de sombra
 * que reduce al presionar para dar feedback táctil.
 *
 * @param label Texto que se muestra en el botón.
 * @param onClick Acción a ejecutar al pulsar.
 * @param modifier Modificador para ajustar layout.
 * @param enabled Indica si el botón responde a interacciones.
 * @param containerColor Color de fondo del botón.
 * @param contentColor Color del texto del botón.
 */
@Composable
fun PetHelpButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .animateContentSize(animationSpec = tween(200, easing = FastOutSlowInEasing)),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
    }
}

/**
 * Campo de texto con borde estilizado y colores adaptados al tema.
 *
 * Envuelve [OutlinedTextField] para mantener consistencia en la app,
 * usando fondo [surface] y borde semitransparente.
 *
 * @param value Texto actual del campo.
 * @param onValueChange Callback cuando el texto cambia.
 * @param modifier Modificador para ajustar layout.
 * @param placeholder Texto de sugerencia cuando el campo está vacío.
 * @param leadingIcon Icono opcional al inicio del campo.
 * @param trailingIcon Icono opcional al final del campo.
 * @param singleLine Indica si el campo debe ser de una sola línea.
 * @param shape Forma del borde del campo.
 */
@Composable
fun PetHelpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    )
}

/**
 * Badge tipo pill que se superpone sobre imágenes de publicaciones.
 *
 * Muestra la categoría (adopción, perdido, etc.) con fondo
 * [primaryContainer] semitransparente para legibilidad sobre fotos.
 *
 * @param label Texto de la categoría.
 * @param modifier Modificador para ajustar posición y tamaño.
 */
@Composable
fun PetHelpCategoryBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Indicador visual de pasos con barras de progreso horizontales.
 *
 * Cada paso se representa como una barra coloreada según su estado:
 * completado, actual o pendiente.
 *
 * @param currentStep Índice del paso actual (0-based).
 * @param totalSteps Número total de pasos.
 * @param modifier Modificador para ajustar layout.
 */
@Composable
fun PetHelpStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 0 until totalSteps) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}

/**
 * Badge semántico que cambia de color según el estado indicado.
 *
 * Útil para mostrar etiquetas de éxito, advertencia, error o información
 * en listas de publicaciones o notificaciones.
 *
 * @param label Texto que se muestra en el badge.
 * @param status Estado semántico que determina los colores.
 * @param modifier Modificador para ajustar layout.
 */
@Composable
fun PetHelpStatusBadge(
    label: String,
    status: PetHelpStatus,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (status) {
        PetHelpStatus.SUCCESS ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        PetHelpStatus.WARNING ->
            Color(0xFFFFF4E5) to Color(0xFF7A2E0E)
        PetHelpStatus.ERROR ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        PetHelpStatus.INFO ->
            Color(0xFFE0F2FE) to Color(0xFF0369A1)
        PetHelpStatus.NEUTRAL ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

/** Estados semánticos para [PetHelpStatusBadge]. */
enum class PetHelpStatus {
    SUCCESS, WARNING, ERROR, INFO, NEUTRAL
}

/**
 * Efecto shimmer para placeholders de carga.
 *
 * Muestra un degradado animado horizontal que simula el efecto de
 * brillo mientras los datos reales se están cargando.
 *
 * @param modifier Modificador para ajustar tamaño y posición.
 * @param shape Forma del placeholder; por defecto esquinas redondeadas de 8 dp.
 */
@Composable
fun PetHelpShimmerLoading(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim.value - 500f, y = 0f),
        end = Offset(x = translateAnim.value, y = 0f),
        tileMode = TileMode.Clamp
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Contenedor para bottom sheets con indicador de arrastre (handle).
 *
 * Renderiza una columna con esquinas superiores redondeadas y una
 * barra gris centrada que indica al usuario que puede arrastrar.
 *
 * @param modifier Modificador para ajustar layout.
 * @param content Contenido composable que se renderiza debajo del handle.
 */
@Composable
fun PetHelpBottomSheetCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Handle indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
        content()
    }
}

/**
 * Título de sección con estilo consistente y acción opcional.
 *
 * Muestra un texto en negrita alineado a la izquierda y, si se proporciona,
 * un texto clickeable alineado a la derecha para acciones como "Ver más".
 *
 * @param title Texto del título de la sección.
 * @param modifier Modificador para ajustar layout.
 * @param actionLabel Texto de la acción; null si no se muestra.
 * @param onAction Acción a ejecutar al pulsar el texto de acción.
 */
@Composable
fun PetHelpSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}
