package com.pethelp.app.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que provee las instancias de Firebase necesarias en la app.
 *
 * Al declarar estos proveedores aquí, Hilt puede inyectar los servicios de
 * Firebase en cualquier clase que los necesite sin que esa clase tenga que
 * crearlos manualmente.
 *
 * @InstallIn(SingletonComponent::class) garantiza que estas dependencias
 * tengan alcance singleton durante todo el ciclo de vida de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Proporciona la instancia de Firebase Auth.
     *
     * FirebaseAuth es el servicio que maneja la autenticación de usuarios.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    /**
     * Proporciona la instancia de Firebase Firestore.
     *
     * Firestore se usa para leer y escribir datos estructurados en la nube.
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    /**
     * Proporciona la instancia de Firebase Storage.
     *
     * FirebaseStorage se usa para subir y descargar archivos, como imágenes.
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    /**
     * Proporciona la instancia de Firebase Messaging.
     *
     * FirebaseMessaging se usa para recibir notificaciones push y tokens de
     * registro de dispositivo.
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging =
        FirebaseMessaging.getInstance()
}
