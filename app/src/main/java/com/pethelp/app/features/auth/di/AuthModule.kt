package com.pethelp.app.features.auth.di

import com.pethelp.app.features.auth.data.repository.FirebaseAuthRepository
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que vincula la interfaz AuthRepository
 * con su implementación concreta FirebaseAuthRepository.
 *
 * Gracias a @Binds, Hilt sabe cómo inyectar AuthRepository
 * en cualquier clase que lo solicite (por ejemplo, AuthViewModel).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository
}
