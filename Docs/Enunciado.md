# Enunciado del proyecto — Red de adopción de mascotas

## 1. Descripción general
El proyecto final consiste en desarrollar una aplicación móvil completa usando **Jetpack Compose** (Kotlin). Cada grupo recibirá un tema asignado por el curso; todos los proyectos comparten la misma estructura base, los requisitos técnicos y los criterios de evaluación.

---

## 2. Requisitos comunes (aplican a todas las temáticas)

### 2.1 Roles
La aplicación debe gestionar dos tipos de usuarios: **Usuario** y **Moderador**.

#### Moderador — funciones
- Iniciar sesión.  
- Revisar, aprobar o rechazar publicaciones. Al rechazar, registrar un motivo; las publicaciones rechazadas no se muestran en el feed público.  
- Marcar publicaciones como resueltas/finalizadas.

#### Usuario — funciones
- Registrar cuenta e iniciar sesión (correo electrónico + contraseña).  
- Ver un feed de publicaciones en vista lista o en mapa; filtrar por categoría y por ubicación (cercanos, ciudad, etc.).  
- Crear, editar y eliminar publicaciones (todas las publicaciones se crean inicialmente sin verificación).  
- Marcar su publicación como “Resuelta/Finalizada”.  
- Marcar publicaciones con “Me interesa” / votar para determinar relevancia.  
- Comentar en publicaciones; al comentar, notificar al autor.  
- Ver el detalle de cada publicación (comentarios, ubicación exacta, fotos, información completa).  
- Editar datos personales y eliminar la cuenta.  
- Consultar estadísticas personales (publicaciones activas, finalizadas y pendientes de verificación).  
- Participar en el sistema de reputación y logros (puntos por crear publicaciones, comentar, recibir votos, etc.).  
- Niveles de usuario (4 niveles definidos por la temática).  
- Insignias/badges por logros (ej.: primera publicación, 10 publicaciones verificadas, publicación destacada del mes).

---

### 2.2 Requisitos técnicos
- Implementación obligatoria con **Jetpack Compose** (Kotlin).  
- Cada publicación debe incluir: título, categoría, descripción, ubicación (latitud/longitud) y al menos una imagen.  
- La ubicación se obtiene mediante selección en un mapa; almacenar latitud/longitud en la base de datos. (Mapbox o Google Maps recomendados.)  
- Recuperación de contraseña por enlace enviado al correo electrónico.  
- Notificaciones push en tiempo real (recomendado: Firebase Cloud Messaging).  
- Las notificaciones deben ser georreferenciadas: definir un radio (km) que determine relevancia.  
- Gestión de imágenes mediante un servicio externo (Cloudinary, AWS S3, Google Cloud Storage, etc.).  
- Código fuente en repositorio GitHub; todos los integrantes deben contribuir.  
- Moderadores precargados en la base de datos inicial.  
- Para autenticación, almacenamiento y notificaciones se puede usar Firebase u otro servicio similar; no es obligatorio implementar un backend propio.
- **IMPORTANTE:** cada grupo debe pensar e implementar una necesidad adicional (no trivial) en su sistema.

---

## 3. Temática 4 — Red de adopción de mascotas

### Contexto
Miles de mascotas son abandonadas cada año. Se requiere una plataforma que conecte a quienes desean dar en adopción con quienes buscan adoptar, y que permita reportar mascotas perdidas o encontradas.

### Entidad principal
**Publicación de mascota**

### Categorías
- Adopción  
- Perdidos  
- Encontrados  
- Hogar temporal  
- Eventos veterinarios (vacunación, esterilización, etc.)

### Particularidades
- Cada publicación incluirá: tipo de animal, raza aproximada, tamaño y estado de vacunación.  
- Botón **“Me interesa adoptar”** para enviar solicitud al autor de la publicación.  
- Moderadores verifican la veracidad y evitan publicaciones relacionadas con venta de animales.  
- Niveles de usuario: Amigo Animal, Protector, Guardián, Héroe de las Mascotas.

---

## 4. Requisitos con Inteligencia Artificial (elegir 1)
Integrar una de las siguientes funcionalidades con un LLM (OpenAI GPT, Google Gemini, Claude, u otro):

1. **Clasificación automática de categorías** — sugerir categoría a partir del título y la descripción.  
2. **Validación de imágenes relevantes** — detectar imágenes que no correspondan con la categoría/descripción.  
3. **Detección de contenido inapropiado** — analizar texto y sugerir reformulaciones si es necesario.  
4. **Generación de resúmenes para moderadores** — resumen automático con acciones recomendadas.  
5. **Detección de publicaciones duplicadas** — búsqueda semántica en la misma zona y sugerencia de duplicados.

---

## 5. Fases del proyecto

### Fase 1 — Diseño
- Entregable: mockups de todas las pantallas (Material You). Herramienta recomendada: Figma.

### Fase 2 — Funcionalidades básicas
- Entregable: prototipo funcional con navegación y lógica en memoria (no persistente).

### Fase 3 — Funcionalidades completas
- Entregable: aplicación completa con mapas, persistencia, internacionalización, subida de imágenes, autenticación, IA integrada y recuperación de contraseña (Firebase u otro).
