package com.pethelp.app.features.profile.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.security.BiometricAuthGate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPasswordConfirmDialog by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? FragmentActivity
    val promptExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    fun requireSensitiveAuth(onAuthorized: () -> Unit) {
        if (activity == null) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.security_biometric_unavailable))
            }
            return
        }

        when (BiometricAuthGate.availability(context)) {
            BiometricAuthGate.Availability.AVAILABLE -> {
                BiometricAuthGate.authenticate(
                    activity = activity,
                    executor = promptExecutor,
                    title = context.getString(R.string.security_biometric_prompt_title),
                    subtitle = context.getString(R.string.security_biometric_prompt_subtitle),
                    description = context.getString(R.string.security_biometric_prompt_description),
                    onSuccess = onAuthorized,
                    onError = { errorCode, errorMessage ->
                        val isUserCancel = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_CANCELED
                        if (!isUserCancel) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.security_biometric_error, errorMessage)
                                )
                            }
                        }
                    }
                )
            }

            BiometricAuthGate.Availability.NONE_ENROLLED -> {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.security_biometric_not_enrolled))
                }
            }

            else -> {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.security_biometric_unavailable))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { uiText ->
            snackbarHostState.showSnackbar(uiText.asString(context))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.security_title), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // CONTRASEÑA
            SecuritySectionHeader(Icons.Outlined.Key, stringResource(R.string.security_password_section), MaterialTheme.colorScheme.primary)
            SecurityCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.security_current_password), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("********") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.security_new_password), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("********") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                                showPasswordConfirmDialog = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.error_field_required))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                    },
                    title = { 
                        Text(
                            stringResource(R.string.security_update_password),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    text = { 
                        Text(
                            stringResource(R.string.security_change_password_confirm_body),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ) 
                    },
                    confirmButton = {
                        Button(
                            onClick = { 
                                // Capturar valores antes de limpiar (el flow biométrico es asincrónico)
                                val savedCurrentPassword = currentPassword
                                val savedNewPassword = newPassword
                                requireSensitiveAuth {
                                    viewModel.updatePassword(savedCurrentPassword, savedNewPassword)
                                }
                                showPasswordConfirmDialog = false
                                currentPassword = ""
                                newPassword = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.security_change_password_confirm_action), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPasswordConfirmDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true)
                )
            }

            Spacer(Modifier.height(24.dp))

            // AUTENTICACIÓN
            SecuritySectionHeader(Icons.Default.Shield, stringResource(R.string.security_auth_section), MaterialTheme.colorScheme.secondary)
            SecurityCard {
                val uiState = viewModel.uiState.collectAsState().value
                val user = (uiState as? ProfileUiState.Success)?.user
                val twoStepEnabled = user?.twoFactorEnabled ?: false
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.security_2fa_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(stringResource(R.string.security_2fa_desc), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    }
                    Switch(
                        checked = twoStepEnabled,
                        onCheckedChange = { enabled ->
                            user?.let {
                                viewModel.updateProfile(it.copy(twoFactorEnabled = enabled))
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // DISPOSITIVOS
            SecuritySectionHeader(Icons.Outlined.Smartphone, stringResource(R.string.security_devices_section), MaterialTheme.colorScheme.primary)
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
                    title = { Text(stringResource(R.string.security_danger_zone), color = MaterialTheme.colorScheme.error) },
                    text = { Text(stringResource(R.string.security_delete_account_desc), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                requireSensitiveAuth {
                                    viewModel.deleteAccount(onSuccess = {
                                        navController.navigate(Screen.Login) {
                                            popUpTo(0)
                                        }
                                    })
                                }
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.btn_delete_account))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            SecurityCard(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.security_danger_zone), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.security_delete_account_desc),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SecurityCard(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
            modifier = Modifier.size(40.dp).background(if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(deviceName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(deviceInfo, fontSize = 12.sp, color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (showLogout) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}
