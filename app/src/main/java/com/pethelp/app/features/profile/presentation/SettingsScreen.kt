package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla de configuraciones de cuenta, privacidad, apariencia y soporte.
 */
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_settings), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back_to_start),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // CUENTA
            SettingsSectionTitle(stringResource(R.string.settings_account_section))
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.PersonOutline,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.edit_profile_title),
                    onClick = { navController.navigate(Screen.EditProfile) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsItem(
                    icon = Icons.Default.Shield,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_security),
                    onClick = { navController.navigate(Screen.Security) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                val currentLanguage by viewModel.language.collectAsState()
                
                SettingsItem(
                    icon = Icons.Default.Language,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_language),
                    value = if (currentLanguage == "es") "Español" else "English",
                    onClick = { navController.navigate(Screen.Language) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // NOTIFICACIONES
            SettingsSectionTitle(stringResource(R.string.settings_notifications_section))
            SettingsCard {
                var pushEnabled by remember { mutableStateOf(true) }
                var emailEnabled by remember { mutableStateOf(false) }

                SettingsToggleItem(
                    icon = Icons.Default.NotificationsNone,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_push_notifications),
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsToggleItem(
                    icon = Icons.Default.PhoneIphone,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_email_alerts),
                    checked = emailEnabled,
                    onCheckedChange = { emailEnabled = it }
                )
            }

            Spacer(Modifier.height(24.dp))

            // PRIVACIDAD
            SettingsSectionTitle(stringResource(R.string.settings_privacy_section))
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.LockOpen,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_privacy_policy),
                    onClick = { navController.navigate(Screen.Privacy) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsItem(
                    icon = Icons.Default.Visibility,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_profile_visibility),
                    value = stringResource(R.string.settings_visibility_public),
                    onClick = { navController.navigate(Screen.ProfileVisibility) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // APARIENCIA
            SettingsSectionTitle(stringResource(R.string.settings_appearance_section))
            SettingsCard {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                SettingsToggleItem(
                    icon = Icons.Default.LightMode,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_dark_mode),
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // SOPORTE
            SettingsSectionTitle(stringResource(R.string.settings_support_section))
            SettingsCard {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_help_center),
                    onClick = { navController.navigate(Screen.HelpCenter) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Botón Cerrar sesión
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Feed> { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                elevation = null
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_logout), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "PetHelp v1.0.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        letterSpacing = 0.5.sp
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (value != null) {
            Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
