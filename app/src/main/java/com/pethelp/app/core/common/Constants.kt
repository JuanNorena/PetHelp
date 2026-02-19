package com.pethelp.app.core.common

/**
 * Constantes globales de la aplicación.
 * Centralizar aquí evita magic strings dispersos en el código.
 */
object Constants {

    // ── Colecciones de Firestore ──────────────────────────────────────────────
    const val COLLECTION_USERS          = "users"
    const val COLLECTION_POSTS          = "posts"
    const val COLLECTION_COMMENTS       = "comments"
    const val COLLECTION_NOTIFICATIONS  = "notifications"
    const val COLLECTION_VOTES          = "votes"
    const val COLLECTION_ADOPTION_REQUESTS = "adoptionRequests"
    const val COLLECTION_BADGES         = "badges"

    // ── Estados de publicación ────────────────────────────────────────────────
    const val POST_STATUS_PENDING    = "PENDING"
    const val POST_STATUS_VERIFIED   = "VERIFIED"
    const val POST_STATUS_REJECTED   = "REJECTED"
    const val POST_STATUS_RESOLVED   = "RESOLVED"

    // ── Categorías de publicación ─────────────────────────────────────────────
    const val CATEGORY_ADOPTION      = "ADOPTION"
    const val CATEGORY_LOST          = "LOST"
    const val CATEGORY_FOUND         = "FOUND"
    const val CATEGORY_TEMP_HOME     = "TEMP_HOME"
    const val CATEGORY_VET_EVENT     = "VET_EVENT"

    // ── Roles de usuario ──────────────────────────────────────────────────────
    const val ROLE_USER              = "USER"
    const val ROLE_MODERATOR         = "MODERATOR"

    // ── Notificaciones ────────────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID   = "pethelp_channel"
    const val NOTIFICATION_CHANNEL_NAME = "PetHelp Alertas"
    const val DEFAULT_NOTIFICATION_RADIUS_KM = 10.0   // radio por defecto en km

    // ── Reputación — puntos por acción ────────────────────────────────────────
    const val POINTS_CREATE_POST     = 10
    const val POINTS_COMMENT         = 5
    const val POINTS_RECEIVE_VOTE    = 3
    const val POINTS_POST_VERIFIED   = 15

    // ── Niveles de usuario ────────────────────────────────────────────────────
    const val LEVEL_1_NAME   = "Amigo Animal"
    const val LEVEL_1_MIN    = 0
    const val LEVEL_2_NAME   = "Protector"
    const val LEVEL_2_MIN    = 50
    const val LEVEL_3_NAME   = "Guardián"
    const val LEVEL_3_MIN    = 150
    const val LEVEL_4_NAME   = "Héroe de las Mascotas"
    const val LEVEL_4_MIN    = 350

    // ── DataStore keys ────────────────────────────────────────────────────────
    const val DS_KEY_NOTIFICATION_RADIUS = "notification_radius_km"
    const val DS_KEY_PREFERRED_LANGUAGE  = "preferred_language"

    // ── Cloudinary ────────────────────────────────────────────────────────────
    const val CLOUDINARY_FOLDER_POSTS    = "pethelp/posts"
    const val CLOUDINARY_FOLDER_AVATARS  = "pethelp/avatars"

    // ── Misceláneo ────────────────────────────────────────────────────────────
    const val MAX_IMAGES_PER_POST = 5
    const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm"
}
