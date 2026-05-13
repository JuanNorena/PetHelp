/**
 * Módulo Hilt que provee dependencias del sistema de gamificación.
 *
 * Inyecta [GamificationEngine], [GamificationLocalDataSource] y
 * [GamificationPointsRepository] para que los ViewModels puedan
 * registrar eventos y sincronizar puntos sin conocer detalles de infraestructura.
 */
package com.pethelp.app.features.gamification.di

import com.pethelp.app.features.gamification.data.local.GamificationLocalDataSource
import com.pethelp.app.features.gamification.data.remote.GamificationPointsRepository
import com.pethelp.app.features.gamification.domain.GamificationEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que provee el [GamificationEngine] como singleton.
 *
 * El engine requiere dos dependencias:
 * - [GamificationLocalDataSource] para leer/escribir el estado local.
 * - [GamificationPointsRepository] para sincronizar puntos con Firestore.
 *
 * Al proveer el engine desde este módulo, cualquier ViewModel que lo inyecte
 * recibe la misma instancia compartida, garantizando consistencia de estado.
 */
@Module
@InstallIn(SingletonComponent::class)
object GamificationModule {

    /**
     * Crea y provee el motor de gamificación.
     *
     * @param localDataSource Fuente de datos local para persistir estado.
     * @param pointsRepository Repositorio remoto para actualizar puntos en Firestore.
     * @return Instancia del [GamificationEngine] lista para ser inyectada.
     */
    @Provides
    @Singleton
    fun provideGamificationEngine(
        localDataSource: GamificationLocalDataSource,
        pointsRepository: GamificationPointsRepository
    ): GamificationEngine {
        return GamificationEngine(localDataSource, pointsRepository)
    }
}
