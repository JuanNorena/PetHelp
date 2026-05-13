package com.pethelp.app.features.notifications.di

import com.pethelp.app.features.notifications.data.repository.FirebaseNotificationRepository
import com.pethelp.app.features.notifications.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que vincula la interfaz [NotificationRepository]
 * con su implementación concreta [FirebaseNotificationRepository].
 *
 * Esto permite que los ViewModels dependan de la abstracción
 * mientras Hilt inyecta automáticamente la implementación de Firebase.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    /**
     * Vincula la implementación de Firebase con la interfaz de dominio.
     *
     * @param firebaseNotificationRepository Implementación que usa Firestore.
     * @return Interfaz [NotificationRepository] lista para inyección.
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        firebaseNotificationRepository: FirebaseNotificationRepository
    ): NotificationRepository
}
