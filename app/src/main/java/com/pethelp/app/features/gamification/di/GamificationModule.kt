package com.pethelp.app.features.gamification.di

import com.pethelp.app.features.gamification.data.local.GamificationLocalDataSource
import com.pethelp.app.features.gamification.data.remote.GamificationPointsRepository
import com.pethelp.app.features.gamification.domain.GamificationEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GamificationModule {

    @Provides
    @Singleton
    fun provideGamificationEngine(
        localDataSource: GamificationLocalDataSource,
        pointsRepository: GamificationPointsRepository
    ): GamificationEngine {
        return GamificationEngine(localDataSource, pointsRepository)
    }
}
