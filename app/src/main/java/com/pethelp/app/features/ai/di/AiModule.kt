/**
 * Módulo Hilt que provee el repositorio de IA ([AiChatRepository]).
 *
 * Inyecta [OkHttpClient] para que [AiChatRepositoryImpl] pueda realizar
 * llamadas HTTP al servicio NVIDIA NIM cuando el fallback es necesario.
 */
package com.pethelp.app.features.ai.di

import com.pethelp.app.features.ai.data.repository.AiChatRepositoryImpl
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Módulo Hilt que provee el repositorio de IA ([AiChatRepository]).
 *
 * Inyecta [OkHttpClient] (configurado en [NetworkModule]) para que
 * [AiChatRepositoryImpl] pueda realizar llamadas HTTP a los servicios
 * de IA (Gemini/NVIDIA) cuando el fallback es necesario.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    /**
     * Crea el repositorio de IA inyectando el cliente HTTP.
     *
     * @param okHttpClient Cliente HTTP compartido de la aplicación.
     * @return Implementación concreta del repositorio de IA.
     */
    @Provides
    @Singleton
    fun provideAiChatRepository(okHttpClient: OkHttpClient): AiChatRepository {
        return AiChatRepositoryImpl(okHttpClient)
    }
}
