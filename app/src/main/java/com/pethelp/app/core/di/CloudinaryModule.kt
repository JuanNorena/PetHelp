package com.pethelp.app.core.di

import com.pethelp.app.core.data.upload.CloudinaryImageUploader
import com.pethelp.app.core.domain.upload.ImageUploader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que vincula la implementación concreta de carga de imágenes
 * (`CloudinaryImageUploader`) con la abstracción de dominio (`ImageUploader`).
 *
 * En lugar de inyectar directamente la clase concreta, las dependencias usan
 * la interfaz `ImageUploader`. Esto separa el contrato de su implementación.
 *
 * Beneficios principales:
 * 1. Facilita pruebas unitarias: los tests pueden sustituir `ImageUploader`
 *    por un doble de prueba.
 * 2. Mejora la flexibilidad: se puede cambiar la implementación sin afectar
 *    a los consumidores (por ejemplo, reemplazar Cloudinary por otro servicio).
 * 3. Refuerza la arquitectura limpia y la inversión de dependencias.
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
