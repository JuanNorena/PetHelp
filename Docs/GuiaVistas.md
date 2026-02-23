# Guía de Desarrollo de Vistas — PetHelp

> Documento técnico que traduce cada pantalla del diseño Figma a instrucciones concretas de implementación en **Jetpack Compose + Material 3**.  
> Fecha: Febrero 2026 · Versión: 1.0

---

## Índice

1. [Sistema de diseño — Tokens y componentes reutilizables](#1-sistema-de-diseño)
2. [Pantalla 1 — Splash / Bienvenida](#pantalla-1--splash--bienvenida)
3. [Pantalla 2 — Inicio de sesión (Login)](#pantalla-2--inicio-de-sesión-login)
4. [Pantalla 3 — Registro (Sign Up)](#pantalla-3--registro-sign-up)
5. [Pantalla 4 — Recuperación de contraseña](#pantalla-4--recuperación-de-contraseña)
6. [Pantalla 5 — Feed principal (Vista Lista)](#pantalla-5--feed-principal-vista-lista)
7. [Pantalla 6 — Feed principal (Vista Mapa)](#pantalla-6--feed-principal-vista-mapa)
8. [Pantalla 7 — Detalle de publicación](#pantalla-7--detalle-de-publicación)
9. [Pantalla 8 — Crear publicación](#pantalla-8--crear-publicación)
10. [Pantalla 9 — Panel de moderación](#pantalla-9--panel-de-moderación)
11. [Pantalla 10 — Diálogo rechazar publicación](#pantalla-10--diálogo-rechazar-publicación)
12. [Pantalla 11 — Notificaciones](#pantalla-11--notificaciones)
13. [Pantalla 12 — Perfil / Dashboard de gamificación](#pantalla-12--perfil--dashboard-de-gamificación)
14. [Pantallas faltantes del Figma](#pantallas-faltantes-del-figma)
15. [Mapa de archivos — Pantalla → Archivo Kotlin](#mapa-de-archivos)

---

## 1. Sistema de diseño

Antes de implementar cualquier pantalla, hay que alinear los **tokens de diseño** del Figma con lo que existe en código. Esto evita crear colores ad-hoc en cada pantalla.

### 1.1 Paleta de colores — Figma vs Código

| Token Figma | Hex Figma | Variable Kotlin actual | Hex Kotlin | Acción |
|---|---|---|---|---|
| Color Primario (Verde Salvia) | `#4CAF50` | `PetHelpGreen` | `#2E7D32` | **Actualizar** → el Figma usa un verde más claro y vibrante |
| Color Secundario (Coral) | `#FF8A65` | `WarmOrange` | `#E65100` | **Actualizar** → el Figma usa coral suave, no naranja oscuro |
| Color Terciario (Púrpura IA) | `#B39DDB` | `SoftBlue` | `#1565C0` | **Cambiar** → Figma usa violeta, código actual tiene azul |
| Fondo Light | `#FAFAFA` / `#F5F5F7` | `BackgroundLight` | `#F9FAF8` | ≈ OK, ajuste mínimo |
| Fondo Dark | `#1E1E1E` / `#121212` | `BackgroundDark` | `#111512` | ≈ OK |
| Error / Rechazo | `#E57373` | `ErrorRed` | `#B00020` | **Actualizar** → el Figma usa rojo pastel amigable |

> **Tarea prioritaria:** actualizar `Color.kt` para que coincida exactamente con la paleta del Figma antes de implementar pantallas. Se recomienda hacer esta actualización en un solo commit: `chore(theme): sincronizar paleta de colores con Figma`.

### 1.2 Tipografía

El Figma especifica: **Outfit**, Inter o Roboto (sans-serif geométrica).

El código actual usa `FontFamily.Default` (Roboto en Android). Dos opciones:

| Opción | Esfuerzo | Resultado |
|---|---|---|
| **A)** Dejar `FontFamily.Default` (Roboto) | Ninguno | OK para Fase 2 — Roboto es Material 3 nativo |
| **B)** Integrar Outfit vía Google Fonts | Medio | Más fiel al Figma — importar con `googleFont()` de Compose |

**Recomendación:** Opción A para Fase 2, Opción B para Fase 3.

### 1.3 Formas (Shapes)

El Figma establece bordes **muy redondeados** y botones **pill-shaped** (totalmente redondeados). Crear en `core/ui/theme/Shape.kt`:

```kotlin
val PetHelpShapes = Shapes(
    small = RoundedCornerShape(8.dp),       // chips, tags
    medium = RoundedCornerShape(16.dp),     // tarjetas, campos de texto
    large = RoundedCornerShape(28.dp),      // diálogos, bottom sheets
    extraLarge = RoundedCornerShape(50),    // botones pill (totalmente redondos)
)
```

### 1.4 Componentes reutilizables a crear

Antes de las pantallas, crear estos composables en `core/ui/components/`:

| Componente | Archivo | Usado en |
|---|---|---|
| `PetHelpButton` | `PetHelpButton.kt` | Todas las pantallas |
| `PetHelpTextField` | `PetHelpTextField.kt` | Login, Registro, Recuperar, Crear publicación |
| `PetHelpTopBar` | `PetHelpTopBar.kt` | Feed, Detalle, Moderación, Perfil |
| `PetHelpBottomBar` | `PetHelpBottomBar.kt` | Feed, Mapa, Notificaciones, Perfil |
| `PostCard` | `PostCard.kt` | Feed lista, Feed mapa (bottom sheet) |
| `CategoryChip` | `CategoryChip.kt` | Feed lista, Feed mapa |
| `BadgeItem` | `BadgeItem.kt` | Perfil gamificación |
| `StatCard` | `StatCard.kt` | Estadísticas, Perfil |
| `NotificationItem` | `NotificationItem.kt` | Notificaciones |

---

## Pantalla 1 — Splash / Bienvenida

**Archivo:** `features/auth/presentation/SplashScreen.kt`  
**Ruta:** `Screen.Splash`  
**Figma:** Pantalla 1 (§3.1)

### Diseño visual

```
┌─────────────────────────┐
│                         │
│                         │
│      [Logo central]     │ ← Corazón + manos + pata (icono vectorial)
│                         │
│       PetHelp           │ ← headlineLarge, Bold, color primary
│  Encuentra, cuida,      │
│       adopta            │ ← bodyLarge, color onSurfaceVariant
│                         │
│                         │
│   ┌─────────────────┐   │
│   │    Comenzar      │   │ ← FilledButton pill, color primary, full-width
│   └─────────────────┘   │
│                         │
└─────────────────────────┘
Fondo: verde salvia pastel o crema (#FAFAFA)
```

### Estructura Compose

```kotlin
@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    // Layout: Column centrado vertical y horizontalmente
    // Scaffold NO — pantalla full-bleed sin TopBar
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Imagen/Logo — usar un painterResource (ic_pethelp_logo)
        //    Tamaño: ~120.dp x 120.dp
        //    Si no hay logo aún, usar un Icon placeholder (Icons.Default.Pets)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 2. Título "PetHelp" 
        //    Style: headlineLarge + FontWeight.Bold
        //    Color: MaterialTheme.colorScheme.primary
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 3. Lema "Encuentra, cuida, adopta"
        //    Style: bodyLarge
        //    Color: MaterialTheme.colorScheme.onSurfaceVariant
        
        Spacer(modifier = Modifier.weight(1f)) // empuja botón al fondo
        
        // 4. Botón "Comenzar"
        //    Button(shape = RoundedCornerShape(50))  ← pill
        //    modifier = Modifier.fillMaxWidth().height(56.dp)
        //    onClick = onNavigateToLogin
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
```

### Datos necesarios
- Ninguno — pantalla estática.

### Navegación
- Clic "Comenzar" → `Screen.Login`

---

## Pantalla 2 — Inicio de sesión (Login)

**Archivo:** `features/auth/presentation/LoginScreen.kt`  
**Ruta:** `Screen.Login`  
**Figma:** Pantalla 2 (§3.1)

### Diseño visual

```
┌─────────────────────────┐
│                         │
│   ¡Hola de nuevo!       │ ← headlineMedium, Bold
│                         │
│   [Ilustración mascota] │ ← Image o placeholder Icon(Pets)
│                         │
│   ┌─────────────────┐   │
│   │ 📧 Correo       │   │ ← OutlinedTextField, RoundedCorner(16.dp)
│   └─────────────────┘   │
│   ┌─────────────────┐   │
│   │ 🔒 Contraseña 👁│   │ ← OutlinedTextField + trailing icon toggle
│   └─────────────────┘   │
│                         │
│   ¿Olvidaste tu         │
│     contraseña? ─►      │ ← TextButton, color tertiary (violeta)
│                         │
│   ┌─────────────────┐   │
│   │ Iniciar Sesión   │   │ ← FilledButton pill, full-width
│   └─────────────────┘   │
│                         │
│  ¿No tienes cuenta?     │
│     Regístrate  ─►      │ ← AnnotatedString, "Regístrate" en color primary
│                         │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToFeed: () -> Unit,
    // viewModel: AuthViewModel = hiltViewModel()  // Fase 2
) {
    // Estado local (Fase 2: mover a ViewModel)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Título "¡Hola de nuevo!"
        //    Style: headlineMedium + Bold
        //    Alignment: Start
        
        Spacer(Modifier.height(16.dp))
        
        // 2. Ilustración — Image(painterResource(R.drawable.login_pet))
        //    size: 150.dp, contentScale Fit
        //    Fallback Fase 2: Icon(Icons.Filled.Pets, tint=primary, size=80.dp)
        
        Spacer(Modifier.height(32.dp))
        
        // 3. Campo email
        //    OutlinedTextField(
        //        value = email,
        //        onValueChange = { email = it },
        //        label = { Text(stringResource(R.string.email_hint)) },
        //        leadingIcon = { Icon(Icons.Outlined.Email) },
        //        keyboardOptions = KeyboardOptions(keyboardType = Email),
        //        shape = RoundedCornerShape(16.dp),
        //        modifier = Modifier.fillMaxWidth()
        //    )
        
        Spacer(Modifier.height(12.dp))
        
        // 4. Campo contraseña (con toggle visibilidad)
        //    OutlinedTextField(
        //        ...
        //        trailingIcon = {
        //            IconButton(onClick = { passwordVisible = !passwordVisible }) {
        //                Icon(if (passwordVisible) Visibility else VisibilityOff)
        //            }
        //        },
        //        visualTransformation = if (passwordVisible) None else PasswordVisualTransformation()
        //    )
        
        Spacer(Modifier.height(4.dp))
        
        // 5. "¿Olvidaste tu contraseña?"
        //    TextButton alineado a End
        //    Color: MaterialTheme.colorScheme.tertiary
        //    onClick → onNavigateToForgotPassword
        
        Spacer(Modifier.height(24.dp))
        
        // 6. Botón "Iniciar Sesión"
        //    Button(shape = RoundedCornerShape(50))
        //    Modifier.fillMaxWidth().height(56.dp)
        //    Validación: email no vacío, password no vacío
        //    onClick → onNavigateToFeed (Fase 2: viewModel.login())
        
        Spacer(Modifier.height(16.dp))
        
        // 7. "¿No tienes cuenta? Regístrate"
        //    Usar AnnotatedString con SpanStyle(color = primary) en "Regístrate"
        //    ClickableText → onNavigateToRegister
    }
}
```

### Validaciones (Fase 2)
- Email: formato válido (`Patterns.EMAIL_ADDRESS`)
- Contraseña: mínimo 6 caracteres
- Mostrar error debajo del campo con `isError = true` y `supportingText`

### Navegación
- "Iniciar Sesión" → `Screen.Feed` (usuario) o `Screen.ModeratorPanel` (moderador)
- "¿Olvidaste tu contraseña?" → `Screen.ForgotPassword`
- "Regístrate" → `Screen.Register`

---

## Pantalla 3 — Registro (Sign Up)

**Archivo:** `features/auth/presentation/RegisterScreen.kt`  
**Ruta:** `Screen.Register`  
**Figma:** Pantalla 3 (§3.1)

### Diseño visual

```
┌─────────────────────────┐
│ ← (Back)                │ ← IconButton(Icons.AutoMirrored.Filled.ArrowBack)
│                         │
│   Crea tu cuenta        │ ← headlineMedium, Bold
│                         │
│   ┌─────────────────┐   │
│   │ 👤 Nombre       │   │ ← OutlinedTextField
│   └─────────────────┘   │
│   ┌─────────────────┐   │
│   │ 📧 Correo       │   │ ← OutlinedTextField
│   └─────────────────┘   │
│   ┌─────────────────┐   │
│   │ 🔒 Contraseña   │   │ ← OutlinedTextField
│   └─────────────────┘   │
│                         │
│   [████████░░] Fuerte   │ ← LinearProgressIndicator + Text
│                         │
│   ☑ Acepto los términos │ ← Checkbox + Text
│                         │
│   ┌─────────────────┐   │
│   │   Registrarme    │   │ ← FilledButton pill, primary, con sombra
│   └─────────────────┘   │
│                         │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            // TopAppBar con navigationIcon = IconButton(ArrowBack)
            // title = {} (vacío — el título va en el body)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // 1. Título "Crea tu cuenta"
            
            Spacer(Modifier.height(24.dp))
            
            // 2. Campo "Nombre completo"
            //    leadingIcon = Icons.Outlined.Person
            
            Spacer(Modifier.height(12.dp))
            
            // 3. Campo "Correo electrónico"
            //    leadingIcon = Icons.Outlined.Email
            //    keyboardOptions = KeyboardOptions(keyboardType = Email)
            
            Spacer(Modifier.height(12.dp))
            
            // 4. Campo "Contraseña"
            //    leadingIcon = Icons.Outlined.Lock
            //    trailingIcon = toggle visibilidad
            
            Spacer(Modifier.height(8.dp))
            
            // 5. Indicador de fortaleza de contraseña
            //    LinearProgressIndicator(progress = calculateStrength(password))
            //    Text al lado: "Débil" / "Media" / "Fuerte"
            //    Color: Red→Yellow→Green según nivel
            //    Función calculateStrength():
            //       < 6 chars → 0.2f (Débil)
            //       tiene mayúscula + número → 0.6f (Media)
            //       + símbolo especial → 1.0f (Fuerte)
            
            Spacer(Modifier.height(16.dp))
            
            // 6. Checkbox términos y condiciones
            //    Row {
            //        Checkbox(checked = termsAccepted, onCheckedChange = ...)
            //        Text("Acepto los términos y condiciones",
            //             style = bodyMedium)
            //    }
            
            Spacer(Modifier.height(24.dp))
            
            // 7. Botón "Registrarme"
            //    enabled = name.isNotBlank() && email.isNotBlank()
            //              && password.length >= 6 && termsAccepted
            //    Sombra: elevation = 4.dp (ElevatedButton o Button + shadow)
            //    shape = RoundedCornerShape(50)
        }
    }
}
```

### Validaciones
- Nombre: no vacío, mínimo 2 caracteres
- Email: formato válido
- Contraseña: mínimo 6 caracteres
- Checkbox de términos: obligatorio activado

### Navegación
- Flecha ← → `popBackStack()` (volver a Login)
- "Registrarme" con éxito → `Screen.Feed`

---

## Pantalla 4 — Recuperación de contraseña

**Archivo:** `features/auth/presentation/ForgotPasswordScreen.kt`  
**Ruta:** `Screen.ForgotPassword`  
**Figma:** Pantalla 4 (§3.1)

### Diseño visual

```
┌─────────────────────────┐
│ ← (Back)                │
│                         │
│   ¿Olvidaste tu         │ ← headlineMedium, Bold
│   contraseña?           │
│                         │
│   No te preocupes.      │
│   Ingresa tu correo     │ ← bodyMedium, color onSurfaceVariant
│   y te enviaremos un    │
│   enlace para recuperar │
│   el acceso...          │
│                         │
│   [Ilustración llave]   │ ← Imagen decorativa (opcional)
│                         │
│   ┌─────────────────┐   │
│   │ 📧 Correo       │   │ ← OutlinedTextField
│   └─────────────────┘   │
│                         │
│   ┌─────────────────┐   │
│   │  Enviar enlace   │   │ ← FilledButton pill, primary, full-width
│   └─────────────────┘   │
│                         │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            // TopAppBar con ArrowBack → onNavigateBack
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // 1. Título "¿Olvidaste tu contraseña?"
            //    headlineMedium, Bold
            
            Spacer(Modifier.height(12.dp))
            
            // 2. Texto descriptivo empático
            //    stringResource(R.string.forgot_password_desc)
            //    bodyMedium, color onSurfaceVariant
            
            Spacer(Modifier.height(32.dp))
            
            // 3. (Opcional) Ilustración decorativa — llave + mascota
            //    Fallback: Icon(Icons.Outlined.Lock, size=64.dp, tint=primary)
            
            Spacer(Modifier.height(32.dp))
            
            // 4. Campo email — igual que en Login
            //    leadingIcon = Icons.Outlined.Email
            
            Spacer(Modifier.height(24.dp))
            
            // 5. Botón "Enviar enlace" — pill, full-width
            //    enabled = email válido
            //    Fase 2: mostrar Snackbar("Enlace enviado — revisa tu correo")
            //    Fase 3: FirebaseAuth.sendPasswordResetEmail(email)
            
            // 6. Si emailSent → mostrar texto de confirmación verde
            if (emailSent) {
                // Text("Enlace enviado — revisa tu bandeja de entrada")
                // color = primary, icon = CheckCircle
            }
        }
    }
}
```

### Navegación
- Flecha ← → `popBackStack()` (volver a Login)
- "Enviar enlace" → Queda en la misma pantalla mostrando confirmación

---

## Pantalla 5 — Feed principal (Vista Lista)

**Archivo:** `features/feed/presentation/FeedScreen.kt`  
**Ruta:** `Screen.Feed`  
**Figma:** Pantalla 5 (§3.2)

### Diseño visual

```
┌─────────────────────────┐
│ PetHelp          🔔 [👤]│ ← TopAppBar: título + notificaciones + avatar
├─────────────────────────┤
│ [🐾 Adopción] [🔍 Perd.]│ ← LazyRow de FilterChips horizontales
│ [📍 Encon.] [🏠 Hogar] │    deslizable, con scroll horizontal
│ [💉 Vet.]               │
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │ [FOTO MASCOTA      ]│ │ ← ElevatedCard, imagen arriba
│ │         📍 a 2 km   │ │ ← Badge glassmorphism esquina
│ │                      │ │
│ │ Max - Golden Retri.. │ │ ← titleMedium, Bold
│ │ ♂ Macho · Mediano   │ │ ← bodySmall + tags
│ │                 ❤ 12 │ │ ← IconButton corazón + conteo
│ └─────────────────────┘ │
│                          │
│ ┌─────────────────────┐ │ ← Otra tarjeta...
│ │ ...                  │ │
│ └─────────────────────┘ │
│                          │
├─────────────────────────┤
│ 🏠  🗺  [➕]  🔔  👤   │ ← NavigationBar M3 (5 ítems)
│ Feed Map  New  Noti Prof│    Ítem central más grande (FAB integrado)
└─────────────────────────┘
```

### Estructura Compose (compleja — dividir en sub-composables)

```kotlin
@Composable
fun FeedScreen(
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMapView: () -> Unit,
    // viewModel: FeedViewModel = hiltViewModel()
) {
    // Estado Fase 2: lista mock en ViewModel
    var selectedCategory by remember { mutableStateOf<PostCategory?>(null) }
    var isMapView by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            // CenterAlignedTopAppBar o TopAppBar
            // title = { Text("PetHelp", style = titleLarge, fontWeight = Bold) }
            // actions = {
            //     IconButton → onNavigateToNotifications
            //         Icon(Icons.Outlined.Notifications)
            //         Si hay notifs no leídas → BadgedBox con Badge(){ Text("3") }
            //     IconButton → onNavigateToProfile
            //         Avatar circular pequeño (32.dp) — AsyncImage o placeholder
            // }
        },
        bottomBar = {
            // ** PetHelpBottomBar — componente reutilizable **
            // NavigationBar con 5 destinos:
            //   NavigationBarItem(icon = Home, label = "Feed", selected = true)
            //   NavigationBarItem(icon = Map, label = "Mapa")
            //   NavigationBarItem(icon = Add, label = "Nuevo")  ← estilo especial
            //   NavigationBarItem(icon = Notifications, label = "Alertas")
            //   NavigationBarItem(icon = Person, label = "Perfil")
            //
            // El ítem central "Nuevo" debe verse más grande:
            //   FloatingActionButton dentro del NavigationBar, o
            //   NavigationBarItem con icono Add más grande (28.dp)
        },
        // NO usar floatingActionButton — el FAB está dentro del bottomBar
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // ─── FILA DE CHIPS ────────────────────────────────
            // LazyRow(
            //     contentPadding = PaddingValues(horizontal = 16.dp),
            //     horizontalArrangement = Arrangement.spacedBy(8.dp)
            // ) {
            //     item { CategoryChip("🐾 Adopción", selected, onClick) }
            //     item { CategoryChip("🔍 Perdidos", ...) }
            //     item { CategoryChip("📍 Encontrados", ...) }
            //     item { CategoryChip("🏠 Hogar temporal", ...) }
            //     item { CategoryChip("💉 Veterinaria", ...) }
            // }
            //
            // Usar FilterChip de M3:
            // FilterChip(
            //     selected = isSelected,
            //     label = { Text(text) },
            //     leadingIcon = if (isSelected) { { Icon(Done) } } else null,
            //     shape = RoundedCornerShape(50)
            // )
            
            Spacer(Modifier.height(8.dp))
            
            // ─── LISTA DE TARJETAS ───────────────────────────
            // LazyColumn(
            //     contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            //     verticalArrangement = Arrangement.spacedBy(12.dp)
            // ) {
            //     items(filteredPosts) { post ->
            //         PostCard(
            //             post = post,
            //             onClick = { onNavigateToPostDetail(post.id) }
            //         )
            //     }
            // }
        }
    }
}
```

### Componente PostCard (crear en `core/ui/components/PostCard.kt`)

```kotlin
@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onVoteClick: () -> Unit = {}
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Imagen superior — Box para superponer badge de distancia
        Box {
            // AsyncImage(url) o Image(placeholder)
            // Modifier.fillMaxWidth().height(180.dp)
            // clip(RoundedCornerShape(topStart = 16, topEnd = 16))
            
            // Badge distancia — esquina superior derecha
            // Surface(
            //     color = Color.Black.copy(alpha = 0.5f),
            //     shape = RoundedCornerShape(12.dp),
            //     modifier = Modifier.align(TopEnd).padding(8.dp)
            // ) {
            //     Text("📍 a 2 km", color = White, style = labelSmall)
            // }
        }
        
        // 2. Contenido texto
        Column(Modifier.padding(12.dp)) {
            // Título: titleMedium, Bold
            // Text(post.title, style = titleMedium, fontWeight = Bold, maxLines = 1)
            
            Spacer(Modifier.height(4.dp))
            
            // Tags: Row de AssistChips o Text con iconos
            // Row(horizontalArrangement = spacedBy(8.dp)) {
            //     Text("♂ ${post.animalType}", style = bodySmall)
            //     Text("· ${post.size}", style = bodySmall, color = onSurfaceVariant)
            // }
            
            Spacer(Modifier.height(8.dp))
            
            // Botón de votos — alineado a End
            // Row(Modifier.fillMaxWidth(), horizontalArrangement = End) {
            //     IconButton(onClick = onVoteClick) {
            //         Icon(Icons.Outlined.FavoriteBorder / Filled.Favorite)
            //     }
            //     Text("${post.votes}", style = labelLarge)
            // }
        }
    }
}
```

### Datos necesarios (ViewModel)
```kotlin
data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val selectedCategory: PostCategory? = null,
    val isMapView: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Navegación
- Clic en tarjeta → `Screen.PostDetail.createRoute(postId)`
- Bottom bar "Nuevo" → `Screen.CreatePost`
- Bottom bar "Alertas" → `Screen.Notifications`
- Bottom bar "Perfil" → `Screen.Profile`
- Bottom bar "Mapa" → cambiar `isMapView = true` (misma pantalla)
- Campana top → `Screen.Notifications`
- Avatar top → `Screen.Profile`

---

## Pantalla 6 — Feed principal (Vista Mapa)

**Archivo:** `features/feed/presentation/FeedScreen.kt` (mismo archivo, condicional `isMapView`)  
**Ruta:** `Screen.Feed` (mismo destino, estado diferente)  
**Figma:** Pantalla 6 (§3.2)

### Diseño visual

```
┌─────────────────────────┐
│ ┌─────────────────────┐ │ ← SearchBar flotante con glassmorphism
│ │ 🔍 Buscar mascota  │ │    Surface(tonalElevation + alpha)
│ └─────────────────────┘ │
│ [🐾 Adop.] [🔍 Perd.]  │ ← Chips flotando sobre el mapa
│                          │
│    🟢      🟢            │ ← Marcadores personalizados (foto circular)
│         🟢               │    o íconos de pata/mascota
│              🟢          │
│                          │
│ ┌─────────────────────┐ │ ← BottomSheet "peek" (asomándose)
│ │ [📷] Luna - Perdida │ │    Tarjeta horizontal de resumen
│ │      en Av. San...  │ │
│ │          [Ver más →]│ │ ← TextButton
│ └─────────────────────┘ │
├─────────────────────────┤
│ 🏠  🗺  [➕]  🔔  👤   │ ← Mismo BottomBar
└─────────────────────────┘
```

### Estructura Compose

```kotlin
// Dentro de FeedScreen, cuando isMapView == true:
// Reemplazar el LazyColumn con:

Box(modifier = Modifier.fillMaxSize()) {
    // 1. GoogleMap — ocupa todo el fondo
    //    GoogleMap(
    //        modifier = Modifier.fillMaxSize(),
    //        cameraPositionState = cameraPositionState,
    //        properties = MapProperties(isMyLocationEnabled = true)
    //    ) {
    //        posts.forEach { post ->
    //            Marker(
    //                state = MarkerState(position = LatLng(post.latitude, post.longitude)),
    //                title = post.title,
    //                onClick = { selectedPost = post; true }
    //            )
    //        }
    //    }
    //
    // Fase 2: Usar un Box con Text("Mapa — Disponible en Fase 3")
    // Fase 3: GoogleMap real con marcadores
    
    // 2. SearchBar flotante arriba
    //    Surface(
    //        modifier = Modifier.align(TopCenter).padding(16.dp),
    //        shape = RoundedCornerShape(28.dp),
    //        tonalElevation = 4.dp
    //    ) {
    //        OutlinedTextField(placeholder = "Buscar mascota...")
    //    }
    
    // 3. Chips flotantes debajo de la SearchBar
    //    LazyRow debajo del Surface, con padding top
    
    // 4. BottomSheet peek — tarjeta del post seleccionado
    //    AnimatedVisibility(visible = selectedPost != null)
    //    ModalBottomSheet o Surface fijo en bottom
    //    Row: Image(64x64) + Column(título + dirección) + TextButton("Ver más")
}
```

### Nota Fase 2
En Fase 2 no hay Google Maps (necesita API Key). Mostrar un placeholder visual:
```kotlin
Box(Modifier.fillMaxSize().background(Color(0xFFE8F5E9)), contentAlignment = Center) {
    Column(horizontalAlignment = CenterHorizontally) {
        Icon(Icons.Filled.Map, modifier = Modifier.size(80.dp), tint = primary)
        Spacer(Modifier.height(8.dp))
        Text("Vista de mapa disponible en Fase 3", style = bodyLarge)
    }
}
```

---

## Pantalla 7 — Detalle de publicación

**Archivo:** `features/post/presentation/PostDetailScreen.kt`  
**Ruta:** `Screen.PostDetail`  
**Figma:** Pantalla 7 (§3.3)

### Diseño visual

```
┌─────────────────────────┐
│ ←                       │ ← IconButton flotante translúcido
│                          │
│ [       HERO IMAGE      ]│ ← ~40% de la pantalla
│ [       del animal      ]│
│                          │
│╭────────────────────────╮│ ← Panel con esquinas superiores redondeadas
││ Kira          Por JuanP││ ← Row: título + avatar + nombre autor
││                        ││
││ ┌────┐┌────┐┌────┐┌──┐││ ← Grid 2x2 de chips de características
││ │Hembr││Schna││Media││Vac│││    Surface con color primaryContainer
││ └────┘└────┘└────┘└──┘││
││                        ││
││ Descripción del animal ││ ← bodyMedium, varias líneas
││ ...                    ││
││                        ││
││ ┌─────────────────────┐││ ← Mini-mapa rectangular
││ │    [Mapa con pin]   │││    GoogleMap size fijo o placeholder
││ └─────────────────────┘││
││                        ││
││ Comentarios            ││ ← titleMedium "Comentarios"
││ ┌──────────────────┐   ││
││ │ [👤] Escribe...  │   ││ ← OutlinedTextField
││ └──────────────────┘   ││
││ [👤] Comentario 1...   ││ ← LazyColumn de comentarios
││ [👤] Comentario 2...   ││
│╰────────────────────────╯│
│                          │
│ ┌ Votos: 45 ──────────┐ │ ← Sticky bottom bar con elevación
│ │ 🐾 Solicitar Adopción│ │    FilledButton GRANDE coral/primary
│ └──────────────────────┘ │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    // viewModel: PostViewModel = hiltViewModel()
) {
    // Fase 2: obtener post de lista en memoria por ID
    
    Scaffold(
        // Sticky bottom bar
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text("Votos: ${post.votes}", style = titleMedium)
                    Spacer(Modifier.weight(1f))
                    // Button("🐾 Solicitar Adopción")
                    //   shape = RoundedCornerShape(50)
                    //   colors = ButtonDefaults.buttonColors(containerColor = secondary)
                    //   Solo visible si categoría == ADOPTION
                    //   Si otra categoría → "❤ Me interesa"
                }
            }
        }
    ) { padding ->
        // LazyColumn para scroll de todo el contenido
        LazyColumn(modifier = Modifier.padding(padding)) {
            // ─── 1. Hero Image ─────────────────────────
            item {
                Box {
                    // AsyncImage(model = post.imageUrls.first())
                    // Modifier.fillMaxWidth().height(300.dp)
                    
                    // Botón back flotante
                    // IconButton(
                    //     onClick = onNavigateBack,
                    //     modifier = Modifier.padding(16.dp)
                    //         .background(Color.Black.copy(0.3f), CircleShape)
                    // ) { Icon(ArrowBack, tint = White) }
                }
            }
            
            // ─── 2. Panel de contenido ──────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.offset(y = (-24).dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        // a) Título + Autor
                        //    Row: Text(post.title, headlineMedium, Bold)
                        //         Spacer(weight)
                        //         Avatar(32.dp) + Text(authorName, bodySmall)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // b) Chips de características — grid 2x2
                        //    FlowRow(horizontalArrangement = spacedBy(8.dp)) {
                        //        CharacteristicChip("Hembra", Icons.Female)
                        //        CharacteristicChip(post.breed, Icons.Pets)
                        //        CharacteristicChip(post.size.name, Icons.Straighten)
                        //        CharacteristicChip(
                        //            if(post.vaccinated) "Vacunada" else "Sin vacunas",
                        //            Icons.HealthAndSafety
                        //        )
                        //    }
                        //
                        //    CharacteristicChip = Surface(
                        //        color = primaryContainer,
                        //        shape = RoundedCornerShape(12.dp)
                        //    ) { Row(Modifier.padding(h=12, v=8)) { Icon + Text } }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // c) Descripción
                        //    Text(post.description, bodyMedium)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // d) Mini-mapa
                        //    Fase 2: placeholder Box con pin icon
                        //    Fase 3: GoogleMap(Modifier.fillMaxWidth().height(150.dp))
                        //            con un solo Marker en post.latitude, post.longitude
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // e) Sección de comentarios
                        //    Text("Comentarios", titleMedium)
                        //    OutlinedTextField para nuevo comentario
                        //    Lista de comentarios existentes
                    }
                }
            }
        }
    }
}
```

---

## Pantalla 8 — Crear publicación

**Archivo:** `features/post/presentation/CreatePostScreen.kt`  
**Ruta:** `Screen.CreatePost`  
**Figma:** Pantalla 8 (§3.4)

### Diseño visual

```
┌─────────────────────────┐
│ ← Ayuda a un peludo     │ ← TopAppBar con back + título
├─────────────────────────┤
│ ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐ │
│ │   📷               │ │ ← Recuadro dashed border
│ │  Toca para agregar  │ │    Clickeable → abrir galería/cámara
│ │   hasta 5 fotos     │ │
│ │  [📸1] [📸2] [+]   │ │ ← Fotos añadidas + botón "+"
│ └─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘ │
│                          │
│ ┌──────────────────────┐ │
│ │ Título de publicación│ │ ← OutlinedTextField
│ └──────────────────────┘ │
│ ┌──────────────────────┐ │
│ │ Descripción de la    │ │ ← OutlinedTextField multiline
│ │ situación            │ │    minLines = 4
│ └──────────────────────┘ │
│                          │
│ ✨ Categoría sugerida    │ ← IA label con sparkles (violeta)
│ ┌──────────────────────┐ │
│ │ Adopción          ▼  │ │ ← ExposedDropdownMenuBox
│ └──────────────────────┘ │
│                          │
│ Tipo de animal:          │
│ [🐕 Perro] [🐈 Gato]   │ ← ChoiceChips (SingleSelectRow)
│ [🐾 Otro]               │
│                          │
│ Tamaño:                  │
│ [S] [M] [L]             │ ← ChoiceChips
│                          │
│ ☑ Vacunado              │ ← Checkbox
│                          │
│ ┌──────────────────────┐ │
│ │ Siguiente: Ubicación │ │ ← FilledButton pill primary
│ └──────────────────────┘ │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    // viewModel: PostViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PostCategory?>(null) }
    var animalType by remember { mutableStateOf("") }        // Perro/Gato/Otro
    var selectedSize by remember { mutableStateOf<AnimalSize?>(null) }
    var isVaccinated by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Launcher para picker de imágenes
    // val imagePickerLauncher = rememberLauncherForActivityResult(
    //     contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    // ) { uris -> selectedImages = uris }
    
    Scaffold(
        topBar = {
            // TopAppBar: ArrowBack + "Ayuda a un peludo"
        },
        bottomBar = {
            // Botón "Siguiente: Ubicación" fijo abajo
            // Fase 2 puede ser "Publicar" directamente (sin mapa)
            // Fase 3: navegar a pantalla de selección de ubicación en mapa
            Surface(Modifier.fillMaxWidth()) {
                // Button(modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            
            // ─── Selector de imágenes ──────────────────
            // Surface con border dashed (no existe nativo en Compose,
            // usar: Modifier.border(1.dp, color, shape) con estilo custom
            // o simplemente un OutlinedCard con borde punteado simulado)
            //
            // OutlinedCard(
            //     modifier = Modifier.fillMaxWidth().height(160.dp),
            //     onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ImageOnly)) }
            // ) {
            //     if (selectedImages.isEmpty()) {
            //         // Icon(CameraAlt, size=48) + Text("Toca para agregar hasta 5 fotos")
            //     } else {
            //         // LazyRow de imágenes seleccionadas + botón "+"
            //         // Cada imagen: Box con Image(uri) + IconButton(Close) para eliminar
            //     }
            // }
            
            Spacer(Modifier.height(16.dp))
            
            // ─── Campo título ──────────────────────────
            // OutlinedTextField(label = "Título de publicación")
            
            Spacer(Modifier.height(12.dp))
            
            // ─── Campo descripción ─────────────────────
            // OutlinedTextField(label = "Descripción de la situación")
            // minLines = 4, maxLines = 8
            
            Spacer(Modifier.height(16.dp))
            
            // ─── Categoría con IA ──────────────────────
            // Row con icono sparkles violeta + Text("Categoría sugerida por IA")
            // ExposedDropdownMenuBox {
            //     TextField(readOnly = true, value = selectedCategory?.name)
            //     ExposedDropdownMenu {
            //         PostCategory.values().forEach { cat ->
            //             DropdownMenuItem(text = { Text(cat.name) }) {
            //                 selectedCategory = cat
            //             }
            //         }
            //     }
            // }
            //
            // Fase 3: al cambiar título o descripción,
            //          llamar IA para sugerir categoría automáticamente
            
            Spacer(Modifier.height(16.dp))
            
            // ─── Tipo de animal (ChoiceChips) ──────────
            // Text("Tipo de animal", titleSmall)
            // FlowRow {
            //     listOf("🐕 Perro", "🐈 Gato", "🐾 Otro").forEach { type ->
            //         FilterChip(selected = animalType == type, ...)
            //     }
            // }
            
            Spacer(Modifier.height(12.dp))
            
            // ─── Tamaño (ChoiceChips) ──────────────────
            // Text("Tamaño", titleSmall)
            // FlowRow {
            //     AnimalSize.values().forEach { size ->
            //         FilterChip(selected = selectedSize == size, ...)
            //     }
            // }
            
            Spacer(Modifier.height(12.dp))
            
            // ─── Checkbox vacunado ─────────────────────
            // Row { Checkbox + Text("Vacunado") }
            
            Spacer(Modifier.height(80.dp)) // espacio para el bottom button
        }
    }
}
```

---

## Pantalla 9 — Panel de moderación

**Archivo:** `features/moderation/presentation/ModeratorPanelScreen.kt`  
**Ruta:** `Screen.ModeratorPanel`  
**Figma:** Pantalla 9 (§3.5)

### Diseño visual

```
┌─────────────────────────┐
│ Bandeja - Pendientes     │ ← TopAppBar
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │[📸]  Buscamos hogar │ │ ← ListItem: thumbnail + título + categoría
│ │       para Max      │ │
│ │      - Adopción -   │ │
│ │ ┌─────────────────┐ │ │
│ │ │ ✨ Resumen IA:  │ │ │ ← tag violeta con bordes redondeados
│ │ │ Posible adopción│ │ │    color: tertiary (violeta del Figma)
│ │ │ real. Imagen    │ │ │
│ │ │ coincide...     │ │ │
│ │ └─────────────────┘ │ │
│ │       [✅ Aprobar]   │ │ ← FilledTonalButton verde
│ │       [❌ Rechazar]  │ │ ← FilledTonalButton error
│ └─────────────────────┘ │
│                          │
│ ┌─────────────────────┐ │ ← Siguiente tarjeta...
│ │  ...                 │ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun ModeratorPanelScreen(
    onNavigateToDetail: (String) -> Unit
    // viewModel: ModerationViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            // TopAppBar(title = "Bandeja de entrada — Pendientes")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // items(pendingPosts) { post ->
            //     ModerationCard(
            //         post = post,
            //         onApprove = { viewModel.approve(post.id) },
            //         onReject = { showRejectDialog = true; selectedPostId = post.id },
            //         onCardClick = { onNavigateToDetail(post.id) }
            //     )
            // }
        }
    }
}

@Composable
fun ModerationCard(
    post: Post,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCardClick: () -> Unit
) {
    ElevatedCard(onClick = onCardClick, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(12.dp)) {
            // 1. Thumbnail cuadrado (64x64)
            //    AsyncImage o placeholder, clip RoundedCornerShape(8.dp)
            
            Spacer(Modifier.width(12.dp))
            
            Column(Modifier.weight(1f)) {
                // 2. Título + categoría
                //    Text(post.title, titleSmall, Bold, maxLines=2)
                //    Text(post.category.name, bodySmall, color=onSurfaceVariant)
                
                Spacer(Modifier.height(8.dp))
                
                // 3. Tag resumen IA — badge especial violeta
                //    Surface(
                //        color = MaterialTheme.colorScheme.tertiaryContainer,
                //        shape = RoundedCornerShape(8.dp)
                //    ) {
                //        Row(Modifier.padding(8.dp)) {
                //            Icon(Icons.AutoMirrored.Filled.Stars)  // sparkles
                //            Text("Resumen IA: ...", bodySmall)
                //        }
                //    }
                //    Fase 2: mostrar texto placeholder
                //    Fase 3: resultado real del LLM
                
                Spacer(Modifier.height(8.dp))
                
                // 4. Botones Aprobar / Rechazar
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // FilledTonalButton(onClick = onApprove,
                    //     colors = ButtonDefaults.filledTonalButtonColors(
                    //         containerColor = Color(0xFFC8E6C9)),
                    //     shape = RoundedCornerShape(50)
                    // ) { Icon(Check) + Text("Aprobar") }
                    
                    // FilledTonalButton(onClick = onReject,
                    //     colors = ButtonDefaults.filledTonalButtonColors(
                    //         containerColor = errorContainer),
                    //     shape = RoundedCornerShape(50)
                    // ) { Icon(Close) + Text("Rechazar") }
                }
            }
        }
    }
}
```

---

## Pantalla 10 — Diálogo rechazar publicación

**Archivo:** `features/moderation/presentation/RejectDialog.kt` (nuevo)  
**Tipo:** `ModalBottomSheet` (M3)  
**Figma:** Pantalla 10 (§3.5)

### Estructura Compose

```kotlin
@Composable
fun RejectBottomSheet(
    onDismiss: () -> Unit,
    onReject: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            // 1. Título "Motivo del rechazo"
            //    Text(style = titleLarge, fontWeight = Bold)
            
            Spacer(Modifier.height(16.dp))
            
            // 2. Campo de texto grande
            //    OutlinedTextField(
            //        value = reason,
            //        placeholder = "Explica respetuosamente al usuario..."
            //        minLines = 4,
            //        shape = RoundedCornerShape(16.dp),
            //        modifier = Modifier.fillMaxWidth()
            //    )
            
            Spacer(Modifier.height(8.dp))
            
            // 3. Texto auxiliar
            //    Text("Este motivo será enviado al autor.",
            //         style = bodySmall, color = onSurfaceVariant)
            
            Spacer(Modifier.height(24.dp))
            
            // 4. Botones Cancelar + Rechazar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // TextButton("Cancelar", onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                // Button("Rechazar",
                //     onClick = { onReject(reason) },
                //     enabled = reason.isNotBlank(),
                //     colors = ButtonDefaults.buttonColors(containerColor = error),
                //     shape = RoundedCornerShape(50)
                // )
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
```

---

## Pantalla 11 — Notificaciones

**Archivo:** `features/notifications/presentation/NotificationsScreen.kt`  
**Ruta:** `Screen.Notifications`  
**Figma:** Pantalla 11 (§3.6)

### Diseño visual

```
┌─────────────────────────┐
│ ← Notificaciones        │ ← TopAppBar con back
├─────────────────────────┤
│ Nuevas                   │ ← label/titleSmall como separador
│ ┌─────────────────────┐ │
│ │[🔔] Tu publicación  │▒│ ← fondo tintado primaryContainer (no leída)
│ │     ha sido          │▒│    "▒" = sombreado sutil
│ │     verificada  2h   │▒│    hora a la derecha
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │[👤] Santi comentó   │▒│
│ │     en Max - Golden  │▒│
│ │     Retriever   45m  │▒│
│ └─────────────────────┘ │
│                          │
│ Ayer                     │ ← separador
│ ┌─────────────────────┐ │
│ │[🏆] ¡Has alcanzado  │ │ ← fondo normal (ya leída)
│ │     el nivel         │ │
│ │     Protector!  1d   │ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit
    // viewModel: NotificationsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { /* TopAppBar ArrowBack + "Notificaciones" */ }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Agrupar por fecha: "Nuevas", "Ayer", "Esta semana"
            // stickyHeader { Text("Nuevas", titleSmall, Modifier.padding(h=16, v=8)) }
            // items(newNotifications) { notif -> NotificationItem(notif) }
            // stickyHeader { Text("Ayer", ...) }
            // items(yesterdayNotifications) { notif -> NotificationItem(notif) }
        }
    }
}

@Composable
fun NotificationItem(notification: PetNotification) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!notification.read)
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                else Modifier
            ),
        leadingContent = {
            // Avatar/icono circular según tipo:
            //   NEW_POST_NEARBY → Icon(LocationOn)
            //   NEW_COMMENT → avatar del comentarista
            //   POST_APPROVED → Icon(CheckCircle, tint = primary)
            //   POST_REJECTED → Icon(Cancel, tint = error)
            //   NEW_BADGE → Icon(EmojiEvents, tint = secondary)
            //   LEVEL_UP → Icon(TrendingUp, tint = secondary)
        },
        headlineContent = {
            // Text(notification.title, bodyMedium, maxLines=2)
        },
        trailingContent = {
            // Text(timeAgo(notification.createdAt), labelSmall, color = onSurfaceVariant)
        }
    )
}
```

---

## Pantalla 12 — Perfil / Dashboard de gamificación

**Archivo:** `features/reputation/presentation/ReputationScreen.kt`  
**Ruta:** `Screen.Reputation`  
**Figma:** Pantalla 12 (§3.6)

### Diseño visual

```
┌─────────────────────────┐
│ ← Mi Reputación         │ ← TopAppBar
├─────────────────────────┤
│                          │
│        [AVATAR]          │ ← Avatar grande (96.dp) con aro de progreso
│     ┌──gradiente───┐    │    CircularProgressIndicator alrededor
│     │     👤        │    │    color = secondary (coral/dorado)
│     └──────────────┘    │
│     Juan Sebastián       │ ← titleLarge, Bold, centrado
│     👑 Héroe de las      │ ← Badge: Surface(secondaryContainer)
│        Mascotas          │    con ícono corona y texto del nivel
│                          │
│  ┌────────────────────┐  │
│  │   🔥 1,450 puntos  │  │ ← ElevatedCard grande centrado
│  │     ⭐ ⭐ ⭐        │  │    displaySmall + decoración
│  └────────────────────┘  │
│                          │
│  ┌─────────┐┌─────────┐ │ ← Row de 2 tarjetas a 2 columnas
│  │ Activas ││Solicitud.│ │    ElevatedCard + headlineSmall (número)
│  │    4    ││   12     │ │    + bodySmall (label)
│  └─────────┘└─────────┘ │
│                          │
│  Tus Insignias           │ ← titleMedium
│  ┌───┐ ┌───┐ ┌───┐      │ ← LazyRow horizontal de badges
│  │🛡️ │ │🏅 │ │⭐ │      │    Cada badge: Surface circular o Card
│  │Nov.│ │10v│ │Pop│      │    con ícono + texto debajo
│  └───┘ └───┘ └───┘      │
│                          │
└─────────────────────────┘
```

### Estructura Compose

```kotlin
@Composable
fun ReputationScreen(
    onNavigateBack: () -> Unit
    // viewModel: ReputationViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { /* TopAppBar ArrowBack + "Mi Reputación" */ }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            // ─── 1. Avatar con aro de progreso ────────
            Box(contentAlignment = Alignment.Center) {
                // CircularProgressIndicator(
                //     progress = userPoints / nextLevelThreshold,
                //     modifier = Modifier.size(108.dp),
                //     trackColor = surfaceVariant,
                //     color = secondary,  // coral/dorado
                //     strokeWidth = 4.dp
                // )
                // AsyncImage o placeholder avatar (96.dp) circular
            }
            
            Spacer(Modifier.height(12.dp))
            
            // ─── 2. Nombre ────────────────────────────
            // Text(userName, titleLarge, Bold)
            
            Spacer(Modifier.height(4.dp))
            
            // ─── 3. Badge de nivel ────────────────────
            // Surface(
            //     color = secondaryContainer,
            //     shape = RoundedCornerShape(50)
            // ) {
            //     Row(Modifier.padding(h=12,v=6)) {
            //         Text("👑", fontSize = 16.sp)
            //         Text(userLevel.displayName, labelLarge, fontWeight = SemiBold)
            //     }
            // }
            
            Spacer(Modifier.height(24.dp))
            
            // ─── 4. Tarjeta de puntos ─────────────────
            // ElevatedCard(Modifier.fillMaxWidth()) {
            //     Column(Modifier.padding(24.dp), center) {
            //         Text("🔥", fontSize = 32.sp)
            //         Text("$points puntos", headlineMedium, Bold)
            //     }
            // }
            
            Spacer(Modifier.height(16.dp))
            
            // ─── 5. Grid de estadísticas (2 columnas) ─
            // Row(horizontalArrangement = spacedBy(12.dp)) {
            //     StatCard(Modifier.weight(1f), "Activas", "4")
            //     StatCard(Modifier.weight(1f), "Solicitudes", "12")
            // }
            
            Spacer(Modifier.height(24.dp))
            
            // ─── 6. Sección "Tus Insignias" ──────────
            // Text("Tus Insignias", titleMedium, Bold, align = Start)
            // LazyRow(horizontalArrangement = spacedBy(12.dp)) {
            //     items(badges) { badge -> BadgeItem(badge) }
            // }
        }
    }
}
```

---

## Pantallas faltantes del Figma

El Figma documenta 12 pantallas. Hay **3 pantallas funcionales del proyecto que no tienen mockup** en el documento de prompts. Se deben diseñar siguiendo el mismo estilo:

| Pantalla | Archivo | Ruta | Qué debe contener |
|---|---|---|---|
| **Editar publicación** | `PostScreens.kt` → `EditPostScreen` | `Screen.EditPost` | Mismo formulario de Crear publicación, pero precargado con datos existentes + botón "Guardar cambios" |
| **Editar perfil** | `ProfileScreens.kt` → `EditProfileScreen` | `Screen.EditProfile` | TopAppBar "Editar perfil" + campos: avatar (clickeable para cambiar foto), nombre, email (no editable), botones: "Guardar", "Cerrar sesión" (TextButton), "Eliminar cuenta" (TextButton error) |
| **Estadísticas** | `StatisticsScreen.kt` | `Screen.Statistics` | TopAppBar "Mis Estadísticas" + 3 tarjetas grandes: Activas, Finalizadas, Pendientes de verificación + gráfico simple opcional (barra horizontal con %) |

---

## Mapa de archivos

| # | Pantalla Figma | Archivo Kotlin | Ruta navegación |
|---|---|---|---|
| 1 | Splash / Bienvenida | `features/auth/presentation/SplashScreen.kt` | `Screen.Splash` |
| 2 | Login | `features/auth/presentation/LoginScreen.kt` | `Screen.Login` |
| 3 | Registro | `features/auth/presentation/RegisterScreen.kt` | `Screen.Register` |
| 4 | Recuperar contraseña | `features/auth/presentation/ForgotPasswordScreen.kt` | `Screen.ForgotPassword` |
| 5 | Feed lista | `features/feed/presentation/FeedScreen.kt` | `Screen.Feed` |
| 6 | Feed mapa | `features/feed/presentation/FeedScreen.kt` (condicional) | `Screen.Feed` |
| 7 | Detalle publicación | `features/post/presentation/PostDetailScreen.kt` | `Screen.PostDetail` |
| 8 | Crear publicación | `features/post/presentation/CreatePostScreen.kt` | `Screen.CreatePost` |
| 9 | Panel moderación | `features/moderation/presentation/ModeratorPanelScreen.kt` | `Screen.ModeratorPanel` |
| 10 | Diálogo rechazar | `features/moderation/presentation/RejectDialog.kt` | Modal (no ruta) |
| 11 | Notificaciones | `features/notifications/presentation/NotificationsScreen.kt` | `Screen.Notifications` |
| 12 | Gamificación | `features/reputation/presentation/ReputationScreen.kt` | `Screen.Reputation` |
| — | Editar publicación | `features/post/presentation/EditPostScreen.kt` | `Screen.EditPost` |
| — | Editar perfil | `features/profile/presentation/EditProfileScreen.kt` | `Screen.EditProfile` |
| — | Estadísticas | `features/stats/presentation/StatisticsScreen.kt` | `Screen.Statistics` |

---

## Orden de implementación recomendado

```
PRIORIDAD 1 — Sistema de diseño (1 día)
├── Actualizar Color.kt (sincronizar con Figma)
├── Crear Shape.kt
├── Crear PetHelpButton.kt, PetHelpTextField.kt
├── Crear PetHelpBottomBar.kt
└── Crear PostCard.kt, CategoryChip.kt

PRIORIDAD 2 — Flujo de autenticación (1-2 días)
├── SplashScreen.kt
├── LoginScreen.kt
├── RegisterScreen.kt
└── ForgotPasswordScreen.kt

PRIORIDAD 3 — Feed y detalle (2-3 días)
├── FeedScreen.kt (vista lista)
├── PostCard.kt (componente)
├── PostDetailScreen.kt
└── FeedScreen.kt (vista mapa — placeholder Fase 2)

PRIORIDAD 4 — Creación y moderación (2 días)
├── CreatePostScreen.kt
├── EditPostScreen.kt (reutilizar formulario)
├── ModeratorPanelScreen.kt
└── RejectDialog.kt

PRIORIDAD 5 — Perfil y gamificación (1-2 días)
├── ProfileScreen.kt (hub de navegación)
├── EditProfileScreen.kt
├── ReputationScreen.kt
├── StatisticsScreen.kt
└── NotificationsScreen.kt
```

**Tiempo estimado total: 7-10 días** para implementar todas las vistas con datos mock.
