# 🐾 PetHelp — Red de adopción de mascotas

> Aplicación móvil Android desarrollada con Jetpack Compose y Kotlin como proyecto final de la materia **Diseño y Desarrollo de Aplicaciones Móviles** — Universidad del Quindío · 2026-1.

---

## 👥 Equipo de desarrollo

| Nombre | Universidad | Rol |
|---|---|---|
| Juan Sebastián Noreña Espinosa | Universidad del Quindío | Desarrollador |
| Santiago Londoño Gaviria | Universidad del Quindío | Desarrollador |
| Diego Alejandro López | Universidad del Quindío | Desarrollador |

**Materia:** Diseño y Desarrollo de Aplicaciones Móviles  
**Semestre:** 2026-1

---

## 📋 Descripción del proyecto

PetHelp es una plataforma móvil que conecta a personas que desean dar mascotas en adopción con personas que buscan adoptar, y permite reportar mascotas perdidas o encontradas. La aplicación aborda el problema del abandono animal mediante una comunidad georreferenciada, con moderación de contenido e inteligencia artificial integrada.

### Categorías de publicación

| Categoría | Descripción |
|---|---|
| 🐶 Adopción | Mascotas disponibles para adoptar |
| 🔍 Perdidos | Mascotas extraviadas |
| 📍 Encontrados | Mascotas encontradas en la calle |
| 🏠 Hogar temporal | Hogares de paso temporales |
| 💉 Veterinaria | Jornadas de vacunación y esterilización gratuita |

---

## 🏗️ Arquitectura y decisiones técnicas

### ¿Por qué Clean Architecture + MVVM?

La arquitectura del proyecto se divide en tres capas con una regla fundamental: **las capas internas nunca dependen de las externas**.

```
┌──────────────────────────────────────────────────────┐
│               CAPA DE PRESENTACIÓN                   │
│  Pantallas Compose ←→ ViewModel ←→ UIState           │
└─────────────────────────┬────────────────────────────┘
                          │ invoca
┌─────────────────────────▼────────────────────────────┐
│               CAPA DE DOMINIO                        │
│  Casos de uso (lógica de negocio) + Interfaces       │
└─────────────────────────┬────────────────────────────┘
                          │ implementado por
┌─────────────────────────▼────────────────────────────┐
│               CAPA DE DATOS                          │
│  Repositorios (impl) + Firebase + Room + APIs        │
└──────────────────────────────────────────────────────┘
```

**Beneficios concretos para PetHelp:**
- Si en Fase 3 se cambia Cloudinary por otro servicio de imágenes, **solo se modifica el repositorio** — las pantallas y la lógica de negocio no se tocan.
- Con 3 integrantes en el equipo, cada uno puede trabajar en su feature **sin generar conflictos de merge**.
- Cada capa es **testeable de forma independiente**.

**¿Por qué MVVM y no MVP o MVI?**
- MVVM es el patrón **oficialmente recomendado por Google** para Jetpack Compose.
- El `ViewModel` sobrevive a rotaciones de pantalla — crítico en Android.
- `StateFlow` + Compose garantiza que la UI se redibuje **solo cuando el estado cambia**.

---

### ¿Por qué estructura de carpetas por feature?

```
features/
  auth/         ← Registro, login, perfil
  feed/         ← Feed lista/mapa, filtros
  moderation/   ← Panel del moderador
  post/         ← CRUD publicaciones
  ...
```

Cada feature contiene sus propias capas `data/`, `domain/` y `presentation/`. Esto permite que cada integrante trabaje en **su carpeta sin interferir con los demás** y que al abrir una feature, **todos sus archivos estén en un solo lugar**. Sigue las recomendaciones de los **Android Architecture Samples oficiales de Google (2024)**.

---

### ¿Por qué Hilt?

Hilt es la librería de inyección de dependencias **oficial de Google para Android**. Detecta errores de configuración **en tiempo de compilación** (no en runtime) y tiene integración nativa con `ViewModel`, `WorkManager` y `Navigation Compose`.

---

### ¿Por qué Firebase?

| Servicio | Razón de elección |
|---|---|
| Firebase Auth | Maneja registro, login, sesión persistente y recuperación de contraseña con mínimo código |
| Firestore | Base de datos en **tiempo real** — el feed se actualiza automáticamente sin hacer polling |
| FCM | Estándar de notificaciones push en Android; requerido explícitamente por el enunciado |

El plan gratuito (Spark) es suficiente para el alcance del proyecto académico.

---

### ¿Por qué Cloudinary para imágenes?

Ofrece **transformaciones desde la URL** (redimensionar, comprimir) lo que evita cargar imágenes pesadas en tarjetas del feed, con un plan gratuito más generoso que Firebase Storage para almacenamiento de imágenes.

---

### ¿Por qué Coil y no Glide?

Coil está escrito **100% en Kotlin** con soporte nativo para Jetpack Compose (`AsyncImage`). Glide y Picasso son de la era XML/Views y requieren wrappers adicionales en Compose.

---

### ¿Por qué KSP y no KAPT?

KSP (Kotlin Symbol Processing) procesa las anotaciones de Hilt y Room **2-3x más rápido** que KAPT. Google recomienda migrar a KSP para todos los proyectos nuevos desde 2023.

---

### ¿Por qué Room si ya hay Firestore?

En Fase 2 el enunciado exige "datos en memoria". Room actúa como base de datos local que simula Firestore — cuando llegue Fase 3, solo cambia el `RepositoryImpl` de Room a Firestore; los casos de uso y ViewModels **no se modifican**. En Fase 3, Room sirve además como **caché offline**.

---

## 🛠️ Stack tecnológico

| Tecnología | Versión | Rol en el proyecto |
|---|---|---|
| Kotlin | 2.0.21 | Lenguaje principal |
| Jetpack Compose BOM | 2024.12.01 | UI declarativa — requerida por el enunciado |
| Material3 | BOM | Sistema de diseño Material You |
| Hilt | 2.52 | Inyección de dependencias oficial de Android |
| Navigation Compose | 2.8.4 | Navegación type-safe en arquitectura Single-Activity |
| Firebase BOM | 34.9.0 | Auth, Firestore, FCM |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 | Llamadas HTTP a la API del LLM |
| Coroutines | 1.9.0 | Programación asíncrona |
| Coil | 2.7.0 | Carga de imágenes optimizada para Compose |
| Cloudinary Android | 2.6.0 | Almacenamiento externo de imágenes |
| Maps Compose | 6.2.1 | Google Maps en Jetpack Compose |
| Play Services Location | 21.3.0 | GPS del dispositivo |
| Room | 2.6.1 | Caché local + datos en memoria (Fase 2) |
| DataStore Preferences | 1.1.1 | Preferencias del usuario (radio, idioma) |
| KSP | 2.0.21-1.0.27 | Procesador de anotaciones (Hilt + Room) |
| MockK | 1.13.12 | Testing — mocking idiomático para Kotlin |
| Turbine | 1.2.0 | Testing de Flow y StateFlow |

**Versión mínima de Android:** API 30 (Android 11 — Red Velvet Cake)  
**Target SDK:** API 35 (Android 15)

---

## 📁 Estructura del proyecto

```
PetHelp/
├── app/src/main/java/com/pethelp/app/
│   ├── MainActivity.kt               ← Single Activity
│   ├── PetHelpApplication.kt         ← Punto de entrada Hilt
│   ├── core/                         ← Código compartido entre features
│   │   ├── common/                   ← Resource.kt, Constants.kt
│   │   ├── di/                       ← Módulos Hilt (Firebase, Network)
│   │   ├── domain/model/             ← Modelos de dominio (Post, User, Comment...)
│   │   ├── navigation/               ← NavGraph central + rutas
│   │   ├── notifications/            ← FCM Service
│   │   └── ui/theme/                 ← Theme, Color, Type (Material You)
│   └── features/
│       ├── auth/                     ← Registro, login, recuperar contraseña
│       ├── feed/                     ← Feed lista/mapa, filtros por categoría
│       ├── post/                     ← Crear, ver detalle, editar publicación
│       │   ├── data/repository/      ← FirebasePostRepository (Firestore)
│       │   ├── di/                   ← PostModule (Hilt binding)
│       │   ├── domain/repository/    ← PostRepository (interfaz)
│       │   └── presentation/         ← Screens + ViewModels
│       ├── moderation/               ← Panel del moderador (aprobar/rechazar)
│       ├── notifications/            ← Lista de notificaciones
│       ├── profile/                  ← Perfil y edición de datos
│       ├── stats/                    ← Dashboard de estadísticas personales
│       ├── reputation/               ← Puntos, niveles e insignias
│       └── ai/                       ← Funcionalidad de inteligencia artificial
├── Docs/
│   ├── Enunciado.md                  ← Requisitos del proyecto
│   ├── Epicas.md                     ← Épicas e historias de usuario (INVEST)
│   └── PlanDesarrollo.md             ← Plan técnico completo
├── gradle/libs.versions.toml         ← Catálogo central de versiones
└── local.properties.example          ← Plantilla de claves API (no subir al repo)
```

---

## 🚀 Configuración del entorno de desarrollo

### Prerrequisitos
- Android Studio Ladybug (2024.2.x) o superior
- JDK 17
- SDK Android 35
- Emulador o dispositivo físico con Android 11 (API 30) o superior

### Pasos

**1. Clonar el repositorio**
```bash
git clone https://github.com/TU_ORG/PetHelp.git
cd PetHelp
```

**2. Crear `local.properties`**
```bash
cp local.properties.example local.properties
```
Completar con los valores reales:
```properties
sdk.dir=C:\Users\TU_USUARIO\AppData\Local\Android\Sdk
MAPS_API_KEY=AIzaSy...
CLOUDINARY_CLOUD_NAME=...
OPENAI_API_KEY=sk-...
```

**3. Configurar Firebase**
1. Crear proyecto en [Firebase Console](https://console.firebase.google.com).
2. Agregar app Android con package `com.pethelp.app`.
3. Descargar `google-services.json` → ubicar en `app/`.
4. Habilitar: Authentication (Email/Password), Firestore, Cloud Messaging.

**4. Configurar Google Maps**
1. Ir a [Google Cloud Console](https://console.cloud.google.com).
2. Habilitar: Maps SDK for Android + Geocoding API.
3. Crear API Key restringida al package `com.pethelp.app`.

**5. Configurar Cloudinary**
1. Crear cuenta en [Cloudinary](https://cloudinary.com).
2. Copiar el Cloud Name a `local.properties`.
3. Crear un upload preset sin firma para uso mobile.

**6. Compilar**
```bash
./gradlew assembleDebug
```

---

## 🌿 Flujo de trabajo Git

| Rama | Propósito |
|---|---|
| `main` | Código estable — entregable por fase |
| `develop` | Integración continua del equipo |
| `feature/nombre` | Desarrollo de cada historia de usuario |
| `fix/descripcion` | Corrección de errores |

**Formato de commits:**
```
feat(auth): implementar pantalla de registro con validaciones
fix(feed): corregir filtro por categoría
chore(deps): actualizar Firebase BOM a 34.9.0
```

---

## 📅 Fases del proyecto

| Fase | Entregable | Estado |
|---|---|---|
| Fase 1 — Diseño | Mockups en Figma (Material You) | ✅ Completada |
| Fase 2 — Básico | App funcional con datos en memoria | ✅ Completada |
| Fase 3 — Completo | Firebase, mapas, IA, i18n, imágenes | 🔨 En progreso |

---

## ✅ Funcionalidades implementadas

### Autenticación (Firebase Auth)

- **Splash screen** con navegación automática según estado de sesión.
- **Inicio de sesión** (email + contraseña) con validaciones locales y errores de Firebase mapeados a español.
- **Registro** con nombre, correo, contraseña y confirmación de contraseña. Incluye indicador de fortaleza de contraseña.
- **Recuperación de contraseña** por email — protegida contra enumeración de usuarios.
- **Términos y Condiciones** con modal scrollable que cumple legislación colombiana (Ley 1581/2012, Ley 1273/2009, Ley 84/1989, Ley 1774/2016). Checkbox obligatorio para registrarse.
- **Persistencia de usuario** en Firestore al registrarse (nombre, email, rol, fecha de creación).

### Publicaciones — Detalle (`PostDetailScreen`)

- **Imagen principal** con `AsyncImage` (Coil) y gradiente overlay para legibilidad.
- **Tarjeta informativa** con título, chip de autor, y 4 chips de información (raza, sexo, tamaño, vacunación) en grid 2×2 con colores diferenciados.
- **Sección "Sobre [mascota]"** con descripción completa.
- **Sistema de votos** — botón corazón con contador, toggle en tiempo real contra Firestore con transacciones atómicas.
- **Solicitud de adopción** — botón condicional (solo en categoría Adopción) que persiste en colección `adoptionRequests`.
- **Sección de ubicación** con placeholder para mapa.
- **Comentarios en tiempo real** — lista de comentarios con avatar de iniciales, campo de texto + botón "Publicar", persistencia en Firestore con actualización atómica del contador.
- **Navegación**: botón de retroceso y compartir en la cabecera.

### Publicaciones — Crear (`CreatePostScreen`)

- **Selector de fotos** — hasta 5 imágenes con `PickVisualMedia`, thumbnails con botón de eliminar e indicador de posición ("1/5").
- **Campos del formulario**: título y descripción con `OutlinedTextField` estilizado.
- **Sugerencia de categoría con IA** — sección visual con icono de sparkles y fondo degradado, dropdown con todas las categorías (`PostCategory`).
- **Chips de tipo de animal** — Perro, Gato, Otro con iconos.
- **Chips de tamaño** — Pequeño, Mediano, Grande.
- **Barra inferior** con botón teal "Siguiente: Ubicación".
- **Diseño fiel a Figma** — colores TealAccent (#00BCB4), tipografía y espaciado exactos.

### Infraestructura de publicaciones (Clean Architecture)

- **`PostRepository`** (interfaz de dominio) — 10 operaciones: `getPostById`, `getPosts`, `createPost`, `updatePost`, `votePost`, `unvotePost`, `hasUserVoted`, `getComments`, `addComment`, `requestAdoption`.
- **`FirebasePostRepository`** (implementación) — listeners en tiempo real via `callbackFlow`, transacciones atómicas para votos y contadores, mapeo manual de snapshots.
- **`PostModule`** (Hilt DI) — binding `@Singleton` de la interfaz a la implementación.
- **`PostDetailViewModel`** — carga post, comentarios y estado de voto; acciones: `toggleVote()`, `addComment()`, `requestAdoption()`.
- **`CreatePostViewModel`** — gestión de formulario con validación, límite de imágenes, creación de publicación.

### Patrones de UX y calidad

- **Snackbar** (Material Design) para mensajes de error — patrón de evento único con `SharedFlow` (reemplaza `Toast` y `Text` inline).
- **Validación en dos capas**: ViewModel (validaciones de formato) + Repository (validaciones de Firebase).
- **Mensajes de error en español** para: credenciales inválidas, email en uso, contraseña débil, sin conexión, demasiados intentos, errores internos.
- **Deshabilitación de reCAPTCHA** en builds de debug (evita error `CONFIGURATION_NOT_FOUND` sin necesidad de App Check).
- **Idioma de Firebase Auth** configurado a español (`setLanguageCode("es")`).
- **Datos en tiempo real** — Firestore listeners con `callbackFlow` para actualización automática de publicaciones, comentarios y votos.

---

## 🔑 Servicios externos requeridos

| Servicio | Propósito | Obtener en |
|---|---|---|
| Firebase | Autenticación + base de datos + notificaciones | console.firebase.google.com |
| Google Maps SDK | Mapas y geolocalización | console.cloud.google.com |
| Cloudinary | Almacenamiento de imágenes | cloudinary.com |
| OpenAI / Gemini / Claude | Funcionalidad de IA | platform.openai.com / ai.google.dev |

> ⚠️ **Ninguna clave de API debe subirse al repositorio.** Usar siempre `local.properties` (ignorado por `.gitignore`).

---

## 📄 Documentación

- [Docs/Enunciado.md](Docs/Enunciado.md) — Requisitos completos del proyecto
- [Docs/Epicas.md](Docs/Epicas.md) — Épicas e historias de usuario (método INVEST)
- [Docs/PlanDesarrollo.md](Docs/PlanDesarrollo.md) — Plan técnico, librerías y convenciones

---

*Universidad del Quindío · Ingeniería de Sistemas · Diseño y Desarrollo de Aplicaciones Móviles · 2026*
