package com.pethelp.app.features.ai.di

import com.pethelp.app.features.ai.data.repository.AiChatRepositoryImpl
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiChatRepository(): AiChatRepository {
        return AiChatRepositoryImpl()
    }
}
