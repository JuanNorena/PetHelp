package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio — Usuario de la aplicación.
 */
data class User(
    val id: String          = "",
    val name: String        = "",
    val email: String       = "",
    val photoUrl: String    = "",
    val role: UserRole      = UserRole.USER,
    val points: Int         = 0,
    val level: UserLevel    = UserLevel.FRIEND,
    val badges: List<Badge> = emptyList(),
    val notificationRadiusKm: Double = 10.0,
    val createdAt: Long     = System.currentTimeMillis()
)

enum class UserRole { USER, MODERATOR }

enum class UserLevel(val displayName: String, val minPoints: Int) {
    FRIEND("Amigo Animal", 0),
    PROTECTOR("Protector", 50),
    GUARDIAN("Guardián", 150),
    HERO("Héroe de las Mascotas", 350)
}

data class Badge(
    val id: String       = "",
    val name: String     = "",
    val description: String = "",
    val iconUrl: String  = "",
    val earnedAt: Long   = System.currentTimeMillis()
)
