/**
 * Modelo de dominio que representa un usuario registrado en la aplicación.
 *
 * Contiene datos de perfil, permisos por rol, puntos de gamificación,
 * nivel, insignias y preferencias de notificación del usuario.
 */
package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio que representa un usuario registrado en la aplicación.
 *
 * Este modelo se utiliza en la capa de dominio y presentación para mostrar
 * información del perfil, permisos y configuración de notificaciones.
 */
data class User(
    /** Identificador único del usuario. */
    val id: String = "",

    /** Nombre real o visible del usuario. */
    val name: String = "",

    /** Nombre de usuario público usado en la aplicación. */
    val username: String = "",

    /** Correo electrónico del usuario. */
    val email: String = "",

    /** URL de la imagen de perfil del usuario. */
    val photoUrl: String = "",

    /** Biografía corta o descripción del usuario. */
    val bio: String = "",

    /** Ciudad de residencia del usuario. */
    val city: String = "",

    /** Rol de usuario para controlar permisos. */
    val role: UserRole = UserRole.USER,

    /** Puntos de reputación acumulados por el usuario. */
    val points: Int = 0,

    /** Nivel actual del usuario según sus puntos. */
    val level: UserLevel = UserLevel.FRIEND,

    /** Insignias obtenidas por el usuario. */
    val badges: List<Badge> = emptyList(),

    /** Preferencias de tipo de mascota que le interesan al usuario. */
    val petPreferences: List<String> = emptyList(),

    /** Si el usuario quiere recibir alertas de publicaciones cercanas. */
    val alertsNearMe: Boolean = true,

    /** Radio en kilómetros para recibir alertas de ubicaciones cercanas. */
    val notificationRadiusKm: Double = 10.0,

    /** Visibilidad del perfil: por ejemplo "public" o "private". */
    val profileVisibility: String = "public",

    /** Si el usuario permite mostrar su email en su perfil. */
    val showEmail: Boolean = false,

    /** Si el usuario permite mostrar su ciudad en su perfil. */
    val showCity: Boolean = true,

    /** Si el usuario tiene habilitadas las notificaciones push. */
    val pushNotificationsEnabled: Boolean = true,

    /** Si el usuario recibe alertas por correo electrónico. */
    val emailAlertsEnabled: Boolean = false,

    /** Si el usuario tiene activada la verificación en dos pasos. */
    val twoFactorEnabled: Boolean = false,

    /** Fecha de creación del usuario en milisegundos desde Epoch. */
    val createdAt: Long = System.currentTimeMillis()
)

/** Rol del usuario dentro de la aplicación. */
enum class UserRole {
    USER,
    MODERATOR
}

/**
 * Niveles de usuario basados en puntos de reputación.
 *
 * Cada nivel tiene un umbral mínimo de puntos para alcanzarlo.
 */
enum class UserLevel(val minPoints: Int) {
    FRIEND(0),
    PROTECTOR(50),
    GUARDIAN(150),
    HERO(350)
}

/**
 * Insignia ganada por el usuario dentro del sistema de reputación.
 */
data class Badge(
    /** Identificador único de la insignia. */
    val id: String = "",

    /** Nombre legible de la insignia. */
    val name: String = "",

    /** Descripción corta de cómo se obtuvo la insignia. */
    val description: String = "",

    /** URL del ícono que representa la insignia. */
    val iconUrl: String = "",

    /** Momento en el que se ganó la insignia. */
    val earnedAt: Long = System.currentTimeMillis()
)
