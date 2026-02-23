# Guía Detallada de Desarrollo de Vistas — PetHelp

> **Basado en:** Análisis del diseño Figma (PromptsFigma.md) · Épicas e Historias de Usuario · Código fuente actual del proyecto  
> **Stack:** Jetpack Compose + Material 3 + Kotlin · Clean Architecture + MVVM  
> **Fecha:** Febrero 2026

---

## Índice

1. [Resumen del estado actual del proyecto](#1-resumen-del-estado-actual-del-proyecto)
2. [Cómo usar Figma for VS Code para inspeccionar los diseños](#2-cómo-usar-figma-for-vs-code-para-inspeccionar-los-diseños)
3. [Sistema de diseño implementado (tokens de tema)](#3-sistema-de-diseño-implementado-tokens-de-tema)
4. [Componentes reutilizables a crear](#4-componentes-reutilizables-a-crear)
5. [Instrucciones por pantalla — Flujo de Autenticación](#5-instrucciones-por-pantalla--flujo-de-autenticación)
6. [Instrucciones por pantalla — Flujo Principal (Feed)](#6-instrucciones-por-pantalla--flujo-principal-feed)
7. [Instrucciones por pantalla — Detalle e Interacción](#7-instrucciones-por-pantalla--detalle-e-interacción)
8. [Instrucciones por pantalla — Creación/Edición de Publicación](#8-instrucciones-por-pantalla--creacióne-dición-de-publicación)
9. [Instrucciones por pantalla — Moderación](#9-instrucciones-por-pantalla--moderación)
10. [Instrucciones por pantalla — Notificaciones](#10-instrucciones-por-pantalla--notificaciones)
11. [Instrucciones por pantalla — Perfil y Dashboard](#11-instrucciones-por-pantalla--perfil-y-dashboard)
12. [Navegación y Bottom Navigation Bar](#12-navegación-y-bottom-navigation-bar)
13. [Checklist de implementación por fase](#13-checklist-de-implementación-por-fase)
14. [Convenciones y mejores prácticas para Compose](#14-convenciones-y-mejores-prácticas-para-compose)

---

## 1. Resumen del estado actual del proyecto

### Archivos existentes (esqueleto funcional)

| Archivo | Estado | Qué contiene |
|---|---|---|
| `core/ui/theme/Color.kt` | ✅ Completo | 14 colores: verdes, naranja, azul, fondos, errores |
| `core/ui/theme/Theme.kt` | ✅ Completo | `PetHelpTheme` con Dynamic Color + light/dark schemes |
| `core/ui/theme/Type.kt` | ✅ Completo | 15 estilos tipográficos Material 3 completos |
| `core/navigation/Screen.kt` | ✅ Completo | 14 rutas selladas con parámetros |
| `core/navigation/PetHelpNavGraph.kt` | ✅ Completo | NavHost con 14 destinos |
| `core/domain/model/Post.kt` | ✅ Completo | 20 campos + enums `PostCategory`, `PostStatus`, `AnimalSize` |
| `core/domain/model/User.kt` | ✅ Completo | `User`, `UserRole`, `UserLevel` (4 niveles), `Badge` |
| `core/domain/model/Comment.kt` | ✅ Completo | `Comment` con 6 campos |
| `core/domain/model/PetNotification.kt` | ✅ Completo | `PetNotification` + 6 tipos de notificación |
| `core/common/Constants.kt` | ✅ Completo | Todas las constantes: puntos, niveles, categorías |
| `features/auth/presentation/AuthScreens.kt` | ⚠️ Placeholder | 4 composables esqueleto (Splash, Login, Register, ForgotPassword) |
| `features/feed/presentation/FeedScreen.kt` | ⚠️ Placeholder | Scaffold básico con toggle lista/mapa |
| `features/post/presentation/PostScreens.kt` | ⚠️ Placeholder | 3 composables esqueleto (Detail, Create, Edit) |
| `features/moderation/presentation/ModerationScreens.kt` | ⚠️ Placeholder | 2 composables esqueleto (Panel, Detail) |
| `features/notifications/presentation/NotificationsScreen.kt` | ⚠️ Placeholder | Scaffold con back navigation |
| `features/profile/presentation/ProfileScreens.kt` | ⚠️ Placeholder | 2 composables esqueleto (Profile, EditProfile) |
| `features/reputation/presentation/ReputationScreen.kt` | ⚠️ Placeholder | Puntos, nivel, badges en placeholder |
| `features/stats/presentation/StatisticsScreen.kt` | ⚠️ Placeholder | 3 `StatCard` con valores "—" |
| `core/ui/components/` | ❌ No existe | Carpeta para composables reutilizables — **por crear** |

### Lo que falta implementar en las vistas

Todos los archivos en `features/*/presentation/` son **placeholders** con textos estáticos y `TODO` comments. Deben reemplazarse completamente con las vistas diseñadas en Figma siguiendo esta guía.

---

## 2. Cómo usar Figma for VS Code para inspeccionar los diseños

La extensión **Figma for VS Code** ya está instalada en el workspace. Permite inspeccionar diseños directamente desde el editor.

### Configuración inicial

1. **Abrir la barra lateral de Figma:** clic en el ícono de Figma en la barra de actividades izquierda de VS Code (o `Ctrl+Shift+P` → "Figma: Open").
2. **Autenticarse:** la primera vez pedirá login con tu cuenta de Figma.
3. **Vincular el archivo de diseño:** pegar la URL del archivo Figma del proyecto PetHelp.

### Flujo de trabajo recomendado

```
┌─────────────────────────────────────────────────────┐
│  1. Abrir panel Figma lateral en VS Code            │
│  2. Seleccionar la pantalla/frame a implementar     │
│  3. Inspeccionar propiedades del componente:        │
│     • Colores (HEX → mapear a Color.kt)            │
│     • Tamaños de fuente (sp → mapear a Type.kt)    │
│     • Paddings y margins (dp)                       │
│     • Corner radius (dp)                            │
│     • Elevación/sombras                             │
│  4. Copiar sugerencia de código si está disponible  │
│  5. Implementar en Compose siguiendo esta guía      │
└─────────────────────────────────────────────────────┘
```

### Mapeo de tokens Figma → Compose

| Propiedad en Figma | Equivalente en Compose |
|---|---|
| Color primario verde | `MaterialTheme.colorScheme.primary` → `PetHelpGreen` |
| Color secundario naranja | `MaterialTheme.colorScheme.secondary` → `WarmOrange` |
| Color terciario azul | `MaterialTheme.colorScheme.tertiary` → `SoftBlue` |
| Fondo claro `#F9FAF8` | `MaterialTheme.colorScheme.background` |
| Superficie blanca | `MaterialTheme.colorScheme.surface` |
| Error rojo | `MaterialTheme.colorScheme.error` |
| Título grande bold | `MaterialTheme.typography.headlineLarge` |
| Body text | `MaterialTheme.typography.bodyLarge` |
| Label/chip | `MaterialTheme.typography.labelLarge` |
| Corner radius grande | `RoundedCornerShape(28.dp)` (pill) |
| Corner radius medio | `RoundedCornerShape(16.dp)` |
| Corner radius pequeño | `RoundedCornerShape(12.dp)` |
| Elevación card | `CardDefaults.cardElevation(defaultElevation = 2.dp)` |
| Spacing estándar | `16.dp` (márgenes), `8.dp` (gaps internos) |

### Extraer valores exactos desde Figma for VS Code

1. **Seleccionar un elemento** en el panel de Figma.
2. En la pestaña **"Inspect"**, ver:
   - **Layout:** width, height, padding, gaps.
   - **Fill:** colores exactos en HEX.
   - **Typography:** nombre de fuente, peso, tamaño, line-height.
   - **Effects:** sombras, blur.
3. En la pestaña **"Code"**, ver sugerencias de código CSS/Swift/Android.
4. Adaptar manualmente a Jetpack Compose usando los tokens del tema.

---

## 3. Sistema de diseño implementado (tokens de tema)

### 3.1 Paleta de colores (`Color.kt`)

```
MODO CLARO                              MODO OSCURO
──────────────────────────────          ──────────────────────────────
primary:     #2E7D32 (verde bosque)     primary:     #60AD5E (verde claro)
secondary:   #E65100 (naranja cálido)   secondary:   #FF8A65 (naranja suave)
tertiary:    #1565C0 (azul confianza)   tertiary:    #82B1FF (azul claro)
background:  #F9FAF8 (crema suave)      background:  #111512 (gris profundo)
surface:     #FFFFFF (blanco)           surface:     #1E2420 (gris oscuro)
error:       #B00020 (rojo)             error:       #CF6679 (rojo pastel)
```

**Colores adicionales del Figma por mapear:**
| Uso en el diseño Figma | Color sugerido | Cómo acceder en Compose |
|---|---|---|
| Fondo píldora IA (violeta) | `#B39DDB` | Crear `val AIPurple = Color(0xFFB39DDB)` en Color.kt |
| Tag IA border | `#9575CD` | Crear `val AIPurpleDark = Color(0xFF9575CD)` en Color.kt |
| Coral CTA (Solicitar Adopción) | `#FF8A65` | `MaterialTheme.colorScheme.secondary` (modo oscuro) o `WarmOrangeDark` |
| Fondo notificación no leída | `primary.copy(alpha = 0.08f)` | Usar `MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)` |

### 3.2 Tipografía (`Type.kt`)

Los 15 estilos de Material 3 Type Scale ya están definidos. Usar así:

| Elemento de Figma | Estilo Compose |
|---|---|
| Título de app "PetHelp" | `headlineLarge` (32sp, SemiBold) |
| "¡Hola de nuevo!" en login | `headlineMedium` (28sp, SemiBold) |
| Título de tarjeta de mascota | `titleLarge` (22sp, Bold) |
| Nombre de mascota en detalle | `headlineLarge` (32sp, SemiBold) |
| Texto descriptivo | `bodyLarge` (16sp) |
| Texto en chips/tags | `labelLarge` (14sp, Medium) |
| Texto auxiliar/gris | `bodySmall` (12sp) |
| Hora de notificación | `labelSmall` (11sp) |
| Contadores "1,450 puntos" | `displaySmall` (36sp) |

### 3.3 Shapes (a definir)

Crear `core/ui/theme/Shape.kt`:

```kotlin
package com.pethelp.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PetHelpShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),   // tarjetas, campos
    large      = RoundedCornerShape(24.dp),   // bottom sheets, modales
    extraLarge = RoundedCornerShape(28.dp),   // botones pill, FABs
)
```

---

## 4. Componentes reutilizables a crear

Crear la carpeta `core/ui/components/` con los siguientes composables. Cada componente se compartirá entre múltiples pantallas.

### 4.1 `PetHelpButton.kt` — Botón principal pill-shaped

```
Archivo: core/ui/components/PetHelpButton.kt
```

| Propiedad | Valor |
|---|---|
| Forma | `RoundedCornerShape(28.dp)` — totalmente redondeado (pill) |
| Altura mínima | `56.dp` |
| Ancho | `fillMaxWidth()` por defecto |
| Color de fondo | `MaterialTheme.colorScheme.primary` (o `.secondary` para CTAs corales) |
| Texto | `labelLarge` en `onPrimary` |
| Elevation | `ButtonDefaults.buttonElevation(defaultElevation = 2.dp)` |
| Variantes | `Primary`, `Secondary` (coral), `Error` (rojo), `Text` (sin fondo) |

**Instrucciones de implementación:**
1. Crear un `@Composable fun PetHelpButton(text: String, onClick: () -> Unit, modifier: Modifier, variant: ButtonVariant, enabled: Boolean, icon: ImageVector?)`.
2. Usar `Button()` de Material 3 con `shape = RoundedCornerShape(28.dp)`.
3. Si `icon != null`, añadir `Icon` antes del texto con spacing de `8.dp`.
4. Variantes: `enum class ButtonVariant { Primary, Secondary, Error, Text }`.

### 4.2 `PetHelpTextField.kt` — Campo de texto outlined redondeado

```
Archivo: core/ui/components/PetHelpTextField.kt
```

| Propiedad | Valor |
|---|---|
| Estilo | `OutlinedTextField` de Material 3 |
| Shape | `RoundedCornerShape(16.dp)` |
| Ícono leading | Opcional (email, password, search) |
| Ícono trailing | Opcional (ojo para password, clear) |
| Colors | `OutlinedTextFieldDefaults.colors()` con primary para focus |
| Error | Mostrar texto de error debajo en rojo |

**Instrucciones de implementación:**
1. Crear `@Composable fun PetHelpTextField(value: String, onValueChange: (String) -> Unit, label: String, leadingIcon: ImageVector?, trailingIcon: @Composable (() -> Unit)?, isError: Boolean, errorMessage: String?, keyboardType: KeyboardType, modifier: Modifier)`.
2. Usar `OutlinedTextField` con `shape = RoundedCornerShape(16.dp)`.
3. Si `isError`, mostrar `Text(errorMessage)` debajo con `color = MaterialTheme.colorScheme.error`.

### 4.3 `PostCard.kt` — Tarjeta de publicación para el feed

```
Archivo: core/ui/components/PostCard.kt
```

**Diseño según Figma (Pantalla 5 — Feed Lista):**

```
┌──────────────────────────────────────────┐
│  ┌────────────────────────────────────┐  │
│  │           IMAGEN (ratio 16:9)      │  │
│  │                      ┌──────────┐  │  │
│  │                      │📍 a 2 km │  │  │
│  │                      └──────────┘  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  Max - Golden Retriever     titleLarge   │
│  🐕 Macho · Mediano · Medellín          │
│                                          │
│                              ♥ 12        │
└──────────────────────────────────────────┘
```

| Propiedad | Valor |
|---|---|
| Componente base | `ElevatedCard` de Material 3 |
| Esquinas | `RoundedCornerShape(16.dp)` |
| Elevación | `CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)` |
| Imagen | `AsyncImage` (Coil) con `clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))` y `contentScale = ContentScale.Crop` |
| Píldora distancia | `Surface` con `color = Color.Black.copy(alpha = 0.6f)` + `shape = RoundedCornerShape(12.dp)` + `Text` blanco |
| Título | `titleLarge` |
| Tags | `Row` de `labelMedium` en gris con íconos |
| Botón interés | `IconButton` con `Icons.Outlined.FavoriteBorder` + conteo |
| Padding interno | `16.dp` horizontal, `12.dp` vertical |

**Instrucciones de implementación:**
1. Parámetro: `fun PostCard(post: Post, distanceKm: Double?, onCardClick: () -> Unit, onVoteClick: () -> Unit, modifier: Modifier)`.
2. Usar `ElevatedCard(onClick = onCardClick)`.
3. Sección superior: `Box` con `AsyncImage` y píldora de distancia posicionada con `Modifier.align(Alignment.BottomEnd).padding(8.dp)`.
4. Sección inferior: `Column(modifier = Modifier.padding(16.dp))` con título, fila de tags, y fila de votos alineada a la derecha.
5. Para Fase 2 sin Coil: usar `Image(painterResource(R.drawable.placeholder))`.

### 4.4 `CategoryFilterChips.kt` — Fila de chips de categoría

```
Archivo: core/ui/components/CategoryFilterChips.kt
```

| Propiedad | Valor |
|---|---|
| Tipo | `LazyRow` de `FilterChip` (Material 3) |
| Chips | 🐾 Adopción, 🔍 Perdidos, 📍 Encontrados, 🏠 Hogar temporal, 💉 Veterinaria |
| Selección | Resaltado con color primary + checkmark |
| Espaciado | `horizontalArrangement = Arrangement.spacedBy(8.dp)` |
| Padding externo | `contentPadding = PaddingValues(horizontal = 16.dp)` |

**Instrucciones de implementación:**
1. Parámetro: `fun CategoryFilterChips(selectedCategories: Set<PostCategory>, onCategoryToggle: (PostCategory) -> Unit, modifier: Modifier)`.
2. Usar `LazyRow` con `FilterChip(selected = category in selectedCategories, onClick = { onCategoryToggle(category) }, label = { Text("🐾 ${category.displayName}") })`.
3. Mapear `PostCategory` → emoji + nombre display.

### 4.5 `PetHelpBottomNavBar.kt` — Barra de navegación inferior

```
Archivo: core/ui/components/PetHelpBottomNavBar.kt
```

**Diseño según Figma (Pantalla 5):**

```
┌─────────────────────────────────────────────────────┐
│   🏠        🗺️        ➕ (FAB)       🔔       👤   │
│  Home      Mapa      Nueva         Notifs   Perfil  │
│   ●                    Pub                           │
└─────────────────────────────────────────────────────┘
```

| Propiedad | Valor |
|---|---|
| Componente | `NavigationBar` de Material 3 |
| Items | 5: Home/Feed, Mapa, Crear (FAB central), Notificaciones, Perfil |
| Item activo | Píldora con color `primaryContainer` |
| FAB central | Más grande que los otros ítems, estilo FAB flotante |
| Íconos | `Icons.Filled.Home`, `Icons.Filled.Map`, `Icons.Filled.Add`, `Icons.Filled.Notifications`, `Icons.Filled.Person` |

**Instrucciones de implementación:**
1. Parámetro: `fun PetHelpBottomNavBar(currentRoute: String, onNavigate: (Screen) -> Unit, modifier: Modifier)`.
2. Usar `NavigationBar` con 5 `NavigationBarItem`.
3. El ítem central (Crear) se destaca con un `FloatingActionButton` embebido o con un icono más grande.
4. Mapear rutas a ítems: Feed → Home, Feed(mapa) → Mapa, CreatePost → ➕, Notifications → 🔔, Profile → 👤.

### 4.6 `PetAttributeChips.kt` — Cuadrícula de atributos de mascota

```
Archivo: core/ui/components/PetAttributeChips.kt
```

**Diseño según Figma (Pantalla 7 — Detalle):**

```
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ ♀ Hembra │  │🐕 Schnau │  │📏 Mediano│  │💉 Vacuna │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

| Propiedad | Valor |
|---|---|
| Layout | `FlowRow` (de `androidx.compose.foundation.layout`) o `LazyVerticalGrid` |
| Cada chip | `Surface` con `color = primaryContainer` + `shape = RoundedCornerShape(12.dp)` |
| Padding chip | `horizontal = 12.dp, vertical = 8.dp` |
| Texto | `labelLarge` |
| Ícono | Emoji o `Icon` a la izquierda |

### 4.7 `MiniMap.kt` — Mini-mapa para detalle de publicación

```
Archivo: core/ui/components/MiniMap.kt
```

| Propiedad | Valor |
|---|---|
| Tamaño | `fillMaxWidth()` × `180.dp` |
| Shape | `RoundedCornerShape(16.dp)` con `clip` |
| Contenido | Google Map con un marcador en las coordenadas del post |
| Interacción | Click abre mapa completo o navigationIntent |
| Placeholder (Fase 2) | `Box` con fondo gris + ícono de mapa centrado |

### 4.8 `CommentSection.kt` — Sección de comentarios

```
Archivo: core/ui/components/CommentSection.kt
```

**Diseño según Figma (Pantalla 7):**

```
┌─────────────────────────────────────────────┐
│  👤 Avatar   Juan P.           Hace 2h      │
│              Gran comentario sobre la...     │
├─────────────────────────────────────────────┤
│  👤 Avatar | Escribe un comentario...  [➜]  │
└─────────────────────────────────────────────┘
```

| Elemento | Implementación |
|---|---|
| Lista de comentarios | `LazyColumn` limitada con `heightIn(max = 300.dp)` |
| Cada comentario | `Row` con avatar circular + `Column(text, time)` |
| Input | `Row` con avatar + `OutlinedTextField(singleLine)` + `IconButton(send)` |

### 4.9 `AIBadge.kt` — Etiqueta de IA (violeta)

```
Archivo: core/ui/components/AIBadge.kt
```

| Propiedad | Valor |
|---|---|
| Fondo | `Color(0xFFB39DDB).copy(alpha = 0.15f)` |
| Border | `BorderStroke(1.dp, Color(0xFF9575CD))` |
| Ícono | ✨ sparkles o `Icons.Filled.AutoAwesome` |
| Texto color | `Color(0xFF7E57C2)` |
| Shape | `RoundedCornerShape(8.dp)` |

---

## 5. Instrucciones por pantalla — Flujo de Autenticación

### 5.1 Pantalla 1: Splash Screen & Bienvenida

**Archivo:** `features/auth/presentation/AuthScreens.kt` → función `SplashScreen`  
**Ruta:** `Screen.Splash`  
**HU relacionada:** N/A (pantalla transicional)

#### Diseño Figma

```
┌───────────────────────────────────┐
│                                   │
│           (espacio)               │
│                                   │
│        🐾❤️🤲                     │  ← Logo abstracto
│        (glassmorphism)            │
│                                   │
│        P e t H e l p              │  ← headlineLarge, Bold
│   Encuentra, cuida, adopta        │  ← bodyLarge, gris medio
│                                   │
│           (espacio)               │
│                                   │
│   ┌─────────────────────────┐     │
│   │       Comenzar          │     │  ← Botón pill, primary
│   └─────────────────────────┘     │
│                                   │
└───────────────────────────────────┘
Fondo: primaryContainer o crema suave (#F9FAF8)
```

#### Instrucciones paso a paso

1. **Contenedor:** `Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))` con `contentAlignment = Alignment.Center`.
2. **Layout interno:** `Column(horizontalAlignment = CenterHorizontally, verticalArrangement = Arrangement.Center)`.
3. **Logo:** Crear un composable `PetHelpLogo()` que combine un `Icon` de pata + corazón. Para Fase 2, usar un `Image(painterResource(R.drawable.pethelp_logo))` o un `Text("🐾", fontSize = 64.sp)` provisional.
4. **Título:** `Text("PetHelp", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))`.
5. **Subtítulo:** `Text("Encuentra, cuida, adopta", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)`.
6. **Spacer:** `Spacer(modifier = Modifier.height(48.dp))`.
7. **Botón:** `PetHelpButton(text = "Comenzar", onClick = { navegar a Login })` con `Modifier.fillMaxWidth().padding(horizontal = 32.dp)`.
8. **Animación opcional:** `AnimatedVisibility` para fade-in del logo y botón al cargar.

#### Navegación
- Botón "Comenzar" → `Screen.Login`
- Si hay sesión activa (Fase 3): auto-navegar a `Screen.Feed` después de 1.5s.

---

### 5.2 Pantalla 2: Inicio de Sesión (Login)

**Archivo:** `features/auth/presentation/AuthScreens.kt` → función `LoginScreen`  
**Ruta:** `Screen.Login`  
**HU relacionada:** HU-01.2, HU-01.3

#### Diseño Figma

```
┌───────────────────────────────────┐
│                                   │
│   ¡Hola de nuevo!                 │  ← headlineMedium
│                                   │
│        🐶 (ilustración)           │  ← Imagen decorativa
│                                   │
│   ┌─────────────────────────┐     │
│   │ 📧 Correo Electrónico  │     │  ← OutlinedTextField
│   └─────────────────────────┘     │
│                                   │
│   ┌─────────────────────────┐     │
│   │ 🔒 Contraseña       👁  │     │  ← OutlinedTextField + toggle
│   └─────────────────────────┘     │
│           ¿Olvidaste tu           │  ← TextButton alineado derecha
│            contraseña?            │     color = tertiary o primary
│                                   │
│   ┌─────────────────────────┐     │
│   │     Iniciar Sesión      │     │  ← Botón pill, primary/secondary
│   └─────────────────────────┘     │
│                                   │
│  ¿No tienes cuenta? Regístrate    │  ← bodyMedium + TextButton
│                                   │
└───────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **Estado del formulario:** Crear variables `var email by remember { mutableStateOf("") }`, `var password by remember { mutableStateOf("") }`, `var passwordVisible by remember { mutableStateOf(false) }`, `var isLoading by remember { mutableStateOf(false) }`.
2. **Contenedor:** `Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp))`.
3. **Título:** `Text("¡Hola de nuevo!", style = MaterialTheme.typography.headlineMedium)` con `Modifier.padding(top = 48.dp)`.
4. **Ilustración:** `Image` o placeholder de 120.dp de alto centrado. Para Fase 2, usar `Box(Modifier.size(120.dp).clip(CircleShape).background(primaryContainer))` con emoji `🐶`.
5. **Campo email:** `PetHelpTextField(value = email, label = "Correo Electrónico", leadingIcon = Icons.Outlined.Email, keyboardType = KeyboardType.Email)`.
6. **Spacer:** `Spacer(Modifier.height(16.dp))`.
7. **Campo contraseña:**
   ```kotlin
   PetHelpTextField(
       value = password,
       label = "Contraseña",
       leadingIcon = Icons.Outlined.Lock,
       trailingIcon = {
           IconButton(onClick = { passwordVisible = !passwordVisible }) {
               Icon(
                   if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                   contentDescription = "Toggle password"
               )
           }
       },
       visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
       keyboardType = KeyboardType.Password
   )
   ```
8. **Link ¿Olvidaste?:** `TextButton(onClick = { navegar a ForgotPassword })` alineado a la derecha con `Modifier.align(Alignment.End)`. Texto con color `MaterialTheme.colorScheme.primary`.
9. **Spacer:** `Spacer(Modifier.height(24.dp))`.
10. **Botón login:** `PetHelpButton(text = "Iniciar Sesión", onClick = { validar y autenticar })` full width.
11. **Link registro:** `Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))` con `Text("¿No tienes cuenta? ")` + `TextButton { Text("Regístrate", color = primary, fontWeight = Bold) }`.

#### Validaciones (Fase 2 — en memoria)
- Email no vacío y formato válido (regex).
- Contraseña no vacía.
- Si falla, mostrar `Snackbar` o campo en estado error.
- Fase 2: comparar contra lista de usuarios mock.
- Fase 3: Firebase Auth `signInWithEmailAndPassword`.

#### Navegación
- Login exitoso (rol USER) → `Screen.Feed`
- Login exitoso (rol MODERATOR) → `Screen.ModeratorPanel`
- "Regístrate" → `Screen.Register`
- "¿Olvidaste tu contraseña?" → `Screen.ForgotPassword`

---

### 5.3 Pantalla 3: Registro (Sign Up)

**Archivo:** `features/auth/presentation/AuthScreens.kt` → función `RegisterScreen`  
**Ruta:** `Screen.Register`  
**HU relacionada:** HU-01.1

#### Diseño Figma

```
┌───────────────────────────────────┐
│ ← (back)                         │
│                                   │
│   Crea tu cuenta                  │  ← headlineMedium
│                                   │
│   ┌─────────────────────────┐     │
│   │ 👤 Nombre completo      │     │  ← OutlinedTextField
│   └─────────────────────────┘     │
│                                   │
│   ┌─────────────────────────┐     │
│   │ 📧 Correo electrónico   │     │  ← OutlinedTextField
│   └─────────────────────────┘     │
│                                   │
│   ┌─────────────────────────┐     │
│   │ 🔒 Contraseña           │     │  ← OutlinedTextField
│   └─────────────────────────┘     │
│   ▓▓▓▓▓▓▓▓░░  Fortaleza: Fuerte  │  ← LinearProgressIndicator
│                                   │
│   ☑ Acepto los términos           │  ← Checkbox + Text
│                                   │
│   ┌─────────────────────────┐     │
│   │       Registrarme       │     │  ← Botón pill, primary
│   └─────────────────────────┘     │
│                                   │
└───────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **Estado:** `var name`, `var email`, `var password`, `var termsAccepted by remember { mutableStateOf(false) }`.
2. **TopAppBar:** `IconButton(onClick = { navController.popBackStack() })` con `Icons.AutoMirrored.Filled.ArrowBack`.
3. **Título:** `Text("Crea tu cuenta", style = headlineMedium)`.
4. **3 campos:**
   - Nombre: leadingIcon = `Icons.Outlined.Person`
   - Email: leadingIcon = `Icons.Outlined.Email`
   - Contraseña: leadingIcon = `Icons.Outlined.Lock` con toggle visibility
5. **Barra de fortaleza de contraseña:**
   ```kotlin
   val strength = calculatePasswordStrength(password) // 0f..1f
   val strengthColor = when {
       strength < 0.3f -> MaterialTheme.colorScheme.error
       strength < 0.7f -> WarmOrange
       else -> PetHelpGreen
   }
   val strengthText = when {
       strength < 0.3f -> "Débil"
       strength < 0.7f -> "Moderada"
       else -> "Fuerte"
   }
   LinearProgressIndicator(
       progress = { strength },
       modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
       color = strengthColor
   )
   Text("Fortaleza: $strengthText", style = labelSmall, color = strengthColor)
   ```
6. **Función de fortaleza:**
   ```kotlin
   private fun calculatePasswordStrength(password: String): Float {
       var score = 0f
       if (password.length >= 8) score += 0.25f
       if (password.any { it.isUpperCase() }) score += 0.25f
       if (password.any { it.isDigit() }) score += 0.25f
       if (password.any { !it.isLetterOrDigit() }) score += 0.25f
       return score
   }
   ```
7. **Checkbox términos:** `Row(verticalAlignment = CenterVertically)` con `Checkbox(checked = termsAccepted)` + `Text("Acepto los términos y condiciones")`.
8. **Botón registrar:** habilitado solo si todos los campos son válidos y `termsAccepted == true`.

#### Validaciones
- Nombre: mínimo 2 caracteres.
- Email: formato válido.
- Contraseña: mínimo 8 caracteres.
- Terms: checkbox marcado.
- Email duplicado → error (comprobar contra lista mock en Fase 2).

#### Navegación
- Registro exitoso → `Screen.Feed`
- Botón back → `Screen.Login`

---

### 5.4 Pantalla 4: Recuperación de Contraseña

**Archivo:** `features/auth/presentation/AuthScreens.kt` → función `ForgotPasswordScreen`  
**Ruta:** `Screen.ForgotPassword`  
**HU relacionada:** HU-01.4

#### Diseño Figma

```
┌───────────────────────────────────┐
│ ← (back)                         │
│                                   │
│   ¿Olvidaste tu contraseña?       │  ← headlineMedium
│                                   │
│   No te preocupes. Ingresa tu     │  ← bodyLarge, gris
│   correo electrónico y te         │
│   enviaremos un enlace para       │
│   recuperar el acceso.            │
│                                   │
│        🔑🐾 (ilustración)         │
│                                   │
│   ┌─────────────────────────┐     │
│   │ 📧 Correo Electrónico  │     │  ← OutlinedTextField
│   └─────────────────────────┘     │
│                                   │
│   ┌─────────────────────────┐     │
│   │      Enviar enlace      │     │  ← Botón pill, primary
│   └─────────────────────────┘     │
│                                   │
└───────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **TopAppBar** con botón back.
2. **Título:** `headlineMedium`.
3. **Texto descriptivo:** `bodyLarge` con color `onSurfaceVariant`.
4. **Ilustración opcional:** placeholder centrado.
5. **Campo email:** `PetHelpTextField` con `leadingIcon = Icons.Outlined.Email`.
6. **Botón:** `PetHelpButton("Enviar enlace")`.
7. **Éxito:** Mostrar `Snackbar` o diálogo "Enlace enviado a tu correo".
8. **Fase 2:** Mostrar mensaje informativo (no envía nada real).
9. **Fase 3:** `Firebase.auth.sendPasswordResetEmail(email)`.

---

## 6. Instrucciones por pantalla — Flujo Principal (Feed)

### 6.1 Pantalla 5: Feed Principal — Vista Lista (Home)

**Archivo:** `features/feed/presentation/FeedScreen.kt`  
**Ruta:** `Screen.Feed`  
**HU relacionadas:** HU-03.1, HU-03.3

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│  PetHelp             🔔  👤              │  ← TopAppBar
├───────────────────────────────────────────┤
│  🐾 Adopción | 🔍 Perdidos | 📍 Encontr. │  ← Chips horizontales
├───────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐  │
│  │         [FOTO PERRO]               │  │
│  │                        📍 a 2 km   │  │
│  ├─────────────────────────────────────┤  │
│  │  Max - Golden Retriever            │  │
│  │  🐕 Macho · Mediano · Medellín     │  │
│  │                            ♥ 12    │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │         [FOTO GATA]                │  │
│  │                        📍 a 5 km   │  │
│  ├─────────────────────────────────────┤  │
│  │  Luna - Siamesa                    │  │
│  │  🐱 Hembra · Pequeña · Armenia     │  │
│  │                            ♥ 8     │  │
│  └─────────────────────────────────────┘  │
│                                           │
│                   (scroll)                │
│                                           │
├───────────────────────────────────────────┤
│  🏠    🗺️    [➕ FAB]    🔔     👤     │  ← BottomNav
└───────────────────────────────────────────┘
```

#### Estructura del Composable

```kotlin
@Composable
fun FeedScreen(
    navController: NavController,
    // viewModel: FeedViewModel = hiltViewModel()  ← Fase 2+
) {
    val isMapView = remember { mutableStateOf(false) }
    val selectedCategories = remember { mutableStateOf(setOf<PostCategory>()) }
    
    Scaffold(
        topBar = { FeedTopAppBar(isMapView, navController) },
        bottomBar = { PetHelpBottomNavBar(currentRoute = Screen.Feed.route, ...) },
        floatingActionButton = {
            // NO poner FAB aquí si está integrado en BottomNav
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Chips de categoría
            CategoryFilterChips(
                selectedCategories = selectedCategories.value,
                onCategoryToggle = { /* toggle */ }
            )
            
            if (isMapView.value) {
                FeedMapView(posts = filteredPosts)  // Pantalla 6
            } else {
                FeedListView(posts = filteredPosts, onPostClick = { post ->
                    navController.navigate(Screen.PostDetail.createRoute(post.id))
                })
            }
        }
    }
}
```

#### FeedTopAppBar

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopAppBar(
    isMapView: MutableState<Boolean>,
    navController: NavController
) {
    TopAppBar(
        title = { Text("PetHelp", style = MaterialTheme.typography.titleLarge) },
        actions = {
            // Toggle lista/mapa
            IconButton(onClick = { isMapView.value = !isMapView.value }) {
                Icon(
                    if (isMapView.value) Icons.Filled.ViewList else Icons.Filled.Map,
                    contentDescription = "Cambiar vista"
                )
            }
            // Campana de notificaciones (con badge si hay no leídas)
            BadgedBox(badge = { Badge { Text("3") } }) {
                IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                }
            }
            // Avatar usuario
            IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
            }
        }
    )
}
```

#### FeedListView

```kotlin
@Composable
private fun FeedListView(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onVoteClick: (Post) -> Unit = {}
) {
    if (posts.isEmpty()) {
        // Estado vacío
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = CenterHorizontally) {
                Icon(Icons.Outlined.Pets, modifier = Modifier.size(64.dp), tint = onSurfaceVariant)
                Text("No hay publicaciones", style = bodyLarge, color = onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    distanceKm = null, // Fase 3: calcular con ubicación real
                    onCardClick = { onPostClick(post) },
                    onVoteClick = { onVoteClick(post) }
                )
            }
        }
    }
}
```

#### Datos Mock para Fase 2

Crear `features/feed/data/MockData.kt`:

```kotlin
object MockPosts {
    val samplePosts = listOf(
        Post(
            id = "1",
            title = "Max - Golden Retriever",
            description = "Max es un perrito de 3 años muy cariñoso...",
            category = PostCategory.ADOPTION,
            animalType = "Perro",
            breed = "Golden Retriever",
            size = AnimalSize.MEDIUM,
            isVaccinated = true,
            latitude = 4.5339,
            longitude = -75.6811,
            imageUrls = listOf("placeholder"),
            authorId = "user1",
            authorName = "JuanP",
            status = PostStatus.VERIFIED,
            voteCount = 12,
            createdAt = System.currentTimeMillis()
        ),
        // ... más posts mock
    )
}
```

---

### 6.2 Pantalla 6: Feed Principal — Vista Mapa

**Archivo:** `features/feed/presentation/FeedScreen.kt` → composable `FeedMapView`  
**HU relacionada:** HU-03.2

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│  ┌─────────────────────────────────────┐  │
│  │  🔍 Buscar mascotas...      ☰      │  │  ← SearchBar flotante
│  └─────────────────────────────────────┘  │
│  🐾 Adopción | 🔍 Perdidos | 📍...       │  ← Chips debajo
│  ┌─────────────────────────────────────┐  │
│  │                                     │  │
│  │          MAPA COMPLETO              │  │
│  │                                     │  │
│  │    🐶          🐱                   │  │  ← Marcadores personalizados
│  │         🐕                          │  │
│  │                      🐱             │  │
│  │                                     │  │
│  └─────────────────────────────────────┘  │
│  ┌─────────────────────────────────────┐  │  ← Bottom Sheet parcial
│  │  [📷]  Luna - Perdida en Av...      │  │
│  │         Ver detalle →               │  │
│  └─────────────────────────────────────┘  │
├───────────────────────────────────────────┤
│  🏠    🗺️    [➕]    🔔     👤         │
└───────────────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **Fase 2:** Reemplazar el mapa por un `Box` con fondo gris claro, íconos de marcadores estáticos y texto "Mapa disponible en Fase 3".
2. **Fase 3:** 
   - Usar `GoogleMap` de Maps Compose: `com.google.maps.android.compose.GoogleMap`.
   - `CameraPositionState` centrado en ubicación del usuario.
   - Marcadores personalizados: `MarkerComposable` con `AsyncImage` circular (foto de mascota) de 40.dp.
   - Al click en marcador: mostrar `ModalBottomSheet` con resumen.
3. **SearchBar flotante:** `DockedSearchBar` de Material 3 posicionado con `Modifier.align(Alignment.TopCenter).padding(16.dp)`.
4. **Bottom Sheet parcial:** `ModalBottomSheet` o `BottomSheetScaffold` con peek height de `100.dp`.
5. **Tarjeta resumen:** `Row` con `AsyncImage(64.dp, clip = CircleShape)` + `Column(title, subtitle)` + `TextButton("Ver detalle")`.

---

## 7. Instrucciones por pantalla — Detalle e Interacción

### 7.1 Pantalla 7: Detalle de Publicación de Adopción

**Archivo:** `features/post/presentation/PostScreens.kt` → función `PostDetailScreen`  
**Ruta:** `Screen.PostDetail`  
**HU relacionadas:** HU-03.4, HU-05.1, HU-05.2, HU-05.3

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│  ┌─────────────────────────────────────┐  │
│  │                                     │  │
│  │       HERO IMAGE (40% alto)         │  │
│  │                                     │  │
│  │  ← (back translúcido)              │  │
│  │                                     │  │
│  └─────────────────────────────────────┘  │
│  ╭─────────────────────────────────────╮  │  ← Panel con esquinas top rounded
│  │                                     │  │
│  │  Kira                 👤 Por JuanP  │  │  ← headlineLarge + avatar
│  │                                     │  │
│  │  ┌────┐ ┌─────────┐ ┌──────┐ ┌───┐ │  │
│  │  │♀Hem│ │🐕Schnau │ │📏Med │ │💉V│ │  │  ← PetAttributeChips
│  │  └────┘ └─────────┘ └──────┘ └───┘ │  │
│  │                                     │  │
│  │  (Descripción completa del animal)  │  │  ← bodyLarge
│  │                                     │  │
│  │  ┌─────────────────────────────┐    │  │
│  │  │      MINI MAPA con pin     │    │  │  ← MiniMap
│  │  └─────────────────────────────┘    │  │
│  │                                     │  │
│  │  Comentarios (3)                    │  │
│  │  👤 Santi: ¡Qué hermosa!  2h       │  │
│  │  👤 María: ¿Aún disponible? 1d     │  │
│  │                                     │  │
│  │  👤 | Escribe un comentario [➜]     │  │
│  │                                     │  │
│  ╰─────────────────────────────────────╯  │
│  ┌─────────────────────────────────────┐  │  ← Sticky bottom bar
│  │  Votos: 45    │  🐾 Solicitar       │  │
│  │               │     Adopción        │  │
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘
```

#### Estructura del Composable

```kotlin
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavController
) {
    // TODO Fase 2: obtener post de datos mock
    // TODO Fase 3: viewModel.getPost(postId)
    
    Scaffold(
        bottomBar = {
            // Sticky bottom bar
            PostDetailBottomBar(
                voteCount = post.voteCount,
                hasVoted = false, // Estado del usuario
                isAdoption = post.category == PostCategory.ADOPTION,
                onVoteClick = { /* toggle voto */ },
                onAdoptClick = { /* solicitar adopción */ }
            )
        }
    ) { paddingValues ->
        // Contenido scrollable
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            // 1. Hero Image
            item { HeroImageSection(post, navController) }
            // 2. Info panel
            item { PostInfoSection(post) }
            // 3. Atributos
            item { PetAttributeChips(post) }
            // 4. Descripción
            item { DescriptionSection(post.description) }
            // 5. Mini-mapa
            item { MiniMap(lat = post.latitude, lng = post.longitude) }
            // 6. Comentarios
            item { CommentSection(comments = post.comments, onSendComment = { /* */ }) }
        }
    }
}
```

#### Hero Image Section

```kotlin
@Composable
private fun HeroImageSection(post: Post, navController: NavController) {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        // Imagen de fondo
        AsyncImage(
            model = post.imageUrls.firstOrNull(),
            contentDescription = post.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Botón back translúcido
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}
```

#### Sticky Bottom Bar

```kotlin
@Composable
private fun PostDetailBottomBar(
    voteCount: Int,
    hasVoted: Boolean,
    isAdoption: Boolean,
    onVoteClick: () -> Unit,
    onAdoptClick: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Votos
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoteClick) {
                    Icon(
                        if (hasVoted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Votar",
                        tint = if (hasVoted) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                }
                Text("$voteCount votos", style = MaterialTheme.typography.labelLarge)
            }
            
            // Botón CTA (solo si es adopción)
            if (isAdoption) {
                Button(
                    onClick = onAdoptClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmOrangeDark // Coral vibrante
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Solicitar Adopción", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
```

---

## 8. Instrucciones por pantalla — Creación/Edición de Publicación

### 8.1 Pantalla 8: Crear Publicación

**Archivo:** `features/post/presentation/PostScreens.kt` → función `CreatePostScreen`  
**Ruta:** `Screen.CreatePost`  
**HU relacionadas:** HU-02.1, HU-08.1, HU-11.1a (IA)

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│ ← Ayuda a un peludo                      │  ← TopAppBar
├───────────────────────────────────────────┤
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │    ┌────┐ ┌────┐                    │  │
│  │    │foto│ │foto│  ┌──┐              │  │
│  │    │ 1  │ │ 2  │  │+ │              │  │
│  │    └────┘ └────┘  └──┘              │  │
│  │                                     │  │
│  │  📸 Toca para agregar hasta 5 fotos │  │  ← Zona de upload
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │  Título de publicación              │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │  Descripción de la situación        │  │  ← TextArea (minLines = 4)
│  │                                     │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  ✨ Categoría sugerida por IA             │  ← AIBadge + Dropdown
│  ┌────────────────────────────── ▼  ───┐  │
│  │  Adopción                           │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  Tipo de animal                           │
│  ( Perro )  ( Gato )  ( Otro )            │  ← ChoiceChips
│                                           │
│  Tamaño                                   │
│  ( Pequeño )  ( Mediano )  ( Grande )     │  ← ChoiceChips
│                                           │
│  Estado de vacunación                     │
│  ☑ Vacunado/a                             │  ← Switch o Checkbox
│                                           │
│  Raza aproximada                          │
│  ┌─────────────────────────────────────┐  │
│  │  Ej: Golden Retriever              │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │     Siguiente: Ubicación →         │  │  ← Botón pill, primary
│  └─────────────────────────────────────┘  │
│                                           │
└───────────────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **Estado del formulario:**
   ```kotlin
   data class CreatePostUiState(
       val images: List<Uri> = emptyList(),
       val title: String = "",
       val description: String = "",
       val category: PostCategory? = null,
       val aiSuggestedCategory: PostCategory? = null,
       val animalType: String = "",  // "Perro", "Gato", "Otro"
       val size: AnimalSize? = null,
       val isVaccinated: Boolean = false,
       val breed: String = "",
       val isLoading: Boolean = false,
       val titleError: String? = null,
       val descriptionError: String? = null,
   )
   ```

2. **Zona de imágenes:**
   ```kotlin
   @Composable
   private fun ImageUploadSection(
       images: List<Uri>,
       onAddImage: () -> Unit,
       onRemoveImage: (Int) -> Unit
   ) {
       Surface(
           modifier = Modifier.fillMaxWidth().height(180.dp),
           shape = RoundedCornerShape(16.dp),
           border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
           color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
       ) {
           if (images.isEmpty()) {
               // Estado vacío
               Column(
                   modifier = Modifier.fillMaxSize().clickable(onClick = onAddImage),
                   horizontalAlignment = CenterHorizontally,
                   verticalArrangement = Arrangement.Center
               ) {
                   Icon(Icons.Outlined.CameraAlt, modifier = Modifier.size(48.dp))
                   Text("Toca para agregar hasta 5 fotos", style = bodyMedium)
               }
           } else {
               // Fila de thumbnails + botón "+"
               LazyRow(
                   contentPadding = PaddingValues(12.dp),
                   horizontalArrangement = Arrangement.spacedBy(8.dp)
               ) {
                   items(images.size) { index ->
                       ImageThumbnail(images[index], onRemove = { onRemoveImage(index) })
                   }
                   if (images.size < 5) {
                       item { AddImageButton(onClick = onAddImage) }
                   }
               }
           }
       }
   }
   ```

3. **Selector de categoría con IA:**
   ```kotlin
   Row(verticalAlignment = Alignment.CenterVertically) {
       AIBadge() // ✨ sparkles violeta
       Spacer(Modifier.width(8.dp))
       Text("Categoría sugerida por IA", style = labelLarge)
   }
   // ExposedDropdownMenuBox con las 5 categorías
   // Si aiSuggestedCategory != null, preseleccionar
   ```

4. **Choice Chips para tipo de animal:**
   ```kotlin
   @Composable
   private fun AnimalTypeSelector(selected: String, onSelect: (String) -> Unit) {
       Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
           listOf("Perro", "Gato", "Otro").forEach { type ->
               FilterChip(
                   selected = selected == type,
                   onClick = { onSelect(type) },
                   label = { Text(type) },
                   leadingIcon = if (selected == type) {
                       { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                   } else null
               )
           }
       }
   }
   ```

5. **Botón siguiente:** Navega a una pantalla de selección de ubicación (mapa) o directamente crea la publicación si el mapa se implementa en el mismo formulario vía scroll adicional.

6. **Para el flujo completo en 2 pasos:**
   - Paso 1 (esta pantalla): datos + fotos → botón "Siguiente: Ubicación"
   - Paso 2 (nueva pantalla o sección): selector de mapa → botón "Publicar"
   - **Alternativa 1 paso:** Todo en un solo `LazyColumn` scrollable con mapa al final.

---

## 9. Instrucciones por pantalla — Moderación

### 9.1 Pantalla 9: Panel de Moderación

**Archivo:** `features/moderation/presentation/ModerationScreens.kt` → función `ModeratorPanelScreen`  
**Ruta:** `Screen.ModeratorPanel`  
**HU relacionadas:** HU-04.1, HU-04.2, HU-04.3, HU-11.1d (IA)

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│  Bandeja de Entrada - Pendientes          │  ← TopAppBar
├───────────────────────────────────────────┤
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │ [📷] Buscamos hogar para Max       │  │
│  │      Adopción · Por JuanP          │  │
│  │  ┌─────────────────────────────┐   │  │
│  │  │ ✨ Resumen IA: Posible      │   │  │  ← AIBadge
│  │  │ adopción real. Imagen       │   │  │
│  │  │ coincide Border Collie.     │   │  │
│  │  │ Sin tono de venta.          │   │  │
│  │  └─────────────────────────────┘   │  │
│  │                      ✅    ❌      │  │  ← Aprobar / Rechazar
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │ [📷] Gatita encontrada en parque   │  │
│  │      Encontrados · Por MaríaL      │  │
│  │  ┌─────────────────────────────┐   │  │
│  │  │ ✨ Resumen IA: Reporte      │   │  │
│  │  │ legítimo de hallazgo...     │   │  │
│  │  └─────────────────────────────┘   │  │
│  │                      ✅    ❌      │  │
│  └─────────────────────────────────────┘  │
│                                           │
│              (más items...)               │
│                                           │
└───────────────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **TopAppBar:** `Text("Bandeja de Entrada - Pendientes")` + opcionalmente badge con conteo.
2. **Lista:** `LazyColumn` de `ModerationCard`:

```kotlin
@Composable
private fun ModerationCard(
    post: Post,
    aiSummary: String?, // null si no disponible
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCardClick: () -> Unit
) {
    ElevatedCard(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Thumbnail
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.title, style = titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${post.category.displayName} · Por ${post.authorName}", style = bodySmall, color = onSurfaceVariant)
                
                // Resumen IA
                if (aiSummary != null) {
                    Spacer(Modifier.height(8.dp))
                    AIBadge()
                    Text(aiSummary, style = bodySmall, maxLines = 3)
                }
                
                // Botones aprobar/rechazar
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = onApprove,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PetHelpGreen.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Aprobar", tint = PetHelpGreen)
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = onReject,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ErrorRed.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Rechazar", tint = ErrorRed)
                    }
                }
            }
        }
    }
}
```

### 9.2 Pantalla 10: Diálogo — Rechazar Publicación

**HU relacionada:** HU-04.3

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│            (fondo difuminado)             │
│                                           │
│  ╭─────────────────────────────────────╮  │
│  │          ── (drag handle) ──        │  │
│  │                                     │  │
│  │    Motivo del Rechazo               │  │  ← Title Large
│  │                                     │  │
│  │  ┌─────────────────────────────┐    │  │
│  │  │  Explica respetuosamente    │    │  │
│  │  │  al usuario por qué la     │    │  │
│  │  │  publicación no puede ser   │    │  │
│  │  │  aprobada...                │    │  │
│  │  │                             │    │  │
│  │  └─────────────────────────────┘    │  │
│  │                                     │  │
│  │  Este motivo será enviado al autor. │  │  ← bodySmall, gris
│  │                                     │  │
│  │       Cancelar    [Rechazar]        │  │  ← TextButton + FilledButton(error)
│  │                                     │  │
│  ╰─────────────────────────────────────╯  │
│                                           │
└───────────────────────────────────────────┘
```

#### Implementación

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RejectPostBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onReject: (reason: String) -> Unit
) {
    if (isVisible) {
        var reason by remember { mutableStateOf("") }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Motivo del Rechazo", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = {
                        Text("Explica respetuosamente al usuario por qué la publicación no puede ser aprobada...")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(Modifier.height(8.dp))
                Text(
                    "Este motivo será enviado al autor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onReject(reason) },
                        enabled = reason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Rechazar")
                    }
                }
                
                Spacer(Modifier.height(16.dp)) // padding para gestos
            }
        }
    }
}
```

---

## 10. Instrucciones por pantalla — Notificaciones

### 10.1 Pantalla 11: Lista de Notificaciones

**Archivo:** `features/notifications/presentation/NotificationsScreen.kt`  
**Ruta:** `Screen.Notifications`  
**HU relacionada:** HU-06.3

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│ ← Notificaciones                         │
├───────────────────────────────────────────┤
│  Nuevas                                   │  ← Sección header
│  ┌─────────────────────────────────────┐  │
│  │ 🔔 Tu publicación ha sido          │  │  ← Fondo tintado (no leída)
│  │    verificada y ya está pública     │  │
│  │                          Hace 2h    │  │
│  ├─────────────────────────────────────┤  │
│  │ 💬 Santi ha comentado en           │  │
│  │    Max - Golden Retriever           │  │
│  │                          Hace 4h    │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  Ayer                                     │  ← Sección header
│  ┌─────────────────────────────────────┐  │
│  │ ✅ Tu publicación "Luna" fue       │  │  ← Fondo normal (leída)
│  │    aprobada por un moderador        │  │
│  │                          Ayer       │  │
│  ├─────────────────────────────────────┤  │
│  │ 🐾 Nueva publicación de adopción   │  │
│  │    cerca de tu ubicación            │  │
│  │                        Hace 1d      │  │
│  └─────────────────────────────────────┘  │
│                                           │
└───────────────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **Scaffold** con `TopAppBar` incluyendo botón back y título "Notificaciones".
2. **Agrupación:** Agrupar notificaciones por fecha ("Nuevas" = hoy, "Ayer", fecha anterior).
3. **LazyColumn** con `stickyHeader` para cada grupo:

```kotlin
@Composable
fun NotificationsScreen(navController: NavController) {
    val notifications = remember { MockNotifications.sample } // Fase 2
    val grouped = notifications.groupBy { it.dateGroup } // "Nuevas", "Ayer", "12 Feb"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            grouped.forEach { (header, items) ->
                stickyHeader {
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(items) { notification ->
                    NotificationItem(notification, onClick = { /* navegar */ })
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: PetNotification,
    onClick: () -> Unit
) {
    val bgColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    
    ListItem(
        modifier = Modifier
            .background(bgColor)
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2
            )
        },
        supportingContent = {
            Text(notification.body, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        },
        leadingContent = {
            // Avatar circular con ícono según tipo
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    notification.type.icon(), // Extension function
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        trailingContent = {
            Text(
                notification.timeAgo(), // "Hace 2h"
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
```

---

## 11. Instrucciones por pantalla — Perfil y Dashboard

### 11.1 Pantalla 12: Perfil de Gamificación / Dashboard de Puntos

**Archivo:** `features/reputation/presentation/ReputationScreen.kt` + `features/profile/presentation/ProfileScreens.kt`  
**Rutas:** `Screen.Profile`, `Screen.Reputation`  
**HU relacionadas:** HU-01.5, HU-09.1, HU-10.1, HU-10.2, HU-10.3

#### Diseño Figma

```
┌───────────────────────────────────────────┐
│ ← Mi Perfil                  ⚙️          │
├───────────────────────────────────────────┤
│                                           │
│           ╭─────────╮                     │
│           │  👤      │ ← Aro de progreso  │
│           │  AVATAR  │    gradiente        │
│           ╰─────────╯                     │
│                                           │
│         Juan Sebastián                    │  ← headlineMedium
│         👑 Héroe de las Mascotas          │  ← Badge dorado
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │                                     │  │
│  │          🌟 1,450 puntos 🌟         │  │  ← displaySmall
│  │                                     │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌────────────────┐ ┌────────────────┐    │
│  │ Pub. Activas   │ │ Solicitudes    │    │
│  │      4         │ │     12         │    │
│  └────────────────┘ └────────────────┘    │
│                                           │
│  ┌────────────────┐ ┌────────────────┐    │
│  │ Finalizadas    │ │ Pendientes     │    │
│  │      8         │ │     2          │    │
│  └────────────────┘ └────────────────┘    │
│                                           │
│  Tus Insignias                            │
│  ┌──────┐ ┌──────┐ ┌──────┐              │
│  │🏅    │ │🎖️    │ │⭐    │  ← Scroll   │
│  │Volun.│ │10 Pub│ │Pop.  │    horizontal │
│  │Novato│ │Valid.│ │Mes   │              │
│  └──────┘ └──────┘ └──────┘              │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │           Editar Perfil             │  │
│  └─────────────────────────────────────┘  │
│  ┌─────────────────────────────────────┐  │
│  │           Cerrar Sesión             │  │  ← Outlined, error color
│  └─────────────────────────────────────┘  │
│                                           │
└───────────────────────────────────────────┘
```

#### Instrucciones paso a paso

1. **Avatar con aro de progreso:**
   ```kotlin
   @Composable
   private fun ProfileAvatar(
       photoUrl: String?,
       levelProgress: Float, // 0f..1f progreso al siguiente nivel
       modifier: Modifier = Modifier
   ) {
       Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
           // Aro de progreso circular
           CircularProgressIndicator(
               progress = { levelProgress },
               modifier = Modifier.fillMaxSize(),
               strokeWidth = 4.dp,
               color = WarmOrangeDark, // Coral gradiente
               trackColor = MaterialTheme.colorScheme.surfaceVariant
           )
           // Avatar
           AsyncImage(
               model = photoUrl,
               modifier = Modifier
                   .size(100.dp)
                   .clip(CircleShape)
                   .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
               contentScale = ContentScale.Crop
           )
       }
   }
   ```

2. **Badge de nivel:**
   ```kotlin
   @Composable
   private fun LevelBadge(level: UserLevel) {
       Surface(
           shape = RoundedCornerShape(16.dp),
           color = when (level) {
               UserLevel.ANIMAL_FRIEND -> LightGreenContainer
               UserLevel.PROTECTOR -> SoftBlue.copy(alpha = 0.15f)
               UserLevel.GUARDIAN -> WarmOrangeDark.copy(alpha = 0.15f)
               UserLevel.PET_HERO -> Color(0xFFFFD700).copy(alpha = 0.2f) // Dorado
           },
           modifier = Modifier.padding(top = 4.dp)
       ) {
           Text(
               text = "👑 ${level.displayName}",
               style = MaterialTheme.typography.labelLarge,
               modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
           )
       }
   }
   ```

3. **Tarjeta de puntos:**
   ```kotlin
   ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
       Row(
           modifier = Modifier.fillMaxWidth().padding(24.dp),
           horizontalArrangement = Arrangement.Center,
           verticalAlignment = Alignment.CenterVertically
       ) {
           Text("🌟", fontSize = 24.sp)
           Spacer(Modifier.width(8.dp))
           Text(
               "$totalPoints puntos",
               style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
           )
           Spacer(Modifier.width(8.dp))
           Text("🌟", fontSize = 24.sp)
       }
   }
   ```

4. **Grid de estadísticas (2x2):**
   ```kotlin
   @Composable
   private fun StatsGrid(stats: UserStats) {
       Column(modifier = Modifier.padding(horizontal = 16.dp)) {
           Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
               StatCard("Pub. Activas", stats.activePosts, Modifier.weight(1f))
               StatCard("Solicitudes", stats.receivedRequests, Modifier.weight(1f))
           }
           Spacer(Modifier.height(12.dp))
           Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
               StatCard("Finalizadas", stats.finishedPosts, Modifier.weight(1f))
               StatCard("Pendientes", stats.pendingPosts, Modifier.weight(1f))
           }
       }
   }
   
   @Composable
   private fun StatCard(label: String, value: Int, modifier: Modifier) {
       ElevatedCard(modifier = modifier) {
           Column(
               modifier = Modifier.padding(16.dp),
               horizontalAlignment = CenterHorizontally
           ) {
               Text("$value", style = headlineMedium, fontWeight = FontWeight.Bold)
               Text(label, style = labelMedium, color = onSurfaceVariant)
           }
       }
   }
   ```

5. **Carrusel de insignias:**
   ```kotlin
   @Composable
   private fun BadgesCarousel(badges: List<Badge>) {
       Column(modifier = Modifier.padding(start = 16.dp)) {
           Text("Tus Insignias", style = titleMedium)
           Spacer(Modifier.height(12.dp))
           LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
               items(badges) { badge ->
                   BadgeCard(badge)
               }
           }
       }
   }
   
   @Composable
   private fun BadgeCard(badge: Badge) {
       ElevatedCard(
           modifier = Modifier.size(width = 100.dp, height = 120.dp)
       ) {
           Column(
               modifier = Modifier.fillMaxSize().padding(12.dp),
               horizontalAlignment = CenterHorizontally,
               verticalArrangement = Arrangement.Center
           ) {
               Text(badge.icon, fontSize = 32.sp) // Emoji como ícono
               Spacer(Modifier.height(8.dp))
               Text(
                   badge.name,
                   style = labelSmall,
                   textAlign = TextAlign.Center,
                   maxLines = 2
               )
           }
       }
   }
   ```

---

## 12. Navegación y Bottom Navigation Bar

### 12.1 Integración de BottomNav con NavGraph

El `PetHelpBottomNavBar` debe mostrarse solo en las pantallas principales del usuario (no en auth, no en moderación). Modificar `MainActivity.kt` o `PetHelpNavGraph.kt`:

```kotlin
@Composable
fun PetHelpNavGraph(navController: NavHostController = rememberNavController()) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    // Pantallas que muestran el BottomNav
    val bottomNavRoutes = setOf(
        Screen.Feed.route,
        Screen.Notifications.route,
        Screen.Profile.route,
        Screen.Statistics.route,
        Screen.Reputation.route,
    )
    
    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                PetHelpBottomNavBar(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Feed.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ... composable destinations
        }
    }
}
```

### 12.2 Definición de ítems de navegación

```kotlin
enum class BottomNavItem(
    val route: String,
    val screen: Screen,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    HOME(Screen.Feed.route, Screen.Feed, Icons.Outlined.Home, Icons.Filled.Home, "Inicio"),
    MAP(Screen.Feed.route + "?map=true", Screen.Feed, Icons.Outlined.Map, Icons.Filled.Map, "Mapa"),
    CREATE(Screen.CreatePost.route, Screen.CreatePost, Icons.Outlined.Add, Icons.Filled.Add, "Publicar"),
    NOTIFICATIONS(Screen.Notifications.route, Screen.Notifications, Icons.Outlined.Notifications, Icons.Filled.Notifications, "Alertas"),
    PROFILE(Screen.Profile.route, Screen.Profile, Icons.Outlined.Person, Icons.Filled.Person, "Perfil")
}
```

---

## 13. Checklist de implementación por fase

### Fase 1 — Diseño (Mockups Figma) ✅

Los prompts están en `Docs/PromptsFigma.md`. Verificar que cada pantalla del Figma cubre:

- [ ] Splash Screen & Bienvenida
- [ ] Login
- [ ] Registro
- [ ] Recuperación de Contraseña
- [ ] Feed Lista (Home)
- [ ] Feed Mapa
- [ ] Detalle de Publicación
- [ ] Crear Publicación (con IA)
- [ ] Panel de Moderación
- [ ] Diálogo Rechazar Publicación
- [ ] Notificaciones
- [ ] Perfil / Dashboard Gamificación

### Fase 2 — Funcionalidades básicas (datos en memoria)

**Prioridad 1 — Componentes base (hacer primero):**
- [ ] Crear `core/ui/theme/Shape.kt` y agregar al `MaterialTheme`
- [ ] Crear `core/ui/components/PetHelpButton.kt`
- [ ] Crear `core/ui/components/PetHelpTextField.kt`
- [ ] Crear `core/ui/components/PostCard.kt`
- [ ] Crear `core/ui/components/CategoryFilterChips.kt`
- [ ] Crear `core/ui/components/PetHelpBottomNavBar.kt`
- [ ] Crear `core/ui/components/PetAttributeChips.kt`
- [ ] Crear `core/ui/components/CommentSection.kt`
- [ ] Crear `core/ui/components/AIBadge.kt`
- [ ] Crear `core/ui/components/MiniMap.kt` (placeholder gris)

**Prioridad 2 — Pantallas de autenticación:**
- [ ] Implementar `SplashScreen` completo según Figma
- [ ] Implementar `LoginScreen` completo con validaciones
- [ ] Implementar `RegisterScreen` completo con fortaleza de contraseña
- [ ] Implementar `ForgotPasswordScreen` completo
- [ ] Crear datos mock de usuarios (2 users + 1 moderador)
- [ ] Lógica de autenticación en memoria (comparar contra mock)
- [ ] Redirección por rol (USER → Feed, MODERATOR → Panel)

**Prioridad 3 — Feed y exploración:**
- [ ] Implementar `FeedScreen` con vista lista según Figma
- [ ] Crear datos mock de publicaciones (mínimo 6, 2 por categoría)
- [ ] Implementar filtros por categoría (chips funcionales)
- [ ] Integrar `PetHelpBottomNavBar` en el NavGraph
- [ ] Vista mapa placeholder ("Disponible en Fase 3")

**Prioridad 4 — Detalle e interacción:**
- [ ] Implementar `PostDetailScreen` completo según Figma
- [ ] Sistema de comentarios en memoria
- [ ] Botón "Me interesa" / votos con estado toggle
- [ ] Botón "Solicitar Adopción" (solo en categoría Adopción)
- [ ] Mini-mapa placeholder

**Prioridad 5 — CRUD de publicaciones:**
- [ ] Implementar `CreatePostScreen` completo según Figma
- [ ] Selector de imágenes desde galería (sin upload real)
- [ ] Validaciones del formulario
- [ ] IA simulada: preselección de categoría basada en keywords del título
- [ ] Implementar `EditPostScreen` (reutilizar formulario de creación)
- [ ] Eliminar publicación con diálogo de confirmación

**Prioridad 6 — Moderación:**
- [ ] Implementar `ModeratorPanelScreen` con lista de pendientes
- [ ] Resumen IA simulado (textos hardcoded)
- [ ] Botones aprobar/rechazar funcionales (cambio de estado en memoria)
- [ ] Implementar `RejectPostBottomSheet`

**Prioridad 7 — Perfil y gamificación:**
- [ ] Implementar `ProfileScreen` con avatar, stats y badges
- [ ] Implementar `EditProfileScreen` 
- [ ] Implementar `StatisticsScreen` con conteos calculados
- [ ] Implementar `ReputationScreen` con puntos, nivel y badges
- [ ] Lógica de acumulación de puntos en memoria
- [ ] Evaluación de nivel según umbrales de Constants.kt
- [ ] 3 insignias base: "Primera Publicación", "10 Verificadas", "Popular del Mes"

**Prioridad 8 — Notificaciones:**
- [ ] Implementar `NotificationsScreen` con lista agrupada
- [ ] Crear datos mock de notificaciones
- [ ] Marcar leída/no leída (visual)

### Fase 3 — Funcionalidades completas

- [ ] Reemplazar autenticación mock → Firebase Auth
- [ ] Reemplazar datos mock → Firestore
- [ ] Integrar Cloudinary para subida real de imágenes
- [ ] Integrar Coil `AsyncImage` con URLs reales
- [ ] Integrar Google Maps en Feed y Detalle
- [ ] Selector de ubicación con mapa interactivo
- [ ] FCM para notificaciones push
- [ ] Integrar LLM API para funcionalidad de IA elegida
- [ ] `res/values-en/strings.xml` para i18n
- [ ] Funcionalidad adicional no trivial (EP-13)

---

## 14. Convenciones y mejores prácticas para Compose

### 14.1 Estructura de un Screen Composable

```kotlin
/**
 * Estructura estándar de una pantalla en PetHelp.
 * 
 * 1. Parámetros: navController + viewModel (inyectado por Hilt)
 * 2. Colectar estado del ViewModel
 * 3. Scaffold con topBar, bottomBar, FAB según corresponda
 * 4. Contenido principal en Column/LazyColumn
 * 5. Manejar estados: Loading, Error, Empty, Content
 */
@Composable
fun ExampleScreen(
    navController: NavController,
    viewModel: ExampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { /* TopAppBar */ },
        bottomBar = { /* BottomNav si aplica */ }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorContent(message = uiState.error!!, onRetry = { viewModel.onEvent(Retry) })
            }
            else -> {
                // Contenido principal
                ScreenContent(
                    data = uiState.data,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
```

### 14.2 Nombrado de composables

| Tipo | Convención | Ejemplo |
|---|---|---|
| Pantalla completa | `{Feature}Screen` | `FeedScreen`, `LoginScreen` |
| Sección de pantalla | `{Feature}{Seccion}Section` | `PostInfoSection`, `HeroImageSection` |
| Componente reutilizable | `PetHelp{Componente}` | `PetHelpButton`, `PetHelpTextField` |
| Componente de dominio | `{Entidad}Card` / `{Entidad}Item` | `PostCard`, `NotificationItem` |
| Diálogo/Modal | `{Accion}Dialog` / `{Accion}BottomSheet` | `RejectPostBottomSheet`, `DeleteAccountDialog` |

### 14.3 Uso de Preview

Cada composable debe tener al menos una `@Preview`:

```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PostCardPreview() {
    PetHelpTheme {
        PostCard(
            post = MockPosts.samplePosts.first(),
            distanceKm = 2.5,
            onCardClick = {},
            onVoteClick = {}
        )
    }
}
```

### 14.4 Responsiveness

- Usar `Modifier.fillMaxWidth()` en lugar de anchos fijos.
- Usar `Modifier.padding(horizontal = 16.dp)` consistentemente.
- Para grids, usar `LazyVerticalGrid` con `GridCells.Fixed(2)` o `Adaptive(minSize = 150.dp)`.
- Testear en emulador con pantallas de 5" a 6.7".

### 14.5 Accesibilidad

- Siempre incluir `contentDescription` en `Icon` e `Image`.
- Usar `Modifier.semantics { }` para componentes complejos.
- No depender solo del color para transmitir información (acompañar con íconos/texto).

### 14.6 Performance

- Usar `key` en `items()` de `LazyColumn/LazyRow` para evitar re-composiciones innecesarias.
- Usar `remember {}` para cálculos costosos.
- Evitar lambdas anónimas en `onClick` dentro de bucles — usar `remember(id) { { onClick(id) } }`.
- Usar `derivedStateOf` para estados derivados.

---

## Apéndice A — Mapa de pantalla → archivo → épica → ruta

| # | Pantalla Figma | Archivo Kotlin | Épica | Ruta NavGraph |
|---|---|---|---|---|
| 1 | Splash & Bienvenida | `auth/presentation/AuthScreens.kt` → `SplashScreen` | — | `splash` |
| 2 | Login | `auth/presentation/AuthScreens.kt` → `LoginScreen` | EP-01 | `login` |
| 3 | Registro | `auth/presentation/AuthScreens.kt` → `RegisterScreen` | EP-01 | `register` |
| 4 | Recuperar Contraseña | `auth/presentation/AuthScreens.kt` → `ForgotPasswordScreen` | EP-01 | `forgot_password` |
| 5 | Feed Lista (Home) | `feed/presentation/FeedScreen.kt` → `FeedScreen` | EP-03 | `feed` |
| 6 | Feed Mapa | `feed/presentation/FeedScreen.kt` → `FeedMapView` | EP-03, EP-07 | `feed` (toggle) |
| 7 | Detalle Publicación | `post/presentation/PostScreens.kt` → `PostDetailScreen` | EP-03, EP-05 | `post_detail/{postId}` |
| 8 | Crear Publicación | `post/presentation/PostScreens.kt` → `CreatePostScreen` | EP-02, EP-08, EP-11 | `create_post` |
| 9 | Panel Moderación | `moderation/presentation/ModerationScreens.kt` → `ModeratorPanelScreen` | EP-04 | `moderator_panel` |
| 10 | Rechazar (Modal) | `moderation/presentation/ModerationScreens.kt` → `RejectPostBottomSheet` | EP-04 | — (modal) |
| 11 | Notificaciones | `notifications/presentation/NotificationsScreen.kt` | EP-06 | `notifications` |
| 12 | Perfil / Dashboard | `profile/presentation/ProfileScreens.kt` + `reputation/ReputationScreen.kt` | EP-09, EP-10 | `profile` / `reputation` |
| — | Editar Publicación | `post/presentation/PostScreens.kt` → `EditPostScreen` | EP-02 | `edit_post/{postId}` |
| — | Editar Perfil | `profile/presentation/ProfileScreens.kt` → `EditProfileScreen` | EP-01 | `edit_profile` |
| — | Estadísticas | `stats/presentation/StatisticsScreen.kt` | EP-09 | `statistics` |
| — | Detalle Moderador | `moderation/presentation/ModerationScreens.kt` → `ModeratorDetailScreen` | EP-04 | `moderator_detail/{postId}` |

---

## Apéndice B — Imports frecuentes para copiar

```kotlin
// Material 3
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// Compose Foundation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

// Compose UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Compose Runtime
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Navigation
import androidx.navigation.NavController

// Coil (Fase 3)
import coil.compose.AsyncImage

// Proyecto
import com.pethelp.app.core.ui.theme.*
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.core.navigation.Screen
```

---

*Documento generado a partir del análisis del proyecto PetHelp: diseño Figma (PromptsFigma.md), épicas (Epicas.md), plan de desarrollo (PlanDesarrollo.md) y código fuente actual.*
