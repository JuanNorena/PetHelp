package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.privacy_title), 
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            
            // Icono de Escudo Superior
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // 1. Introducción
            PrivacySection(
                title = stringResource(R.string.privacy_intro_title),
                body = stringResource(R.string.privacy_intro_body)
            )

            // 2. Información que recopilamos
            PrivacySection(
                title = stringResource(R.string.privacy_data_collect_title),
                body = stringResource(R.string.privacy_data_collect_body)
            )
            
            BulletPoint(stringResource(R.string.privacy_data_item_profile))
            BulletPoint(stringResource(R.string.privacy_data_item_contact))
            BulletPoint(stringResource(R.string.privacy_data_item_usage))

            Spacer(Modifier.height(24.dp))

            // 3. Uso de la información
            PrivacySection(
                title = stringResource(R.string.privacy_usage_title),
                body = stringResource(R.string.privacy_usage_body)
            )

            // 4. Compartir información
            PrivacySection(
                title = stringResource(R.string.privacy_share_title),
                body = stringResource(R.string.privacy_share_body)
            )

            // 5. Tus derechos
            PrivacySection(
                title = stringResource(R.string.privacy_rights_title),
                body = stringResource(R.string.privacy_rights_body)
            )

            Spacer(Modifier.height(40.dp))
            
            // Fecha de actualización
            Text(
                text = stringResource(R.string.privacy_last_update),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun PrivacySection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
