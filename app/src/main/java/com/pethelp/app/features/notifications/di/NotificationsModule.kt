package com.pethelp.app.features.notifications.di

import com.pethelp.app.features.notifications.data.repository.FirebaseNotificationRepository
import com.pethelp.app.features.notifications.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        firebaseNotificationRepository: FirebaseNotificationRepository
    ): NotificationRepository
}
