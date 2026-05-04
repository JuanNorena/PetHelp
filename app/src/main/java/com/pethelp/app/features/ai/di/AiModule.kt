package com.pethelp.app.features.ai.di

import com.google.gson.Gson
import com.pethelp.app.features.ai.data.repository.AiChatRepositoryImpl
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideAiChatRepository(
        httpClient: OkHttpClient,
        gson: Gson
    ): AiChatRepository {
        return AiChatRepositoryImpl(httpClient, gson)
    }
}
