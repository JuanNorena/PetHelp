package com.pethelp.app.features.post.di

import com.pethelp.app.features.post.data.repository.FirebasePostRepository
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que vincula la interfaz PostRepository
 * con su implementación concreta FirebasePostRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PostModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        firebasePostRepository: FirebasePostRepository
    ): PostRepository
}
