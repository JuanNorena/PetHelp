package com.pethelp.app.core.domain.model

/**
 * Modelo de dominio — Usuario de la aplicación.
 */
data class User(
    val id: String          = "",
    val name: String        = "",
    val username: String    = "",
    val email: String       = "",
    val photoUrl: String    = "",
    val bio: String         = "",
    val city: String        = "",
    val role: UserRole      = UserRole.USER,
    val points: Int         = 0,
    val level: UserLevel    = UserLevel.FRIEND,
    val badges: List<Badge> = emptyList(),
    val petPreferences: List<String> = emptyList(),
    val alertsNearMe: Boolean = true,
    val notificationRadiusKm: Double = 10.0,
    val profileVisibility: String = "public",
    val showEmail: Boolean = false,
    val showCity: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val emailAlertsEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val createdAt: Long     = System.currentTimeMillis()
)

enum class UserRole { USER, MODERATOR }

enum class UserLevel(val minPoints: Int) {
    FRIEND(0),
    PROTECTOR(50),
    GUARDIAN(150),
    HERO(350)
}

data class Badge(
    val id: String       = "",
    val name: String     = "",
    val description: String = "",
    val iconUrl: String  = "",
    val earnedAt: Long   = System.currentTimeMillis()
)
