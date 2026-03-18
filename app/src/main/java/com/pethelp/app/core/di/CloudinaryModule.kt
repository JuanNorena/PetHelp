package com.pethelp.app.core.di

import com.pethelp.app.core.data.upload.CloudinaryImageUploader
import com.pethelp.app.core.domain.upload.ImageUploader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que vincula la implementación concreta de subida de imágenes
 * (`CloudinaryImageUploader`) con la interfaz de dominio (`ImageUploader`).
 *
 * Esto permite que el `ViewModel` o cualquier otra clase que necesite subir
 * imágenes dependa de la abstracción (`ImageUploader`) y no de la implementación.
 *
 * Ventajas:
 * 1) Facilita pruebas unitarias (mockear `ImageUploader`).
 * 2) Permite cambiar el proveedor (Cloudinary, S3, Firebase, etc.) sin tocar
 *    el código de la UI.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CloudinaryModule {

    @Binds
    @Singleton
    abstract fun bindImageUploader(
        cloudinaryImageUploader: CloudinaryImageUploader
    ): ImageUploader
}
