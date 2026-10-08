package ru.kolkhoz.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.kolkhoz.data.db.KolkhozDatabase

/**
 * Предоставляет Room-базу и DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KolkhozDatabase =
        Room.databaseBuilder(
            context,
            KolkhozDatabase::class.java,
            "kolkhoz.db",
        ).build()

    @Provides
    fun provideGameDao(db: KolkhozDatabase) = db.gameDao()

    @Provides
    fun providePlayerDao(db: KolkhozDatabase) = db.playerDao()

    @Provides
    fun provideEventDao(db: KolkhozDatabase) = db.eventDao()
}
