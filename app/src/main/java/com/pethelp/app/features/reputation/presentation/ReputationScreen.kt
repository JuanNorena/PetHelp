/**
 * Pantalla de reputación y logros del usuario.
 *
 * Muestra los puntos acumulados, el nivel actual y el progreso hacia
 * el siguiente nivel. También lista las insignias obtenidas en el
 * sistema de gamificación de PetHelp.
 *
 * Datos en construcción: actualmente muestra valores de ejemplo;
 * en futuras fases se conectará con [GamificationViewModel].
 */
package com.pethelp.app.features.reputation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.ui.components.PetHelpCard
import com.pethelp.app.core.ui.components.PetHelpEmptyState
import com.pethelp.app.core.ui.components.pethelpFadeScaleIn
import com.pethelp.app.core.ui.components.PETHELP_STAGGER_DELAY

/**
 * Pantalla de reputación y logros del usuario.
 *
 * TODO (Fase 2): mostrar puntos, nivel actual, progreso al siguiente nivel e insignias.
 *   - Datos en memoria / mock.
 *   - Mostrar los 4 niveles: Amigo Animal, Protector, Guardián, Héroe de las Mascotas.
 *
 * TODO (Fase 3): carga desde Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reputation_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            androidx.compose.animation.AnimatedVisibility(visible = true, enter = pethelpFadeScaleIn(delay = 0)) {
                PetHelpCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    borderAlpha = 0.35f
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.reputation_points, "—"),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reputation_level_label, stringResource(R.string.level_friend)),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            androidx.compose.animation.AnimatedVisibility(visible = true, enter = pethelpFadeScaleIn(delay = PETHELP_STAGGER_DELAY)) {
                PetHelpEmptyState(
                    title = stringResource(R.string.reputation_badges_title),
                    subtitle = stringResource(R.string.reputation_badges_empty)
                )
            }
        }
    }
}
