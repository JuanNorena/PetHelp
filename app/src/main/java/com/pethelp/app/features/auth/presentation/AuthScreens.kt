/**
 * Archivo que centraliza las pantallas y componentes visuales del módulo de Autenticación.
 *
 * **Responsabilidad Principal:**
 * Proporcionar las interfaces de usuario para el flujo de bienvenida (Splash), inicio de sesión (Login),
 * registro de nuevos usuarios (Register) y recuperación de cuenta (Forgot Password).
 *
 * **Estructura del Archivo:**
 * 1. **Pantallas Principales**: Funciones Composable que representan cada vista completa.
 * 2. **Componentes Visuales**: Elementos decorativos como el logo compuesto o manchas desenfocadas.
 * 3. **Componentes Reutilizables**: Inputs y botones personalizados que mantienen la estética en todo el módulo.
 *
 * **Notas para Junior Developers:**
 * - Cada pantalla utiliza un `ViewModel` inyectado mediante [hiltViewModel] para gestionar su lógica y estado.
 * - Se hace un uso intensivo de [Modifier] para aplicar sombras, bordes redondeados y efectos visuales avanzados (blur).
 * - La navegación se gestiona a través de [navController], permitiendo transiciones fluidas entre pantallas.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 */
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
import com.pethelp.app.core.domain.model.UserRole
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Función auxiliar para determinar a qué pantalla debe ir un usuario tras autenticarse.
 *
 * @param role El rol del usuario ([UserRole]).
 * @return La ruta ([Screen]) correspondiente al rol.
 */
private fun authenticatedDestination(role: UserRole): Screen {
    return when (role) {
        UserRole.MODERATOR -> Screen.ModeratorPanel
        UserRole.USER -> Screen.Feed
    }
}

// ─── Splash ───────────────────────────────────────────────────────────────────
/**
 * Pantalla de bienvenida (Splash) de la aplicación.
 *
 * **Responsabilidad:**
 * Presentar la identidad de marca (logo y lema) y verificar si el usuario ya tiene una sesión
 * activa para redirigirlo automáticamente.
 *
 * **Lógica de UI:**
 * 1. **Redirección Automática:** Observa el estado de autenticación. Si el usuario está logueado,
 *    salta directamente a la pantalla principal correspondiente a su rol.
 * 2. **Fondo Estético:** Aplica un degradado vertical y "manchas" desenfocadas para un aspecto moderno.
 * 3. **Comienzo Manual:** Ofrece un botón destacado para iniciar el flujo de login si no hay sesión activa.
 *
 * @param navController Controlador de navegación para redirigir al usuario.
 * @param viewModel ViewModel que gestiona el estado de autenticación inicial.
 * @return No devuelve nada (función Composable).
 *
 * @author Equipo de Desarrollo PetHelp
 */
@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // PASO 1: Si ya está autenticado, redirigimos automáticamente según el rol del usuario.
    LaunchedEffect(uiState) {
        val authenticatedState = uiState as? AuthUiState.Authenticated
        if (authenticatedState != null) {
            navController.navigate(authenticatedDestination(authenticatedState.user.role)) {
                popUpTo<Screen.Splash>() { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // PASO 2: Fondo con degradado vertical crema → verde claro (primaryContainer).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // PASO 3: Elementos decorativos (manchas desenfocadas) de colores de marca.
        BlurredCircle(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            size = 288.dp,
            offsetX = 185.dp,
            offsetY = 160.dp
        )
        BlurredCircle(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
            size = 224.dp,
            offsetX = 80.dp,
            offsetY = 629.dp
        )

        // PASO 4: Contenido central (Logo + Nombre + Lema).
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con efecto Glassmorphism.
            PetHelpLogo()

            Spacer(Modifier.height(24.dp))

            // Título "PetHelp" con tipografía destacada.
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.2).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            // Lema de la aplicación.
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // PASO 5: Botón inferior "Comenzar" para avanzar manualmente al Login.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { navController.navigate(Screen.Login) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            ) {
                Text(
                    text = stringResource(R.string.btn_start),
                    color = MaterialTheme.colorScheme.onPrimary,
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
/**
 * Componente que renderiza el logo artístico de PetHelp.
 *
 * **Responsabilidad:**
 * Crear una composición visual que simboliza el cuidado animal (pata, corazón y mano)
 * utilizando un contenedor con efecto Glassmorphism.
 *
 * **Estructura:**
 * - **Contenedor**: Un [Box] con fondo semi-transparente, bordes redondeados y sombra.
 * - **Iconos**: Tres elementos superpuestos que forman la identidad visual de la app.
 *
 * @param modifier Modificador para ajustar el tamaño o posición del logo.
 * @return No devuelve nada (función Composable).
 *
 * @author Equipo de Desarrollo PetHelp
 */
@Composable
private fun PetHelpLogo(modifier: Modifier = Modifier) {
    // PASO 1: Contenedor glassmorphism (blanco 60 % + sombra suave + esquinas 24 dp).
    Box(
        modifier = modifier
            .size(160.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // PASO 2: Composición de iconos superpuestos.
        Box(modifier = Modifier.size(96.dp)) {
            // Icono de pata (Verde - Primario).
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
            // Icono de corazón (Naranja - Secundario).
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-8).dp)
            )
            // Icono de mano interactiva (Púrpura - Terciario).
            Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .rotate(-15f)
            )
        }
    }
}

/**
 * Crea una mancha circular desenfocada para fondos estéticos.
 *
 * @param color Color de la mancha (usualmente con transparencia).
 * @param size Diámetro del círculo.
 * @param offsetX Desplazamiento en el eje X.
 * @param offsetY Desplazamiento en el eje Y.
 */
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
/**
 * Pantalla de inicio de sesión de la aplicación.
 *
 * **Responsabilidad:**
 * Permitir a los usuarios existentes acceder a su cuenta mediante correo y contraseña.
 * Maneja estados de validación, errores de autenticación y redirección post-login.
 *
 * **Lógica de Funcionamiento:**
 * 1. **Gestión de Estado Local:** Controla los campos de texto y la visibilidad de la contraseña.
 * 2. **Suscripción a Eventos:** Escucha el canal de `snackbarMessage` del ViewModel para mostrar
 *    errores de red o credenciales inválidas.
 * 3. **Redirección:** Al detectar un estado `Authenticated`, navega a la pantalla principal.
 * 4. **Interfaz de Usuario:** Utiliza un diseño limpio con una imagen circular, campos de texto
 *    estilizados y un botón de acción principal con indicador de carga.
 *
 * @param navController Controlador para navegar a registro o recuperación de contraseña.
 * @param viewModel ViewModel que procesa la solicitud de login.
 * @return No devuelve nada (función Composable).
 *
 * @author Equipo de Desarrollo PetHelp
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // PASO 1: Configuramos el Snackbar para mostrar mensajes informativos o de error.
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { uiText ->
            snackbarHostState.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
        }
    }

    // PASO 2: Observamos si el login es exitoso para navegar a la pantalla principal.
    LaunchedEffect(uiState) {
        val authenticatedState = uiState as? AuthUiState.Authenticated
        if (authenticatedState != null) {
            navController.navigate(authenticatedDestination(authenticatedState.user.role)) {
                popUpTo<Screen.Splash>() { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo degradado y decorativo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        BlurredCircle(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            size = 288.dp,
            offsetX = 185.dp,
            offsetY = 160.dp
        )

        // PASO 3: Contenido principal scrolleable (para evitar problemas con el teclado).
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen de cabecera: Perrito feliz.
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        spotColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
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

            // Título de bienvenida.
            Text(
                text = stringResource(R.string.login_greeting),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = (-0.75).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            // Subtítulo instructivo.
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // PASO 4: Campos de entrada (Correo y Contraseña).
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.email_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f),
                        spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)
                    )
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.password_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f),
                        spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)
                    )
            )

            Spacer(Modifier.height(4.dp))

            // Enlace de recuperación de contraseña.
            Text(
                text = stringResource(R.string.forgot_password_link),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { navController.navigate(Screen.ForgotPassword) }
                    .padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // PASO 5: Botón de acción principal.
            Button(
                onClick = {
                    viewModel.login(email.trim(), password)
                },
                enabled = email.isNotBlank() && password.isNotBlank()
                        && uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            ) {
                if (uiState is AuthUiState.Loading) {
                    // Indicador de carga mientras se procesa la autenticación.
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_login),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.45.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // PASO 6: Enlace para ir a la pantalla de registro.
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.no_account_action),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register)
                    }
                )
            }
        }

        // Host del Snackbar.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ─── Register ─────────────────────────────────────────────────────────────────
/**
 * Pantalla de registro para nuevos usuarios de PetHelp.
 *
 * **Responsabilidad:**
 * Recolectar la información necesaria (nombre, correo, contraseña) para crear una nueva cuenta.
 * Incluye validación de fortaleza de contraseña y aceptación de términos y condiciones.
 *
 * **Lógica de UI:**
 * 1. **Estado de Formulario:** Gestiona los inputs y el checkbox de términos.
 * 2. **Validación Visual:** Muestra un indicador dinámico de la fortaleza de la contraseña.
 * 3. **Diálogos:** Integra un diálogo para la lectura de los términos legales.
 * 4. **Flujo de Éxito:** Al registrarse, redirige al usuario al Feed de la aplicación.
 *
 * @param navController Controlador para navegar de regreso al Login.
 * @param viewModel ViewModel que gestiona la creación del usuario en Firebase.
 * @return No devuelve nada (función Composable).
 *
 * @author Equipo de Desarrollo PetHelp
 */
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

    // PASO 1: Gestión de notificaciones (Snackbars).
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { uiText ->
            snackbarHostState.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
        }
    }

    // PASO 2: Navegación post-registro exitoso.
    LaunchedEffect(uiState) {
        val authenticatedState = uiState as? AuthUiState.Authenticated
        if (authenticatedState != null) {
            navController.navigate(authenticatedDestination(authenticatedState.user.role)) {
                popUpTo<Screen.Splash>() { inclusive = true }
            }
        }
    }

    // PASO 3: Modal de Términos y Condiciones.
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
        // Fondo decorativo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        BlurredCircle(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            size = 320.dp,
            offsetX = 153.dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = 597.dp
        )

        // PASO 4: Contenido Scrollable.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 800.dp) // Aproximación para centrado visual.
            ) {
                // PASO 5: Botón de regreso.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, start = 20.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    BackButton(onClick = { navController.popBackStack() })
                }

                // PASO 6: Bloque de Registro Centrado.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 120.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Título y encabezado.
                    Text(
                        text = stringResource(R.string.register_heading),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(32.dp))

                    // Inputs del formulario.
                    AuthTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = stringResource(R.string.name_hint),
                        leadingIcon = Icons.Filled.Person,
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(Modifier.height(20.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(R.string.email_hint),
                        leadingIcon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(20.dp))

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

                    // Indicador de fortaleza de contraseña.
                    Spacer(Modifier.height(8.dp))
                    PasswordStrengthIndicator(password)

                    Spacer(Modifier.height(24.dp))

                    // PASO 7: Aceptación de términos.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Text(
                            text = stringResource(R.string.terms_prefix),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.terms_link),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showTermsDialog = true }
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // PASO 8: Botón de acción principal con validación de formulario.
                    val isFormValid = name.isNotBlank() && email.isNotBlank()
                            && password.length >= 6 && termsAccepted
                    Button(
                        onClick = {
                            viewModel.register(name.trim(), email.trim(), password)
                        },
                        enabled = isFormValid && uiState !is AuthUiState.Loading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.btn_register_me),
                                color = MaterialTheme.colorScheme.onPrimary,
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
        }

        // Host del Snackbar.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ─── Forgot Password ──────────────────────────────────────────────────────────
/**
 * Pantalla para la recuperación de contraseña de PetHelp.
 *
 * **Responsabilidad:**
 * Facilitar al usuario el envío de un correo electrónico para restablecer su contraseña.
 * Maneja dos estados visuales: el formulario de entrada y la confirmación de envío.
 *
 * **Lógica de UI:**
 * 1. **Estado Inicial:** Muestra un campo de texto para el correo y un botón de envío.
 * 2. **Estado de Éxito:** Al confirmarse el envío desde Firebase, muestra una tarjeta informativa ([LinkSentCard]).
 * 3. **Navegación:** Permite regresar al Login en cualquier momento.
 *
 * @param navController Controlador de navegación.
 * @param viewModel ViewModel que procesa la solicitud de restablecimiento.
 * @return No devuelve nada (función Composable).
 *
 * @author Equipo de Desarrollo PetHelp
 */
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resetEmailSent by viewModel.resetEmailSent.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }

    // PASO 1: Gestión de mensajes de error o informativos (Snackbars).
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { uiText ->
            snackbarHostState.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // PASO 2: Fondo decorativo con degradado vertical.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        BlurredCircle(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            size = 256.dp,
            offsetX = (-80).dp,
            offsetY = (-80).dp
        )
        BlurredCircle(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            size = 288.dp,
            offsetX = 185.dp,
            offsetY = 160.dp
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // PASO 3: Cabecera con botón de regreso a la pantalla anterior.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BackButton(onClick = { navController.popBackStack() })
            }

            // PASO 4: Contenido dinámico centrado según el estado de la solicitud.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.weight(0.15f))

                // Icono ilustrativo central (Llave + Pata).
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ForgotPasswordIcon()
                }

                Spacer(Modifier.height(32.dp))

                // Título y descripción de la funcionalidad.
                Text(
                    text = stringResource(R.string.forgot_password_heading),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        letterSpacing = (-0.75).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.forgot_password_body),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                if (!resetEmailSent) {
                    // PASO 5: Estado Inicial - Formulario para ingresar el correo electrónico.
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(R.string.email_hint),
                        leadingIcon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(32.dp))

                    // Botón para disparar la solicitud de recuperación a Firebase.
                    Button(
                        onClick = { viewModel.sendPasswordReset(email.trim()) },
                        enabled = email.isNotBlank() && uiState !is AuthUiState.Loading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            // Feedback visual de carga.
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.btn_send_reset),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.45.sp
                                )
                            )
                        }
                    }
                } else {
                    // PASO 6: Estado de Éxito - Muestra la confirmación de envío.
                    Spacer(Modifier.height(16.dp))
                    LinkSentCard(
                        email = email,
                        onBackToStart = {
                            navController.navigate(Screen.Login) {
                                popUpTo<Screen.Splash>() { inclusive = false }
                            }
                        }
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }

        // Host para notificaciones flotantes.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ─── Tarjeta "¡Enlace enviado!" ───────────────────────────────────────────────
/**
 * Tarjeta informativa que confirma el envío del enlace de recuperación de contraseña.
 *
 * @param email Correo electrónico al que se ha enviado el enlace de restablecimiento.
 * @param onBackToStart Función de callback para navegar de regreso a la pantalla de inicio (Login).
 */
@Composable
private fun LinkSentCard(email: String, onBackToStart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de correo: Pata verde dentro de un círculo suave.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Mensaje principal de éxito.
            Text(
                text = stringResource(R.string.link_sent_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            // Cuerpo del mensaje indicando el destino del correo de forma resaltada.
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Botón para finalizar el flujo y volver al Login.
            OutlinedButton(
                onClick = onBackToStart,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.btn_back_to_start),
                    color = MaterialTheme.colorScheme.primary,
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
/**
 * Icono decorativo compuesto "Llave + Pata" para la identidad visual de recuperación de contraseña.
 *
 * @param modifier Modificador opcional para personalizar el layout del componente.
 */
@Composable
private fun ForgotPasswordIcon(modifier: Modifier = Modifier) {
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // PASO 1: Círculo de fondo turquesa translúcido.
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
        // PASO 2: Borde blanco semi-transparente dibujado manualmente para mayor control.
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
                            color = outlineColor,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }
                )
        )
        // PASO 3: Icono de llave (VpnKey) principal.
        Icon(
            imageVector = Icons.Filled.VpnKey,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(64.dp)
        )
        // PASO 4: Pata pequeña con sombra y fondo, rotada para dinamismo.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-20).dp)
                .size(40.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f),
                    spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)
                )
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .rotate(12f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─── Componentes reutilizables ────────────────────────────────────────────────

/**
 * Botón circular personalizado para la navegación de regreso.
 *
 * **Responsabilidad:**
 * Ofrecer una forma consistente y visualmente atractiva de volver a la pantalla anterior,
 * siguiendo el diseño circular de Figma con una sombra suave.
 *
 * @param onClick Función de callback que se ejecuta al pulsar el botón.
 */
@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.common_back),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Campo de texto personalizado diseñado específicamente para los formularios de autenticación.
 *
 * **Responsabilidad:**
 * Proporcionar una entrada de datos consistente que incluya iconos, gestión de contraseñas
 * y un estilo visual alineado con la marca (bordes redondeados y sombras).
 *
 * **Características:**
 * - **Soporte de Contraseña:** Gestiona automáticamente la visibilidad y transformación del texto.
 * - **Iconografía:** Permite definir un icono principal descriptivo.
 * - **Estilo Figma:** Fondo blanco, esquinas de 14dp y una sombra sutil.
 *
 * @param value Valor actual del texto.
 * @param onValueChange Callback para notificar cambios en el texto.
 * @param placeholder Texto de ayuda (hint) cuando el campo está vacío.
 * @param leadingIcon Icono visual a la izquierda del campo.
 * @param keyboardType Tipo de teclado a mostrar (Email, Password, etc.).
 * @param isPassword Define si el campo debe ocultar el texto.
 * @param passwordVisible Estado actual de visibilidad de la contraseña.
 * @param onTogglePassword Callback para cambiar la visibilidad de la contraseña.
 */
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
            Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)
            )
    )
}

/**
 * Barra de progreso dinámica que indica la fortaleza de la contraseña ingresada.
 *
 * **Responsabilidad:**
 * Proporcionar feedback visual inmediato al usuario sobre la seguridad de su contraseña,
 * cambiando de color y longitud según la complejidad.
 *
 * **Lógica de Evaluación:**
 * - **Rojo (0-4 caracteres):** Muy débil.
 * - **Naranja (6 caracteres):** Débil.
 * - **Violeta (8 caracteres):** Media.
 * - **Turquesa (12+ caracteres):** Fuerte.
 *
 * @param password La cadena de texto de la contraseña a evaluar.
 */
@Composable
private fun PasswordStrengthIndicator(password: String) {
    // Calculamos el nivel de fortaleza (0f a 1f).
    val strength = when {
        password.length < 4  -> 0f
        password.length < 6  -> 0.25f
        password.length < 8  -> 0.5f
        password.length < 12 -> 0.75f
        else                 -> 1f
    }
    // Asignamos un color semántico según el nivel obtenido.
    val strengthColor = when {
        strength <= 0.25f -> MaterialTheme.colorScheme.error
        strength <= 0.5f  -> MaterialTheme.colorScheme.secondary
        strength <= 0.75f -> MaterialTheme.colorScheme.tertiary
        else              -> MaterialTheme.colorScheme.primary
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
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

// ─── Terms & Conditions Dialog ────────────────────────────────────────────────
/**
 * Diálogo modal que muestra los Términos y Condiciones legales de PetHelp.
 *
 * **Responsabilidad:**
 * Presentar el texto legal de la aplicación en un contenedor scrolleable, asegurando
 * que el usuario pueda leerlo antes de aceptar.
 *
 * @param onDismiss Función para cerrar el diálogo sin realizar acciones adicionales.
 * @param onAccept Función que se ejecuta cuando el usuario pulsa el botón "Aceptar".
 */
@Composable
private fun TermsAndConditionsDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Contenedor principal del diálogo.
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── PASO 1: Cabecera del diálogo ──
                Text(
                    text = stringResource(R.string.terms_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── PASO 2: Contenido legal scrolleable ──
                Text(
                    text = stringResource(R.string.terms_dialog_content),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── PASO 3: Botones de acción (Cerrar / Aceptar) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = stringResource(R.string.terms_dialog_close),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.terms_dialog_accept),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─── Helper placeholder ───────────────────────────────────────────────────────
/**
 * Pantalla de marcador de posición (Placeholder) para secciones en desarrollo.
 *
 * **Responsabilidad:**
 * Mostrar un mensaje temporal y un botón de acción para facilitar la navegación
 * durante la fase de prototipado o cuando una pantalla aún no ha sido implementada.
 *
 * @param title El título o mensaje a mostrar en el centro de la pantalla.
 * @param onNavigate Función que se ejecuta al pulsar el botón de continuación.
 */
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
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onNavigate,
                shape = RoundedCornerShape(50)
            ) {
                Text(text = stringResource(R.string.btn_continue_placeholder))
            }
        }
    }
}
