package com.pethelp.app.core.common

/**
 * Contenedor de constantes globales de la aplicación.
 *
 * Este objeto agrupa valores inmutables que se usan en diferentes partes de
 * la aplicación, como nombres de colecciones de Firestore, estados de publicaciones,
 * categorías, roles, configuración de notificaciones y reglas de negocio.
 *
 * Usar constantes centralizadas ayuda a:
 * 1. Evitar "magic strings" repetidos en el código.
 * 2. Reducir errores por cambios de nombre.
 * 3. Facilitar el mantenimiento y la lectura del proyecto.
 */
object Constants {

    // ── Colecciones de Firestore ──────────────────────────────────────────────
    // Nombres de las colecciones que se guardan en la base de datos Firestore.
    // Deben coincidir con los nombres reales usados en el backend.
    const val COLLECTION_USERS = "users"
    const val COLLECTION_POSTS = "posts"
    const val COLLECTION_COMMENTS = "comments"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val COLLECTION_VOTES = "votes"
    const val COLLECTION_ADOPTION_REQUESTS = "adoptionRequests"
    const val COLLECTION_BADGES = "badges"

    // ── Estados de publicación ────────────────────────────────────────────────
    // Estados posibles de una publicación. Estos valores se usan para filtrar,
    // mostrar mensajes al usuario y controlar el flujo de moderación.
    const val POST_STATUS_PENDING = "PENDING"
    const val POST_STATUS_VERIFIED = "VERIFIED"
    const val POST_STATUS_REJECTED = "REJECTED"
    const val POST_STATUS_RESOLVED = "RESOLVED"

    // ── Categorías de publicación ─────────────────────────────────────────────
    // Tipos de publicaciones que los usuarios pueden crear.
    // La categoría determina la lógica de la pantalla y los filtros.
    const val CATEGORY_ADOPTION = "ADOPTION"
    const val CATEGORY_LOST = "LOST"
    const val CATEGORY_FOUND = "FOUND"
    const val CATEGORY_TEMP_HOME = "TEMP_HOME"
    const val CATEGORY_VET_EVENT = "VET_EVENT"

    // ── Roles de usuario ──────────────────────────────────────────────────────
    // Determinan permisos y comportamiento dentro de la app.
    const val ROLE_USER = "USER"
    const val ROLE_MODERATOR = "MODERATOR"

    // ── Notificaciones ────────────────────────────────────────────────────────
    // Configuración del canal de notificaciones locales de Android.
    const val NOTIFICATION_CHANNEL_ID = "pethelp_channel"
    const val NOTIFICATION_CHANNEL_NAME = "PetHelp Alertas"
    const val DEFAULT_NOTIFICATION_RADIUS_KM = 10.0

    // ── Reputación — puntos por acción ────────────────────────────────────────
    // Puntos que se asignan al usuario cuando realiza acciones relevantes.
    // Se usan para calcular reputación, niveles o recompensas.
    const val POINTS_CREATE_POST = 10
    const val POINTS_COMMENT = 5
    const val POINTS_RECEIVE_VOTE = 3
    const val POINTS_POST_VERIFIED = 15

    // ── Niveles de usuario ────────────────────────────────────────────────────
    // Define los nombres de cada nivel y los puntos mínimos necesarios.
    // El nivel del usuario se calcula comparando su puntaje total con estos valores.
    const val LEVEL_1_NAME = "Amigo Animal"
    const val LEVEL_1_MIN = 0
    const val LEVEL_2_NAME = "Protector"
    const val LEVEL_2_MIN = 50
    const val LEVEL_3_NAME = "Guardián"
    const val LEVEL_3_MIN = 150
    const val LEVEL_4_NAME = "Héroe de las Mascotas"
    const val LEVEL_4_MIN = 350

    // ── DataStore keys ────────────────────────────────────────────────────────
    // Claves usadas para guardar preferencias del usuario en DataStore local.
    const val DS_KEY_NOTIFICATION_RADIUS = "notification_radius_km"
    const val DS_KEY_PREFERRED_LANGUAGE = "preferred_language"

    // ── Cloudinary ────────────────────────────────────────────────────────────
    // Rutas de carpeta en Cloudinary donde se almacenan imágenes.
    const val CLOUDINARY_FOLDER_POSTS = "pethelp/posts"
    const val CLOUDINARY_FOLDER_AVATARS = "pethelp/avatars"

    // ── Misceláneo ────────────────────────────────────────────────────────────
    // Valores generales que afectan el comportamiento de la app.
    const val MAX_IMAGES_PER_POST = 5
    const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm"
}
