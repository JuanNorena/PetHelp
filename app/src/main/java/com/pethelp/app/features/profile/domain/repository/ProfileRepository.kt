package com.pethelp.app.features.profile.domain.repository

import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getCurrentUser(): Flow<Resource<User>>
    fun updateProfile(user: User): Flow<Resource<User>>
    fun updateProfilePhoto(imageUri: String): Flow<Resource<String>>
    fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>>
    fun deleteAccount(): Flow<Resource<Unit>>
}
