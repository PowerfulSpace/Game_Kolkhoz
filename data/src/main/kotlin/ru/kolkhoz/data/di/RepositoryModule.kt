package ru.kolkhoz.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.kolkhoz.data.repository.LocalGameRepository
import ru.kolkhoz.domain.repository.GameRepository

/**
 * Связывает контракт [GameRepository] (domain) с Room-реализацией.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        impl: LocalGameRepository,
    ): GameRepository
}
