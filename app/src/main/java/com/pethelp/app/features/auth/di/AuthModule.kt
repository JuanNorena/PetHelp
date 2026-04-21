package com.pethelp.app.features.auth.di

import com.pethelp.app.features.auth.data.repository.FirebaseAuthRepository
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt encargado de la Inyección de Dependencias para el módulo de Autenticación.
 *
 * **Responsabilidad Principal:**
 * Este módulo actúa como un "mapeador" o puente que le indica a Hilt (nuestro framework de DI)
 * qué implementación concreta debe utilizar cuando una clase solicita la interfaz [AuthRepository].
 *
 * **Propósito y Arquitectura:**
 * - **Desacoplamiento:** Permite que las capas superiores (como ViewModels) dependan de una
 *   abstracción ([AuthRepository]) en lugar de una clase concreta ([FirebaseAuthRepository]).
 * - **Mantenibilidad:** Si en el futuro decidimos cambiar Firebase por otro servicio de auth,
 *   solo tendríamos que modificar este archivo.
 * - **Singleton:** Asegura que solo exista una instancia del repositorio en toda la aplicación.
 *
 * **Lógica de Funcionamiento:**
 * Al usar la anotación `@Binds`, Hilt genera automáticamente el código necesario para instanciar
 * [FirebaseAuthRepository] y entregarlo como un [AuthRepository]. Es más eficiente que `@Provides`
 * porque no requiere la creación manual del objeto.
 *
 * **Ejemplo de Uso (Inyección):**
 * ```kotlin
 * @HiltViewModel
 * class AuthViewModel @Inject constructor(
 *     private val repository: AuthRepository // Hilt inyectará FirebaseAuthRepository aquí
 * ) : ViewModel() { ... }
 * ```
 *
 * **Notas para Junior Developers:**
 * - `@Module`: Marca esta clase como un contenedor de "recetas" para crear objetos.
 * - `@InstallIn(SingletonComponent::class)`: Significa que los objetos creados aquí vivirán
 *   mientras la aplicación esté abierta.
 * - `@Binds`: Se usa específicamente para vincular una Interfaz con su Clase de implementación.
 *   La función debe ser `abstract` y el parámetro debe ser la clase concreta.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see AuthRepository Interfaz del contrato.
 * @see FirebaseAuthRepository Implementación real.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /**
     * Vincula la interfaz [AuthRepository] con su implementación [FirebaseAuthRepository].
     *
     * @param firebaseAuthRepository La implementación concreta que utiliza Firebase.
     * @return La interfaz [AuthRepository] lista para ser inyectada.
     * @throws IllegalStateException Si Hilt no puede encontrar una forma de crear [FirebaseAuthRepository].
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository
}
