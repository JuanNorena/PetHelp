package com.pethelp.app.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.WarmOrange
import com.pethelp.app.core.ui.theme.SoftBlue

// ─── Splash ───────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(navController: NavController) {
    // TODO (Fase 2): verificar sesión activa en Firebase Auth
    //   → autenticado: navegar a Screen.Feed
    //   → no autenticado: navegar a Screen.Login

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
            // logo — reemplazar por Image(painterResource(R.drawable.ic_pethelp_logo))
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = PetHelpGreen,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = PetHelpGreen
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // botón inferior
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_start),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
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
fun LoginScreen(navController: NavController) {
    // TODO (Fase 2): implementar formulario de login
    //   → Firebase Auth email/password
    //   → Detección de rol (USER/MODERATOR) y redirección
    PlaceholderScreen(
        title = "Iniciar sesión",
        onNavigate = { navController.navigate(Screen.Feed.route) }
    )
}

// ─── Register ─────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(navController: NavController) {
    // TODO (Fase 2): implementar formulario de registro
    PlaceholderScreen(
        title = "Crear cuenta",
        onNavigate = { navController.navigate(Screen.Feed.route) }
    )
}

// ─── Forgot Password ──────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    // TODO (Fase 3): Firebase Auth sendPasswordResetEmail
    PlaceholderScreen(
        title = "Recuperar contraseña",
        onNavigate = { navController.popBackStack() }
    )
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
