/**
 * Pantalla de selección de idioma de la aplicación.
 *
 * Permite al usuario elegir entre los idiomas soportados (español e inglés)
 * y aplica el cambio inmediatamente usando [AppLanguageManager].
 *
 * El idioma seleccionado se persiste en DataStore y se sincroniza
 * con Firebase Auth para que los mensajes de autenticación aparezcan
 * en el idioma correcto.
 */
package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.preferences.AppLanguageManager
import com.pethelp.app.core.ui.theme.*
import com.pethelp.app.core.ui.components.PetHelpCard
import com.pethelp.app.core.ui.components.pethelpFadeScaleIn
import com.pethelp.app.core.ui.components.PETHELP_STAGGER_DELAY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla para seleccionar el idioma preferido de la aplicacion.
 */
fun LanguageScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentLanguage by viewModel.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_language), 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Sección: Preferencias de Idioma
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_language_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Card de Idiomas
            androidx.compose.animation.AnimatedVisibility(visible = true, enter = pethelpFadeScaleIn(delay = 0)) {
            PetHelpCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                borderAlpha = 0.35f
            ) {
                Column {
                    LanguageItem(
                        title = stringResource(R.string.language_spanish),
                        subtitle = stringResource(R.string.settings_language_spanish),
                        isSelected = currentLanguage == AppLanguageManager.LANGUAGE_SPANISH,
                        onClick = { viewModel.setLanguage(AppLanguageManager.LANGUAGE_SPANISH) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    LanguageItem(
                        title = stringResource(R.string.language_english),
                        subtitle = stringResource(R.string.settings_language_english),
                        isSelected = currentLanguage == AppLanguageManager.LANGUAGE_ENGLISH,
                        onClick = { viewModel.setLanguage(AppLanguageManager.LANGUAGE_ENGLISH) }
                    )
                }
            }
            }

            Spacer(Modifier.height(24.dp))

            androidx.compose.animation.AnimatedVisibility(visible = true, enter = pethelpFadeScaleIn(delay = PETHELP_STAGGER_DELAY)) {
            // Texto informativo inferior
            Text(
                text = stringResource(R.string.settings_language_info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            }
        }
    }
}

/**
 * Item de idioma seleccionable en la pantalla de configuración de idioma.
 *
 * @param title Nombre del idioma.
 * @param subtitle Tag o descripción adicional.
 * @param isSelected Indica si está seleccionado.
 * @param onClick Acción al pulsar el item.
 */
@Composable
fun LanguageItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Crea un [BorderStroke] reutilizable para bordes de selección.
 *
 * @param width Ancho del borde.
 * @param color Color del borde.
 * @return Instancia de [BorderStroke].
 */
private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
