package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.user_guide_title), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 20.sp 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // CARD 1: BIENVENIDA
            GuideWelcomeCard(
                title = stringResource(R.string.user_guide_welcome_title),
                body = stringResource(R.string.user_guide_welcome_body)
            )

            Spacer(Modifier.height(16.dp))

            // CARD 2: CÓMO EMPEZAR
            GuideStepCard(
                icon = Icons.Default.PersonAdd,
                iconColor = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.user_guide_start_title),
                body = stringResource(R.string.user_guide_start_body)
            )

            Spacer(Modifier.height(16.dp))

            // CARD 3: ADOPTAR
            GuideStepCard(
                icon = Icons.Default.FavoriteBorder,
                iconColor = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.user_guide_adopt_title),
                body = stringResource(R.string.user_guide_adopt_body)
            )

            Spacer(Modifier.height(16.dp))

            // CARD 4: PUBLICAR
            GuideStepCard(
                icon = Icons.Default.CameraAlt,
                iconColor = MaterialTheme.colorScheme.tertiary,
                title = stringResource(R.string.user_guide_publish_title),
                body = stringResource(R.string.user_guide_publish_body)
            )

            Spacer(Modifier.height(16.dp))

            // CARD 5: SEGURIDAD
            GuideStepCard(
                icon = Icons.Default.Shield,
                iconColor = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.user_guide_security_title),
                body = stringResource(R.string.user_guide_security_body)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun GuideWelcomeCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = body,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun GuideStepCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = body,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
                modifier = Modifier.padding(start = 0.dp) // Ajustar si se quiere indentado bajo el texto
            )
        }
    }
}
