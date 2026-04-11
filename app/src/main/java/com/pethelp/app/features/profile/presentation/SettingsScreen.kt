package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.pethelp.app.core.ui.theme.PetHelpPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back_to_start))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
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
                    iconColor = Color(0xFF3B82F6),
                    title = stringResource(R.string.edit_profile_title),
                    onClick = { navController.navigate(Screen.EditProfile) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                SettingsItem(
                    icon = Icons.Default.Shield,
                    iconColor = Color(0xFF10B981),
                    title = stringResource(R.string.settings_security),
                    onClick = { navController.navigate(Screen.Security) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                SettingsItem(
                    icon = Icons.Default.Language,
                    iconColor = Color(0xFF8B5CF6),
                    title = stringResource(R.string.settings_language),
                    value = stringResource(R.string.settings_language_value),
                    onClick = { /* TODO */ }
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
                    iconColor = Color(0xFFF87171),
                    title = stringResource(R.string.settings_push_notifications),
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                SettingsToggleItem(
                    icon = Icons.Default.PhoneIphone,
                    iconColor = Color(0xFF64748B),
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
                    iconColor = Color(0xFF2DD4BF),
                    title = stringResource(R.string.settings_privacy_policy),
                    onClick = { navController.navigate(Screen.Privacy) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                SettingsItem(
                    icon = Icons.Default.Visibility,
                    iconColor = Color(0xFF6366F1),
                    title = stringResource(R.string.settings_profile_visibility),
                    value = stringResource(R.string.settings_visibility_public),
                    onClick = { /* TODO */ }
                )
            }

            Spacer(Modifier.height(24.dp))

            // APARIENCIA
            SettingsSectionTitle(stringResource(R.string.settings_appearance_section))
            SettingsCard {
                var darkMode by remember { mutableStateOf(false) }
                SettingsToggleItem(
                    icon = Icons.Default.LightMode,
                    iconColor = Color(0xFFFBBF24),
                    title = stringResource(R.string.settings_dark_mode),
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            Spacer(Modifier.height(24.dp))

            // SOPORTE
            SettingsSectionTitle(stringResource(R.string.settings_support_section))
            SettingsCard {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconColor = Color(0xFF06B6D4),
                    title = stringResource(R.string.settings_help_center),
                    onClick = { /* TODO */ }
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
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFEF4444)
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
                color = Color(0xFF9CA3AF),
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
        color = Color(0xFF6B7280),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        letterSpacing = 0.5.sp
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
            color = Color(0xFF111827)
        )
        if (value != null) {
            Text(text = value, color = Color(0xFF9CA3AF), fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
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
            color = Color(0xFF111827)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF22C55E),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E7EB),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
