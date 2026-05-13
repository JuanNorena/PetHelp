<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-4285F4?logo=android&logoColor=white" alt="Compose">
  <img src="https://img.shields.io/badge/Material%20Design%203-BOM-757575?logo=materialdesign&logoColor=white" alt="Material3">
  <img src="https://img.shields.io/badge/Firebase-BOM%2034.9.0-FFCA28?logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/minSdk-30%20(Android%2011)-3DDC84?logo=android&logoColor=white" alt="minSdk">
  <img src="https://img.shields.io/badge/targetSdk-35%20(Android%2015)-3DDC84?logo=android&logoColor=white" alt="targetSdk">
</p>

<h1 align="center">🐾 PetHelp</h1>
<p align="center"><b>Red social móvil para adopción, búsqueda y rescate de mascotas</b></p>

> Aplicación Android desarrollada con **Jetpack Compose**, **Kotlin** y **Firebase** como proyecto final de la materia *Diseño y Desarrollo de Aplicaciones Móviles* — **Universidad del Quindío · 2026-1**.

---

## 👥 Equipo de desarrollo

| Nombre | Rol | Contribuciones principales |
|---|---|---|
| **Juan Sebastián Noreña Espinosa** | Desarrollador Full-Stack | Arquitectura MVVM, Firebase, notificaciones, IA, bottom nav premium |
| **Santiago Londoño Gaviria** | Desarrollador | UI/UX, perfil, gamificación, mapas, chat |
| **Diego Alejandro López** | Desarrollador | Autenticación, publicaciones, moderación, documentación |

**Materia:** Diseño y Desarrollo de Aplicaciones Móviles  
**Semestre:** 2026-1  
**Universidad:** Universidad del Quindío — Ingeniería de Sistemas

---

## 📋 Descripción del proyecto

PetHelp es una plataforma móvil que conecta a personas que desean dar mascotas en adopción con personas que buscan adoptar, y permite reportar mascotas perdidas o encontradas. La aplicación aborda el problema del abandono animal mediante una comunidad **georreferenciada**, con **moderación de contenido**, **inteligencia artificial integrada** y un **sistema de notificaciones en tiempo real** que mantiene a los usuarios informados de cada interacción importante.

### Categorías de publicación

| Categoría | Icono | Descripción |
|---|---|---|
| 🐶 **Adopción** | `Pets` | Mascotas disponibles para adoptar |
| 🔍 **Perdidos** | `Search` | Mascotas extraviadas reportadas por sus dueños |
| 📍 **Encontrados** | `LocationOn` | Mascotas encontradas en la calle esperando reunificación |
| 🏠 **Hogar temporal** | `Home` | Hogares de paso para mascotas en transición |
| 💉 **Veterinaria** | `LocalHospital` | Jornadas de vacunación y esterilización gratuitas |

---

## 🏗️ Arquitectura

PetHelp sigue una arquitectura **Single-Activity + Jetpack Compose + MVVM** con organización por *features* y una clara separación de responsabilidades.

### Diagrama de flujo

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────────┐
│   Compose UI    │────▶│    ViewModel    │────▶│   Repository    │────▶│  Firebase / APIs    │
│  (Screens +     │◄────│  (StateFlow +   │◄────│  (Domain +      │◄────│  (Auth, Firestore,  │
│   Components)   │     │   SharedFlow)   │     │   Data Impl)    │     │   FCM, Maps, AI)    │
└─────────────────┘     └─────────────────┘     └─────────────────┘     └─────────────────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  Theme / Design │     │   Hilt (DI)     │
│  System (MD3)   │     │                 │
└─────────────────┘     └─────────────────┘
```

### Capas del proyecto

| Capa | Responsabilidad | Tecnologías clave |
|---|---|---|
| **Presentación** | Pantallas Compose, ViewModels, UiState, eventos puntuales | `StateFlow`, `SharedFlow`, `Compose`, `Navigation Compose` |
| **Dominio** | Modelos de datos, contratos de repositorio, lógica de negocio pura | `data class`, `interface Repository`, `sealed class` |
| **Datos** | Implementaciones concretas de Firebase, autenticación, imágenes, notificaciones | `FirebaseFirestore`, `FirebaseAuth`, `Cloudinary`, `FCM` |
| **Compartido** | Tema, navegación, seguridad, preferencias, utilidades | `MaterialTheme`, `DataStore`, `BiometricPrompt`, `Security` |

### Decisiones arquitectónicas clave

- **MVVM:** La UI solo observa estado; la lógica de negocio vive en el ViewModel.
- **Feature-based structure:** Cada módulo agrupa `data/`, `domain/` y `presentation/` para mantener cohesión alta y acoplamiento bajo.
- **Hilt:** Inyección de dependencias para ViewModels, repositorios y servicios (singletones).
- **StateFlow + SharedFlow:** Estado persistente reactivo + eventos de una sola vez (snackbar, navegación).
- **Firebase + servicios externos:** Autenticación, base de datos en tiempo real, notificaciones push, mapas e imágenes.

---

## 📁 Estructura del proyecto

```
PetHelp/
├── app/src/main/java/com/pethelp/app/
│   ├── MainActivity.kt                    # Punto de entrada Single-Activity
│   ├── PetHelpApplication.kt              # Application + Hilt inicialización
│   ├── core/
│   │   ├── common/                        # Constantes, Resource<T>, utilidades
│   │   ├── di/                            # Módulos Hilt (Auth, Post, AI, etc.)
│   │   ├── domain/                        # Modelos: Post, User, Notification, Comment
│   │   ├── navigation/                    # NavGraph type-safe (Screen sealed class)
│   │   ├── notifications/                 # FCM: PetHelpMessagingService, FcmTokenSyncManager
│   │   ├── preferences/                   # DataStore: idioma, radio de notificaciones
│   │   ├── security/                      # BiometricAuthGate, EncryptedPrefs
│   │   ├── ui/
│   │   │   ├── components/                # PetHelpBottomNavBar (premium glassmorphism)
│   │   │   └── theme/                     # Color.kt, Theme.kt, Type.kt (Material3)
│   │   └── util/                          # Extension functions, formatters
│   └── features/
│       ├── auth/                          # Login, Register, ForgotPassword, Terms
│       ├── feed/                          # FeedScreen con filtros y búsqueda
│       ├── post/                          # CreatePost, PostDetail, EditPost, Moderation
│       ├── map/                           # MapScreen con geolocalización real
│       ├── chat/                          # ChatList, ChatThread con mensajes en tiempo real
│       ├── notifications/                 # NotificationsScreen con badge unread
│       ├── profile/                       # Profile, Settings, Security, Language, Privacy
│       ├── moderation/                    # ModerationQueue, PostReviewScreen
│       ├── reputation/                    # Leaderboard, points system
│       ├── gamification/                  # Badges, levels, missions, confetti
│       ├── stats/                         # GlobalMetrics, admin dashboard
│       └── ai/                            # AIQuizScreen, AIResultsScreen, AiChat
├── functions/src/index.ts                 # Cloud Functions: 13 triggers de notificaciones
├── Docs/                                  # Documentación del proyecto
└── local.properties.example               # Plantilla de configuración
```

---

## 🛠️ Stack tecnológico

| Tecnología | Versión | Rol en el proyecto |
|---|---|---|
| Kotlin | 2.0.21 | Lenguaje principal |
| Jetpack Compose BOM | 2024.12.01 | UI declarativa — requerida por el enunciado |
| Material3 | BOM | Sistema de diseño Material You |
| Hilt | 2.52 | Inyección de dependencias oficial de Android |
| Navigation Compose | 2.8.4 | Navegación type-safe en arquitectura Single-Activity |
| Firebase BOM | 34.9.0 | Auth, Firestore, FCM, AI Logic (Gemini) |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 | Llamadas HTTP a la API del LLM (fallback NVIDIA) |
| Coroutines | 1.9.0 | Programación asíncrona y reactive streams |
| Coil | 2.7.0 | Carga de imágenes optimizada para Compose |
| Cloudinary Android | 2.6.0 | Almacenamiento externo de imágenes |
| Maps Compose | 6.2.1 | Google Maps en Jetpack Compose |
| Play Services Location | 21.3.0 | GPS del dispositivo con alta precisión |
| Room | 2.6.1 | Caché local + datos en memoria (Fase 2) |
| DataStore Preferences | 1.1.1 | Preferencias del usuario (radio, idioma) |
| KSP | 2.0.21-1.0.27 | Procesador de anotaciones (Hilt + Room) |
| MockK | 1.13.12 | Testing — mocking idiomático para Kotlin |
| Turbine | 1.2.0 | Testing de Flow y StateFlow |
| TypeScript / Node | 20.x | Cloud Functions (Firebase Functions v2) |

---

## ✨ Funcionalidades implementadas

### 🔐 Autenticación

- **Splash screen** con navegación automática según estado de sesión.
- **Inicio de sesión** (email + contraseña) con validaciones locales y errores de Firebase mapeados a español.
- **Registro** con nombre, correo, contraseña, confirmación e indicador de fortaleza.
- **Recuperación de contraseña** por email — protegida contra enumeración de usuarios.
- **Términos y Condiciones** con modal scrollable que cumple legislación colombiana (Ley 1581/2012, Ley 1273/2009, Ley 84/1989, Ley 1774/2016).
- **Persistencia de usuario** en Firestore al registrarse (nombre, email, rol, fecha de creación, tokens FCM).
- **Autenticación biométrica** con `BiometricAuthGate` para acciones sensibles.

### 📰 Feed y publicaciones

- **FeedScreen** con lista de publicaciones en tiempo real, filtros por categoría y búsqueda por texto.
- **CreatePostScreen** con selector de hasta 5 fotos, campos de título/descripción, chips de tipo/tamaño, y sugerencia de categoría con **IA (Gemini)**.
- **PostDetailScreen** con imagen principal (Coil + gradiente overlay), tarjeta informativa 2×2, sistema de votos/favoritos con transacciones atómicas, solicitud de adopción, comentarios en tiempo real y sección de ubicación.
- **EditPostScreen** para modificar publicaciones existentes.
- **PostReviewScreen** para moderadores: aprobar/rechazar con motivo.

### 💬 Chat en tiempo real

- **ChatListScreen** con lista de conversaciones, último mensaje y contador de no leídos.
- **ChatThreadScreen** con mensajes en tiempo real via Firestore, envío de texto, y creación automática de threads al aceptar una adopción.
- **Notificaciones locales** cuando hay mensajes nuevos y el usuario no está en la pantalla de chat.

### 🗺️ Mapas y geolocalización

- **MapScreen** con Google Maps en Compose, marcadores de publicaciones con coordenadas.
- **Geolocalización real** usando `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY)` con fallback a `lastLocation`.
- **Cálculo de distancia** para notificaciones de posts cercanos (Cloud Functions + Haversine).

### 🤖 Inteligencia artificial

- **AIQuizScreen** — Quiz interactivo para recomendar mascotas. Preguntas dinámicas con animaciones y opciones ilustradas.
- **AIResultsScreen** — Recomendaciones basadas en el quiz, filtrando solo posts `VERIFIED` y observando cambios en tiempo real vía `PostRepository`.
- **AiChat** — Chat con asistente de IA. Proveedor primario: **Firebase AI Logic (Gemini)**. Fallback directo a **NVIDIA NIM** (`meta/llama-4-maverick-17b-128e-instruct`) cuando Gemini falla.

### 🔔 Sistema de notificaciones (13 eventos)

| Evento | Tipo | Destinatario | Canal |
|--------|------|-------------|-------|
| Nuevo comentario en tu post | `NEW_COMMENT` | Autor del post | Push + In-app |
| Nuevo mensaje en chat | `NEW_MESSAGE` | Destinatario(s) | Push + In-app |
| Nueva solicitud de adopción | `ADOPTION_REQUEST_RECEIVED` | Autor del post | Push + In-app |
| Solicitud aceptada | `ADOPTION_REQUEST_ACCEPTED` | Solicitante | Push + In-app |
| Solicitud rechazada | `ADOPTION_REQUEST_REJECTED` | Solicitante | Push + In-app |
| **¡Mascota adoptada!** | **`ADOPTION_COMPLETED`** | **Autor del post** | **Push + In-app** |
| Post aprobado por moderador | `POST_APPROVED` | Autor del post | Push + In-app |
| Post rechazado por moderador | `POST_REJECTED` | Autor del post | Push + In-app |
| Nuevo post cerca de ti | `NEW_POST_NEARBY` | Usuarios con `alertsNearMe` | Push + In-app |
| **Nuevo voto en tu post** | **`NEW_VOTE`** | **Autor del post** | **Push + In-app** |
| **Nuevo favorito** | **`NEW_FAVORITE`** | **Autor del post** | **Push + In-app** |
| **Nueva insignia desbloqueada** | **`NEW_BADGE`** | **Usuario** | **Push + In-app** |
| **Subida de nivel** | **`LEVEL_UP`** | **Usuario** | **Push + In-app** |

**Arquitectura:** Cloud Functions (TypeScript) → crean documento en `notifications` → envían push FCM multicast → `PetHelpMessagingService` recibe y muestra notificación local. Las notificaciones in-app se observan en tiempo real desde Firestore.

### 🎨 UI/UX premium

- **Bottom Navigation Bar rediseñada** con estilo **glassmorphism**:
  - Fondo semitransparente (`alpha = 0.72f`) con sombra difusa de 8dp.
  - **Notch central** simulado con círculo del color de fondo.
  - **FAB circular** con gradiente horizontal `primary (turquesa) → secondary (naranja)`, borde blanco semitransparente y sombra pronunciada.
  - **Ítems animados:** iconos outline/filled, escala 1.0→1.15x, glow sutil (`primary.copy(alpha = 0.10f)`), indicador de punto centrado, etiquetas con opacidad animada, y badges de chat con sombra.
- **Tarjetas de post modernas** con bordes redondeados, badges de categoría, chips de información y elevación suave.
- **Animaciones** con `spring(stiffness = 300f, dampingRatio = 0.6f)` en escalas, colores y opacidades.
- **Soporte de idiomas:** Español e Inglés. Localización completa de strings, content descriptions y textos de pantalla.

### 🎮 Gamificación

- **Sistema de puntos** por acciones: crear post (+10), comentar (+5), recibir voto (+3), post verificado (+15), adopción aceptada (+30).
- **Niveles:** Amigo Animal → Protector → Guardián → Héroe de las Mascotas.
- **Insignias** desbloqueadas por hitos.
- **Confetti** al completar misiones.

### 🛡️ Moderación y administración

- **ModerationQueue** para revisar posts pendientes.
- **GlobalMetrics** con estadísticas de uso.
- **Reputación y leaderboard** con ranking de usuarios.

---

## 🚀 Configuración del entorno

### Prerrequisitos
- Android Studio Ladybug (2024.2.x) o superior
- JDK 17
- SDK Android 35
- Node.js 20+ (para Cloud Functions)
- Emulador o dispositivo con Android 11 (API 30) o superior

### 1. Clonar el repositorio
```bash
git clone https://github.com/TU_ORG/PetHelp.git
cd PetHelp
```

### 2. Crear `local.properties`
```bash
cp local.properties.example local.properties
```
Completar con valores reales:
```properties
sdk.dir=C:\Users\TU_USUARIO\AppData\Local\Android\Sdk
MAPS_API_KEY=AIzaSy...
CLOUDINARY_CLOUD_NAME=...
NVIDIA_API_KEY=nvapi-...
NVIDIA_BASE_URL=https://integrate.api.nvidia.com/v1/
NVIDIA_MODEL=meta/llama-4-maverick-17b-128e-instruct
```

### 3. Configurar Firebase
1. Crear proyecto en [Firebase Console](https://console.firebase.google.com).
2. Agregar app Android con package `com.pethelp.app`.
3. Descargar `google-services.json` → ubicar en `app/`.
4. Habilitar: **Authentication** (Email/Password), **Firestore**, **Cloud Messaging**, **AI Logic (Gemini)**.

### 4. Configurar Google Maps
1. Ir a [Google Cloud Console](https://console.cloud.google.com).
2. Habilitar: **Maps SDK for Android** + **Geocoding API**.
3. Crear API Key restringida al package `com.pethelp.app`.

### 5. Configurar Cloudinary
1. Crear cuenta en [Cloudinary](https://cloudinary.com).
2. Copiar el Cloud Name a `local.properties`.
3. Crear un upload preset sin firma para uso mobile.

### 6. Compilar Android
```bash
./gradlew assembleDebug
```

### 7. Desplegar Cloud Functions (opcional, para notificaciones push)
```bash
cd functions
npm install
npm run build
firebase deploy --only functions
```

---

## 🌿 Flujo de trabajo Git

| Rama | Propósito |
|---|---|
| `main` | Código estable — entregable por fase |
| `develop` | Integración continua del equipo |
| `feature/nombre` | Desarrollo de cada historia de usuario |
| `fix/descripcion` | Corrección de errores |

**Formato de commits (Conventional Commits):**
```
feat(auth): implementar pantalla de registro con validaciones
fix(feed): corregir filtro por categoría
chore(deps): actualizar Firebase BOM a 34.9.0
docs(readme): agregar sección de notificaciones
```

---

## 📅 Fases del proyecto

| Fase | Entregable | Estado |
|---|---|---|
| Fase 1 — Diseño | Mockups en Figma (Material You) | ✅ Completada |
| Fase 2 — Básico | App funcional con datos en memoria + Room | ✅ Completada |
| Fase 3 — Completo | Firebase, mapas, IA, i18n, imágenes, notificaciones, gamificación | ✅ Completada |

---

## 📄 Documentación del proyecto

| Documento | Descripción |
|---|---|
| [Docs/Enunciado.md](Docs/Enunciado.md) | Requisitos completos del proyecto |
| [Docs/Epicas.md](Docs/Epicas.md) | Épicas e historias de usuario (método INVEST) |
| [Docs/PlanDesarrollo.md](Docs/PlanDesarrollo.md) | Plan técnico, librerías y convenciones de código |
| [Docs/GuiaVistas.md](Docs/GuiaVistas.md) | Catálogo de pantallas y flujos de navegación |
| [Docs/PromptsFigma.md](Docs/PromptsFigma.md) | Guía de diseño y prompts usados para Figma |
| [Docs/EstadoDocumentacion.md](Docs/EstadoDocumentacion.md) | Cobertura de documentación KDoc del código |

---

## 🔑 Servicios externos

| Servicio | Propósito | Enlace |
|---|---|---|
| Firebase | Auth, Firestore, FCM, AI Logic (Gemini) | [console.firebase.google.com](https://console.firebase.google.com) |
| Google Cloud | Maps SDK for Android, Geocoding API | [console.cloud.google.com](https://console.cloud.google.com) |
| Cloudinary | Almacenamiento y CDN de imágenes | [cloudinary.com](https://cloudinary.com) |
| NVIDIA NIM | Fallback de IA (Llama 4 Maverick) | [integrate.api.nvidia.com](https://integrate.api.nvidia.com) |

> ⚠️ **Ninguna clave de API debe subirse al repositorio.** Usar siempre `local.properties` (ignorado por `.gitignore`).

---

<p align="center">
  <sub>Universidad del Quindío · Ingeniería de Sistemas · Diseño y Desarrollo de Aplicaciones Móviles · 2026</sub>
</p>
