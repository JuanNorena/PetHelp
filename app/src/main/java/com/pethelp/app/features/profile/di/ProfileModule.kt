/**
 * Módulo Hilt que vincula la interfaz [ProfileRepository]
 * con su implementación concreta [FirebaseProfileRepository].
 */
package com.pethelp.app.features.profile.di

import com.pethelp.app.features.profile.data.repository.FirebaseProfileRepository
import com.pethelp.app.features.profile.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modulo Hilt de la feature de perfil.
 *
 * Declara los bindings de interfaces de dominio a implementaciones de datos.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    /**
     * Vincula la interfaz [ProfileRepository] con su implementación [FirebaseProfileRepository].
     *
     * @param firebaseProfileRepository Implementación concreta que usa Firebase Auth y Firestore.
     * @return Interfaz [ProfileRepository] lista para inyección.
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        firebaseProfileRepository: FirebaseProfileRepository
    ): ProfileRepository
}
