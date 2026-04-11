package com.tamdao.cinestream.core.database

import android.content.Context
import androidx.room.Room
import com.tamdao.cinestream.core.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CineDatabase {
        return Room.databaseBuilder(
            context,
            CineDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideMovieDao(db: CineDatabase): MovieDao {
        return db.movieDao()
    }
}
