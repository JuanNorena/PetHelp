package com.pethelp.app.features.notifications.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.PetNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<Resource<List<PetNotification>>>
    fun markAsRead(notificationId: String): Flow<Resource<Unit>>
    fun markAllAsRead(): Flow<Resource<Unit>>
}
