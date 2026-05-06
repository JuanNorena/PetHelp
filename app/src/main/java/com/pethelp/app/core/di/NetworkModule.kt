package com.pethelp.app.core.di

import com.pethelp.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt que provee los componentes de red usados por la aplicación.
 *
 * Contiene la configuración de OkHttp y Retrofit para conectarse a la API
 * definida en `BuildConfig.BASE_URL`.
 *
 * Aunque esta app tiene Firebase, este módulo se enfoca en llamadas HTTP
 * externas, típicamente a servicios como OpenAI o Gemini.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Crea un interceptor de logging para OkHttp.
     *
     * En modo DEBUG registra el cuerpo completo de las peticiones y respuestas.
     * En modo release no registra nada para no exponer datos sensibles.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

    /**
     * Crea el cliente de OkHttp que maneja las llamadas HTTP.
     *
     * Se configura con el interceptor de logging y tiempos de espera de 30 s
     * para conexión, lectura y escritura.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Crea la instancia de Retrofit usada para consumir la API.
     *
     * Retrofit convierte JSON a objetos Kotlin usando Gson y utiliza el
     * cliente HTTP configurado anteriormente.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
