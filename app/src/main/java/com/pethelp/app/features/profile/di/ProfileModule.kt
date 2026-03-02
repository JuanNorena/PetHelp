package com.pethelp.app.features.profile.di

import com.pethelp.app.features.profile.data.repository.FirebaseProfileRepository
import com.pethelp.app.features.profile.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        firebaseProfileRepository: FirebaseProfileRepository
    ): ProfileRepository
}
