package com.pethelp.app.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.DarkGreen
import com.pethelp.app.core.ui.theme.PetHelpGreen
import com.pethelp.app.core.ui.theme.SoftBlue
import com.pethelp.app.core.ui.theme.WarmOrange

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
