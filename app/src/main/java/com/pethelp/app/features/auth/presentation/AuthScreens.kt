package com.pethelp.app.features.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.DarkGreen
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.SoftBlue
import com.pethelp.app.core.ui.theme.WarmOrange
import kotlinx.coroutines.flow.collectLatest

// ─── Splash ───────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Si ya está autenticado, ir directo al Feed
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // fondo degradado vertical crema → verde claro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFE8F5E9))
                    )
                )
        )

        // manchas desenfocadas de colores primario/secundario/terciario
        BlurredCircle(
            color = PetHelpGreen.copy(alpha = 0.1f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = WarmOrange.copy(alpha = 0.1f),
            size = 288.dp,
            offsetX = 185.dp,
            offsetY = 160.dp
        )
        BlurredCircle(
            color = SoftBlue.copy(alpha = 0.1f),
            size = 224.dp,
            offsetX = 80.dp,
            offsetY = 629.dp
        )

        // contenido central
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo: contenedor glassmorphism con iconos compuestos ──
            PetHelpLogo()

            Spacer(Modifier.height(24.dp))

            // título "PetHelp" en verde oscuro (#2E7D32) según Figma
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.2).sp
                ),
                color = DarkGreen
            )

            Spacer(Modifier.height(12.dp))

            // lema
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF6A7282)
            )
        }

        // botón inferior "Comenzar"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PetHelpGreen),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = PetHelpGreen.copy(alpha = 0.3f),
                        spotColor = PetHelpGreen.copy(alpha = 0.3f)
                    )
            ) {
                Text(
                    text = stringResource(R.string.btn_start),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

// ─── Logo compuesto: pata + corazón + mano ────────────────────────────────────
@Composable
private fun PetHelpLogo(modifier: Modifier = Modifier) {
    // Contenedor glassmorphism (blanco 40 % + sombra suave + esquinas 24 dp)
    Box(
        modifier = modifier
            .size(160.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.10f)
            )
            .background(
                color = Color.White.copy(alpha = 0.40f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(96.dp)) {
            // Pata principal — verde (64 dp, centrada)
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = PetHelpGreen,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
            // Corazón — coral (36 dp, arriba-izquierda)
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = WarmOrange,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-8).dp)
            )
            // Mano interactiva — púrpura (36 dp, abajo-derecha, rotada −15°)
            Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = SoftBlue,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .rotate(-15f)
            )
        }
    }
}

@Composable
private fun BlurredCircle(
    color: Color,
    size: Dp,
    offsetX: Dp,
    offsetY: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .offset(x = offsetX, y = offsetY)
            .background(color = color, shape = CircleShape)
            .blur(64.dp)
    )
}

// ─── Login ────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ── Snackbar (Material Design best practice para mensajes de error) ──
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Navegar al Feed cuando el login sea exitoso
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // fondo degradado igual que splash
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFE8F5E9))
                    )
                )
        )

        // manchas desenfocadas
        BlurredCircle(
            color = PetHelpGreen.copy(alpha = 0.1f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = WarmOrange.copy(alpha = 0.1f),
            size = 288.dp,
            offsetX = 185.dp,
            offsetY = 160.dp
        )

        // contenido principal centrado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Foto circular del perrito ──
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFFF3F4F6),
                        spotColor = Color(0xFFF3F4F6)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_happy_puppy),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Título ──
            Text(
                text = stringResource(R.string.login_greeting),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = (-0.75).sp
                ),
                color = Color(0xFF101828)
            )

            Spacer(Modifier.height(8.dp))

            // ── Subtítulo ──
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = Color(0xFF6A7282)
            )

            Spacer(Modifier.height(32.dp))

            // ── Campo Correo electrónico ──
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.email_hint),
                        color = Color(0xFF99A1AF),
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = Color(0xFF99A1AF),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PetHelpGreen,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = PetHelpGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
            )

            Spacer(Modifier.height(20.dp))

            // ── Campo Contraseña ──
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.password_hint),
                        color = Color(0xFF99A1AF),
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFF99A1AF),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF99A1AF)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PetHelpGreen,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = PetHelpGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
            )

            Spacer(Modifier.height(4.dp))

            // ── ¿Olvidaste tu contraseña? ──
            Text(
                text = stringResource(R.string.forgot_password_link),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = PetHelpGreen,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { navController.navigate(Screen.ForgotPassword.route) }
                    .padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── Botón Iniciar Sesión ──
            Button(
                onClick = {
                    viewModel.login(email.trim(), password)
                },
                enabled = email.isNotBlank() && password.isNotBlank()
                        && uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PetHelpGreen),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = PetHelpGreen.copy(alpha = 0.3f),
                        spotColor = PetHelpGreen.copy(alpha = 0.3f)
                    )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_login),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.45.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── ¿No tienes cuenta? Regístrate ──
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_account_prompt),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF6A7282)
                )
                Text(
                    text = stringResource(R.string.no_account_action),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = PetHelpGreen,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
        }

        // ── Snackbar anclado en la parte inferior ──
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ─── Register ─────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // ── Snackbar ──
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Navegar al Feed cuando el registro sea exitoso
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // ── Modal de Términos y Condiciones ──
    if (showTermsDialog) {
        TermsAndConditionsDialog(
            onDismiss = { showTermsDialog = false },
            onAccept = {
                termsAccepted = true
                showTermsDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFE8F5E9))
                    )
                )
        )

        // manchas desenfocadas
        BlurredCircle(
            color = PetHelpGreen.copy(alpha = 0.05f),
            size = 320.dp,
            offsetX = 153.dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = WarmOrange.copy(alpha = 0.05f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = 597.dp
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header con botón atrás ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BackButton(onClick = { navController.popBackStack() })
            }

            // ── Contenido principal ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // ── Título ──
                Text(
                    text = stringResource(R.string.register_heading),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.9).sp
                    ),
                    color = Color(0xFF101828)
                )

                Spacer(Modifier.height(32.dp))

                // ── Campo Nombre completo ──
                AuthTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.name_hint),
                    leadingIcon = Icons.Filled.Person,
                    keyboardType = KeyboardType.Text
                )

                Spacer(Modifier.height(20.dp))

                // ── Campo Correo electrónico ──
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(R.string.email_hint),
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(20.dp))

                // ── Campo Contraseña ──
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = stringResource(R.string.password_hint),
                    leadingIcon = Icons.Filled.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                // ── Indicador de fortaleza ──
                Spacer(Modifier.height(8.dp))
                PasswordStrengthIndicator(password)

                Spacer(Modifier.height(24.dp))

                // ── Checkbox términos ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PetHelpGreen,
                            uncheckedColor = Color(0xFFD1D5DC)
                        )
                    )
                    Text(
                        text = stringResource(R.string.terms_prefix),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF4A5565)
                    )
                    Text(
                        text = stringResource(R.string.terms_link),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = PetHelpGreen,
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ── Botón Registrarme ──
                val isFormValid = name.isNotBlank() && email.isNotBlank()
                        && password.length >= 6 && termsAccepted
                Button(
                    onClick = {
                        viewModel.register(name.trim(), email.trim(), password)
                    },
                    enabled = isFormValid && uiState !is AuthUiState.Loading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetHelpGreen,
                        disabledContainerColor = Color(0xFFD1D5DC).copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isFormValid) 10.dp else 0.dp,
                        pressedElevation = 4.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.btn_register_me),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.45.sp
                            )
                        )
                    }
                }
            }
        }

        // ── Snackbar anclado en la parte inferior ──
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ─── Forgot Password ──────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resetEmailSent by viewModel.resetEmailSent.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }

    // ── Snackbar ──
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFE8F5E9))
                    )
                )
        )

        // manchas desenfocadas
        BlurredCircle(
            color = PetHelpGreen.copy(alpha = 0.1f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = WarmOrange.copy(alpha = 0.1f),
            size = 288.dp,
            offsetX = 185.dp,
            offsetY = 160.dp
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header con botón atrás ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BackButton(onClick = { navController.popBackStack() })
            }

            // ── Contenido ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.weight(0.15f))

                // ── Icono llave + pata ──
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ForgotPasswordIcon()
                }

                Spacer(Modifier.height(32.dp))

                // ── Título ──
                Text(
                    text = stringResource(R.string.forgot_password_heading),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        letterSpacing = (-0.75).sp
                    ),
                    color = Color(0xFF101828)
                )

                Spacer(Modifier.height(12.dp))

                // ── Descripción ──
                Text(
                    text = stringResource(R.string.forgot_password_body),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 26.sp
                    ),
                    color = Color(0xFF6A7282)
                )

                Spacer(Modifier.height(32.dp))

                if (!resetEmailSent) {
                    // ── Estado: formulario de correo ──

                    // ── Campo Correo ──
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(R.string.email_hint),
                        leadingIcon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(32.dp))

                    // ── Botón Enviar enlace ──
                    Button(
                        onClick = { viewModel.sendPasswordReset(email.trim()) },
                        enabled = email.isNotBlank() && uiState !is AuthUiState.Loading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = PetHelpGreen),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 10.dp,
                            pressedElevation = 4.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 15.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = PetHelpGreen.copy(alpha = 0.3f),
                                spotColor = PetHelpGreen.copy(alpha = 0.3f)
                            )
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.btn_send_reset),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.45.sp
                                )
                            )
                        }
                    }
                } else {
                    // ── Estado: enlace enviado ──
                    Spacer(Modifier.height(16.dp))
                    LinkSentCard(
                        email = email,
                        onBackToStart = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = false }
                            }
                        }
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }

        // ── Snackbar host ──
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ─── Tarjeta "¡Enlace enviado!" ───────────────────────────────────────────────
@Composable
private fun LinkSentCard(email: String, onBackToStart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // icono de correo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color(0xFFC8E6C9),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.link_sent_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = DarkGreen
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.link_sent_body_prefix))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(email)
                    }
                    append(stringResource(R.string.link_sent_body_suffix))
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                color = Color(0xFF388E3C),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Botón "Volver al inicio"
            OutlinedButton(
                onClick = onBackToStart,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, DarkGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.btn_back_to_start),
                    color = DarkGreen,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

// ─── Icono "Llave + Pata" para Forgot Password ──────────────────────────────
@Composable
private fun ForgotPasswordIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Círculo de fondo teal claro
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = Color(0xFFE0F2F1),
                    shape = CircleShape
                )
        )
        // Borde blanco semi-transparente
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .then(
                    Modifier.drawWithContent {
                        drawContent()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }
                )
        )
        // Icono de llave
        Icon(
            imageVector = Icons.Filled.VpnKey,
            contentDescription = null,
            tint = Color(0xFF78909C),
            modifier = Modifier.size(64.dp)
        )
        // Pata pequeña con fondo blanco (esquina inferior derecha)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-20).dp)
                .size(40.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .background(Color.White, CircleShape)
                .rotate(12f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = WarmOrange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─── Componentes reutilizables ────────────────────────────────────────────────

/** Botón circular blanco "atrás" — igual que en Figma */
@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(
                color = Color.White,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Atrás",
            tint = Color(0xFF101828),
            modifier = Modifier.size(24.dp)
        )
    }
}

/** Campo de texto estilizado según Figma (white fill, 16dp corners, shadow) */
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, color = Color(0xFF99A1AF), fontSize = 16.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color(0xFF99A1AF),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF99A1AF)
                    )
                }
            }
        } else null,
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = PetHelpGreen,
            unfocusedBorderColor = Color(0xFFE5E7EB),
            cursorColor = PetHelpGreen
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
    )
}

/** Indicador visual de fortaleza de contraseña (barra de progreso) */
@Composable
private fun PasswordStrengthIndicator(password: String) {
    val strength = when {
        password.length < 4  -> 0f
        password.length < 6  -> 0.25f
        password.length < 8  -> 0.5f
        password.length < 12 -> 0.75f
        else                 -> 1f
    }
    val strengthColor = when {
        strength <= 0.25f -> Color(0xFFE57373)
        strength <= 0.5f  -> WarmOrange
        strength <= 0.75f -> Color(0xFFFFD54F)
        else              -> PetHelpGreen
    }

    if (password.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { strength },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = strengthColor,
                trackColor = Color(0xFFF3F4F6),
            )
        }
    }
}

// ─── Terms & Conditions Dialog ────────────────────────────────────────────────
@Composable
private fun TermsAndConditionsDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──
                Text(
                    text = stringResource(R.string.terms_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF1A1A2E),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                )

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── Scrollable content ──
                Text(
                    text = stringResource(R.string.terms_dialog_content),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = Color(0xFF4B5563),
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── Buttons ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Text(
                            text = stringResource(R.string.terms_dialog_close),
                            color = Color(0xFF6B7280)
                        )
                    }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = PetHelpGreen)
                    ) {
                        Text(
                            text = stringResource(R.string.terms_dialog_accept),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─── Helper placeholder ───────────────────────────────────────────────────────
@Composable
private fun PlaceholderScreen(title: String, onNavigate: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onNavigate) {
                Text("Continuar (placeholder)")
            }
        }
    }
}
