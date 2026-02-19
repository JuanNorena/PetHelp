# Plan de Desarrollo — PetHelp
> Red de adopción de mascotas · Android · Kotlin · Jetpack Compose  
> Versión: 1.0 | Fecha: Febrero 2026

---

## 1. Información general del proyecto

| Campo | Detalle |
|---|---|
| Nombre | PetHelp |
| Plataforma | Android |
| Lenguaje | Kotlin |
| UI Toolkit | Jetpack Compose |
| Arquitectura | Clean Architecture + MVVM |
| BDD | Firebase Firestore |
| Autenticación | Firebase Auth |
| Notificaciones | Firebase Cloud Messaging (FCM) |
| Almacenamiento de imágenes | Cloudinary |
| Mapas | Google Maps SDK + Maps Compose |
| Inyección de dependencias | Hilt |
| Min SDK | 30 (Android 11 — Red Velvet Cake) |
| Target SDK | 35 (Android 15) |

---

## 2. Arquitectura del proyecto

### 2.1 Patrón: Clean Architecture + MVVM

```
┌──────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                 │
│  Compose Screens ←→ ViewModel ←→ UIState (StateFlow) │
└─────────────────────────┬────────────────────────────┘
                          │ llama a
┌─────────────────────────▼────────────────────────────┐
│                    DOMAIN LAYER                      │
│  UseCases (lógica de negocio) + Interfaces Repos     │
└─────────────────────────┬────────────────────────────┘
                          │ implementado por
┌─────────────────────────▼────────────────────────────┐
│                     DATA LAYER                       │
│  Repositories (impl) + Firebase/Room/API DataSources │
└──────────────────────────────────────────────────────┘
```

**Regla fundamental:** las capas superiores nunca conocen las inferiores; solo dependen de interfaces (inversión de dependencia). Hilt resuelve las implementaciones en tiempo de compilación.

### 2.2 Flujo de datos por feature

```
UI Event → ViewModel.onAction() → UseCase.invoke() → Repository.method()
        ← UIState (Loading/Success/Error) ← Resource<T> ← Firestore/Room
```

### 2.3 Gestión de estado en ViewModel

```kotlin
// Patrón estándar en todos los ViewModels del proyecto
data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
}
```

---

## 3. Estructura de carpetas del proyecto

```
PetHelp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pethelp/app/
│   │   │   │   ├── MainActivity.kt               ← Single Activity
│   │   │   │   ├── PetHelpApplication.kt         ← @HiltAndroidApp
│   │   │   │   │
│   │   │   │   ├── core/                         ← Código compartido entre features
│   │   │   │   │   ├── common/
│   │   │   │   │   │   ├── Resource.kt           ← sealed class Loading/Success/Error
│   │   │   │   │   │   └── Constants.kt          ← Constantes globales
│   │   │   │   │   ├── di/                       ← Módulos Hilt
│   │   │   │   │   │   ├── FirebaseModule.kt
│   │   │   │   │   │   └── NetworkModule.kt
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   └── model/                ← Modelos de dominio
│   │   │   │   │   │       ├── Post.kt
│   │   │   │   │   │       ├── User.kt
│   │   │   │   │   │       ├── Comment.kt
│   │   │   │   │   │       └── PetNotification.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── Screen.kt             ← Rutas (sealed class)
│   │   │   │   │   │   └── PetHelpNavGraph.kt    ← NavHost central
│   │   │   │   │   ├── notifications/
│   │   │   │   │   │   └── PetHelpMessagingService.kt ← FCM Service
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── theme/
│   │   │   │   │       │   ├── Color.kt
│   │   │   │   │       │   ├── Theme.kt
│   │   │   │   │       │   └── Type.kt
│   │   │   │   │       └── components/           ← Composables reutilizables
│   │   │   │   │
│   │   │   │   └── features/                     ← Una carpeta por feature
│   │   │   │       ├── auth/
│   │   │   │       │   ├── data/
│   │   │   │       │   │   ├── dto/              ← UserDto (Firebase → domain)
│   │   │   │       │   │   └── repository/       ← AuthRepositoryImpl
│   │   │   │       │   ├── domain/
│   │   │   │       │   │   ├── repository/       ← AuthRepository (interface)
│   │   │   │       │   │   └── usecase/          ← LoginUseCase, RegisterUseCase...
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── AuthScreens.kt
│   │   │   │       │       └── AuthViewModel.kt
│   │   │   │       │
│   │   │   │       ├── feed/
│   │   │   │       │   ├── data/
│   │   │   │       │   │   └── repository/       ← FeedRepositoryImpl
│   │   │   │       │   ├── domain/
│   │   │   │       │   │   ├── repository/       ← FeedRepository (interface)
│   │   │   │       │   │   └── usecase/          ← GetPostsUseCase, FilterPostsUseCase
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── FeedScreen.kt
│   │   │   │       │       └── FeedViewModel.kt
│   │   │   │       │
│   │   │   │       ├── post/
│   │   │   │       │   ├── data/
│   │   │   │       │   │   ├── dto/              ← PostDto
│   │   │   │       │   │   └── repository/       ← PostRepositoryImpl
│   │   │   │       │   ├── domain/
│   │   │   │       │   │   ├── repository/       ← PostRepository (interface)
│   │   │   │       │   │   └── usecase/          ← CreatePostUseCase, VotePostUseCase...
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── PostScreens.kt
│   │   │   │       │       └── PostViewModel.kt
│   │   │   │       │
│   │   │   │       ├── moderation/
│   │   │   │       │   ├── data/repository/
│   │   │   │       │   ├── domain/               ← ApprovePostUseCase, RejectPostUseCase
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── ModerationScreens.kt
│   │   │   │       │       └── ModerationViewModel.kt
│   │   │   │       │
│   │   │   │       ├── notifications/
│   │   │   │       │   ├── data/repository/
│   │   │   │       │   ├── domain/               ← GetNotificationsUseCase
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── NotificationsScreen.kt
│   │   │   │       │       └── NotificationsViewModel.kt
│   │   │   │       │
│   │   │   │       ├── profile/
│   │   │   │       │   ├── data/repository/
│   │   │   │       │   ├── domain/               ← UpdateProfileUseCase, DeleteAccountUseCase
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── ProfileScreens.kt
│   │   │   │       │       └── ProfileViewModel.kt
│   │   │   │       │
│   │   │   │       ├── stats/
│   │   │   │       │   ├── data/repository/
│   │   │   │       │   ├── domain/               ← GetUserStatsUseCase
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── StatisticsScreen.kt
│   │   │   │       │       └── StatisticsViewModel.kt
│   │   │   │       │
│   │   │   │       ├── reputation/
│   │   │   │       │   ├── data/repository/
│   │   │   │       │   ├── domain/               ← AddPointsUseCase, CheckBadgesUseCase
│   │   │   │       │   └── presentation/
│   │   │   │       │       ├── ReputationScreen.kt
│   │   │   │       │       └── ReputationViewModel.kt
│   │   │   │       │
│   │   │   │       └── ai/
│   │   │   │           ├── data/                 ← Llamadas a la API del LLM
│   │   │   │           ├── domain/               ← SuggestCategoryUseCase (u otro según elección)
│   │   │   │           └── presentation/         ← Integrado en CreatePost/Moderation
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml               ← i18n base (español)
│   │   │   │   │   ├── themes.xml
│   │   │   │   │   └── colors.xml
│   │   │   │   └── values-en/
│   │   │   │       └── strings.xml               ← i18n inglés (Fase 3)
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/                                 ← Unit tests (JUnit + MockK)
│   │   └── androidTest/                          ← UI tests (Compose Test)
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
│   └── libs.versions.toml                        ← Catálogo central de versiones
│
├── build.gradle.kts                              ← Plugins del proyecto
├── settings.gradle.kts                           ← Nombre + módulos
├── .gitignore
├── local.properties.example                     ← Plantilla de claves (sin subir)
└── Docs/
    ├── Enunciado.md
    ├── Epicas.md
    └── PlanDesarrollo.md                        ← Este archivo
```

---

## 4. Librerías utilizadas y justificación

| Librería | Versión | Justificación |
|---|---|---|
| Jetpack Compose BOM | 2024.12.01 | BOM garantiza compatibilidad entre todos los artefactos Compose |
| Material3 | (BOM) | Sistema de diseño Material You requerido por el enunciado |
| Hilt | 2.51.1 | Inyección de dependencias oficial de Android + integración Compose |
| Navigation Compose | 2.8.4 | Navegación type-safe en Single-Activity Architecture |
| Firebase Auth | (BOM 33.7.0) | Autenticación + recuperación de contraseña por email |
| Firebase Firestore | (BOM) | Base de datos NoSQL en tiempo real — publicaciones, usuarios, comentarios |
| Firebase Storage | (BOM) | Almacenamiento alternativo de archivos (respaldo) |
| Firebase Messaging | (BOM) | Notificaciones push en tiempo real (FCM) |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 | Llamadas HTTP a la API del LLM (OpenAI/Gemini) |
| Coroutines | 1.9.0 | Programación asíncrona; integración native con Compose y Firebase |
| StateFlow / Flow | (stdlib Kotlin) | Gestión de estado reactivo en ViewModels |
| Coil | 2.7.0 | Carga de imágenes asíncrona con caché — optimizado para Compose |
| Cloudinary Android | 2.6.0 | Subida de imágenes al servicio externo requerido por el enunciado |
| Maps Compose | 6.2.1 | Wrapper oficial de Google Maps para Jetpack Compose |
| Play Services Maps | 19.0.0 | SDK base de Google Maps |
| Play Services Location | 21.3.0 | Obtener ubicación GPS del dispositivo |
| Room | 2.6.1 | Caché local + datos en memoria para Fase 2 |
| DataStore Preferences | 1.1.1 | Almacenamiento de preferencias del usuario (radio de notificaciones, idioma) |
| Accompanist Permissions | 0.36.0 | Manejo declarativo de permisos en Compose |
| Core SplashScreen | 1.0.1 | Implementación de Splash screen con API oficial de Android 12+ |
| KSP | 2.0.21-1.0.27 | Procesador de anotaciones para Hilt y Room (más rápido que KAPT) |
| MockK | 1.13.12 | Testing: mocking idiomático para Kotlin |
| Turbine | 1.2.0 | Testing: simplifica pruebas de Flow/StateFlow |

---

## 5. Fases del proyecto y tareas detalladas

### Fase 1 — Diseño (Mockups)

**Objetivo:** producir los mockups de todas las pantallas en Figma siguiendo Material You.

| Tarea | Descripción |
|---|---|
| F1-01 | Definir paleta de colores, tipografía, espaciado y componentes en Figma |
| F1-02 | Mockup: Splash screen |
| F1-03 | Mockup: Login + Registro + Recuperar contraseña |
| F1-04 | Mockup: Feed (lista) con filtros de categoría y ubicación |
| F1-05 | Mockup: Feed (mapa) con marcadores |
| F1-06 | Mockup: Detalle de publicación (imágenes, mapa, comentarios) |
| F1-07 | Mockup: Crear / Editar publicación |
| F1-08 | Mockup: Panel de moderación (lista pendientes + detalle) |
| F1-09 | Mockup: Perfil del usuario + Editar perfil |
| F1-10 | Mockup: Dashboard de estadísticas |
| F1-11 | Mockup: Pantalla de reputación, niveles e insignias |
| F1-12 | Mockup: Lista de notificaciones |
| F1-13 | Mockup: Flujo de IA seleccionado (clasificación / validación / etc.) |

---

### Fase 2 — Funcionalidades básicas (datos en memoria)

**Objetivo:** app funcional con navegación completa, toda la lógica de negocio y datos mock (sin Firebase ni servicios externos).

| Tarea | Épica | Descripción |
|---|---|---|
| F2-01 | EP-01 | Registro e inicio de sesión — datos en memoria, con validaciones |
| F2-02 | EP-01 | Lógica de roles: redirigir a feed (USER) o panel moderación (MODERATOR) |
| F2-03 | EP-02 | CRUD completo de publicaciones en memoria |
| F2-04 | EP-02 | Formulario de publicación con todos los campos del enunciado |
| F2-05 | EP-03 | Feed en vista lista con tarjetas de publicación |
| F2-06 | EP-03 | Filtros por categoría y ubicación (lógica sobre lista en memoria) |
| F2-07 | EP-03 | Pantalla de detalle de publicación completa |
| F2-08 | EP-04 | Panel de moderación: listar pendientes, aprobar, rechazar con motivo |
| F2-09 | EP-05 | Sistema de comentarios en publicaciones |
| F2-10 | EP-05 | Botón de votos "Me interesa" con conteo |
| F2-11 | EP-05 | Botón "Me interesa adoptar" (solo en categoría Adopción) |
| F2-12 | EP-09 | Dashboard de estadísticas (conteos calculados en memoria) |
| F2-13 | EP-10 | Acumulación de puntos (lógica en memoria) y 4 niveles de usuario |
| F2-14 | EP-10 | Insignias: criterios definidos y otorgados al cumplirse |
| F2-15 | EP-01 | Editar perfil y eliminar cuenta |
| F2-16 | EP-02 | Cambio de estado a Resuelta/Finalizada (por usuario y por moderador) |

---

### Fase 3 — Funcionalidades completas

**Objetivo:** integrar todos los servicios externos, persistencia real, mapas, notificaciones, internacionalización e IA.

#### 3.1 Autenticación y persistencia

| Tarea | Épica | Descripción |
|---|---|---|
| F3-01 | EP-01 | Integrar Firebase Auth: registro, login, logout, recuperación de contraseña |
| F3-02 | EP-01 | Persistir y leer datos de usuario en Firestore |
| F3-03 | EP-01 | Precarga de cuentas moderador en Firestore (script o seed manual) |

#### 3.2 Publicaciones y comentarios

| Tarea | Épica | Descripción |
|---|---|---|
| F3-04 | EP-02 | CRUD de publicaciones en Firestore |
| F3-05 | EP-05 | Comentarios persistidos en Firestore; notificación al autor al comentar |
| F3-06 | EP-05 | Votos y solicitudes de adopción persistidos en Firestore |

#### 3.3 Imágenes

| Tarea | Épica | Descripción |
|---|---|---|
| F3-07 | EP-08 | Integrar Cloudinary: subida de imágenes y almacenamiento de URLs en Firestore |
| F3-08 | EP-08 | Carga de imágenes con Coil (caché + placeholder + error) |

#### 3.4 Mapas y geolocalización

| Tarea | Épica | Descripción |
|---|---|---|
| F3-09 | EP-07 | Obtener ubicación del dispositivo (permisos en runtime) |
| F3-10 | EP-07 | Selector de ubicación en mapa (Google Maps) al crear/editar publicación |
| F3-11 | EP-03 | Vista de mapa en el feed con marcadores por publicación |
| F3-12 | EP-03 | Filtro por radio de cercanía usando Haversine o GeoHash |

#### 3.5 Notificaciones

| Tarea | Épica | Descripción |
|---|---|---|
| F3-13 | EP-06 | Integrar FCM: guardar token del dispositivo en Firestore |
| F3-14 | EP-06 | Enviar push al autor cuando alguien comenta |
| F3-15 | EP-06 | Enviar push a usuarios que tengan publicaciones nuevas en su zona |
| F3-16 | EP-06 | Lista de notificaciones leídas/no leídas en la app |

#### 3.6 Inteligencia Artificial

| Tarea | Épica | Descripción |
|---|---|---|
| F3-17 | EP-11 | Integrar API del LLM elegido (OpenAI / Gemini / Claude) |
| F3-18 | EP-11 | Implementar la funcionalidad de IA seleccionada (1 de las 5 opciones) |
| F3-19 | EP-11 | Manejo de fallback si el servicio LLM no está disponible |

#### 3.7 Internacionalización

| Tarea | Épica | Descripción |
|---|---|---|
| F3-20 | EP-12 | Crear `res/values-en/strings.xml` con las traducciones al inglés |
| F3-21 | EP-12 | Verificar que toda la UI usa recursos de strings (sin textos hardcoded) |

#### 3.8 Funcionalidad adicional no trivial

| Tarea | Épica | Descripción |
|---|---|---|
| F3-22 | EP-13 | Diseñar e implementar la funcionalidad adicional definida por el equipo |

---

## 6. Convenciones de código

### 6.1 Nombrado

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases / Objetos | PascalCase | `FeedViewModel`, `PostRepository` |
| Funciones / variables | camelCase | `getPostById()`, `isLoading` |
| Constantes | SCREAMING_SNAKE_CASE | `MAX_IMAGES_PER_POST` |
| Composables | PascalCase + sufijo `Screen` / `Card` / `Item` | `FeedScreen`, `PostCard` |
| Recursos XML | snake_case | `post_detail_title`, `ic_paw` |
| Paquetes | minúsculas sin guiones | `com.pethelp.app.features.feed` |

### 6.2 Estructura de un ViewModel estándar

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val useCase: ExampleUseCase
) : ViewModel() {

    // Estado UI inmutable expuesto a la pantalla
    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    // Acciones del usuario
    fun onEvent(event: ExampleEvent) {
        when (event) {
            is ExampleEvent.Load -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            useCase().collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is Resource.Loading -> state.copy(isLoading = true)
                        is Resource.Success -> state.copy(isLoading = false, data = result.data)
                        is Resource.Error   -> state.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }
}
```

### 6.3 Estructura de un UseCase estándar

```kotlin
class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(filter: PostFilter): Flow<Resource<List<Post>>> =
        repository.getPosts(filter)
}
```

### 6.4 Commits y ramas (Git Flow simplificado)

| Rama | Propósito |
|---|---|
| `main` | Código estable entregable |
| `develop` | Integración continua del trabajo del equipo |
| `feature/nombre-feature` | Desarrollo de cada feature |
| `fix/descripcion-bug` | Corrección de errores |

**Formato de commit:**
```
<tipo>(<scope>): <descripción corta>

Ejemplos:
feat(auth): implementar pantalla de registro con validaciones
fix(feed): corregir filtro por categoría que no resetea paginación
chore(deps): actualizar Firebase BOM a 33.7.0
```

---

## 7. Configuración inicial del entorno (paso a paso)

### Paso 1 — Prerrequisitos
- [ ] Android Studio Ladybug (2024.2.x) o superior instalado.
- [ ] JDK 17 configurado en Android Studio.
- [ ] SDK Android 35 descargado desde SDK Manager.
- [ ] Emulador o dispositivo físico con Android 11 (API 30) o superior.

### Paso 2 — Clonar el repositorio
```bash
git clone https://github.com/TU_ORG/PetHelp.git
cd PetHelp
```

### Paso 3 — Crear `local.properties`
Copiar la plantilla y completar las claves:
```bash
cp local.properties.example local.properties
# Editar local.properties con tu editor y ag los valores:
#   sdk.dir=C:\Users\TU_USUARIO\AppData\Local\Android\Sdk
#   MAPS_API_KEY=AIzaSy...
#   CLOUDINARY_CLOUD_NAME=...
#   OPENAI_API_KEY=sk-...
```

### Paso 4 — Configurar Firebase
1. Crear proyecto en [Firebase Console](https://console.firebase.google.com).
2. Agregar app Android con package name `com.pethelp.app`.
3. Descargar `google-services.json` y ubicarlo en `app/`.
4. Habilitar en Firebase Console:
   - Authentication → Email/Password
   - Firestore Database → modo producción o prueba
   - Cloud Messaging
   - Storage

### Paso 5 — Configurar Google Maps
1. Ir a [Google Cloud Console](https://console.cloud.google.com).
2. Habilitar: Maps SDK for Android + Geocoding API + Places API.
3. Crear API Key y restringirla al package `com.pethelp.app`.
4. Agregar la clave a `local.properties` como `MAPS_API_KEY`.

### Paso 6 — Configurar Cloudinary
1. Crear cuenta en [Cloudinary](https://cloudinary.com).
2. Obtener Cloud Name del Dashboard.
3. Agregar a `local.properties` como `CLOUDINARY_CLOUD_NAME`.
4. Crear un upload preset sin firma para uso desde mobile.

### Paso 7 — Sincronizar y compilar
1. Abrir el proyecto en Android Studio.
2. Esperar la sincronización de Gradle (descarga dependencias).
3. Ejecutar: `Build → Make Project` o `./gradlew assembleDebug`.
4. Correr en emulador/dispositivo.

---

## 8. Criterios de calidad (definition of done por tarea)

Una tarea se considera completada cuando:
- [ ] La funcionalidad implementada cumple todos los criterios de aceptación de su HU.
- [ ] No tiene errores de compilación.
- [ ] Ha sido probada manualmente en emulador o dispositivo.
- [ ] El código sigue las convenciones de nombrado y arquitectura del proyecto.
- [ ] Los textos visibles al usuario están en `strings.xml` (no hardcodeados).
- [ ] El commit está en la rama correspondiente con el formato de mensaje acordado.
- [ ] Todos los integrantes del grupo han contribuido con al menos 1 commit en el sprint.

---

## 9. Servicios externos y credenciales necesarias

| Servicio | Propósito | Cómo obtener |
|---|---|---|
| Firebase Auth | Autenticación de usuarios | console.firebase.google.com |
| Firebase Firestore | Base de datos principal | console.firebase.google.com |
| Firebase Cloud Messaging | Notificaciones push | console.firebase.google.com |
| Google Maps Android SDK | Mapas y geolocalización | console.cloud.google.com |
| Cloudinary | Almacenamiento de imágenes | cloudinary.com |
| OpenAI / Gemini / Claude | Funcionalidad de IA | platform.openai.com / ai.google.dev |

> ⚠️ **Ninguna clave de API debe ser subida al repositorio.** Usar siempre `local.properties` (ignorado por `.gitignore`). En CI/CD usar variables de entorno o GitHub Secrets.

---

## 10. Riesgos identificados y mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| Cuota gratuita de Firebase agotada | Media | Alto | Monitorear uso; usar emuladores de Firebase para desarrollo |
| API Key de Google Maps expuesta | Baja | Alto | Restricción a package + `local.properties` en .gitignore |
| LLM con alta latencia | Media | Medio | Ejecutar llamada en background + skeleton en la UI |
| Conflictos de merge entre integrantes | Alta | Medio | Git Flow con ramas por feature; revisión de PR antes de merge |
| Mapas lentos en emulador | Alta | Bajo | Desarrollar con datos mock; probar en dispositivo físico para mapas |
| Cloudinary límite de transformaciones | Baja | Bajo | Comprimir imágenes en cliente antes de subir |
