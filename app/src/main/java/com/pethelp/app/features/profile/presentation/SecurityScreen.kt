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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
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

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPasswordConfirmDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.security_title), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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

            // CONTRASEÑA
            SecuritySectionHeader(Icons.Outlined.Key, stringResource(R.string.security_password_section), Color(0xFF2DD4BF))
            SecurityCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.security_current_password), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("********") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.security_new_password), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("********") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            if (newPassword.isNotBlank()) {
                                showPasswordConfirmDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Text(stringResource(R.string.security_update_password), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // DIALOGO DE CONFIRMACIÓN DE CONTRASEÑA
            if (showPasswordConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showPasswordConfirmDialog = false },
                    icon = { 
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFE0F7F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF00BFA5), modifier = Modifier.size(32.dp))
                        }
                    },
                    title = { 
                        Text(
                            stringResource(R.string.security_update_password),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF111827)
                        ) 
                    },
                    text = { 
                        Text(
                            "¿Confirmas que deseas cambiar tu contraseña? Por seguridad, es posible que debas iniciar sesión de nuevo tras el cambio.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color(0xFF4B5563),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ) 
                    },
                    confirmButton = {
                        Button(
                            onClick = { 
                                viewModel.updatePassword(newPassword)
                                showPasswordConfirmDialog = false
                                currentPassword = ""
                                newPassword = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                        ) {
                            Text("Confirmar cambio", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPasswordConfirmDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_cancel), color = Color(0xFF6B7280))
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = Color.White,
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true)
                )
            }

            Spacer(Modifier.height(24.dp))

            // AUTENTICACIÓN
            SecuritySectionHeader(Icons.Default.Shield, stringResource(R.string.security_auth_section), Color(0xFFFBBF24))
            SecurityCard {
                var twoStepEnabled by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.security_2fa_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(stringResource(R.string.security_2fa_desc), fontSize = 13.sp, color = Color(0xFF6B7280), lineHeight = 18.sp)
                    }
                    Switch(
                        checked = twoStepEnabled,
                        onCheckedChange = { twoStepEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E)
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // DISPOSITIVOS
            SecuritySectionHeader(Icons.Outlined.Smartphone, stringResource(R.string.security_devices_section), Color(0xFF8B5CF6))
            SecurityCard {
                val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                val model = android.os.Build.MODEL
                val deviceName = "$manufacturer $model"
                val androidVersion = "Android ${android.os.Build.VERSION.RELEASE}"
                
                DeviceItem(
                    deviceName = deviceName,
                    deviceInfo = "${stringResource(R.string.security_this_device)} • $androidVersion",
                    icon = Icons.Default.Smartphone,
                    isCurrent = true
                )
            }

            Spacer(Modifier.height(24.dp))

            // ZONA DE PELIGRO
            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.security_danger_zone)) },
                    text = { Text(stringResource(R.string.security_delete_account_desc)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteAccount()
                                showDeleteDialog = false
                                navController.navigate(Screen.Login) {
                                    popUpTo(0)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text(stringResource(R.string.btn_delete_account))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            SecurityCard(containerColor = Color(0xFFFFEBEE)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.security_danger_zone), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.security_delete_account_desc),
                        fontSize = 13.sp,
                        color = Color(0xFFF87171),
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_delete_account), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SecuritySectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SecurityCard(
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun DeviceItem(
    deviceName: String,
    deviceInfo: String,
    icon: ImageVector,
    isCurrent: Boolean = false,
    showLogout: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(if (isCurrent) Color(0xFFE0F7F6) else Color(0xFFF3F4F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isCurrent) Color(0xFF00BFA5) else Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(deviceName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(deviceInfo, fontSize = 12.sp, color = if (isCurrent) Color(0xFF00BFA5) else Color(0xFF9CA3AF))
        }
        if (showLogout) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
            }
        }
    }
}
